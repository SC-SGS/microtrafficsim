package microtrafficsim.core.vis;

import microtrafficsim.core.vis.context.RenderContext;

import java.util.Collection;


/**
 * Abstract implementation of the {@code Visualization}, providing the basic features.
 *
 * @author Maximilian Luz
 */
public abstract class AbstractVisualization implements Visualization {

    private RenderContext context;
    private Visualizer    visualizer;


    /**
     * Constructs a new {@code AbstractVisualization} for the given context and visualizer.
     *
     * @param context    the {@code RenderContext} on which this visualization is going to be displayed.
     * @param visualizer the visualizer providing the actual render-code.
     */
    public AbstractVisualization(RenderContext context, Visualizer visualizer) {
        this.context    = context;
        this.visualizer = visualizer;

        context.setRenderer(visualizer);
    }


    @Override
    public VisualizerConfig getDefaultConfig() throws UnsupportedFeatureException {
        return visualizer.getDefaultConfig();
    }


    @Override
    public RenderContext getRenderContext() {
        return context;
    }

    @Override
    public Visualizer getVisualizer() {
        return visualizer;
    }


    @Override
    public Overlay putOverlay(int index, Overlay overlay) {
        return visualizer.putOverlay(index, overlay);
    }

    @Override
    public Overlay removeOverlay(int index) {
        return visualizer.removeOverlay(index);
    }

    @Override
    public Overlay getOverlay(int index) {
        return visualizer.getOverlay(index);
    }

    @Override
    public Collection<Overlay> getAllOverlays() {
        return visualizer.getAllOverlays();
    }
}
