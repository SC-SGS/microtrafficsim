package microtrafficsim.core.vis.map.segments;

import microtrafficsim.core.map.layers.LayerDefinition;
import microtrafficsim.core.vis.context.RenderContext;
import microtrafficsim.core.vis.map.projections.Projection;


public interface SegmentLayerGenerator {
	SegmentLayer generate(RenderContext context, LayerDefinition def, Projection projection);
}
