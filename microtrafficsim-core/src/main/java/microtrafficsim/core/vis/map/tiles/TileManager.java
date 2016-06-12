package microtrafficsim.core.vis.map.tiles;

import com.jogamp.opengl.GL2ES2;
import microtrafficsim.core.map.Bounds;
import microtrafficsim.core.map.tiles.TileId;
import microtrafficsim.core.map.tiles.TileRect;
import microtrafficsim.core.map.tiles.TilingScheme;
import microtrafficsim.core.vis.context.RenderContext;
import microtrafficsim.core.vis.context.tasks.RenderTask;
import microtrafficsim.core.vis.view.OrthographicView;
import microtrafficsim.math.MathUtils;
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

        // dispose layers that have already been loaded
        visible.values().stream().filter(t -> t != null).forEach(t -> provider.release(context, t));

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
        TileRect common = provided != null ? TileRect.intersect(view, provided) : null;     // provided and in view

        // load tiles asynchronously, move loaded tiles to visible, update
        boolean rebuild = mgmtReload(context, common);
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

    private boolean mgmtReload(RenderContext context, TileRect common)
            throws ExecutionException, InterruptedException {

        if (common == null) return false;
        boolean change = false;

        // if explicit request or zoom changed: reload all tiles
        if (reload.getAndSet(false) || common.zoom != this.tiles.zoom) {
            changed.clear();
            for (int x = common.xmin; x <= common.xmax; x++)
                for (int y = common.ymin; y <= common.ymax; y++)
                    change |= mgmtAsyncReload(context, new TileId(x, y, common.zoom));

        // else: reload only tiles that are explicitly marked as invalidated or newly in view
        } else {
            int xmin = MathUtils.clamp(this.tiles.xmin, common.xmin, common.xmax + 1);
            int ymin = MathUtils.clamp(this.tiles.ymin, common.ymin, common.ymax + 1);
            int xmax = MathUtils.clamp(this.tiles.xmax, common.xmin - 1, common.xmax);
            int ymax = MathUtils.clamp(this.tiles.ymax, common.ymin - 1, common.ymax);

            // full x-segments
            for (int y = common.ymin; y <= common.ymax; y++) {
                for (int x = common.xmin; x < xmin; x++)
                    change |= mgmtAsyncReload(context, new TileId(x, y, common.zoom));

                for (int x = common.xmax; x > xmax; x--)
                    change |= mgmtAsyncReload(context, new TileId(x, y, common.zoom));
            }

            // partial y-segments
            for (int x = xmin; x <= xmax; x++) {
                for (int y = common.ymin; y < ymin; y++)
                    change |= mgmtAsyncReload(context, new TileId(x, y, common.zoom));

                for (int y = common.ymax; y > ymax; y--)
                    change |= mgmtAsyncReload(context, new TileId(x, y, common.zoom));
            }

            // changed tiles
            TileId id;
            while((id = changed.poll()) != null) {
                if (id.x >= xmin && id.x <= xmax && id.y >= ymin && id.x <= ymax && id.z == common.zoom)
                    change |= mgmtAsyncReload(context, id);
            }
        }

        return change;
    }

    private boolean mgmtAsyncReload(RenderContext context, TileId id)
            throws ExecutionException, InterruptedException {

        Future<Tile> prev = loading.put(id, worker.submit(new Loader(context, provider, id)));
        if (prev == null) return false;

        // if the previous task is not finished, cancel it
        if (!prev.isDone()) {
            prev.cancel(true);
            cancelling.add(prev);
            return false;
        }

        // if the previous task was cancelled, it is already disposed
        if (prev.isCancelled()) return false;

        // if the previous task was successful, use the generated tile
        provider.release(context, visible.put(id, getFromTaskIgnoringCancellation(prev)));
        return true;
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
                    provider.release(context, visible.put(id, getFromTaskIgnoringCancellation(task)));
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
                cancelling.add(task);
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
        visible.values().stream()
                .filter(x -> x != null)
                .sorted(CMP_TILE)
                .forEach(prebuilt::add);
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


    private static <V> V getFromTaskIgnoringCancellation(Future<V> task)
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
        public Tile call() throws CancellationException, ExecutionException {
            Tile tile;
            try {
                tile = provider.require(context, id);
            } catch (InterruptedException e) {
                throw new CancellationException();      // cancel this task
            }
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
                    try {
                        provider.release(context, task.get());
                    } catch (ExecutionException | CancellationException | InterruptedException e) {
                        /* ignore, we are cleaning up */
                    }
                }

                it.remove();
            }

            if (!cancelling.isEmpty())
                context.addTask(this, true);

            return null;
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
