package microtrafficsim.core.vis.map.segments;

import microtrafficsim.core.map.layers.LayerSource;
import microtrafficsim.core.vis.context.RenderContext;
import microtrafficsim.core.vis.mesh.Mesh;
import microtrafficsim.core.vis.mesh.MeshBucket;
import microtrafficsim.core.vis.mesh.style.FeatureStyle;
import microtrafficsim.core.vis.opengl.shader.attributes.VertexArrayObject;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


public class FeatureSegmentLayer extends SegmentLayer {

	private Mesh mesh;
	private FeatureStyle style;
	private VertexArrayObject vao;
	
	public FeatureSegmentLayer(String name, int index, LayerSource source, Mesh mesh, FeatureStyle style) {
		super(name, index, source);

		this.mesh = mesh;
		this.style = style;
		this.vao = null;
	}
	

	@Override
	public void initialize(RenderContext context) {
		style.initialize(context);
		mesh.initialize(context);
		mesh.load(context);
		vao = mesh.createVAO(context, style.getShaderProgram());
	}

	@Override
	public void dispose(RenderContext context) {
		vao.dispose(context.getDrawable().getGL().getGL2ES3());
		mesh.dispose(context);
		style.dispose(context);
	}
	
	@Override
	public void display(RenderContext context) {
		style.bind(context);
		mesh.display(context, vao);
		style.unbind(context);
	}

	@Override
	public List<SegmentLayerBucket> getBuckets() {
		return mesh.getBuckets().stream()
				.map(FeatureBucket::new)
				.collect(Collectors.toCollection(ArrayList::new));
	}


	private class FeatureBucket extends SegmentLayerBucket {

		private MeshBucket mesh;

		public FeatureBucket(MeshBucket mesh) {
			super(FeatureSegmentLayer.this, mesh.getZIndex());
			this.mesh = mesh;
		}

		@Override
		public void display(RenderContext context) {
			style.bind(context);
			mesh.display(context, vao);
			style.unbind(context);
		}
	}
}
