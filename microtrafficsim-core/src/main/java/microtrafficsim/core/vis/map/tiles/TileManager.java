package microtrafficsim.core.vis.map.tiles;

import com.jogamp.opengl.GL2ES2;
import microtrafficsim.core.map.Bounds;
import microtrafficsim.core.map.tiles.TileId;
import microtrafficsim.core.map.tiles.TileRect;
import microtrafficsim.core.map.tiles.TilingScheme;
import microtrafficsim.core.vis.context.RenderContext;
import microtrafficsim.core.vis.opengl.shader.ShaderProgram;
import microtrafficsim.core.vis.view.OrthographicView;
import microtrafficsim.math.Rect2d;
import microtrafficsim.utils.exceptions.ThisShouldNeverHappenException;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;


public class TileManager {
    private static final Comparator<Tile> CMP_TILE = new TileComparator();

    private TileProvider provider;
    private ExecutorService worker;

    private TileRect tiles;

    private HashMap<TileId, Tile> visible;
    private HashMap<TileId, Future<Tile>> loading;
    private ArrayList<Tile> prebuilt;

    private ConcurrentLinkedQueue<TileId> changed;
    private AtomicBoolean reload;


    public TileManager(TileProvider provider, ExecutorService worker) {
        this.provider = provider;
        this.worker = worker;

        this.tiles = null;

        this.visible = new HashMap<>();
        this.loading = new HashMap<>();
        this.prebuilt = new ArrayList<>();

        this.changed = new ConcurrentLinkedQueue<>();
        this.reload = new AtomicBoolean(true);

        this.provider.addTileChangeListener(new TileChangeListenerImpl());
    }


    public Bounds getBounds() {
        return provider.getBounds();
    }

    public Rect2d getProjectedBounds() {
        return provider.getProjectedBounds();
    }


    public void initialize(RenderContext context) {
        provider.initialize(context);
    }

    public void dispose(RenderContext context) {
        // cancel tiles which are currently beeing loaded
        for (Future<Tile> future : loading.values())
            future.cancel(true);

        // make sure everything is released correctly
        for (Future<Tile> future : loading.values()) {
            try {
                Tile tile = future.get();
                if (tile != null)
                    provider.release(context, tile);
            } catch (InterruptedException | ExecutionException e) {
                // provider should make sure that there is a clean exit on interrupts/exceptions
            }
        }

        // dispose layers that have already been loaded
        for (Tile tile : visible.values())
            if (tile != null)
                provider.release(context, tile);

        loading.clear();
        visible.clear();
        prebuilt.clear();
        changed.clear();

        provider.dispose(context);
    }


    public void update(RenderContext context, OrthographicView observer) {
        boolean rebuild = false;

        double zoom = observer.getZoomLevel();
        TilingScheme scheme = provider.getTilingScheme();
        TileRect view = scheme.getTiles(observer.getViewportBounds(), zoom);
        TileRect provided = scheme.getTiles(provider.getProjectedBounds(), zoom);

        // (re-)load tiles based on view and change-list
        rebuild |= asyncLoadTiles(context, view, provided);
        rebuild |= releaseTiles(context, view);

        // fetch loaded tiles, release obsolete
        rebuild |= addLoadedTiles();

        // if necessary, release replaced tile and rebuild ordered id list
        if (rebuild) {
            cleanupVisibleTiles(view);
            rebuildTileList();
        }

        this.tiles = view;
    }

    private boolean asyncLoadTiles(RenderContext context, TileRect view, TileRect provided) {
        if (provided == null) return false;
        boolean change = false;

        int xmin = Math.max(view.xmin, provided.xmin);
        int xmax = Math.min(view.xmax, provided.xmax);
        int ymin = Math.max(view.ymin, provided.ymin);
        int ymax = Math.min(view.ymax, provided.ymax);

        // if nothing has been loaded or zoom is different, load all
        if (this.tiles == null || view.zoom != this.tiles.zoom || reload.getAndSet(false)) {
            for (int x = xmin; x <= xmax; x++)
                for (int y = ymin; y <= ymax; y++)
                    change |= asyncReload(context, new TileId(x, y, view.zoom));

        // else update and load only necessary tiles
        } else {
            // update changed tiles
            while (!changed.isEmpty()) {
                TileId tile = changed.poll();

                // if tile should have already been loaded and is still visible
                if (view.contains(tile) && tiles.contains(tile) && provided.contains(tile))
                    change |= asyncReload(context, tile);
            }

            // load new tiles
            for (int x = xmin; x <= xmax; x++)
                for (int y = ymin; y <= ymax; y++)
                    if (x < tiles.xmin || x > tiles.xmax || y < tiles.ymin || y > tiles.ymax)
                        change |= asyncReload(context, new TileId(x, y, view.zoom));
        }

        return change;
    }

    private boolean releaseTiles(RenderContext context, TileRect view) {
        boolean change = false;
        TilingScheme scheme = provider.getTilingScheme();

        // release tiles that are already loaded and out of view
        Iterator<Map.Entry<TileId, Tile>> visible = this.visible.entrySet().iterator();
        while (visible.hasNext()) {
            Map.Entry<TileId, Tile> entry = visible.next();
            Tile tile = entry.getValue();
            TileRect rect = scheme.getTiles(entry.getKey(), view.zoom);

            if (rect.xmax < view.xmin || rect.xmin > view.xmax || rect.ymax < view.ymin || rect.ymin > view.ymax) {
                if (tile != null)
                    provider.release(context, tile);

                visible.remove();
                change = true;
            }
        }

        // stop loading tiles that are out of view, release already loaded tiles
        Iterator<Map.Entry<TileId, Future<Tile>>> loading = this.loading.entrySet().iterator();
        while (loading.hasNext()) {
            Map.Entry<TileId, Future<Tile>> entry = loading.next();
            TileRect rect = scheme.getTiles(entry.getKey(), view.zoom);
            Future<Tile> task = entry.getValue();

            if (rect.xmax < view.xmin || rect.xmin > view.xmax || rect.ymax < view.ymin || rect.ymin > view.ymax) {
                task.cancel(true);

                if (!task.isCancelled()) {      // if tile is already loaded, release it
                    try {
                        Tile tile = task.get();
                        if (tile != null)
                            provider.release(context, tile);
                    } catch (ExecutionException | InterruptedException e) {
                        throw new ThisShouldNeverHappenException(e);
                    }
                }

                loading.remove();
            }
        }

        return change;
    }

    private boolean addLoadedTiles() {
        boolean change = false;

        Iterator<Map.Entry<TileId, Future<Tile>>> loading = this.loading.entrySet().iterator();
        while (loading.hasNext()) {
            Map.Entry<TileId, Future<Tile>> entry = loading.next();
            TileId id = entry.getKey();
            Future<Tile> task = entry.getValue();

            // get all finished tasks, ignore cancelled ones
            if (task.isDone()) {
                if (!task.isCancelled()) {
                    try {
                        visible.put(id, task.get());
                        change = true;
                    } catch (ExecutionException | InterruptedException e) {
                        throw new ThisShouldNeverHappenException(e);
                    }
                }

                loading.remove();
            }
        }

        return change;
    }

    private void cleanupVisibleTiles(TileRect view) {
        TilingScheme scheme = provider.getTilingScheme();
        HashSet<TileId> remove = new HashSet<>();
        Set<TileId> visible = this.visible.keySet();

        for (TileId id : visible)
            if (id.z != view.zoom && containsAllInView(visible, scheme.getTiles(id, view.zoom), view))
                remove.add(id);

        // TODO: some tiles are unavailable at higher zoom level, so their parents are kept. Fix this issue.

        visible.removeAll(remove);
    }

    private static boolean containsAllInView(Set<TileId> set, TileRect rect, TileRect view) {
        for (int x = Math.max(rect.xmin, view.xmin); x <= Math.min(rect.xmax, view.xmax); x++)
            for (int y = Math.max(rect.ymin, view.ymin); y <= Math.min(rect.ymax, view.ymax); y++)
                if (!set.contains(new TileId(x, y, view.zoom)))
                    return false;

        return true;
    }

    private void rebuildTileList() {
        prebuilt.clear();
        visible.values().stream().filter(x -> x != null).forEach(prebuilt::add);
        prebuilt.sort(CMP_TILE);
    }


    private boolean asyncReload(RenderContext context, TileId id) {
        /* Note:
         *  This function always stops the loading process of the same tile because we cannot guarantee the order in
         *  which they are finished.
         *  Example:
         *      1. start async loading of tile
         *      2. tile update, async reloading of tile
         *      3. reload finishes
         *      4. initial load finises
         *  The tile may not be updated.
         */

        boolean change = false;

        // stop loading tile
        Future<Tile> task = loading.remove(id);
        if (task != null) {
            task.cancel(true);

            // if the tile is already loaded, use it until the update is loaded
            if (!task.isCancelled()) {
                try {
                    visible.put(id, task.get());
                    change = true;
                } catch (ExecutionException | InterruptedException e) {
                    throw new ThisShouldNeverHappenException(e);
                }
            }
        }

        loading.put(id, worker.submit(new Loader(context, provider, id)));
        return change;
    }


    public void display(RenderContext context) {
        GL2ES2 gl = context.getDrawable().getGL().getGL2ES2();
        context.DepthTest.disable(gl);

        for (Tile tile : prebuilt)
            tile.display(context);

        context.ShaderState.unbind(gl);
    }


    private class TileChangeListenerImpl implements TileProvider.TileChangeListener {

        @Override
        public void tilesChanged() {
            reload.set(true);
        }

        @Override
        public void tileChanged(TileId tile) {
            changed.add(tile);
        }
    }

    private static class Loader implements Callable<Tile> {

        private RenderContext context;
        private TileProvider provider;
        private TileId id;

        private Loader(RenderContext context, TileProvider provider, TileId id) {
            this.context = context;
            this.provider = provider;
            this.id = id;
        }

        @Override
        public Tile call() throws Exception {
            Tile tile = provider.require(context, id);
            // TODO: tile.setTransformation(...);
            return tile;
        }
    }

    private static class TileComparator implements Comparator<Tile> {

        @Override
        public int compare(Tile a, Tile b) {
            int za = a.getId().z;
            int zb = b.getId().z;

            return za > zb ? 1 : za < zb ? -1 : 0;
        }
    }
}
