package microtrafficsim.core.vis;

import microtrafficsim.core.vis.context.RenderContext;
import microtrafficsim.core.vis.view.View;

import java.util.Collection;


public interface Visualizer extends Renderer {
    VisualizerConfig getDefaultConfig() throws UnsupportedFeatureException;

    Overlay putOverlay(int index, Overlay overlay);
    Overlay removeOverlay(int index);
    Overlay getOverlay(int index);
    Collection<Overlay> getAllOverlays();

    View getView();
    void resetView();

    RenderContext getContext();
}
