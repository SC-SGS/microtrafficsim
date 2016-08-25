package microtrafficsim.core.vis.map.tiles.layers;

import microtrafficsim.core.map.tiles.TileId;
import microtrafficsim.core.vis.context.RenderContext;
import microtrafficsim.core.vis.mesh.Mesh;
import microtrafficsim.core.vis.mesh.MeshBucket;
import microtrafficsim.core.vis.mesh.style.FeatureStyle;
import microtrafficsim.core.vis.opengl.shader.ShaderCompileException;
import microtrafficsim.core.vis.opengl.shader.ShaderLinkException;
import microtrafficsim.core.vis.opengl.shader.attributes.VertexArrayObject;
import microtrafficsim.math.Mat4f;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


/**
 * Tile layer for map-features.
 *
 * @author Maximilian Luz
 */
public class FeatureTileLayer extends TileLayer {

    private Mesh              mesh;
    private FeatureStyle      style;
    private VertexArrayObject vao;

    /**
     * Constructs a new {@code FeatureTileLayer} with the given parameters.
     *
     * @param tile      the tile to create tile-this layer for.
     * @param layer     the layer to create this tile-layer from.
     * @param transform the transform to transform the tile-layer to world space.
     * @param mesh      the mesh to be rendered in the tile-layer.
     * @param style     the style the tile-layer should be rendered with.
     */
    public FeatureTileLayer(TileId tile, Layer layer, Mat4f transform, Mesh mesh, FeatureStyle style) {
        super(tile, layer, transform);

        this.mesh  = mesh;
        this.style = style;
        this.vao   = null;
    }


    @Override
    public void initialize(RenderContext context) throws ShaderCompileException, ShaderLinkException {
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

    /**
     * Returns the mesh of this tile-layer.
     *
     * @return the mesh of this tile-layer.
     */
    public Mesh getMesh() {
        return mesh;
    }


    /**
     * Implementation of the {@code TileLayerBucket} for the {@code FeatureTileLayer}.
     */
    private class FeatureBucket extends TileLayerBucket {

        private MeshBucket mesh;

        /**
         * Constructs a new {@code FeatureBucket} with the given layer and mesh.
         *
         * @param layer the tile-layer for which the bucket should be created.
         * @param mesh  the mesh of the bucket.
         */
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
