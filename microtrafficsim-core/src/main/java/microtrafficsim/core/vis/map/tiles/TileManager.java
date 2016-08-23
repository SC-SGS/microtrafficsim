package microtrafficsim.core.vis.map.tiles;

import com.jogamp.opengl.GL2ES2;
import microtrafficsim.core.map.Bounds;
import microtrafficsim.core.map.tiles.TileId;
import microtrafficsim.core.map.tiles.TileRect;
import microtrafficsim.core.map.tiles.TilingScheme;
import microtrafficsim.core.vis.context.RenderContext;
import microtrafficsim.core.vis.context.tasks.RenderTask;
import microtrafficsim.core.vis.view.OrthographicView;
import microtrafficsim.math.Mat4f;
import microtrafficsim.math.MathUtils;
import microtrafficsim.math.Rect2d;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;


/**
 * Manager maintaining and rendering visible tiles.
 *
 * @author Maximilian Luz
 */
public strictfp class TileManager {
    private static final Comparator<Tile> CMP_TILE = new TileComparator();

    private TileProvider    provider;
    private ExecutorService worker;

    private TileRect tiles;

    private HashMap<TileId, Tile>         visible;
    private HashMap<TileId, Future<Tile>> loading;
    private ArrayList<Future<Tile>>       cancelling;    // cleanup cancelled tiles out-of-cycle, to avoid deadlock
    private ArrayList<Tile>               prebuilt;

    private ConcurrentLinkedQueue<TileId> changed;
    private AtomicBoolean                 reload;


    /**
     * Constructs a new {@code TileManager} with the given tile-provider and executor-service.
     *
     * @param provider the provider providing the tiles to be displayed.
     * @param worker   the {@code ExecutorService} responsible for asynchronous tile loading.
     */
    public TileManager(TileProvider provider, ExecutorService worker) {
        this.provider = provider;
        this.worker   = worker;

        this.tiles = new TileRect(0, 0, 0, 0, 0);

        this.visible    = new HashMap<>();
        this.loading    = new HashMap<>();
        this.cancelling = new ArrayList<>();
        this.prebuilt   = new ArrayList<>();

        this.changed = new ConcurrentLinkedQueue<>();
        this.reload  = new AtomicBoolean(true);

        this.provider.addTileChangeListener(new TileChangeListenerImpl());
    }

    /**
     * Return the result from the given task, ignoring {@code CancellationException}s. If the task throws a
     * {@code CancellationException}, {@code null} is returned. This call will block if necessary.
     *
     * @param task the future to get the result from.
     * @param <V> the return-type of the task.
     * @return the result of the task or {@code null} if a {@code CancellationException} has been thrown by the task.
     * @throws ExecutionException   if {@code task.get()} throws an {@code ExecutionException}.
     * @throws InterruptedException it {@code task.get()} throws an {@code InterruptedException}.
     * @see Future#get()
     */
    private static <V> V getFromTaskIgnoringCancellation(Future<V> task)
            throws ExecutionException, InterruptedException {
        try {
            return task.get();
        } catch (CancellationException e) { /* ignore, we cancelled this task */ }

        return null;
    }

    /**
     * Checks if {@code set} contains all tiles in the intersection of {@code rect} and {@code view}.
     *
     * @param set  the set to test for inclusion.
     * @param rect the first rectangle to test against.
     * @param view the second rectangle to test against.
     * @return {@code true} if the set contains all tiles in the intersection of {@code rect} and {@code view}.
     */
    private static boolean containsAllInView(Set<TileId> set, TileRect rect, TileRect view) {
        for (int x = Math.max(rect.xmin, view.xmin); x <= Math.min(rect.xmax, view.xmax); x++)
            for (int y = Math.max(rect.ymin, view.ymin); y <= Math.min(rect.ymax, view.ymax); y++)
                if (!set.contains(new TileId(x, y, view.zoom))) return false;

        return true;
    }

    /**
     * Returns the (un-projected) bounds of the tiles this manager is capable of displaying.
     *
     * @return the (un-projected) bounds of the tiles this manager is capable of displaying.
     */
    public Bounds getBounds() {
        return provider.getBounds();
    }

    /**
     * Returns the (projected) bounds of the tiles this manager is capable of displaying.
     *
     * @return the (projected) bounds of the tiles this manager is capable of displaying.
     */
    public Rect2d getProjectedBounds() {
        return provider.getProjectedBounds();
    }

    /**
     * Initialize this manager.
     *
     * @param context the context on which this manager should be initialized.
     */
    public void initialize(RenderContext context) {
        provider.initialize(context);
    }

    /**
     * Dispose this manager.
     *
     * @param context the context on which this manager has been initialized.
     */
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

    /**
     * Updates the visible tiles using for the given {@code OrthographicView}.
     *
     * @param context  the context on which the tiles are going to be displayed.
     * @param observer the view for which the tiles should be updated.
     * @throws ExecutionException   if an {@code ExecutionException} was thrown by the loading-task of a tile.
     * @throws InterruptedException if the update-operation has been interrupted.
     */
    public void update(RenderContext context, OrthographicView observer)
            throws ExecutionException, InterruptedException {

        TilingScheme scheme = provider.getTilingScheme();

        // view parameters
        Rect2d viewport = observer.getViewportBounds();
        double zoom     = observer.getZoomLevel();

        // get bounds, relative to the current view
        TileRect view     = scheme.getTiles(viewport, zoom);
        TileRect provided = scheme.getTiles(provider.getProjectedBounds(), zoom);
        TileRect common   = provided != null ? TileRect.intersect(view, provided) : null;    // provided and in view

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

    /**
     * Reloads updated and newly in-view tiles.
     *
     * @param context the context on which the tiles are going to be displayed.
     * @param common  the view-rectangle indicating visible tiles.
     * @return {@code true} if the internal list of visible tiles has changed.
     * @throws ExecutionException   if an {@code ExecutionException} was thrown by the loading-task of a tile.
     * @throws InterruptedException if the reload-operation has been interrupted.
     */
    private boolean mgmtReload(RenderContext context, TileRect common) throws ExecutionException, InterruptedException {

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
            while ((id = changed.poll()) != null) {
                if (id.x >= xmin && id.x <= xmax && id.y >= ymin && id.x <= ymax && id.z == common.zoom)
                    change |= mgmtAsyncReload(context, id);
            }
        }

        return change;
    }

    /**
     * Asynchronously (re-)loads the specified tile.
     *
     * @param context the context on which the tiles are going to be displayed.
     * @param id      the id of the tile that should be (re-)loaded.
     * @return {@code true} if the internal list of visible tiles has changed.
     * @throws ExecutionException   if an {@code ExecutionException} was thrown by the loading-task of a tile.
     * @throws InterruptedException if the operation has been interrupted.
     */
    private boolean mgmtAsyncReload(RenderContext context, TileId id) throws ExecutionException, InterruptedException {

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

    /**
     * Transitions all loading tiles that have finished loading to the list of visible tiles.
     *
     * @param context the context on which the tiles are going to be displayed.
     * @return {@code true} if the internal list of visible tiles has changed.
     * @throws ExecutionException   if an {@code ExecutionException} was thrown by the loading-task of a tile.
     * @throws InterruptedException if the operation has been interrupted.
     */
    private boolean mgmtMoveLoaded(RenderContext context) throws ExecutionException, InterruptedException {

        boolean         changed = false;
        HashSet<TileId> remove  = new HashSet<>();

        for (Map.Entry<TileId, Future<Tile>> entry : loading.entrySet()) {
            TileId       id   = entry.getKey();
            Future<Tile> task = entry.getValue();

            if (task.isDone()) {              // if task is done, remove it
                if (!task.isCancelled()) {    // if task is done and has not been cancelled, add the result
                    provider.release(context, visible.put(id, getFromTaskIgnoringCancellation(task)));
                    changed = true;
                }

                remove.add(id);
            }
        }

        loading.keySet().removeAll(remove);
        return changed;
    }

    /**
     * Remove all out-of-view invisible tiles that are currently loaded.
     *
     * @param context the context on which the tiles are going to be displayed.
     * @return {@code true} if the internal list of visible tiles has changed.
     * @throws ExecutionException   if an {@code ExecutionException} was thrown by the loading-task of a tile.
     * @throws InterruptedException if the operation has been interrupted.
     */
    private boolean mgmtRemoveInvisible(RenderContext context, TilingScheme scheme, TileRect common)
            throws ExecutionException, InterruptedException {

        boolean         changed = false;
        HashSet<TileId> remove  = new HashSet<>();

        // remove from visible
        for (Map.Entry<TileId, Tile> entry : visible.entrySet()) {
            TileId id = entry.getKey();

            boolean rem = common == null;
            if (!rem) {
                TileRect rect = scheme.getTiles(id, common.zoom);
                rem           = rect.xmax < common.xmin || rect.xmin > common.xmax || rect.ymax < common.ymin
                      || rect.ymin > common.ymax;
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
            TileId       id   = entry.getKey();
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

    /**
     * Remove all occluded tiles that are currently loaded.
     *
     * @param context the context on which the tiles are going to be displayed.
     * @param scheme  the {@code TilingScheme} that is used for the tiles.
     * @param common  the rectangle describing the viewport, i.e. the visible tiles.
     */
    private void mgmtRemoveOccluded(RenderContext context, TilingScheme scheme, TileRect common) {
        Set<TileId> remove  = new HashSet<>();
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

    /**
     * Rebuilds the pre-built visible tile-bucket-list.
     */
    private void mgmtRebuild() {
        prebuilt.clear();
        visible.values().stream().filter(x -> x != null).sorted(CMP_TILE).forEach(prebuilt::add);
    }

    /**
     * Clean-up cancelled loading tiles.
     *
     * @param context the context on which the tiles are going to be displayed.
     * @throws ExecutionException   if an {@code ExecutionException} was thrown by the loading-task of a tile.
     * @throws InterruptedException if the operation has been interrupted.
     */
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

    /**
     * Renders the visible tiles.
     *
     * @param context the context on which the tiles are going to be displayed.
     * @param view    the view with which the tiles are going to be displayed.
     */
    public void display(RenderContext context, OrthographicView view) {
        GL2ES2 gl         = context.getDrawable().getGL().getGL2ES2();
        Rect2d viewbounds = view.getViewportBounds();

        provider.beforeRendering(context);
        for (Tile tile : prebuilt) {
            Rect2d tilebounds = provider.getTilingScheme().getBounds(tile.getId());

            // translate from [[-1, -1],[1, 1]] to [bounds]
            double tsx = (tilebounds.xmax - tilebounds.xmin) / 2.0;
            double tsy = (tilebounds.ymax - tilebounds.ymin) / 2.0;
            double ttx = tilebounds.xmin + tsx;
            double tty = tilebounds.ymin + tsy;
            double vsx = (viewbounds.xmax - viewbounds.xmin) / 2.0;
            double vsy = (viewbounds.ymax - viewbounds.ymin) / 2.0;
            double vtx = viewbounds.xmin + vsx;
            double vty = viewbounds.ymin + vsy;

            tile.getTransformation().set(Mat4f.identity()
                    .scale((float) (1.0 / vsx), (float) (1 / vsy), 1)
                    .translate((float) (ttx - vtx), (float) (tty - vty), 0)
                    .scale((float) tsx, (float) tsy, 1));

            tile.display(context);
        }
        provider.afterRendering(context);

        context.ShaderState.unbind(gl);
    }

    /**
     * Loader-task for asynchronous tile loading.
     */
    private static class Loader implements Callable<Tile> {

        private RenderContext context;
        private TileProvider  provider;
        private TileId        id;

        /**
         * Constructs a new loader-task.
         *
         * @param context  the context on which the tile is going to be displayed.
         * @param provider the provider providing the tile.
         * @param id       the id of the tile.
         */
        private Loader(RenderContext context, TileProvider provider, TileId id) {
            this.context  = context;
            this.provider = provider;
            this.id       = id;
        }

        @Override
        public Tile call() throws CancellationException, ExecutionException {
            Tile tile;
            try {
                tile = provider.require(context, id);
            } catch (InterruptedException e) {
                throw new CancellationException();    // cancel this task
            }
            return tile;
        }
    }

    /**
     * Comparator to compare tiles by the z-component of their id.
     */
    private static class TileComparator implements Comparator<Tile> {

        @Override
        public int compare(Tile a, Tile b) {
            int za = a.getId().z;
            int zb = b.getId().z;

            return za > zb ? 1 : za < zb ? -1 : 0;
        }
    }

    /**
     * Change-listener implementation to handle changing tiles.
     */
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

    /**
     * Task to clean-up tiles.
     */
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

            if (!cancelling.isEmpty()) context.addTask(this, true);

            return null;
        }
    }
}
