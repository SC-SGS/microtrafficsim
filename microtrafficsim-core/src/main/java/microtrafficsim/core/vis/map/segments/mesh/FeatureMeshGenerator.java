package microtrafficsim.core.vis.map.segments.mesh;

import microtrafficsim.core.vis.context.RenderContext;
import microtrafficsim.core.vis.map.projections.Projection;
import microtrafficsim.core.vis.map.segments.FeatureSegmentLayerSource;
import microtrafficsim.core.vis.mesh.Mesh;


public interface FeatureMeshGenerator {
	interface FeatureMeshKey {}

	FeatureMeshKey getKey(RenderContext context, FeatureSegmentLayerSource src, Projection projection);
	Mesh generate(RenderContext context, FeatureSegmentLayerSource src, Projection projection);
}
