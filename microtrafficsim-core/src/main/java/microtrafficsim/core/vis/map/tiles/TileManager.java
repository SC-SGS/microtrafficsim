package microtrafficsim.core.vis.map.tiles;

import com.jogamp.opengl.GL2ES2;
import microtrafficsim.core.map.Bounds;
import microtrafficsim.core.map.tiles.TileId;
import microtrafficsim.core.map.tiles.TileRect;
import microtrafficsim.core.map.tiles.TilingScheme;
import microtrafficsim.core.vis.context.RenderContext;
import microtrafficsim.core.vis.context.tasks.RenderTask;
import microtrafficsim.core.vis.view.OrthographicView;
import microtrafficsim.math.Rect2d;

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
    private ArrayList<Future<Tile>> cancelling;         // cleanup cancelled tiles out-of-cycle, to avoid deadlock
    private ArrayList<Tile> prebuilt;

    private ConcurrentLinkedQueue<TileId> changed;
    private AtomicBoolean reload;


    public TileManager(TileProvider provider, ExecutorService worker) {
        this.provider = provider;
        this.worker = worker;

        this.tiles = new TileRect(0, 0, 0, 0, 0);

        this.visible = new HashMap<>();
        this.loading = new HashMap<>();
        this.cancelling = new ArrayList<>();
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
        for (Future<Tile> future : loading.values()) {
            future.cancel(true);
            cancelling.add(future);
        }

        // make sure everything is released correctly
        for (Future<Tile> future : loading.values()) {
            try {
                Tile tile = future.get();
                provider.release(context, tile);
            } catch (InterruptedException | ExecutionException e) {
                // we are disposing, ignore all exceptions
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

        // dispose all cancelled tiles
        context.addTask(new TileCleanupTask(cancelling));
    }


    public void update(RenderContext context, OrthographicView observer)
            throws ExecutionException, InterruptedException {

        TilingScheme scheme = provider.getTilingScheme();

        // view parameters
        Rect2d viewport = observer.getViewportBounds();
        double zoom = observer.getZoomLevel();

        // get bounds, relative to the current view
        TileRect view = scheme.getTiles(viewport, zoom);
        TileRect provided = scheme.getTiles(provider.getProjectedBounds(), zoom);

        // get tiles to load
        TileRect common = provided != null ? TileRect.intersect(view, provided) : null; // provided and in view
        Set<TileId> reload = mgmtTilesToLoad(common);                               // provided, in view and not loaded

        // load tiles asynchronously, move loaded tiles to visible, update
        boolean rebuild = mgmtAsyncReload(context, reload);
        rebuild |= mgmtMoveLoaded(context);
        rebuild |= mgmtRemoveInvisible(context, scheme, common);

        // cleanup and rebuild tile list, if something changed
        if (rebuild) {
            mgmtRemoveOccluded(context, scheme, common);
            mgmtRebuild();
        }

        // cleanup cancelled tasks
        mgmtCleanupCancelled(context);

        // update view-state
        this.tiles = view;
    }

    private Set<TileId> mgmtTilesToLoad(TileRect common) {
        if (common == null) {
            return new HashSet<>();

        // if explicit request or zoom changed: reload all tiles
        } else if (reload.getAndSet(false) || common.zoom != this.tiles.zoom) {
            return common.getAll();

        // else: reload only tiles that are explicitly marked as invalidated or newly in view
        } else {
            Set<TileId> res = TileRect.subtract(common, this.tiles);    // add tiles newly in view
            assert res != null;

            TileId id;                                                  // add updated (in view)
            while ((id = changed.poll()) != null)
                if (this.tiles.contains(id) && common.contains(id))
                    res.add(id);

            return res;
        }
    }

    private boolean mgmtAsyncReload(RenderContext context, Set<TileId> tiles)
            throws ExecutionException, InterruptedException {

        boolean change = false;
        for (TileId id : tiles)
            change |= mgmtAsyncReload(context, id);

        return change;
    }

    private boolean mgmtAsyncReload(RenderContext context, TileId id)
            throws ExecutionException, InterruptedException {

        Future<Tile> prev = loading.put(id, worker.submit(new Loader(context, provider, id)));
        if (prev != null) {
            prev.cancel(true);                              // cancel the previous task

            if (!prev.isDone()) {                           // if task is not finished / cancelled, cleanup later
                cancelling.add(prev);
            } else if (!prev.isCancelled()) {               // if task has finished successful, add the tile
                provider.release(context, visible.put(id, getFromTaskIgnoringCancellation(prev)));
                return true;
            }
        }

        return false;
    }

    private boolean mgmtMoveLoaded(RenderContext context)
            throws ExecutionException, InterruptedException {

        boolean changed = false;
        HashSet<TileId> remove = new HashSet<>();

        for (Map.Entry<TileId, Future<Tile>> entry : loading.entrySet()) {
            TileId id = entry.getKey();
            Future<Tile> task = entry.getValue();

            if (task.isDone()) {                    // if task is done, remove it
                if (!task.isCancelled()) {          // if task is done and has not been cancelled, add the result
                    Tile tile = getFromTaskIgnoringCancellation(task);
                    provider.release(context, visible.put(id, tile));
                    changed = true;
                }

                remove.add(id);
            }
        }

        loading.keySet().removeAll(remove);
        return changed;
    }

    private boolean mgmtRemoveInvisible(RenderContext context, TilingScheme scheme, TileRect common)
            throws ExecutionException, InterruptedException {

        boolean changed = false;
        HashSet<TileId> remove = new HashSet<>();

        // remove from visible
        for (Map.Entry<TileId, Tile> entry : visible.entrySet()) {
            TileId id = entry.getKey();

            boolean rem = common == null;
            if (!rem) {
                TileRect rect = scheme.getTiles(id, common.zoom);
                rem = rect.xmax < common.xmin || rect.xmin > common.xmax
                        || rect.ymax < common.ymin || rect.ymin > common.ymax;
            }

            if (rem) {
                provider.release(context, entry.getValue());
                remove.add(id);
                changed = true;
            }
        }
        visible.keySet().removeAll(remove);
        remove.clear();

        // remove from loading
        for (Map.Entry<TileId, Future<Tile>> entry : loading.entrySet()) {
            TileId id = entry.getKey();
            Future<Tile> task = entry.getValue();

            if (common == null || !common.contains(id)) {
                task.cancel(true);

                if (!task.isDone()) {
                    cancelling.add(task);
                } else {
                    Tile tile = getFromTaskIgnoringCancellation(task);
                    provider.release(context, tile);
                }

                remove.add(id);
            }
        }
        loading.keySet().removeAll(remove);

        return changed;
    }

    private void mgmtRemoveOccluded(RenderContext context, TilingScheme scheme, TileRect common) {
        Set<TileId> remove = new HashSet<>();
        Set<TileId> visible = this.visible.keySet();

        for (Map.Entry<TileId, Tile> entry : this.visible.entrySet()) {
            TileId id = entry.getKey();

            if (id.z != common.zoom && containsAllInView(visible, scheme.getTiles(id, common.zoom), common)) {
                remove.add(id);
                provider.release(context, entry.getValue());
            }
        }

        visible.removeAll(remove);
    }

    private void mgmtRebuild() {
        prebuilt.clear();

        for (Tile tile : visible.values())
            if (tile != null)
                prebuilt.add(tile);

        prebuilt.sort(CMP_TILE);
    }

    private void mgmtCleanupCancelled(RenderContext context) throws ExecutionException, InterruptedException {
        Iterator<Future<Tile>> it = cancelling.iterator();

        while (it.hasNext()) {
            Future<Tile> task = it.next();

            if (task.isDone()) {
                Tile tile = getFromTaskIgnoringCancellation(task);
                provider.release(context, tile);
                it.remove();
            }
        }
    }


    private static Tile getFromTaskIgnoringCancellation(Future<Tile> task)
            throws ExecutionException, InterruptedException {
        try {
            return task.get();
        } catch (CancellationException e) {
            /* ignore, we cancelled this task */
        }

        return null;
    }

    private static boolean containsAllInView(Set<TileId> set, TileRect rect, TileRect view) {
        for (int x = Math.max(rect.xmin, view.xmin); x <= Math.min(rect.xmax, view.xmax); x++)
            for (int y = Math.max(rect.ymin, view.ymin); y <= Math.min(rect.ymax, view.ymax); y++)
                if (!set.contains(new TileId(x, y, view.zoom)))
                    return false;

        return true;
    }


    public void display(RenderContext context) {
        GL2ES2 gl = context.getDrawable().getGL().getGL2ES2();
        context.DepthTest.disable(gl);

        provider.beforeRendering(context);
        for (Tile tile : prebuilt)
            tile.display(context);
        provider.afterRendering(context);

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
            if (tile == null) return null;

            // set tile transformation matrix
            Rect2d bounds = provider.getTilingScheme().getBounds(id);

            // translate from [[-1, -1],[1, 1]] to [bounds]
            double sx = (bounds.xmax - bounds.xmin) / 2.0;
            double sy = (bounds.ymax - bounds.ymin) / 2.0;
            tile.getTransformation().set(
                    (float) sx,        0.f, 0.f, (float) (bounds.xmin + sx),
                           0.f, (float) sy, 0.f, (float) (bounds.ymin + sy),
                           0.f,        0.f, 1.f,                        0.f,
                           0.f,        0.f, 0.f,                        1.f
            );

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

    private class TileCleanupTask implements RenderTask<Void> {
        private final ArrayList<Future<Tile>> cancelling;

        TileCleanupTask(ArrayList<Future<Tile>> cancelling) {
            this.cancelling = cancelling;
        }

        @Override
        public Void execute(RenderContext context) throws Exception {
            Iterator<Future<Tile>> it = cancelling.iterator();

            while (it.hasNext()) {
                Future<Tile> task = it.next();

                if (task.isDone()) {
                    Tile tile = getFromTaskIgnoringCancellation(task);
                    provider.release(context, tile);
                }

                it.remove();
            }

            if (!cancelling.isEmpty())
                context.addTask(new TileCleanupTask(cancelling), true);

            return null;
        }
    }
}
