package microtrafficsim.core.vis;

import com.jogamp.newt.event.KeyListener;
import com.jogamp.newt.event.MouseListener;
import microtrafficsim.core.vis.context.RenderContext;

import java.util.Collection;


public abstract class AbstractVisualization implements Visualization {

    private RenderContext context;
    private Visualizer visualizer;


    public AbstractVisualization(RenderContext context, Visualizer visualizer) {
        this.context = context;
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
