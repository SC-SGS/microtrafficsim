package microtrafficsim.core.vis.map.tiles.mesh;

import com.jogamp.opengl.GL3;
import microtrafficsim.core.map.Coordinate;
import microtrafficsim.core.map.TileFeature;
import microtrafficsim.core.map.features.Street;
import microtrafficsim.core.map.tiles.TileId;
import microtrafficsim.core.map.tiles.TileRect;
import microtrafficsim.core.map.tiles.TilingScheme;
import microtrafficsim.core.vis.context.RenderContext;
import microtrafficsim.core.vis.map.projections.Projection;
import microtrafficsim.core.vis.map.tiles.layers.FeatureTileLayerSource;
import microtrafficsim.core.vis.mesh.Mesh;
import microtrafficsim.core.vis.mesh.builder.LineMeshBuilder;
import microtrafficsim.core.vis.mesh.impl.DualFloatAttributeIndexedMesh;
import microtrafficsim.core.vis.mesh.style.Style;
import microtrafficsim.core.vis.mesh.utils.VertexSet;
import microtrafficsim.core.vis.utils.LaneOffset;
import microtrafficsim.math.*;
import microtrafficsim.utils.collections.HashListMultiMap;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;


/**
 * {@code FeatureMeshGenerator} for streets.
 *
 * @author Maximilian Luz
 */
public class StreetMeshGenerator implements FeatureMeshGenerator {

    @Override
    public FeatureMeshKey getKey(RenderContext context, FeatureTileLayerSource source, TileId tile, Rect2d target) {
        StreetStyle style = StreetStyle.from(source.getStyle());
        return new StreetMeshKey(
                context,
                getFeatureBounds(source, tile),
                target,
                source.getFeatureProvider(),
                source.getFeatureName(),
                source.getTilingScheme(),
                source.getRevision(),
                style.lanewidth,
                style.outline,
                style.useJoinsWhenPossible,
                style.drivingOnTheRight
        );
    }

    @Override
    public Mesh generate(RenderContext context, FeatureTileLayerSource src, TileId tile, Rect2d target)
            throws InterruptedException {
        // expand to handle thick lines
        TileRect expanded = new TileRect(tile.x - 1, tile.y - 1, tile.x + 1, tile.y + 1, tile.z);

        // get feature, return null if not available
        TileFeature<Street> feature = src.getFeatureProvider().require(src.getFeatureName(), expanded);
        if (feature == null) return null;

        // get tile and source properties
        TilingScheme scheme = src.getTilingScheme();
        Rect2d bounds = scheme.getBounds(getFeatureBounds(src, tile));
        MeshProjection projection = new MeshProjection(scheme.getProjection(), bounds, target);

        StreetStyle style = StreetStyle.from(src.getStyle());

        // generate mesh
        VertexSet<LineMeshBuilder.Vertex> vertices = new VertexSet<>();
        HashMap<Double, IndexBucket> buckets = new HashMap<>();

        try {
            generate(context, feature, projection, style, vertices, buckets);
        } finally {
            src.getFeatureProvider().release(feature);
        }

        return mesh(vertices, buckets.values(), projection);
    }

    private void generate(RenderContext context, TileFeature<? extends Street> feature, MeshProjection projection,
                          StreetStyle style, VertexSet<LineMeshBuilder.Vertex> vertices, HashMap<Double, IndexBucket> buckets)
            throws InterruptedException
    {
        HashListMultiMap<Coordinate, Street> intersections = null;
        if (style.useJoinsWhenPossible) {
            intersections = new HashListMultiMap<>();

            for (Street street : feature.getData()) {
                intersections.add(street.coordinates[0], street);
                intersections.add(street.coordinates[street.coordinates.length - 1], street);
            }

            intersections.entrySet().removeIf(entry -> {
                if (entry.getValue().size() != 2) return true;

                Street a = entry.getValue().get(0);
                Street b = entry.getValue().get(1);

                if (a == b)
                    return false;

                boolean aligned = a.coordinates[0].equals(b.coordinates[b.coordinates.length - 1])
                        || a.coordinates[a.coordinates.length - 1].equals(b.coordinates[0]);

                if (aligned) {
                    return a.numLanesFwd != b.numLanesFwd || a.numLanesBwd != b.numLanesBwd;
                } else {
                    return a.numLanesFwd != b.numLanesBwd || a.numLanesBwd != b.numLanesFwd;
                }
            });
        }

        int restart = context.PrimitiveRestart.getIndex();
        LineMeshBuilder builder = new LineMeshBuilder(null);

        for (Street street : feature.getData()) {
            if (Thread.interrupted()) throw new InterruptedException();

            IndexBucket bucket = buckets.computeIfAbsent(street.layer, k -> new IndexBucket(street.layer));
            builder.setEmitter(new BucketBuilder(vertices, bucket, restart));

            generate(street, projection, builder, style, intersections);
        }
    }

    private void generate(Street street, MeshProjection projection, LineMeshBuilder builder, StreetStyle style,
                          HashListMultiMap<Coordinate, Street> intersections) {
        Vec3d[] projected = projection.toGlobal(street.coordinates, street.layer);

        double linewidth = style.lanewidth * (street.numLanesFwd + street.numLanesBwd) + 2.0 * style.outline;
        double offset = LaneOffset.getCenterOffset(style.lanewidth, street, style.drivingOnTheRight);

        LineMeshBuilder.Style linestyle = new LineMeshBuilder.Style(style.cap, style.join, linewidth, offset,
                style.miterAngleLimit);

        Vec3d in = null;
        Vec3d out = null;
        if (intersections != null) {
            ArrayList<Street> ax = intersections.get(street.coordinates[0]);
            ArrayList<Street> bx = intersections.get(street.coordinates[street.coordinates.length - 1]);

            if (ax != null) {
                Vec3d a = getOtherPos(ax, street, projection);
                in = LineMeshBuilder.dirvec2d(a, projected[0]);
            }

            if (bx != null) {
                Vec3d b = getOtherPos(bx, street, projection);
                out = LineMeshBuilder.dirvec2d(projected[projected.length - 1], b);
            }
        }

        builder.add(projected, linestyle, in, out);
    }

    /**
     * Generate a Mesh instance from the given vertices and indices.
     *
     * @param vertices   the vertices from which the mesh will be generated.
     * @param indices    the indices in buckets from which the mesh will be generated.
     * @param projection the projection to project the mesh to the tile.
     * @return the generated mesh.
     * @throws InterruptedException if the executing thread is interrupted.
     */
    private Mesh mesh(VertexSet<LineMeshBuilder.Vertex> vertices, Collection<IndexBucket> indices,
                      MeshProjection projection) throws InterruptedException
    {
        // create vertex buffer
        FloatBuffer vb = FloatBuffer.allocate(vertices.size() * 6);
        for (LineMeshBuilder.Vertex v : vertices.getVertices()) {
            if (Thread.interrupted()) throw new InterruptedException();

            Vec3f pos = projection.globalToTile(v.position);

            vb.put(pos.x);
            vb.put(pos.y);
            vb.put(pos.z);
            vb.put((float) v.segment.x);
            vb.put((float) v.segment.y);
            vb.put((float) v.segment.z);
        }
        vb.rewind();

        // create index buffer
        int nIndices = 0;
        for (IndexBucket i : indices)
            nIndices += i.size();

        IntBuffer ib = IntBuffer.allocate(nIndices);
        for (IndexBucket bucket : indices)
            for (int i : bucket)
                ib.put(i);
        ib.rewind();

        // create mesh and buckets
        final int usage = GL3.GL_STATIC_DRAW;
        final int mode = GL3.GL_TRIANGLE_STRIP;
        DualFloatAttributeIndexedMesh mesh = DualFloatAttributeIndexedMesh.newPos3LineMesh(usage, mode, vb, ib);
        ArrayList<DualFloatAttributeIndexedMesh.Bucket> buckets = new ArrayList<>();

        int offset = 0;
        for (IndexBucket indexbucket : indices) {
            double layer = indexbucket.getLayer();
            int    count = indexbucket.size();

            buckets.add(mesh.new Bucket((float) layer, offset, count));
            offset += count;
        }

        mesh.setBuckets(buckets);
        return mesh;
    }


    private Vec3d getOtherPos(ArrayList<Street> streets, Street street, MeshProjection projection) {
        Street other = streets.get(0) != street ? streets.get(0) : streets.get(1);

        Vec3d otherPos;
        if (street.coordinates[street.coordinates.length - 1].equals(other.coordinates[0])) {
            otherPos = projection.toGlobal(other.coordinates[1], street.layer);
        } else if (street.coordinates[0].equals(other.coordinates[other.coordinates.length - 1])) {
            otherPos = projection.toGlobal(other.coordinates[other.coordinates.length - 2], street.layer);
        } else if (street.coordinates[0].equals(other.coordinates[0])) {
            otherPos = projection.toGlobal(other.coordinates[1], street.layer);
        } else {
            otherPos = projection.toGlobal(other.coordinates[other.coordinates.length - 2], street.layer);
        }

        return otherPos;
    }


    private static class StreetStyle {
        double lanewidth;
        double outline;
        LineMeshBuilder.CapType cap;
        LineMeshBuilder.JoinType join;
        double miterAngleLimit;
        boolean useJoinsWhenPossible;
        boolean drivingOnTheRight;

        public StreetStyle(
                double lanewidth,
                double outline,
                LineMeshBuilder.CapType cap,
                LineMeshBuilder.JoinType join,
                double miterAngleLimit,
                boolean useJoinsWhenPossible,
                boolean drivingOnTheRight
        ) {
            this.lanewidth = lanewidth;
            this.outline = outline;
            this.cap = cap;
            this.join = join;
            this.miterAngleLimit = miterAngleLimit;
            this.useJoinsWhenPossible = useJoinsWhenPossible;
            this.drivingOnTheRight = drivingOnTheRight;
        }

        public static StreetStyle from(Style style) {
            return new StreetStyle(
                    style.getProperty("lanewidth", 20.0),
                    style.getProperty("outline", 5.0),
                    LineMeshBuilder.CapType.ROUND,              // TODO
                    LineMeshBuilder.JoinType.ROUND,             // TODO
                    0.5f,                                       // TODO
                    true,                                       // TODO
                    true                                        // TODO
            );
        }
    }


    private static class IndexBucket extends ArrayList<Integer> {
        private double layer;

        public IndexBucket(double layer) {
            this.layer = layer;
        }


        public double getLayer() {
            return layer;
        }
    }

    private static class BucketBuilder implements LineMeshBuilder.VertexEmitter {
        private VertexSet<LineMeshBuilder.Vertex> vertices;
        private ArrayList<Integer> indices;
        private int restart;

        public BucketBuilder(VertexSet<LineMeshBuilder.Vertex> vertices, ArrayList<Integer> indices, int restart) {
            this.vertices = vertices;
            this.indices = indices;
            this.restart = restart;
        }

        public int add(LineMeshBuilder.Vertex vertex) {
            return vertices.add(vertex);
        }

        public void emit(int id) {
            indices.add(id);
        }

        public void next() {
            indices.add(restart);
        }
    }

    private static class MeshProjection {
        private Projection projection;
        private Rect2d from;
        private Rect2d to;

        /**
         * Create a projection to project the given {@code Coordinate} from the given rectangle to the given rectangle
         * using the given projection.
         *
         * @param projection the projection to use.
         * @param from       the source rectangle.
         * @param to         the target rectangle.
         */
        private MeshProjection(Projection projection, Rect2d from, Rect2d to) {
            this.projection = projection;
            this.from = from;
            this.to = to;
        }

        private Vec3d[] toGlobal(Coordinate[] coords, double layer) {
            Vec3d[] projected = new Vec3d[coords.length];
            for (int i = 0; i < coords.length; i++) {
                projected[i] = new Vec3d(projection.project(coords[i]), layer);
            }
            return projected;
        }

        private Vec3d toGlobal(Coordinate coord, double layer) {
            return new Vec3d(projection.project(coord), layer);
        }

        private Vec3f globalToTile(Vec3d p) {
            return new Vec3f(
                    (float) (((p.x - from.xmin) / (from.xmax - from.xmin)) * (to.xmax - to.xmin) + to.xmin),
                    (float) (((p.y - from.ymin) / (from.ymax - from.ymin)) * (to.ymax - to.ymin) + to.ymin),
                    (float) p.z
            );
        }
    }
}
