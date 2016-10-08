package microtrafficsim.core.vis.map.tiles.mesh;

import com.jogamp.opengl.GL3;
import microtrafficsim.core.map.Coordinate;
import microtrafficsim.core.map.TileFeature;
import microtrafficsim.core.map.features.Polygon;
import microtrafficsim.core.map.tiles.TileId;
import microtrafficsim.core.map.tiles.TilingScheme;
import microtrafficsim.core.vis.context.RenderContext;
import microtrafficsim.core.vis.map.projections.Projection;
import microtrafficsim.core.vis.map.tiles.layers.FeatureTileLayerSource;
import microtrafficsim.core.vis.mesh.Mesh;
import microtrafficsim.core.vis.mesh.impl.Pos3IndexedMesh;
import microtrafficsim.math.Rect2d;
import microtrafficsim.math.Vec2d;
import microtrafficsim.utils.collections.ArrayUtils;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.HashMap;


public class PolygonMeshGenerator implements FeatureMeshGenerator {

    @Override
    public FeatureMeshKey getKey(RenderContext context, FeatureTileLayerSource source, TileId tile, Rect2d target) {
        return new PolygonMeshKey(
                context,
                getFeatureBounds(source, tile),
                target,
                source.getFeatureProvider(),
                source.getFeatureName(),
                source.getTilingScheme(),
                source.getRevision()
        );
    }

    @Override
    public Mesh generate(RenderContext context, FeatureTileLayerSource src, TileId tile, Rect2d target)
            throws InterruptedException {

        // get feature, return null if not available
        TileFeature<Polygon> feature = src.getFeatureProvider().require(src.getFeatureName(), tile);
        if (feature == null) return null;

        // get tile and source properties
        TilingScheme scheme     = src.getTilingScheme();
        Projection   projection = scheme.getProjection();
        Rect2d       bounds     = scheme.getBounds(getFeatureBounds(src, tile));

        // generate mesh
        ArrayList<Coordinate> vertices = new ArrayList<>();
        ArrayList<Integer>    indices  = new ArrayList<>();

        try {
            generateMesh(context, feature, vertices, indices);
        } finally {
            src.getFeatureProvider().release(feature);
        }

        // create vertex buffer
        FloatBuffer vb = FloatBuffer.allocate(vertices.size() * 3);
        for (Coordinate v : vertices) {
            if (Thread.interrupted()) throw new InterruptedException();
            Vec2d projected = project(projection, bounds, target, v);
            vb.put((float) projected.x);
            vb.put((float) projected.y);
            vb.put(0.0f);
        }
        vb.rewind();

        // create index buffer
        IntBuffer ib = IntBuffer.allocate(indices.size());
        ib.put(ArrayUtils.toArray(indices, null));
        ib.rewind();

        // create mesh and buckets
        Pos3IndexedMesh mesh = new Pos3IndexedMesh(GL3.GL_STATIC_DRAW, GL3.GL_LINE_STRIP, vb, ib);

        ArrayList<Pos3IndexedMesh.Bucket> buckets = new ArrayList<>();
        buckets.add(mesh.new Bucket(0, 0, indices.size()));
        mesh.setBuckets(buckets);

        return mesh;
    }

    private void generateMesh(RenderContext context, TileFeature<? extends Polygon> feature,
                              ArrayList<Coordinate> vertices, ArrayList<Integer> indices) throws InterruptedException {
        int restart = context.PrimitiveRestart.getIndex();

        int counter = 0;
        HashMap<Coordinate, Integer> indexmap = new HashMap<>();

        for (Polygon polygon : feature.getData()) {
            if (Thread.interrupted()) throw new InterruptedException();

            for (Coordinate c : polygon.outline) {
                int     index;
                Integer indexobj = indexmap.get(c);
                if (indexobj != null) {
                    index = indexobj;
                } else {
                    index = counter++;
                    vertices.add(c);
                    indexmap.put(c, index);
                }

                indices.add(index);
            }

            indices.add(restart);
        }
    }


    /**
     * Project the given {@code Coordinate} from the given rectangle to the given rectangle using the given projection.
     *
     * @param projection the projection to use.
     * @param from       the source rectangle.
     * @param to         the target rectangle.
     * @param c          the coordinate to project.
     * @return the projected coordinate as vector.
     */
    private static Vec2d project(Projection projection, Rect2d from, Rect2d to, Coordinate c) {
        Vec2d p = projection.project(c);
        p.x     = ((p.x - from.xmin) / (from.xmax - from.xmin)) * (to.xmax - to.xmin) + to.xmin;
        p.y     = ((p.y - from.ymin) / (from.ymax - from.ymin)) * (to.ymax - to.ymin) + to.ymin;
        return p;
    }
}
