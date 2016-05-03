package microtrafficsim.core.vis.tilebased;

import microtrafficsim.core.vis.AbstractVisualization;
import microtrafficsim.core.vis.context.RenderContext;
import microtrafficsim.core.vis.input.OrthoInputController;
import microtrafficsim.core.vis.map.tiles.TileProvider;
import microtrafficsim.core.vis.view.OrthographicView;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class TileBasedVisualization extends AbstractVisualization {

    private static final int ZOOM_LEVEL_MIN = 0;
    private static final int ZOOM_LEVEL_MAX = 19;
    private static final float ZOOM_MULTIPLIER = 0.1f;

    private static final float Z_NEAR = 0.1f;
    private static final float Z_FAR = 1000.f;


    public TileBasedVisualization(int width, int height, TileProvider provider, int nWorkerThreads) {
        this(width, height, provider, Executors.newFixedThreadPool(nWorkerThreads));
    }

    public TileBasedVisualization(int width, int height, TileProvider provider, ExecutorService worker) {
        this(new OrthographicView(width, height, Z_NEAR, Z_FAR, ZOOM_LEVEL_MIN, ZOOM_LEVEL_MAX), provider, worker);
    }

    private TileBasedVisualization(OrthographicView view, TileProvider provider, ExecutorService worker) {
        this(new RenderContext(), view, provider, new OrthoInputController(view, ZOOM_MULTIPLIER), worker);
    }

    private TileBasedVisualization(RenderContext context, OrthographicView view, TileProvider provider,
                                   OrthoInputController controller, ExecutorService worker) {
        super(context, new TileBasedVisualizer(context, view, provider, worker), controller, controller);
    }
}
