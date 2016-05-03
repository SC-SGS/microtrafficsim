package microtrafficsim.core.vis;

import com.jogamp.newt.event.MouseListener;
import microtrafficsim.core.vis.context.RenderContext;
import microtrafficsim.core.vis.input.KeyController;

import java.util.Collection;


public abstract class AbstractVisualization implements Visualization {

    private RenderContext context;
    private Visualizer visualizer;
    private KeyController keyController;
    private MouseListener mouseController;


    public AbstractVisualization(RenderContext context, Visualizer visualizer, MouseListener mouseController,
                                 KeyController keyController) {
        this.context = context;
        this.visualizer = visualizer;
        this.keyController = keyController;
        this.mouseController = mouseController;

        context.setRenderer(visualizer);
    }


    @Override
    public VisualizerConfig getDefaultConfig() throws UnsupportedFeatureException {
        return visualizer.getDefaultConfig();
    }

    @Override
    public MouseListener getMouseController() {
        return mouseController;
    }

    @Override
    public KeyController getKeyController() {
        return keyController;
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
