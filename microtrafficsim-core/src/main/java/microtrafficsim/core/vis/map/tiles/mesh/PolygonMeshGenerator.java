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
import microtrafficsim.core.vis.mesh.utils.Polygons;
import microtrafficsim.math.Rect2d;
import microtrafficsim.math.Vec2d;
import microtrafficsim.math.geometry.polygons.SweepLineTriangulator;
import microtrafficsim.math.geometry.polygons.Triangulator;
import microtrafficsim.utils.collections.ArrayUtils;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.HashMap;


public class PolygonMeshGenerator implements FeatureMeshGenerator {

    private Triangulator triangulator = new SweepLineTriangulator();


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

        // generate mesh
        ArrayList<Vec2d>   vertices = new ArrayList<>();
        ArrayList<Integer> indices  = new ArrayList<>();

        try {
            generateMesh(feature, src, tile, target, vertices, indices);
        } finally {
            src.getFeatureProvider().release(feature);
        }

        // create vertex buffer
        FloatBuffer vb = FloatBuffer.allocate(vertices.size() * 3);
        for (Vec2d v : vertices) {
            if (Thread.interrupted()) throw new InterruptedException();
            vb.put((float) v.x);
            vb.put((float) v.y);
            vb.put(0.0f);
        }
        vb.rewind();

        // create index buffer
        IntBuffer ib = IntBuffer.allocate(indices.size());
        ib.put(ArrayUtils.toArray(indices, null));
        ib.rewind();

        // create mesh and buckets
        Pos3IndexedMesh mesh = new Pos3IndexedMesh(GL3.GL_STATIC_DRAW, GL3.GL_TRIANGLES, vb, ib);

        ArrayList<Pos3IndexedMesh.Bucket> buckets = new ArrayList<>();
        buckets.add(mesh.new Bucket(0, 0, indices.size()));
        mesh.setBuckets(buckets);

        return mesh;
    }

    private void generateMesh(TileFeature<? extends Polygon> feature, FeatureTileLayerSource src, TileId tile,
                              Rect2d target, ArrayList<Vec2d> vertices, ArrayList<Integer> indices)
            throws InterruptedException {

        TilingScheme scheme     = src.getTilingScheme();
        Projection   projection = scheme.getProjection();
        Rect2d       bounds     = scheme.getBounds(getFeatureBounds(src, tile));

        // TODO: replace triangulation-method
        // NOTE: outline has start-node == end-node

        int counter = 0;
        for (Polygon polygon : feature.getData()) {
            if (Thread.interrupted()) throw new InterruptedException();

            Vec2d outline[] = project(projection, bounds, target, polygon.outline, polygon.outline.length - 1);
            Triangulator.Result result = triangulator.triangulate(new microtrafficsim.math.geometry.polygons.Polygon(outline).normalize());
            if (result == null) {
                System.err.println("Failed to triangulate polygon (around coordinate " + polygon.outline[0].toString() + ").");
                continue;
            }

            vertices.addAll(result.vertices);
            for (int i : result.indices)
                indices.add(counter + i);

            counter += result.vertices.size();
        }
    }

    /**
     * Project the given {@code Coordinate}s from the given source-rectangle to the given target-rectangle using the
     * given projection.
     *
     * @param projection the projection to use.
     * @param from       the source rectangle.
     * @param to         the target rectangle.
     * @param c          the coordinate to project.
     * @return the projected coordinate as vector.
     */
    private static Vec2d[] project(Projection projection, Rect2d from, Rect2d to, Coordinate[] c, int len) {
        Vec2d[] result = new Vec2d[len];

        for (int i = 0; i < len; i++)
            result[i] = project(projection, from, to, c[i]);

        return result;
    }

    /**
     * Project the given {@code Coordinate} from the given source-rectangle to the given target-rectangle using the
     * given projection.
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
