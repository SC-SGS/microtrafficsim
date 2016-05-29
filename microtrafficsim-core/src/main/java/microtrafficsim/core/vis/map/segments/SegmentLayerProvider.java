package microtrafficsim.core.vis.map.segments;

import java.util.Set;

import microtrafficsim.core.map.Bounds;
import microtrafficsim.core.vis.context.RenderContext;
import microtrafficsim.core.vis.map.projections.Projection;
import microtrafficsim.math.Rect2d;


public interface SegmentLayerProvider {

	interface LayerChangeListener {
		void segmentChanged();
		void layerChanged(String layer);
	}
	
	
	void setProjection(Projection projection);
	Projection getProjection();

	Bounds getBounds();
	Rect2d getProjectedBounds();

	Set<String> getLayers();
	Set<String> getAvailableLayers();

	SegmentLayer require(RenderContext context, String layer) throws InterruptedException;
	void release(SegmentLayer layer);

	boolean addLayerChangeListener(LayerChangeListener listener);
	boolean removeLayerChangeListener(LayerChangeListener listener);
	boolean hasLayerChangeListener(LayerChangeListener listener);
}
