package microtrafficsim.core.vis;

import com.jogamp.newt.event.KeyListener;
import com.jogamp.newt.event.MouseListener;

import microtrafficsim.core.vis.context.RenderContext;

import java.util.Collection;


public interface Visualization {
	VisualizerConfig getDefaultConfig() throws UnsupportedFeatureException;
	
	MouseListener getMouseController();
	KeyListener getKeyController();

	RenderContext getRenderContext();
	Visualizer getVisualizer();

	Overlay putOverlay(int index, Overlay overlay);
	Overlay removeOverlay(int index);
	Overlay getOverlay(int index);
	Collection<Overlay> getAllOverlays();
}
