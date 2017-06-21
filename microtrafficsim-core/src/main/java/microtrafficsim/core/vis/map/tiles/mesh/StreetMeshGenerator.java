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
import microtrafficsim.core.vis.mesh.impl.DualFloatAttributeIndexedMesh;
import microtrafficsim.core.vis.mesh.style.Style;
import microtrafficsim.core.vis.mesh.utils.VertexSet;
import microtrafficsim.math.*;
import microtrafficsim.utils.hashing.FNVHashBuilder;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;


// TODO: use joins instead of caps when possible


/**
 * {@code FeatureMeshGenerator} for streets.
 *
 * @author Maximilian Luz
 */
public class StreetMeshGenerator implements FeatureMeshGenerator {

    @Override
    public FeatureMeshKey getKey(RenderContext context, FeatureTileLayerSource source, TileId tile, Rect2d target) {
        Common common = getCommonProps(source.getStyle(), true);
        return new StreetMeshKey(
                context,
                getFeatureBounds(source, tile),
                target,
                source.getFeatureProvider(),
                source.getFeatureName(),
                source.getTilingScheme(),
                source.getRevision(),
                common.lanewidth,
                common.outline,
                common.drivingOnTheRight
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
        Projection projection = scheme.getProjection();
        Rect2d bounds = scheme.getBounds(getFeatureBounds(src, tile));

        Common props = getCommonProps(src.getStyle(), true);        // TODO: get from config

        // generate mesh
        VertexSet<Vertex>            vertices = new VertexSet<>();
        HashMap<Double, IndexBucket> buckets = new HashMap<>();

        try {
            generate(context, feature, new MeshProjection(projection, bounds, target), props, vertices, buckets);
        } finally {
            src.getFeatureProvider().release(feature);
        }

        return mesh(vertices, buckets.values());
    }

    private void generate(RenderContext context, TileFeature<? extends Street> feature, MeshProjection projection,
                          Common common, VertexSet<Vertex> vertices, HashMap<Double, IndexBucket> buckets)
            throws InterruptedException
    {
        int restart = context.PrimitiveRestart.getIndex();

        for (Street street : feature.getData()) {
            if (Thread.interrupted()) throw new InterruptedException();

            IndexBucket bucket = buckets.computeIfAbsent(street.layer, k -> new IndexBucket(street.layer));
            generate(street, projection, new BucketBuilder(vertices, bucket, restart), common);
        }
    }

    /**
     * Generate a Mesh instance from the given vertices and indices.
     *
     * @param vertices   the vertices from which the mesh will be generated.
     * @param indices    the indices in buckets from which the mesh will be generated.
     * @return the generated mesh.
     * @throws InterruptedException if the executing thread is interrupted.
     */
    private Mesh mesh(VertexSet<Vertex> vertices, Collection<IndexBucket> indices) throws InterruptedException {
        // create vertex buffer
        FloatBuffer vb = FloatBuffer.allocate(vertices.size() * 6);
        for (Vertex v : vertices.getVertices()) {
            if (Thread.interrupted()) throw new InterruptedException();

            vb.put(v.pos.x);
            vb.put(v.pos.y);
            vb.put(v.layer);
            vb.put(v.line.x);
            vb.put(v.line.y);
            vb.put(v.line.z);
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


    private void generate(Street street, MeshProjection projection, BucketBuilder builder, Common common) {
        Vec2d[] projected = projection.project(street.coordinates);
        StreetProps props = new StreetProps(street, common.drivingOnTheRight);

        Vec2d thisPos = projected[0];
        Vec2d nextPos = projected[1];

        Vec3d thisDir = dirvec(thisPos, nextPos);

        genCapBegin(builder, common, thisPos, thisDir, props);

        for (int i = 2; i < projected.length; i++) {
            thisPos = nextPos;
            nextPos = projected[i];

            Vec3d lastDir = thisDir;
            thisDir = dirvec(thisPos, nextPos);

            genJoin(builder, common, thisPos, lastDir, thisDir, props);
        }

        genCapEnd(builder, common, nextPos, thisDir, props);
        builder.restart();
    }

    private void genCapBegin(BucketBuilder builder, Common common, Vec2d pos, Vec3d dir, StreetProps street) {
        switch (common.cap) {
            case ROUND:
                genRoundCapBegin(builder, common, pos, dir, street);
                break;

            case SQUARE:
                genSquareCapBegin(builder, common, pos, dir, street);
                break;

            default:
            case BUTT:
                genButtCap(builder, common, pos, dir, street);
                break;
        }
    }

    private void genCapEnd(BucketBuilder builder, Common common, Vec2d pos, Vec3d dir, StreetProps street) {
        switch (common.cap) {
            case ROUND:
                genRoundCapEnd(builder, common, pos, dir, street);
                break;

            case SQUARE:
                genSquareCapEnd(builder, common, pos, dir, street);
                break;

            default:
            case BUTT:
                genButtCap(builder, common, pos, dir, street);
                break;
        }
    }

    private void genJoin(BucketBuilder builder, Common common, Vec2d pos, Vec3d dirIn, Vec3d dirOut, StreetProps street) {
        if (Math.abs(dirIn.xy().cross(dirOut.xy())) < 0.01) {     // handle aligned lines
            if (dirIn.dot(dirOut) < 0) {                // if lines go in the opposite direction, generate caps
                genCapEnd(builder, common, pos, dirIn, street);
                builder.restart();
                genCapBegin(builder, common, pos, dirOut, street);
            }

            return;
        }

        switch (common.join) {
            case ROUND:
                genRoundJoin(builder, common, pos, dirIn, dirOut, street);
                break;

            case BEVEL:
                genBevelJoin(builder, common, pos, dirIn, dirOut, street);
                break;

            case MITER:
                genMiterJoin(builder, common, pos, dirIn, dirOut, street);
                break;

            default:
            case BUTT:
                genButtCapJoin(builder, common, pos, dirIn, dirOut, street);
                break;
        }
    }


    private void genButtCap(BucketBuilder builder, Common common, Vec2d pos, Vec3d dir, StreetProps street) {
        Vec2d normal = new Vec2d(-dir.y, dir.x);

        double left = common.outline + common.lanewidth * street.lanesLeft;
        double right = common.outline + common.lanewidth * street.lanesRight;
        double ext = (left + right) / 2.0;

        Vec2f p1 = new Vec2f(Vec2d.mul(normal, right).add(pos));
        Vec2f p2 = new Vec2f(Vec2d.mul(normal, -left).add(pos));

        Vec3f l1 = new Vec3f(0.0f, (float)  ext, (float) ext);
        Vec3f l2 = new Vec3f(0.0f, (float) -ext, (float) ext);

        builder.addIndex(builder.addVertex(new Vertex(p1, street.layer, l1)));
        builder.addIndex(builder.addVertex(new Vertex(p2, street.layer, l2)));
    }

    private void genSquareCapBegin(BucketBuilder builder, Common common, Vec2d pos, Vec3d dir, StreetProps street) {
        Vec2d normal = new Vec2d(-dir.y, dir.x);

        double left = common.outline + common.lanewidth * street.lanesLeft;
        double right = common.outline + common.lanewidth * street.lanesRight;
        double ext = (left + right) / 2.0;

        Vec2d p1 = Vec2d.mul(normal, right).add(pos);
        Vec2d p2 = Vec2d.mul(normal, -left).add(pos);
        Vec2d p3 = dir.xy().mul(-ext).add(p1);
        Vec2d p4 = dir.xy().mul(-ext).add(p2);

        Vec3f l1 = new Vec3f(0.0f, (float)  ext, (float) ext);
        Vec3f l2 = new Vec3f(0.0f, (float) -ext, (float) ext);
        Vec3f l3 = new Vec3f(-(float) ext, 0.0f, (float) ext);
        Vec3f l4 = new Vec3f(-(float) ext, 0.0f, (float) ext);

        Vertex v0 = new Vertex(new Vec2f(pos), street.layer, new Vec3f(0.0f, 0.0f, (float) ext));
        Vertex v1 = new Vertex(new Vec2f(p1), street.layer, l1);
        Vertex v2 = new Vertex(new Vec2f(p2), street.layer, l2);
        Vertex v3 = new Vertex(new Vec2f(p3), street.layer, l1);
        Vertex v4 = new Vertex(new Vec2f(p4), street.layer, l2);
        Vertex v5 = new Vertex(new Vec2f(p3), street.layer, l3);
        Vertex v6 = new Vertex(new Vec2f(p4), street.layer, l4);

        int i0 = builder.addVertex(v0);
        int i1 = builder.addVertex(v1);
        int i3 = builder.addVertex(v3);
        int i4 = builder.addVertex(v4);
        int i2 = builder.addVertex(v2);
        int i5 = builder.addVertex(v5);
        int i6 = builder.addVertex(v6);

        builder.addIndex(i0);
        builder.addIndex(i1);
        builder.addIndex(i3);
        builder.restart();
        builder.addIndex(i0);
        builder.addIndex(i4);
        builder.addIndex(i2);
        builder.restart();
        builder.addIndex(i0);
        builder.addIndex(i5);
        builder.addIndex(i6);
        builder.restart();
        builder.addIndex(i1);
        builder.addIndex(i2);
    }

    private void genSquareCapEnd(BucketBuilder builder, Common common, Vec2d pos, Vec3d dir, StreetProps street) {
        Vec2d normal = new Vec2d(-dir.y, dir.x);

        double left = common.outline + common.lanewidth * street.lanesLeft;
        double right = common.outline + common.lanewidth * street.lanesRight;
        double ext = (left + right) / 2.0;

        Vec2d p1 = Vec2d.mul(normal, right).add(pos);
        Vec2d p2 = Vec2d.mul(normal, -left).add(pos);
        Vec2d p3 = dir.xy().mul(ext).add(p1);
        Vec2d p4 = dir.xy().mul(ext).add(p2);

        Vec3f l1 = new Vec3f(0.0f, (float)  ext, (float) ext);
        Vec3f l2 = new Vec3f(0.0f, (float) -ext, (float) ext);
        Vec3f l3 = new Vec3f((float) ext, 0.0f, (float) ext);
        Vec3f l4 = new Vec3f((float) ext, 0.0f, (float) ext);

        Vertex v0 = new Vertex(new Vec2f(pos), street.layer, new Vec3f(0.0f, 0.0f, (float) ext));
        Vertex v1 = new Vertex(new Vec2f(p1), street.layer, l1);
        Vertex v2 = new Vertex(new Vec2f(p2), street.layer, l2);
        Vertex v3 = new Vertex(new Vec2f(p3), street.layer, l1);
        Vertex v4 = new Vertex(new Vec2f(p4), street.layer, l2);
        Vertex v5 = new Vertex(new Vec2f(p3), street.layer, l3);
        Vertex v6 = new Vertex(new Vec2f(p4), street.layer, l4);

        int i1 = builder.addVertex(v1);
        int i2 = builder.addVertex(v2);
        int i0 = builder.addVertex(v0);
        int i3 = builder.addVertex(v3);
        int i4 = builder.addVertex(v4);
        int i6 = builder.addVertex(v6);
        int i5 = builder.addVertex(v5);

        builder.addIndex(i1);
        builder.addIndex(i2);
        builder.restart();
        builder.addIndex(i0);
        builder.addIndex(i3);
        builder.addIndex(i1);
        builder.restart();
        builder.addIndex(i0);
        builder.addIndex(i2);
        builder.addIndex(i4);
        builder.restart();
        builder.addIndex(i0);
        builder.addIndex(i6);
        builder.addIndex(i5);
    }

    private void genRoundCapBegin(BucketBuilder builder, Common common, Vec2d pos, Vec3d dir, StreetProps street) {
        Vec2d normal = new Vec2d(-dir.y, dir.x);

        double left = common.outline + common.lanewidth * street.lanesLeft;
        double right = common.outline + common.lanewidth * street.lanesRight;
        double ext = (left + right) / 2.0;

        Vec2d p1 = Vec2d.mul(normal, right).add(pos);
        Vec2d p2 = Vec2d.mul(normal, -left).add(pos);
        Vec2d p3 = dir.xy().mul(-ext).add(p1);
        Vec2d p4 = dir.xy().mul(-ext).add(p2);

        Vec3f l1 = new Vec3f(        0.0f, (float)  ext, (float) ext);
        Vec3f l2 = new Vec3f(        0.0f, (float) -ext, (float) ext);
        Vec3f l3 = new Vec3f(-(float) ext, (float)  ext, (float) ext);
        Vec3f l4 = new Vec3f(-(float) ext, (float) -ext, (float) ext);

        builder.addIndex(builder.addVertex(new Vertex(new Vec2f(p3), street.layer, l3)));
        builder.addIndex(builder.addVertex(new Vertex(new Vec2f(p4), street.layer, l4)));
        builder.addIndex(builder.addVertex(new Vertex(new Vec2f(p1), street.layer, l1)));
        builder.addIndex(builder.addVertex(new Vertex(new Vec2f(p2), street.layer, l2)));
    }

    private void genRoundCapEnd(BucketBuilder builder, Common common, Vec2d pos, Vec3d dir, StreetProps street) {
        Vec2d normal = new Vec2d(-dir.y, dir.x);

        double left = common.outline + common.lanewidth * street.lanesLeft;
        double right = common.outline + common.lanewidth * street.lanesRight;
        double ext = (left + right) / 2.0;

        Vec2d p1 = Vec2d.mul(normal, right).add(pos);
        Vec2d p2 = Vec2d.mul(normal, -left).add(pos);
        Vec2d p3 = dir.xy().mul(ext).add(p1);
        Vec2d p4 = dir.xy().mul(ext).add(p2);

        Vec3f l1 = new Vec3f(       0.0f, (float)  ext, (float) ext);
        Vec3f l2 = new Vec3f(       0.0f, (float) -ext, (float) ext);
        Vec3f l3 = new Vec3f((float) ext, (float)  ext, (float) ext);
        Vec3f l4 = new Vec3f((float) ext, (float) -ext, (float) ext);

        builder.addIndex(builder.addVertex(new Vertex(new Vec2f(p1), street.layer, l1)));
        builder.addIndex(builder.addVertex(new Vertex(new Vec2f(p2), street.layer, l2)));
        builder.addIndex(builder.addVertex(new Vertex(new Vec2f(p3), street.layer, l3)));
        builder.addIndex(builder.addVertex(new Vertex(new Vec2f(p4), street.layer, l4)));
    }


    private void genButtCapJoin(BucketBuilder builder, Common common, Vec2d pos, Vec3d dirIn, Vec3d dirOut, StreetProps street) {
        genButtCap(builder, common, pos, dirIn, street);
        builder.restart();
        genButtCap(builder, common, pos, dirOut, street);
    }

    private void genMiterJoin(BucketBuilder builder, Common common, Vec2d pos, Vec3d dirIn, Vec3d dirOut, StreetProps street) {
        double left = common.outline + common.lanewidth * street.lanesLeft;
        double right = common.outline + common.lanewidth * street.lanesRight;
        double ext = (left + right) / 2.0;

        double curve = Math.signum(dirIn.xy().cross(dirOut.xy()));

        double inner;
        double outer;

        if (curve > 0) {
            inner = right;
            outer = -left;
        } else {
            inner = -left;
            outer = right;
        }

        Vec2d normalIn  = new Vec2d(-dirIn.y, dirIn.x);
        Vec2d normalOut = new Vec2d(-dirOut.y, dirOut.x);
        Vec2d normalMed = Vec2d.add(normalIn, normalOut).normalize();

        double cos = normalIn.dot(normalMed);
        double innerext = inner / cos;
        boolean intersects = cos != 0.0 && Double.isFinite(innerext)
                && (innerext * innerext) <= (dirIn.z * dirIn.z + inner * inner)
                && (innerext * innerext) <= (dirOut.z * dirOut.z + inner * inner);

        boolean bevel = cos < common.miterAngleLimit;

        Vec3f l0 = new Vec3f(0.0f,         0.0f, (float) ext);
        Vec3f l1 = new Vec3f(0.0f, (float)  ext, (float) ext);
        Vec3f l2 = new Vec3f(0.0f, (float) -ext, (float) ext);
        Vec3f l3 = new Vec3f(0.0f, (float) (-ext * curve), (float) ext);

        if (intersects) {
            Vec2d pInner = Vec2d.mul(normalMed, innerext).add(pos);
            Vec2d pOuterA = Vec2d.mul(normalIn, outer).add(pos);
            Vec2d pOuterB = Vec2d.mul(normalOut, outer).add(pos);
            Vec2f pCenter = new Vec2f(pos);

            if (curve > 0) {
                int iInner = builder.addVertex(new Vertex(new Vec2f(pInner), street.layer, l1));
                int iOuterA = builder.addVertex(new Vertex(new Vec2f(pOuterA), street.layer, l2));
                int iOuterB = builder.addVertex(new Vertex(new Vec2f(pOuterB), street.layer, l2));
                int iCenter = builder.addVertex(new Vertex(new Vec2f(pCenter), street.layer, l0));

                builder.addIndex(iInner);           // connector in
                builder.addIndex(iOuterA);
                builder.restart();
                builder.addIndex(iOuterA);          // filler
                builder.addIndex(iCenter);
                builder.addIndex(iInner);
                builder.addIndex(iOuterB);
                builder.restart();
                if (!bevel) {                       // miter cap
                    Vec2d pOuter = Vec2d.mul(normalMed, -innerext).add(pos);

                    builder.addIndex(iCenter);
                    builder.addIndex(iOuterA);
                    builder.addIndex(builder.addVertex(new Vertex(new Vec2f(pOuter), street.layer, l2)));
                    builder.restart();
                    builder.addIndex(iCenter);
                    builder.addIndex(builder.addVertex(new Vertex(new Vec2f(pOuter), street.layer, l2)));
                    builder.addIndex(iOuterB);
                } else {                            // bevel cap
                    builder.addIndex(iCenter);
                    builder.addIndex(builder.addVertex(new Vertex(new Vec2f(pOuterA), street.layer, l3)));
                    builder.addIndex(builder.addVertex(new Vertex(new Vec2f(pOuterB), street.layer, l3)));
                }
                builder.restart();
                builder.addIndex(iInner);           // connector out
                builder.addIndex(iOuterB);
            } else {
                int iInner = builder.addVertex(new Vertex(new Vec2f(pInner), street.layer, l2));
                int iOuterA = builder.addVertex(new Vertex(new Vec2f(pOuterA), street.layer, l1));
                int iOuterB = builder.addVertex(new Vertex(new Vec2f(pOuterB), street.layer, l1));
                int iCenter = builder.addVertex(new Vertex(new Vec2f(pCenter), street.layer, l0));

                builder.addIndex(iOuterA);          // connector in
                builder.addIndex(iInner);
                builder.restart();
                builder.addIndex(iOuterA);          // filler
                builder.addIndex(iInner);
                builder.addIndex(iCenter);
                builder.addIndex(iOuterB);
                builder.restart();
                if (!bevel) {                       // miter cap
                    Vec2d pOuter = Vec2d.mul(normalMed, -innerext).add(pos);

                    builder.addIndex(iCenter);
                    builder.addIndex(builder.addVertex(new Vertex(new Vec2f(pOuter), street.layer, l1)));
                    builder.addIndex(iOuterA);
                    builder.restart();
                    builder.addIndex(iCenter);
                    builder.addIndex(iOuterB);
                    builder.addIndex(builder.addVertex(new Vertex(new Vec2f(pOuter), street.layer, l1)));
                } else {                            // bevel cap
                    builder.addIndex(iCenter);
                    builder.addIndex(builder.addVertex(new Vertex(new Vec2f(pOuterB), street.layer, l3)));
                    builder.addIndex(builder.addVertex(new Vertex(new Vec2f(pOuterA), street.layer, l3)));
                }
                builder.restart();
                builder.addIndex(iOuterB);          // connector out
                builder.addIndex(iInner);
            }

        } else {
            Vec2f pRightA = new Vec2f(Vec2d.mul(normalIn, right).add(pos));
            Vec2f pLeftA  = new Vec2f(Vec2d.mul(normalIn, -left).add(pos));
            Vec2f pRightB = new Vec2f(Vec2d.mul(normalOut, right).add(pos));
            Vec2f pLeftB  = new Vec2f(Vec2d.mul(normalOut, -left).add(pos));
            Vec2f pCenter = new Vec2f(pos);

            int iRightA = builder.addVertex(new Vertex(pRightA, street.layer, l1));
            int iLeftA  = builder.addVertex(new Vertex(pLeftA, street.layer, l2));
            int iRightB = builder.addVertex(new Vertex(pRightB, street.layer, l1));
            int iLeftB  = builder.addVertex(new Vertex(pLeftB, street.layer, l2));
            int iCenter = builder.addVertex(new Vertex(pCenter, street.layer, l0));

            // connector in
            builder.addIndex(iRightA);
            builder.addIndex(iLeftA);
            builder.restart();

            // bevel cap
            if (curve > 0) {
                if (!bevel) {                       // miter cap
                    Vec2d pOuter = Vec2d.mul(normalMed, -innerext).add(pos);

                    builder.addIndex(iCenter);
                    builder.addIndex(iLeftA);
                    builder.addIndex(builder.addVertex(new Vertex(new Vec2f(pOuter), street.layer, l2)));
                    builder.restart();
                    builder.addIndex(iCenter);
                    builder.addIndex(builder.addVertex(new Vertex(new Vec2f(pOuter), street.layer, l2)));
                    builder.addIndex(iLeftB);
                } else {                            // bevel cap
                    builder.addIndex(builder.addVertex(new Vertex(pLeftA, street.layer, l3)));
                    builder.addIndex(builder.addVertex(new Vertex(pLeftB, street.layer, l3)));
                    builder.addIndex(iCenter);
                }
            } else {
                if (!bevel) {                       // miter cap
                    Vec2d pOuter = Vec2d.mul(normalMed, -innerext).add(pos);

                    builder.addIndex(iCenter);
                    builder.addIndex(builder.addVertex(new Vertex(new Vec2f(pOuter), street.layer, l1)));
                    builder.addIndex(iRightA);
                    builder.restart();
                    builder.addIndex(iCenter);
                    builder.addIndex(iRightB);
                    builder.addIndex(builder.addVertex(new Vertex(new Vec2f(pOuter), street.layer, l1)));
                } else {                            // bevel cap
                    builder.addIndex(builder.addVertex(new Vertex(pRightB, street.layer, l3)));
                    builder.addIndex(builder.addVertex(new Vertex(pRightA, street.layer, l3)));
                    builder.addIndex(iCenter);
                }
            }
            builder.restart();

            // connector out
            builder.addIndex(iRightB);
            builder.addIndex(iLeftB);
        }
    }

    private void genBevelJoin(BucketBuilder builder, Common common, Vec2d pos, Vec3d dirIn, Vec3d dirOut, StreetProps street) {
        double left = common.outline + common.lanewidth * street.lanesLeft;
        double right = common.outline + common.lanewidth * street.lanesRight;
        double ext = (left + right) / 2.0;

        double curve = Math.signum(dirIn.xy().cross(dirOut.xy()));

        double inner;
        double outer;

        if (curve > 0) {
            inner = right;
            outer = -left;
        } else {
            inner = -left;
            outer = right;
        }

        Vec2d normalIn  = new Vec2d(-dirIn.y, dirIn.x);
        Vec2d normalOut = new Vec2d(-dirOut.y, dirOut.x);
        Vec2d normalMed = Vec2d.add(normalIn, normalOut).normalize();

        double cos = normalIn.dot(normalMed);
        double innerext = inner / cos;
        boolean intersects = cos != 0.0 && Double.isFinite(innerext)
                && (innerext * innerext) <= (dirIn.z * dirIn.z + inner * inner)
                && (innerext * innerext) <= (dirOut.z * dirOut.z + inner * inner);

        Vec3f l0 = new Vec3f(0.0f,         0.0f, (float) ext);
        Vec3f l1 = new Vec3f(0.0f, (float)  ext, (float) ext);
        Vec3f l2 = new Vec3f(0.0f, (float) -ext, (float) ext);
        Vec3f l3 = new Vec3f(0.0f, (float) (-ext * curve), (float) ext);

        if (intersects) {
            Vec2d pInner = Vec2d.mul(normalMed, innerext).add(pos);
            Vec2d pOuterA = Vec2d.mul(normalIn, outer).add(pos);
            Vec2d pOuterB = Vec2d.mul(normalOut, outer).add(pos);
            Vec2f pCenter = new Vec2f(pos);

            if (curve > 0) {
                int iInner = builder.addVertex(new Vertex(new Vec2f(pInner), street.layer, l1));
                int iOuterA = builder.addVertex(new Vertex(new Vec2f(pOuterA), street.layer, l2));
                int iOuterB = builder.addVertex(new Vertex(new Vec2f(pOuterB), street.layer, l2));
                int iCenter = builder.addVertex(new Vertex(new Vec2f(pCenter), street.layer, l0));
                int iOuterAC = builder.addVertex(new Vertex(new Vec2f(pOuterA), street.layer, l3));
                int iOuterBC = builder.addVertex(new Vertex(new Vec2f(pOuterB), street.layer, l3));

                builder.addIndex(iInner);       // connector in
                builder.addIndex(iOuterA);
                builder.restart();
                builder.addIndex(iOuterA);      // filler
                builder.addIndex(iCenter);
                builder.addIndex(iInner);
                builder.addIndex(iOuterB);
                builder.restart();
                builder.addIndex(iCenter);      // bevel cap
                builder.addIndex(iOuterAC);
                builder.addIndex(iOuterBC);
                builder.restart();
                builder.addIndex(iInner);       // connector out
                builder.addIndex(iOuterB);
            } else {
                int iInner = builder.addVertex(new Vertex(new Vec2f(pInner), street.layer, l2));
                int iOuterA = builder.addVertex(new Vertex(new Vec2f(pOuterA), street.layer, l1));
                int iOuterB = builder.addVertex(new Vertex(new Vec2f(pOuterB), street.layer, l1));
                int iCenter = builder.addVertex(new Vertex(new Vec2f(pCenter), street.layer, l0));
                int iOuterAC = builder.addVertex(new Vertex(new Vec2f(pOuterA), street.layer, l3));
                int iOuterBC = builder.addVertex(new Vertex(new Vec2f(pOuterB), street.layer, l3));

                builder.addIndex(iOuterA);      // connector in
                builder.addIndex(iInner);
                builder.restart();
                builder.addIndex(iOuterA);      // filler
                builder.addIndex(iInner);
                builder.addIndex(iCenter);
                builder.addIndex(iOuterB);
                builder.restart();
                builder.addIndex(iCenter);      // bevel cap
                builder.addIndex(iOuterBC);
                builder.addIndex(iOuterAC);
                builder.restart();
                builder.addIndex(iOuterB);      // connector out
                builder.addIndex(iInner);
            }

        } else {
            Vec2f pRightA = new Vec2f(Vec2d.mul(normalIn, right).add(pos));
            Vec2f pLeftA  = new Vec2f(Vec2d.mul(normalIn, -left).add(pos));
            Vec2f pRightB = new Vec2f(Vec2d.mul(normalOut, right).add(pos));
            Vec2f pLeftB  = new Vec2f(Vec2d.mul(normalOut, -left).add(pos));
            Vec2f pCenter = new Vec2f(pos);

            int iRightA = builder.addVertex(new Vertex(pRightA, street.layer, l1));
            int iLeftA  = builder.addVertex(new Vertex(pLeftA, street.layer, l2));
            int iRightB = builder.addVertex(new Vertex(pRightB, street.layer, l1));
            int iLeftB  = builder.addVertex(new Vertex(pLeftB, street.layer, l2));

            // connector in
            builder.addIndex(iRightA);
            builder.addIndex(iLeftA);
            builder.restart();

            // bevel cap
            if (curve > 0) {
                builder.addIndex(builder.addVertex(new Vertex(pLeftA,  street.layer, l3)));
                builder.addIndex(builder.addVertex(new Vertex(pLeftB,  street.layer, l3)));
                builder.addIndex(builder.addVertex(new Vertex(pCenter, street.layer, l0)));
            } else {
                builder.addIndex(builder.addVertex(new Vertex(pRightB, street.layer, l3)));
                builder.addIndex(builder.addVertex(new Vertex(pRightA, street.layer, l3)));
                builder.addIndex(builder.addVertex(new Vertex(pCenter, street.layer, l0)));
            }
            builder.restart();

            // connector out
            builder.addIndex(iRightB);
            builder.addIndex(iLeftB);
        }
    }

    private void genRoundJoin(BucketBuilder builder, Common common, Vec2d pos, Vec3d dirIn, Vec3d dirOut, StreetProps street) {
        double left = common.outline + common.lanewidth * street.lanesLeft;
        double right = common.outline + common.lanewidth * street.lanesRight;
        double ext = (left + right) / 2.0;

        double curve = Math.signum(dirIn.xy().cross(dirOut.xy()));

        double inner;
        double outer;

        if (curve > 0) {
            inner = right;
            outer = -left;
        } else {
            inner = -left;
            outer = right;
        }

        Vec2d normalIn  = new Vec2d(-dirIn.y, dirIn.x);
        Vec2d normalOut = new Vec2d(-dirOut.y, dirOut.x);
        Vec2d normalMed = Vec2d.add(normalIn, normalOut).normalize();

        double cos = normalIn.dot(normalMed);
        double sin = normalIn.cross(normalMed);
        double innerext = inner / cos;
        boolean intersects = cos != 0.0 && Double.isFinite(innerext)
                && (innerext * innerext) <= (dirIn.z * dirIn.z + inner * inner)
                && (innerext * innerext) <= (dirOut.z * dirOut.z + inner * inner);

        boolean miter = 1 < 2 * cos;

        Vec3f l0 = new Vec3f(0.0f,         0.0f, (float) ext);
        Vec3f l1 = new Vec3f(0.0f, (float)  ext, (float) ext);
        Vec3f l2 = new Vec3f(0.0f, (float) -ext, (float) ext);

        if (intersects) {
            Vec2d pInner = Vec2d.mul(normalMed, innerext).add(pos);
            Vec2d pOuterA = Vec2d.mul(normalIn, outer).add(pos);
            Vec2d pOuterB = Vec2d.mul(normalOut, outer).add(pos);
            Vec2f pCenter = new Vec2f(pos);

            if (curve > 0) {
                int iInner = builder.addVertex(new Vertex(new Vec2f(pInner), street.layer, l1));
                int iOuterA = builder.addVertex(new Vertex(new Vec2f(pOuterA), street.layer, l2));
                int iOuterB = builder.addVertex(new Vertex(new Vec2f(pOuterB), street.layer, l2));
                int iCenter = builder.addVertex(new Vertex(new Vec2f(pCenter), street.layer, l0));

                builder.addIndex(iInner);       // connector in
                builder.addIndex(iOuterA);
                builder.restart();
                builder.addIndex(iOuterA);      // filler
                builder.addIndex(iCenter);
                builder.addIndex(iInner);
                builder.addIndex(iOuterB);
                builder.restart();
                if (miter) {                    // miter cage for rounded corner
                    Vec2f pOuter = new Vec2f(Vec2d.mul(normalMed, -innerext).add(pos));
                    Vec3f lx = new Vec3f((float) (sin * -innerext), (float) (-ext * curve), (float) ext);
                    int iOuter = builder.addVertex(new Vertex(pOuter, street.layer, lx));

                    builder.addIndex(iOuterA);
                    builder.addIndex(iOuter);
                    builder.addIndex(iCenter);
                    builder.addIndex(iOuterB);
                } else {                        // bevel cage for rounded corner
                    Vec3f lOuter = new Vec3f((float) (1.5 * ext * cos), (float) (-1.5 * ext * sin), (float) ext);
                    Vec3f lOuterX = new Vec3f((float) (ext), (float) (-ext * curve), (float) ext);

                    Vec2f pOuter  = new Vec2f(Vec2d.mul(normalMed, -ext * 1.5 * curve).add(pos));
                    Vec2f pOuterAC = new Vec2f(Vec2d.mul(normalIn, outer).add(dirIn.xy().mul(ext)).add(pos));
                    Vec2f pOuterBC = new Vec2f(Vec2d.mul(normalOut, outer).sub(dirOut.xy().mul(ext)).add(pos));

                    int iOuter = builder.addVertex(new Vertex(pOuter,  street.layer, lOuter));
                    int iOuterAC = builder.addVertex(new Vertex(pOuterAC, street.layer, lOuterX));
                    int iOuterBC = builder.addVertex(new Vertex(pOuterBC, street.layer, lOuterX));

                    builder.addIndex(iOuterA);
                    builder.addIndex(iOuterAC);
                    builder.addIndex(iCenter);
                    builder.addIndex(iOuter);
                    builder.restart();
                    builder.addIndex(iOuter);
                    builder.addIndex(iOuterBC);
                    builder.addIndex(iCenter);
                    builder.addIndex(iOuterB);
                }
                builder.restart();
                builder.addIndex(iInner);       // connector out
                builder.addIndex(iOuterB);
            } else {
                int iInner = builder.addVertex(new Vertex(new Vec2f(pInner), street.layer, l2));
                int iOuterA = builder.addVertex(new Vertex(new Vec2f(pOuterA), street.layer, l1));
                int iOuterB = builder.addVertex(new Vertex(new Vec2f(pOuterB), street.layer, l1));
                int iCenter = builder.addVertex(new Vertex(new Vec2f(pCenter), street.layer, l0));

                builder.addIndex(iOuterA);      // connector in
                builder.addIndex(iInner);
                builder.restart();
                builder.addIndex(iOuterA);      // filler
                builder.addIndex(iInner);
                builder.addIndex(iCenter);
                builder.addIndex(iOuterB);
                builder.restart();
                if (miter) {                    // miter cage for rounded corner
                    Vec2f pOuter = new Vec2f(Vec2d.mul(normalMed, -innerext).add(pos));
                    Vec3f lx = new Vec3f((float) (sin * -innerext), (float) (-ext * curve), (float) ext);
                    int iOuter = builder.addVertex(new Vertex(pOuter, street.layer, lx));

                    builder.addIndex(iOuterA);
                    builder.addIndex(iCenter);
                    builder.addIndex(iOuter);
                    builder.addIndex(iOuterB);
                } else {                        // bevel cage for rounded corner
                    Vec3f lOuter = new Vec3f((float) (1.5 * ext * cos), (float) (-1.5 * ext * sin), (float) ext);
                    Vec3f lOuterX = new Vec3f((float) (ext), (float) (-ext * curve), (float) ext);

                    Vec2f pOuter  = new Vec2f(Vec2d.mul(normalMed, -ext * 1.5 * curve).add(pos));
                    Vec2f pOuterAC = new Vec2f(Vec2d.mul(normalIn, outer).add(dirIn.xy().mul(ext)).add(pos));
                    Vec2f pOuterBC = new Vec2f(Vec2d.mul(normalOut, outer).sub(dirOut.xy().mul(ext)).add(pos));

                    int iOuter = builder.addVertex(new Vertex(pOuter,  street.layer, lOuter));
                    int iOuterAC = builder.addVertex(new Vertex(pOuterAC, street.layer, lOuterX));
                    int iOuterBC = builder.addVertex(new Vertex(pOuterBC, street.layer, lOuterX));

                    builder.addIndex(iOuterA);
                    builder.addIndex(iCenter);
                    builder.addIndex(iOuterAC);
                    builder.addIndex(iOuter);
                    builder.restart();
                    builder.addIndex(iOuter);
                    builder.addIndex(iCenter);
                    builder.addIndex(iOuterBC);
                    builder.addIndex(iOuterB);
                }
                builder.restart();
                builder.addIndex(iOuterB);      // connector out
                builder.addIndex(iInner);
            }

        } else {
            Vec2f pRightA = new Vec2f(Vec2d.mul(normalIn, right).add(pos));
            Vec2f pLeftA  = new Vec2f(Vec2d.mul(normalIn, -left).add(pos));
            Vec2f pRightB = new Vec2f(Vec2d.mul(normalOut, right).add(pos));
            Vec2f pLeftB  = new Vec2f(Vec2d.mul(normalOut, -left).add(pos));
            Vec2f pCenter = new Vec2f(pos);

            int iRightA = builder.addVertex(new Vertex(pRightA, street.layer, l1));
            int iLeftA  = builder.addVertex(new Vertex(pLeftA, street.layer, l2));
            int iRightB = builder.addVertex(new Vertex(pRightB, street.layer, l1));
            int iLeftB  = builder.addVertex(new Vertex(pLeftB, street.layer, l2));
            int iCenter = builder.addVertex(new Vertex(pCenter, street.layer, l0));

            // connector in
            builder.addIndex(iRightA);
            builder.addIndex(iLeftA);
            builder.restart();

            if (miter) {    // miter cage for rounded corner
                Vec2f pOuter = new Vec2f(Vec2d.mul(normalMed, -innerext).add(pos));
                Vec3f lx = new Vec3f((float) (sin * -innerext), (float) (-ext * curve), (float) ext);
                int iOuter = builder.addVertex(new Vertex(pOuter, street.layer, lx));

                if (curve > 0) {
                    builder.addIndex(iLeftA);
                    builder.addIndex(iOuter);
                    builder.addIndex(iCenter);
                    builder.addIndex(iLeftB);
                } else {
                    builder.addIndex(iRightA);
                    builder.addIndex(iCenter);
                    builder.addIndex(iOuter);
                    builder.addIndex(iRightB);
                }

            } else {        // bevel cage for rounded corner
                Vec3f lOuter = new Vec3f((float) (1.5 * ext * cos), (float) (-1.5 * ext * sin), (float) ext);
                Vec3f lOuterX = new Vec3f((float) (ext), (float) (-ext * curve), (float) ext);

                Vec2f pOuter  = new Vec2f(Vec2d.mul(normalMed, -ext * 1.5 * curve).add(pos));
                Vec2f pOuterA = new Vec2f(Vec2d.mul(normalIn, outer).add(dirIn.xy().mul(ext)).add(pos));
                Vec2f pOuterB = new Vec2f(Vec2d.mul(normalOut, outer).sub(dirOut.xy().mul(ext)).add(pos));

                int iOuter = builder.addVertex(new Vertex(pOuter,  street.layer, lOuter));
                int iOuterA = builder.addVertex(new Vertex(pOuterA, street.layer, lOuterX));
                int iOuterB = builder.addVertex(new Vertex(pOuterB, street.layer, lOuterX));

                if (curve > 0) {
                    builder.addIndex(iLeftA);
                    builder.addIndex(iOuterA);
                    builder.addIndex(iCenter);
                    builder.addIndex(iOuter);
                    builder.restart();
                    builder.addIndex(iOuter);
                    builder.addIndex(iOuterB);
                    builder.addIndex(iCenter);
                    builder.addIndex(iLeftB);
                } else {
                    builder.addIndex(iRightA);
                    builder.addIndex(iCenter);
                    builder.addIndex(iOuterA);
                    builder.addIndex(iOuter);
                    builder.restart();
                    builder.addIndex(iOuter);
                    builder.addIndex(iCenter);
                    builder.addIndex(iOuterB);
                    builder.addIndex(iRightB);
                }
            }

            builder.restart();

            // connector out
            builder.addIndex(iRightB);
            builder.addIndex(iLeftB);
        }
    }


    private static Vec3d dirvec(Vec2d posA, Vec2d posB) {
        Vec2d dir = Vec2d.sub(posB, posA);
        double len = dir.len();
        return new Vec3d(dir.normalize(), len);
    }

    private static Common getCommonProps(Style style, boolean drivingOnTheRight) {
        return new Common(
                style.getProperty("lanewidth", 0.001f),
                style.getProperty("outline", 0.0001f),
                CapType.ROUND,                              // TODO
                JoinType.ROUND,                             // TODO
                0.5f,                                       // TODO
                drivingOnTheRight
        );
    }


    private enum CapType { BUTT, SQUARE, ROUND }
    private enum JoinType { BUTT, MITER, BEVEL, ROUND }


    /**
     * Vertex described by coordinate and z-layer.
     */
    private static class Vertex {
        public final Vec2f pos;
        public final float layer;
        public final Vec3f line;

        /**
         * Constructs a new {@code Vertex} with the given coordinate and layer.
         *
         * @param coordinate the coordinate of this vertex.
         * @param layer      the z-layer of this vertex.
         */
        Vertex(Vec2f coordinate, float layer, Vec3f line) {
            this.pos = coordinate;
            this.layer = layer;
            this.line = line;
        }

        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof Vertex)) return false;

            Vertex other = (Vertex) obj;

            return this.pos.equals(other.pos)
                    && this.layer == other.layer
                    && this.line.equals(other.line);
        }

        @Override
        public int hashCode() {
            return new FNVHashBuilder()
                    .add(pos)
                    .add(layer)
                    .add(line)
                    .getHash();
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

    private static class BucketBuilder {
        private VertexSet<Vertex> vertices;
        private ArrayList<Integer> indices;
        private int restart;

        public BucketBuilder(VertexSet<Vertex> vertices, ArrayList<Integer> indices, int restart) {
            this.vertices = vertices;
            this.indices = indices;
            this.restart = restart;
        }

        public int addVertex(Vertex v) {
            return vertices.add(v);
        }

        public void addIndex(int i) {
            indices.add(i);
        }

        public void restart() {
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

        /**
         * Project the given {@code Coordinates} using this projection.
         *
         * @param coords the coordinate to project.
         * @return the projected coordinates as vectors.
         */
        private Vec2d[] project(Coordinate[] coords) {
            Vec2d[] projected = new Vec2d[coords.length];

            for (int i = 0; i < coords.length; i++) {
                Vec2d p = projection.project(coords[i]);
                p.x = ((p.x - from.xmin) / (from.xmax - from.xmin)) * (to.xmax - to.xmin) + to.xmin;
                p.y = ((p.y - from.ymin) / (from.ymax - from.ymin)) * (to.ymax - to.ymin) + to.ymin;
                projected[i] = p;
            }

            return projected;
        }
    }

    private static class Common {
        float lanewidth;
        float outline;
        CapType cap;
        JoinType join;
        float miterAngleLimit;          // as cos of the median direction vector
        boolean drivingOnTheRight;

        Common(float lanewidth, float outline, CapType cap, JoinType join, float miterAngleLimit, boolean drivingOnTheRight) {
            this.lanewidth = lanewidth;
            this.outline = outline;
            this.cap = cap;
            this.join = join;
            this.miterAngleLimit = miterAngleLimit;
            this.drivingOnTheRight = drivingOnTheRight;
        }
    }

    private static class StreetProps {
        float layer;
        double lanesLeft;
        double lanesRight;

        StreetProps(Street street, boolean drivingOnTheRight) {
            this.layer = (float) street.layer;

            double fwd = street.numLanesFwd;
            double bwd = street.numLanesBwd;

            if (fwd != 0.0 && bwd == 0.0) {
                bwd = fwd / 2.0;
                fwd = fwd / 2.0;
            } else if (fwd == 0.0 && bwd != 0.0) {
                fwd = bwd / 2.0;
                bwd = bwd / 2.0;
            }

            if (drivingOnTheRight) {
                this.lanesLeft  = fwd;
                this.lanesRight = bwd;
            } else {
                this.lanesLeft  = bwd;
                this.lanesRight = fwd;
            }
        }
    }
}
