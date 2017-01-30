package microtrafficsim.core.vis.tilebased;

import microtrafficsim.core.map.style.MapStyleSheet;
import microtrafficsim.core.vis.AbstractVisualization;
import microtrafficsim.core.vis.context.RenderContext;
import microtrafficsim.core.vis.input.OrthoInputController;
import microtrafficsim.core.vis.map.tiles.TileProvider;
import microtrafficsim.core.vis.view.OrthographicView;
import microtrafficsim.utils.concurrency.interruptsafe.InterruptSafeExecutors;

import java.util.concurrent.ExecutorService;


/**
 * Tile-based visualization using an {@code OrthographicView}.
 *
 * @author Maximilian Luz
 */
public class TileBasedVisualization extends AbstractVisualization {

    private static final int    ZOOM_LEVEL_MIN = 0;
    private static final int    ZOOM_LEVEL_MAX = 19;
    private static final double ZOOM_FACTOR    = 0.1;

    private static final float Z_NEAR = 0.1f;
    private static final float Z_FAR  = 1000.f;

    private OrthoInputController controller;


    /**
     * Constructs a new tile-based visualization using the given parameters.
     *
     * @param width          the width of the viewport.
     * @param height         the height of the viewport.
     * @param provider       the {@code TileProvider} providing the tiles to be displayed.
     * @param nWorkerThreads the number of worker-threads responsible for loading the tiles.
     */
    public TileBasedVisualization(int width, int height, TileProvider provider, int nWorkerThreads) {
        this(width, height, provider, InterruptSafeExecutors.newFixedThreadPool(nWorkerThreads));
    }

    /**
     * Constructs a new tile-based visualization using the given parameters.
     *
     * @param width    the width of the viewport.
     * @param height   the height of the viewport.
     * @param provider the {@code TileProvider} providing the tiles to be displayed.
     * @param worker   the {@code ExecutorService} providing the worker-threads.
     */
    public TileBasedVisualization(int width, int height, TileProvider provider, ExecutorService worker) {
        this(new OrthographicView(width, height, Z_NEAR, Z_FAR, ZOOM_LEVEL_MIN, ZOOM_LEVEL_MAX), provider, worker);
    }

    /**
     * Constructs a new tile-based visualization using the given parameters.
     *
     * @param view     the {@code OrthographicView} to be used for this visualization.
     * @param provider the {@code TileProvider} providing the tiles to be displayed.
     * @param worker   the {@code ExecutorService} providing the worker-threads.
     */
    private TileBasedVisualization(OrthographicView view, TileProvider provider, ExecutorService worker) {
        this(new RenderContext(), view, provider, worker);
    }

    /**
     * Constructs a new tile-based visualization using the given parameters.
     *
     * @param context  the {@code RenderContext} on which this visualization will be displayed.
     * @param view     the {@code OrthographicView} to be used for this visualization.
     * @param provider the {@code TileProvider} providing the tiles to be displayed.
     * @param worker   the {@code ExecutorService} providing the worker-threads.
     */
    private TileBasedVisualization(RenderContext context, OrthographicView view, TileProvider provider,
                                   ExecutorService worker) {
        super(context, new TileBasedVisualizer(context, view, provider, worker));
        this.controller = new OrthoInputController(view, ZOOM_FACTOR);
    }


    /**
     * Applies the given style-sheet to this visualization.
     *
     * @param style the style-sheet to apply.
     */
    public void apply(MapStyleSheet style) {
        ((TileBasedVisualizer) getVisualizer()).apply(style);
    }

    /*
    |===========================|
    | (c) AbstractVisualization |
    |===========================|
    */
    @Override
    protected OrthoInputController getController() {
        return controller;
    }
}
