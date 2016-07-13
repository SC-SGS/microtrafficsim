package microtrafficsim.core.vis.map.tiles.layers;

import microtrafficsim.core.map.tiles.TileId;
import microtrafficsim.core.vis.context.RenderContext;
import microtrafficsim.core.vis.mesh.Mesh;
import microtrafficsim.core.vis.mesh.MeshBucket;
import microtrafficsim.core.vis.mesh.style.FeatureStyle;
import microtrafficsim.core.vis.opengl.shader.attributes.VertexArrayObject;
import microtrafficsim.math.Mat4f;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


public class FeatureTileLayer extends TileLayer {

    private Mesh              mesh;
    private FeatureStyle      style;
    private VertexArrayObject vao;

    public FeatureTileLayer(TileId tile, Layer layer, Mat4f transform, Mesh mesh, FeatureStyle style) {
        super(tile, layer, transform);

        this.mesh  = mesh;
        this.style = style;
        this.vao   = null;
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
        if (vao != null) {    // if initialized
            vao.dispose(context.getDrawable().getGL().getGL2ES3());
            style.dispose(context);
        }

        // dispose mesh always, it is provided in the constructor
        mesh.dispose(context);
    }

    @Override
    public void display(RenderContext context) {
        style.bind(context);
        mesh.display(context, vao);
        style.unbind(context);
    }

    @Override
    public List<? extends TileLayerBucket> getBuckets() {
        return mesh.getBuckets()
                .stream()
                .map(m -> new FeatureBucket(this, m))
                .collect(Collectors.toCollection(ArrayList::new));
    }

    public Mesh getMesh() {
        return mesh;
    }


    private class FeatureBucket extends TileLayerBucket {

        private MeshBucket mesh;

        FeatureBucket(TileLayer layer, MeshBucket mesh) {
            super(layer, mesh.getZIndex());
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
