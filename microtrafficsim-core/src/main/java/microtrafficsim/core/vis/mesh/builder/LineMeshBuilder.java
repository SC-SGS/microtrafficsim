package microtrafficsim.core.vis.mesh.builder;

import microtrafficsim.math.Vec2d;
import microtrafficsim.math.Vec3d;
import microtrafficsim.utils.hashing.FNVHashBuilder;


// TODO: simplify cpu code by rendering caps/joins with shader
// (see "Shader-Based Antialiased, Dashed, Stroked Polylines")


public class LineMeshBuilder {
    public enum CapType { BUTT, SQUARE, ROUND }
    public enum JoinType { BUTT, MITER, BEVEL, ROUND }

    public static class Style {
        public CapType  cap;
        public JoinType join;
        public double   linewidth;
        public double   offset;
        public double   miterAngleLimit;

        public Style(CapType cap, JoinType join, double linewidth, double offset, double miterAngleLimit) {
            this.cap = cap;
            this.join = join;
            this.linewidth = linewidth;
            this.offset = offset;
            this.miterAngleLimit = miterAngleLimit;
        }

        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof  Style)) return false;

            Style other = (Style) obj;

            return this.cap == other.cap
                    && this.join == other.join
                    && this.linewidth == other.linewidth
                    && this.offset == other.offset
                    && this.miterAngleLimit == other.miterAngleLimit;
        }

        @Override
        public int hashCode() {
            return new FNVHashBuilder()
                    .add(cap)
                    .add(join)
                    .add(linewidth)
                    .add(offset)
                    .add(miterAngleLimit)
                    .getHash();
        }
    }

    public static class Vertex {
        public final Vec3d position;
        public final Vec3d segment;

        public Vertex(Vec3d position, Vec3d segment) {
            this.position = position;
            this.segment  = segment;
        }

        @Override
        public int hashCode() {
            return new FNVHashBuilder()
                    .add(position)
                    .add(segment)
                    .getHash();
        }

        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof Vertex))
                return false;

            Vertex other = (Vertex) obj;

            return this.position.equals(other.position)
                    && this.segment.equals(other.segment);
        }
    }

    public interface VertexEmitter {
        int add(Vertex vertex);
        void emit(int id);
        void next();
    }


    private VertexEmitter emitter;

    public LineMeshBuilder(VertexEmitter emitter) {
        this.emitter = emitter;
    }


    public void setEmitter(VertexEmitter emitter) {
        this.emitter = emitter;
    }

    public VertexEmitter getEmitter() {
        return emitter;
    }


    public void add(Vec3d[] line, Style style) {
        add(line, style, null, null);
    }

    public void add(Vec3d[] line, Style style, Vec3d dirAdjBegin, Vec3d dirAdjEnd) {
        Vec3d thisPos = line[0];
        Vec3d nextPos = line[1];

        Vec3d thisDir = dirvec2d(thisPos, nextPos);

        if (dirAdjBegin == null) {
            emitCapBegin(thisPos, thisDir, style);
        } else {
            emitHalfJoinBegin(thisPos, dirAdjBegin, thisDir, style);
        }

        for (int i = 2; i < line.length; i++) {
            thisPos = nextPos;
            nextPos = line[i];

            Vec3d lastDir = thisDir;
            thisDir = dirvec2d(thisPos, nextPos);

            emitJoin(thisPos, lastDir, thisDir, style);
        }

        if (dirAdjEnd == null) {
            emitCapEnd(nextPos, thisDir, style);
        } else {
            emitHalfJoinEnd(nextPos, thisDir, dirAdjEnd, style);
        }

        emitter.next();
    }


    private void emitCapBegin(Vec3d pos, Vec3d dir, Style style) {
        switch (style.cap) {
            case ROUND:
                emitRoundCapBegin(pos, dir, style);
                break;

            case SQUARE:
                emitSquareCapBegin(pos, dir, style);
                break;

            default:
            case BUTT:
                emitButtCap(pos, dir, style);
                break;
        }
    }

    private void emitCapEnd(Vec3d pos, Vec3d dir, Style style) {
        switch (style.cap) {
            case ROUND:
                emitRoundCapEnd(pos, dir, style);
                break;

            case SQUARE:
                emitSquareCapEnd(pos, dir, style);
                break;

            default:
            case BUTT:
                emitButtCap(pos, dir, style);
                break;
        }
    }

    private void emitJoin(Vec3d pos, Vec3d dirIn, Vec3d dirOut, Style style) {
        if (Math.abs(dirIn.xy().cross(dirOut.xy())) < 0.001) {    // handle aligned lines
            if (dirIn.dot(dirOut) < 0) {                          // if lines go in the opposite direction, generate caps
                emitCapEnd(pos, dirIn, style);
                emitter.next();
                emitCapBegin(pos, dirOut, style);
            }

            return;
        }

        switch (style.join) {
            case ROUND:
                emitRoundJoin(pos, dirIn, dirOut, style);
                break;

            case BEVEL:
                emitBevelJoin(pos, dirIn, dirOut, style);
                break;

            case MITER:
                emitMiterJoin(pos, dirIn, dirOut, style);
                break;

            default:
            case BUTT:
                emitButtCapJoin(pos, dirIn, dirOut, style);
                break;
        }
    }

    private void emitHalfJoinBegin(Vec3d pos, Vec3d dirIn, Vec3d dirOut, Style style) {
        if (Math.abs(dirIn.xy().cross(dirOut.xy())) < 0.001) {    // handle aligned lines
            if (dirIn.dot(dirOut) < 0) {                          // if lines go in the opposite direction, generate cap
                emitCapBegin(pos, dirOut, style);
            } else {
                emitButtCap(pos, dirOut, style);
            }

            return;
        }

        switch (style.join) {
            case ROUND:
                emitRoundHalfJoinBegin(pos, dirIn, dirOut, style);
                break;

            case BEVEL:
                emitBevelHalfJoinBegin(pos, dirIn, dirOut, style);
                break;

            case MITER:
                emitMiterHalfJoinBegin(pos, dirIn, dirOut, style);
                break;

            default:
            case BUTT:
                emitButtCapJoin(pos, dirIn, dirOut, style);
                break;
        }
    }

    private void emitHalfJoinEnd(Vec3d pos, Vec3d dirIn, Vec3d dirOut, Style style) {
        if (Math.abs(dirIn.xy().cross(dirOut.xy())) < 0.001) {    // handle aligned lines
            if (dirIn.dot(dirOut) < 0) {                          // if lines go in the opposite direction, generate cap
                emitCapEnd(pos, dirIn, style);
            } else {
                emitButtCap(pos, dirIn, style);
            }

            return;
        }

        switch (style.join) {
            case ROUND:
                emitRoundHalfJoinEnd(pos, dirIn, dirOut, style);
                break;

            case BEVEL:
                emitBevelHalfJoinEnd(pos, dirIn, dirOut, style);
                break;

            case MITER:
                emitMiterHalfJoinEnd(pos, dirIn, dirOut, style);
                break;

            default:
            case BUTT:
                emitButtCapJoin(pos, dirIn, dirOut, style);
                break;
        }
    }


    private void emitButtCap(Vec3d pos, Vec3d dir, Style style) {
        final Vec2d normal = normal(dir);
        final double ext = style.linewidth / 2.0;

        final Vec3d p1 = new Vec3d(Vec2d.mul(normal, style.offset + ext).add(pos.xy()), pos.z);
        final Vec3d p2 = new Vec3d(Vec2d.mul(normal, style.offset - ext).add(pos.xy()), pos.z);

        final Vec3d l1 = new Vec3d(0.0,  ext, ext);
        final Vec3d l2 = new Vec3d(0.0, -ext, ext);

        emitter.emit(emitter.add(new Vertex(p1, l1)));
        emitter.emit(emitter.add(new Vertex(p2, l2)));
    }

    private void emitSquareCapBegin(Vec3d pos, Vec3d dir, Style style) {
        Vec2d normal = new Vec2d(-dir.y, dir.x);
        double ext = style.linewidth / 2.0;

        Vec2d p0 = Vec2d.mul(normal,       style.offset).add(pos.xy());
        Vec2d p1 = Vec2d.mul(normal, style.offset + ext).add(pos.xy());
        Vec2d p2 = Vec2d.mul(normal, style.offset - ext).add(pos.xy());
        Vec2d p3 = dir.xy().mul(-ext).add(p1);
        Vec2d p4 = dir.xy().mul(-ext).add(p2);

        Vec3d l0 = new Vec3d( 0.0,  0.0, ext);
        Vec3d l1 = new Vec3d( 0.0,  ext, ext);
        Vec3d l2 = new Vec3d( 0.0, -ext, ext);
        Vec3d l3 = new Vec3d(-ext,  0.0, ext);
        Vec3d l4 = new Vec3d(-ext,  0.0, ext);

        Vertex v0 = new Vertex(new Vec3d(p0, pos.z),       l0);
        Vertex v1 = new Vertex(new Vec3d(p1, pos.z), l1);
        Vertex v2 = new Vertex(new Vec3d(p2, pos.z), l2);
        Vertex v3 = new Vertex(new Vec3d(p3, pos.z), l1);
        Vertex v4 = new Vertex(new Vec3d(p4, pos.z), l2);
        Vertex v5 = new Vertex(new Vec3d(p3, pos.z), l3);
        Vertex v6 = new Vertex(new Vec3d(p4, pos.z), l4);

        int i0 = emitter.add(v0);
        int i1 = emitter.add(v1);
        int i3 = emitter.add(v3);
        int i4 = emitter.add(v4);
        int i2 = emitter.add(v2);
        int i5 = emitter.add(v5);
        int i6 = emitter.add(v6);

        emitter.emit(i0);
        emitter.emit(i1);
        emitter.emit(i3);
        emitter.next();
        emitter.emit(i0);
        emitter.emit(i4);
        emitter.emit(i2);
        emitter.next();
        emitter.emit(i0);
        emitter.emit(i5);
        emitter.emit(i6);
        emitter.next();
        emitter.emit(i1);
        emitter.emit(i2);
    }

    private void emitSquareCapEnd(Vec3d pos, Vec3d dir, Style style) {
        Vec2d normal = new Vec2d(-dir.y, dir.x);
        double ext = style.linewidth / 2.0;

        Vec2d p0 = Vec2d.mul(normal,       style.offset).add(pos.xy());
        Vec2d p1 = Vec2d.mul(normal, style.offset + ext).add(pos.xy());
        Vec2d p2 = Vec2d.mul(normal, style.offset - ext).add(pos.xy());
        Vec2d p3 = dir.xy().mul(ext).add(p1);
        Vec2d p4 = dir.xy().mul(ext).add(p2);

        Vec3d l0 = new Vec3d(0.0,  0.0, ext);
        Vec3d l1 = new Vec3d(0.0,  ext, ext);
        Vec3d l2 = new Vec3d(0.0, -ext, ext);
        Vec3d l3 = new Vec3d(ext,  0.0, ext);
        Vec3d l4 = new Vec3d(ext,  0.0, ext);

        Vertex v0 = new Vertex(new Vec3d(p0, pos.z), l0);
        Vertex v1 = new Vertex(new Vec3d(p1, pos.z), l1);
        Vertex v2 = new Vertex(new Vec3d(p2, pos.z), l2);
        Vertex v3 = new Vertex(new Vec3d(p3, pos.z), l1);
        Vertex v4 = new Vertex(new Vec3d(p4, pos.z), l2);
        Vertex v5 = new Vertex(new Vec3d(p3, pos.z), l3);
        Vertex v6 = new Vertex(new Vec3d(p4, pos.z), l4);

        int i1 = emitter.add(v1);
        int i2 = emitter.add(v2);
        int i0 = emitter.add(v0);
        int i3 = emitter.add(v3);
        int i4 = emitter.add(v4);
        int i6 = emitter.add(v6);
        int i5 = emitter.add(v5);

        emitter.emit(i1);
        emitter.emit(i2);
        emitter.next();
        emitter.emit(i0);
        emitter.emit(i3);
        emitter.emit(i1);
        emitter.next();
        emitter.emit(i0);
        emitter.emit(i2);
        emitter.emit(i4);
        emitter.next();
        emitter.emit(i0);
        emitter.emit(i6);
        emitter.emit(i5);
    }

    private void emitRoundCapBegin(Vec3d pos, Vec3d dir, Style style) {
        Vec2d normal = new Vec2d(-dir.y, dir.x);
        double ext = style.linewidth / 2.0;

        Vec2d p1 = Vec2d.mul(normal, style.offset + ext).add(pos.xy());
        Vec2d p2 = Vec2d.mul(normal, style.offset - ext).add(pos.xy());
        Vec2d p3 = dir.xy().mul(-ext).add(p1);
        Vec2d p4 = dir.xy().mul(-ext).add(p2);

        Vec3d l1 = new Vec3d(0.0,  ext, ext);
        Vec3d l2 = new Vec3d(0.0, -ext, ext);
        Vec3d l3 = new Vec3d(ext,  ext, ext);
        Vec3d l4 = new Vec3d(ext, -ext, ext);

        emitter.emit(emitter.add(new Vertex(new Vec3d(p3, pos.z), l3)));
        emitter.emit(emitter.add(new Vertex(new Vec3d(p4, pos.z), l4)));
        emitter.emit(emitter.add(new Vertex(new Vec3d(p1, pos.z), l1)));
        emitter.emit(emitter.add(new Vertex(new Vec3d(p2, pos.z), l2)));
    }

    private void emitRoundCapEnd(Vec3d pos, Vec3d dir, Style style) {
        Vec2d normal = new Vec2d(-dir.y, dir.x);
        double ext = style.linewidth / 2.0;

        Vec2d p1 = Vec2d.mul(normal, style.offset + ext).add(pos.xy());
        Vec2d p2 = Vec2d.mul(normal, style.offset - ext).add(pos.xy());
        Vec2d p3 = dir.xy().mul(ext).add(p1);
        Vec2d p4 = dir.xy().mul(ext).add(p2);

        Vec3d l1 = new Vec3d(0.0,  ext, ext);
        Vec3d l2 = new Vec3d(0.0, -ext, ext);
        Vec3d l3 = new Vec3d(ext,  ext, ext);
        Vec3d l4 = new Vec3d(ext, -ext, ext);

        emitter.emit(emitter.add(new Vertex(new Vec3d(p1, pos.z), l1)));
        emitter.emit(emitter.add(new Vertex(new Vec3d(p2, pos.z), l2)));
        emitter.emit(emitter.add(new Vertex(new Vec3d(p3, pos.z), l3)));
        emitter.emit(emitter.add(new Vertex(new Vec3d(p4, pos.z), l4)));
    }


    private void emitButtCapJoin(Vec3d pos, Vec3d dirIn, Vec3d dirOut, Style style) {
        emitButtCap(pos, dirIn, style);
        emitter.next();
        emitButtCap(pos, dirOut, style);
    }

    private void emitMiterJoin(Vec3d pos, Vec3d dirIn, Vec3d dirOut, Style style) {
        double ext = style.linewidth / 2.0;
        double curve = Math.signum(dirIn.xy().cross(dirOut.xy()));

        Vec2d normalIn  = new Vec2d(-dirIn.y,  dirIn.x);
        Vec2d normalOut = new Vec2d(-dirOut.y, dirOut.x);
        Vec2d normalMed = Vec2d.add(normalIn, normalOut).normalize();

        double cos = normalIn.dot(normalMed);
        double innerext = curve * ext / cos;
        boolean intersects = cos != 0.0 && Double.isFinite(innerext)
                && (innerext * innerext) <= (dirIn.z  * dirIn.z  + ext * ext)
                && (innerext * innerext) <= (dirOut.z * dirOut.z + ext * ext);

        boolean bevel = cos < style.miterAngleLimit;

        Vec3d l0 = new Vec3d(0.0,          0.0, ext);
        Vec3d l1 = new Vec3d(0.0,          ext, ext);
        Vec3d l2 = new Vec3d(0.0,         -ext, ext);
        Vec3d li = new Vec3d(0.0,  ext * curve, ext);
        Vec3d lo = new Vec3d(0.0, -ext * curve, ext);

        Vec3d pCenter = new Vec3d(pos.xy().add(Vec2d.mul(normalMed, style.offset / Math.max(cos, 0.00001))), pos.z);
        Vec3d pInner = new Vec3d(pCenter.xy().add(Vec2d.mul(normalMed, innerext)), pos.z);

        Vec3d pRightA = new Vec3d(Vec2d.mul(normalIn,   ext).add(pCenter.xy()), pos.z);
        Vec3d pLeftA  = new Vec3d(Vec2d.mul(normalIn,  -ext).add(pCenter.xy()), pos.z);
        Vec3d pRightB = new Vec3d(Vec2d.mul(normalOut,  ext).add(pCenter.xy()), pos.z);
        Vec3d pLeftB  = new Vec3d(Vec2d.mul(normalOut, -ext).add(pCenter.xy()), pos.z);

        int iRightA = emitter.add(new Vertex(pRightA, l1));
        int iLeftA  = emitter.add(new Vertex(pLeftA,  l2));
        int iRightB = emitter.add(new Vertex(pRightB, l1));
        int iLeftB  = emitter.add(new Vertex(pLeftB,  l2));
        int iInner  = emitter.add(new Vertex(pInner,  li));
        int iCenter = emitter.add(new Vertex(pCenter, l0));

        if (intersects) {
            if (curve > 0) {
                // connector in
                emitter.emit(iInner);
                emitter.emit(iLeftA);
                emitter.next();

                // filler
                emitter.emit(iLeftA);
                emitter.emit(iCenter);
                emitter.emit(iInner);
                emitter.emit(iLeftB);
            } else {
                // connector in
                emitter.emit(iRightA);
                emitter.emit(iInner);
                emitter.next();

                // filler
                emitter.emit(iRightA);
                emitter.emit(iInner);
                emitter.emit(iCenter);
                emitter.emit(iRightB);
            }
            emitter.next();
        } else {
            // connector in
            emitter.emit(iRightA);
            emitter.emit(iLeftA);
            emitter.next();
        }

        if (!bevel) {                           // miter cap
            Vec3d pOuter = new Vec3d(Vec2d.mul(normalMed, -innerext).add(pCenter.xy()), pos.z);
            int iOuter = emitter.add(new Vertex(pOuter, lo));

            if (curve > 0) {
                int iLeftAC = emitter.add(new Vertex(pLeftA, lo));
                int iLeftBC = emitter.add(new Vertex(pLeftB, lo));

                emitter.emit(iLeftAC);
                emitter.emit(iOuter);
                emitter.emit(iCenter);
                emitter.emit(iLeftBC);
            } else {
                int iRightAC = emitter.add(new Vertex(pRightA, lo));
                int iRightBC = emitter.add(new Vertex(pRightB, lo));

                emitter.emit(iRightAC);
                emitter.emit(iCenter);
                emitter.emit(iOuter);
                emitter.emit(iRightBC);
            }
        } else {                                // bevel cap
            if (curve > 0) {
                int iLeftAC = emitter.add(new Vertex(pLeftA, lo));
                int iLeftBC = emitter.add(new Vertex(pLeftB, lo));

                emitter.emit(iLeftAC);
                emitter.emit(iLeftBC);
                emitter.emit(iCenter);
            } else {
                int iRightAC = emitter.add(new Vertex(pRightA, lo));
                int iRightBC = emitter.add(new Vertex(pRightB, lo));

                emitter.emit(iRightBC);
                emitter.emit(iRightAC);
                emitter.emit(iCenter);
            }
        }
        emitter.next();

        // connector out
        if (intersects) {
            if (curve > 0) {
                emitter.emit(iInner);
                emitter.emit(iLeftB);
            } else {
                emitter.emit(iRightB);
                emitter.emit(iInner);
            }
        } else {
            emitter.emit(iRightB);
            emitter.emit(iLeftB);
        }
    }

    private void emitBevelJoin(Vec3d pos, Vec3d dirIn, Vec3d dirOut, Style style) {
        double ext = style.linewidth / 2.0;
        double curve = Math.signum(dirIn.xy().cross(dirOut.xy()));

        Vec2d normalIn  = new Vec2d(-dirIn.y,  dirIn.x);
        Vec2d normalOut = new Vec2d(-dirOut.y, dirOut.x);
        Vec2d normalMed = Vec2d.add(normalIn, normalOut).normalize();

        double cos = normalIn.dot(normalMed);
        double innerext = curve * ext / cos;
        boolean intersects = cos != 0.0 && Double.isFinite(innerext)
                && (innerext * innerext) <= (dirIn.z  * dirIn.z  + ext * ext)
                && (innerext * innerext) <= (dirOut.z * dirOut.z + ext * ext);

        boolean bevel = cos < style.miterAngleLimit;

        Vec3d l0 = new Vec3d(0.0,          0.0, ext);
        Vec3d l1 = new Vec3d(0.0,          ext, ext);
        Vec3d l2 = new Vec3d(0.0,         -ext, ext);
        Vec3d li = new Vec3d(0.0,  ext * curve, ext);
        Vec3d lo = new Vec3d(0.0, -ext * curve, ext);

        Vec3d pCenter = new Vec3d(pos.xy().add(Vec2d.mul(normalMed, style.offset / Math.max(cos, 0.00001))), pos.z);
        Vec3d pInner = new Vec3d(pCenter.xy().add(Vec2d.mul(normalMed, innerext)), pos.z);

        Vec3d pRightA = new Vec3d(Vec2d.mul(normalIn,   ext).add(pCenter.xy()), pos.z);
        Vec3d pLeftA  = new Vec3d(Vec2d.mul(normalIn,  -ext).add(pCenter.xy()), pos.z);
        Vec3d pRightB = new Vec3d(Vec2d.mul(normalOut,  ext).add(pCenter.xy()), pos.z);
        Vec3d pLeftB  = new Vec3d(Vec2d.mul(normalOut, -ext).add(pCenter.xy()), pos.z);

        int iRightA = emitter.add(new Vertex(pRightA, l1));
        int iLeftA  = emitter.add(new Vertex(pLeftA,  l2));
        int iRightB = emitter.add(new Vertex(pRightB, l1));
        int iLeftB  = emitter.add(new Vertex(pLeftB,  l2));
        int iInner  = emitter.add(new Vertex(pInner,  li));
        int iCenter = emitter.add(new Vertex(pCenter, l0));

        if (intersects) {
            if (curve > 0) {
                // connector in
                emitter.emit(iInner);
                emitter.emit(iLeftA);
                emitter.next();

                // filler
                emitter.emit(iLeftA);
                emitter.emit(iCenter);
                emitter.emit(iInner);
                emitter.emit(iLeftB);
            } else {
                // connector in
                emitter.emit(iRightA);
                emitter.emit(iInner);
                emitter.next();

                // filler
                emitter.emit(iRightA);
                emitter.emit(iInner);
                emitter.emit(iCenter);
                emitter.emit(iRightB);
            }
            emitter.next();
        } else {
            // connector in
            emitter.emit(iRightA);
            emitter.emit(iLeftA);
            emitter.next();
        }

        // bevel cap
        if (curve > 0) {
            int iLeftAC = emitter.add(new Vertex(pLeftA, lo));
            int iLeftBC = emitter.add(new Vertex(pLeftB, lo));

            emitter.emit(iLeftAC);
            emitter.emit(iLeftBC);
            emitter.emit(iCenter);
        } else {
            int iRightAC = emitter.add(new Vertex(pRightA, lo));
            int iRightBC = emitter.add(new Vertex(pRightB, lo));

            emitter.emit(iRightBC);
            emitter.emit(iRightAC);
            emitter.emit(iCenter);
        }
        emitter.next();

        // connector out
        if (intersects) {
            if (curve > 0) {
                emitter.emit(iInner);
                emitter.emit(iLeftB);
            } else {
                emitter.emit(iRightB);
                emitter.emit(iInner);
            }
        } else {
            emitter.emit(iRightB);
            emitter.emit(iLeftB);
        }
    }

    private void emitRoundJoin(Vec3d pos, Vec3d dirIn, Vec3d dirOut, Style style) {
        double ext = style.linewidth / 2.0;
        double curve = Math.signum(dirIn.xy().cross(dirOut.xy()));

        Vec2d normalIn  = new Vec2d(-dirIn.y,  dirIn.x);
        Vec2d normalOut = new Vec2d(-dirOut.y, dirOut.x);
        Vec2d normalMed = Vec2d.add(normalIn, normalOut).normalize();

        double cos = normalIn.dot(normalMed);
        double sin = normalIn.cross(normalMed);
        double innerext = curve * ext / cos;
        boolean intersects = cos != 0.0 && Double.isFinite(innerext)
                && (innerext * innerext) <= (dirIn.z  * dirIn.z  + ext * ext)
                && (innerext * innerext) <= (dirOut.z * dirOut.z + ext * ext);

        boolean miter = 1 < 2 * cos;

        Vec3d l0 = new Vec3d(0.0,          0.0, ext);
        Vec3d l1 = new Vec3d(0.0,          ext, ext);
        Vec3d l2 = new Vec3d(0.0,         -ext, ext);
        Vec3d li = new Vec3d(0.0,  ext * curve, ext);

        Vec3d pCenter = new Vec3d(pos.xy().add(Vec2d.mul(normalMed, style.offset / Math.max(cos, 0.00001))), pos.z);
        Vec3d pInner = new Vec3d(pCenter.xy().add(Vec2d.mul(normalMed, innerext)), pos.z);

        Vec3d pRightA = new Vec3d(Vec2d.mul(normalIn,   ext).add(pCenter.xy()), pos.z);
        Vec3d pLeftA  = new Vec3d(Vec2d.mul(normalIn,  -ext).add(pCenter.xy()), pos.z);
        Vec3d pRightB = new Vec3d(Vec2d.mul(normalOut,  ext).add(pCenter.xy()), pos.z);
        Vec3d pLeftB  = new Vec3d(Vec2d.mul(normalOut, -ext).add(pCenter.xy()), pos.z);

        int iRightA = emitter.add(new Vertex(pRightA, l1));
        int iLeftA  = emitter.add(new Vertex(pLeftA,  l2));
        int iRightB = emitter.add(new Vertex(pRightB, l1));
        int iLeftB  = emitter.add(new Vertex(pLeftB,  l2));
        int iInner  = emitter.add(new Vertex(pInner,  li));
        int iCenter = emitter.add(new Vertex(pCenter, l0));

        if (intersects) {
            if (curve > 0) {
                // connector in
                emitter.emit(iInner);
                emitter.emit(iLeftA);
                emitter.next();

                // filler
                emitter.emit(iLeftA);
                emitter.emit(iCenter);
                emitter.emit(iInner);
                emitter.emit(iLeftB);
            } else {
                // connector in
                emitter.emit(iRightA);
                emitter.emit(iInner);
                emitter.next();

                // filler
                emitter.emit(iRightA);
                emitter.emit(iInner);
                emitter.emit(iCenter);
                emitter.emit(iRightB);
            }
            emitter.next();
        } else {
            // connector in
            emitter.emit(iRightA);
            emitter.emit(iLeftA);
            emitter.next();
        }

        if (miter) {                            // miter cage
            Vec3d pOuter = new Vec3d(Vec2d.mul(normalMed, -innerext).add(pCenter.xy()), pos.z);
            Vec3d lx = new Vec3d((float) (sin * -innerext), (float) (-ext * curve), (float) ext);
            int iOuter = emitter.add(new Vertex(pOuter, lx));

            if (curve > 0) {
                emitter.emit(iLeftA);
                emitter.emit(iOuter);
                emitter.emit(iCenter);
                emitter.emit(iLeftB);
            } else {
                emitter.emit(iRightA);
                emitter.emit(iCenter);
                emitter.emit(iOuter);
                emitter.emit(iRightB);
            }
        } else {                                // bevel cage
            Vec3d lo = new Vec3d(1.5 * cos * ext, -1.5 * sin * ext, ext);
            Vec3d lx = new Vec3d(            ext,     -curve * ext, ext);

            Vec3d pOuter   = new Vec3d(Vec2d.mul(normalMed, -ext * 1.5 * curve).add(pCenter.xy()), pos.z);
            Vec3d pOuterAC = new Vec3d(Vec2d.mul(normalIn,  -curve * ext).add( dirIn.xy().mul(ext)).add(pCenter.xy()), pos.z);
            Vec3d pOuterBC = new Vec3d(Vec2d.mul(normalOut, -curve * ext).sub(dirOut.xy().mul(ext)).add(pCenter.xy()), pos.z);

            int iOuter   = emitter.add(new Vertex(pOuter,   lo));
            int iOuterAC = emitter.add(new Vertex(pOuterAC, lx));
            int iOuterBC = emitter.add(new Vertex(pOuterBC, lx));

            if (curve > 0) {
                emitter.emit(iLeftA);
                emitter.emit(iOuterAC);
                emitter.emit(iCenter);
                emitter.emit(iOuter);
                emitter.next();
                emitter.emit(iOuter);
                emitter.emit(iOuterBC);
                emitter.emit(iCenter);
                emitter.emit(iLeftB);
            } else {
                emitter.emit(iRightA);
                emitter.emit(iCenter);
                emitter.emit(iOuterAC);
                emitter.emit(iOuter);
                emitter.next();
                emitter.emit(iOuter);
                emitter.emit(iCenter);
                emitter.emit(iOuterBC);
                emitter.emit(iRightB);
            }
        }
        emitter.next();

        // connector out
        if (intersects) {
            if (curve > 0) {
                emitter.emit(iInner);
                emitter.emit(iLeftB);
            } else {
                emitter.emit(iRightB);
                emitter.emit(iInner);
            }
        } else {
            emitter.emit(iRightB);
            emitter.emit(iLeftB);
        }
    }


    private void emitMiterHalfJoinBegin(Vec3d pos, Vec3d dirIn, Vec3d dirOut, Style style) {
        double ext = style.linewidth / 2.0;
        double curve = Math.signum(dirIn.xy().cross(dirOut.xy()));

        Vec2d normalIn  = new Vec2d(-dirIn.y,  dirIn.x);
        Vec2d normalOut = new Vec2d(-dirOut.y, dirOut.x);
        Vec2d normalMed = Vec2d.add(normalIn, normalOut).normalize();

        double cos = normalIn.dot(normalMed);
        double innerext = curve * ext / cos;
        boolean intersects = cos != 0.0 && Double.isFinite(innerext)
                && (innerext * innerext) <= (dirIn.z  * dirIn.z  + ext * ext)
                && (innerext * innerext) <= (dirOut.z * dirOut.z + ext * ext);

        boolean bevel = cos < style.miterAngleLimit;

        Vec3d l0 = new Vec3d(0.0,          0.0, ext);
        Vec3d l1 = new Vec3d(0.0,          ext, ext);
        Vec3d l2 = new Vec3d(0.0,         -ext, ext);
        Vec3d li = new Vec3d(0.0,  ext * curve, ext);
        Vec3d lo = new Vec3d(0.0, -ext * curve, ext);

        Vec3d pCenter = new Vec3d(pos.xy().add(Vec2d.mul(normalMed, style.offset / Math.max(cos, 0.00001))), pos.z);
        Vec3d pInner = new Vec3d(pCenter.xy().add(Vec2d.mul(normalMed, innerext)), pos.z);

        Vec3d pRightB = new Vec3d(Vec2d.mul(normalOut,  ext).add(pCenter.xy()), pos.z);
        Vec3d pLeftB  = new Vec3d(Vec2d.mul(normalOut, -ext).add(pCenter.xy()), pos.z);

        int iRightB = emitter.add(new Vertex(pRightB, l1));
        int iLeftB  = emitter.add(new Vertex(pLeftB,  l2));
        int iInner  = emitter.add(new Vertex(pInner,  li));
        int iCenter = emitter.add(new Vertex(pCenter, l0));

        if (intersects) {                       // filler
            if (curve > 0) {
                emitter.emit(iInner);
                emitter.emit(iCenter);
                emitter.emit(iLeftB);
            } else {
                emitter.emit(iCenter);
                emitter.emit(iInner);
                emitter.emit(iRightB);
            }
            emitter.next();
        }

        if (!bevel) {                           // miter cap
            Vec3d pOuter = new Vec3d(Vec2d.mul(normalMed, -innerext).add(pCenter.xy()), pos.z);
            int iOuter = emitter.add(new Vertex(pOuter, lo));

            if (curve > 0) {
                int iLeftBC = emitter.add(new Vertex(pLeftB, lo));

                emitter.emit(iCenter);
                emitter.emit(iOuter);
                emitter.emit(iLeftBC);
            } else {
                int iRightBC = emitter.add(new Vertex(pRightB, lo));

                emitter.emit(iOuter);
                emitter.emit(iCenter);
                emitter.emit(iRightBC);
            }
        } else {                                // bevel cap
            Vec3d pOuter = new Vec3d(Vec2d.mul(normalMed, -curve * ext * cos).add(pCenter.xy()), pos.z);
            int iOuter = emitter.add(new Vertex(pOuter, lo));

            if (curve > 0) {
                int iLeftBC = emitter.add(new Vertex(pLeftB, lo));

                emitter.emit(iOuter);
                emitter.emit(iLeftBC);
                emitter.emit(iCenter);
            } else {
                int iRightBC = emitter.add(new Vertex(pRightB, lo));

                emitter.emit(iRightBC);
                emitter.emit(iOuter);
                emitter.emit(iCenter);
            }
        }
        emitter.next();

        // connector out
        if (intersects) {
            if (curve > 0) {
                emitter.emit(iInner);
                emitter.emit(iLeftB);
            } else {
                emitter.emit(iRightB);
                emitter.emit(iInner);
            }
        } else {
            emitter.emit(iRightB);
            emitter.emit(iLeftB);
        }
    }

    private void emitMiterHalfJoinEnd(Vec3d pos, Vec3d dirIn, Vec3d dirOut, Style style) {
        double ext = style.linewidth / 2.0;
        double curve = Math.signum(dirIn.xy().cross(dirOut.xy()));

        Vec2d normalIn  = new Vec2d(-dirIn.y,  dirIn.x);
        Vec2d normalOut = new Vec2d(-dirOut.y, dirOut.x);
        Vec2d normalMed = Vec2d.add(normalIn, normalOut).normalize();

        double cos = normalIn.dot(normalMed);
        double innerext = curve * ext / cos;
        boolean intersects = cos != 0.0 && Double.isFinite(innerext)
                && (innerext * innerext) <= (dirIn.z  * dirIn.z  + ext * ext)
                && (innerext * innerext) <= (dirOut.z * dirOut.z + ext * ext);

        boolean bevel = cos < style.miterAngleLimit;

        Vec3d l0 = new Vec3d(0.0,          0.0, ext);
        Vec3d l1 = new Vec3d(0.0,          ext, ext);
        Vec3d l2 = new Vec3d(0.0,         -ext, ext);
        Vec3d li = new Vec3d(0.0,  ext * curve, ext);
        Vec3d lo = new Vec3d(0.0, -ext * curve, ext);

        Vec3d pCenter = new Vec3d(pos.xy().add(Vec2d.mul(normalMed, style.offset / Math.max(cos, 0.00001))), pos.z);
        Vec3d pInner = new Vec3d(pCenter.xy().add(Vec2d.mul(normalMed, innerext)), pos.z);

        Vec3d pRightA = new Vec3d(Vec2d.mul(normalIn,   ext).add(pCenter.xy()), pos.z);
        Vec3d pLeftA  = new Vec3d(Vec2d.mul(normalIn,  -ext).add(pCenter.xy()), pos.z);
        Vec3d pRightB = new Vec3d(Vec2d.mul(normalOut,  ext).add(pCenter.xy()), pos.z);
        Vec3d pLeftB  = new Vec3d(Vec2d.mul(normalOut, -ext).add(pCenter.xy()), pos.z);

        int iRightA = emitter.add(new Vertex(pRightA, l1));
        int iLeftA  = emitter.add(new Vertex(pLeftA,  l2));
        int iInner  = emitter.add(new Vertex(pInner,  li));
        int iCenter = emitter.add(new Vertex(pCenter, l0));

        if (intersects) {
            if (curve > 0) {
                // connector in
                emitter.emit(iInner);
                emitter.emit(iLeftA);
                emitter.next();

                // filler
                emitter.emit(iLeftA);
                emitter.emit(iCenter);
                emitter.emit(iInner);
            } else {
                // connector in
                emitter.emit(iRightA);
                emitter.emit(iInner);
                emitter.next();

                // filler
                emitter.emit(iRightA);
                emitter.emit(iInner);
                emitter.emit(iCenter);
            }
            emitter.next();
        } else {
            // connector in
            emitter.emit(iRightA);
            emitter.emit(iLeftA);
            emitter.next();
        }

        if (!bevel) {                           // miter cap
            Vec3d pOuter = new Vec3d(Vec2d.mul(normalMed, -innerext).add(pCenter.xy()), pos.z);
            int iOuter = emitter.add(new Vertex(pOuter, lo));

            if (curve > 0) {
                int iLeftAC = emitter.add(new Vertex(pLeftA, lo));

                emitter.emit(iLeftAC);
                emitter.emit(iOuter);
                emitter.emit(iCenter);
            } else {
                int iRightAC = emitter.add(new Vertex(pRightA, lo));

                emitter.emit(iRightAC);
                emitter.emit(iCenter);
                emitter.emit(iOuter);
            }
        } else {                                // bevel cap
            Vec3d pOuter = new Vec3d(Vec2d.mul(normalMed, -curve * ext * cos).add(pCenter.xy()), pos.z);
            int iOuter = emitter.add(new Vertex(pOuter, lo));

            if (curve > 0) {
                int iLeftAC = emitter.add(new Vertex(pLeftA, lo));

                emitter.emit(iLeftAC);
                emitter.emit(iOuter);
                emitter.emit(iCenter);
            } else {
                int iRightAC = emitter.add(new Vertex(pRightA, lo));
                int iRightBC = emitter.add(new Vertex(pRightB, lo));

                emitter.emit(iOuter);
                emitter.emit(iRightAC);
                emitter.emit(iCenter);
            }
        }
    }

    private void emitBevelHalfJoinBegin(Vec3d pos, Vec3d dirIn, Vec3d dirOut, Style style) {
        double ext = style.linewidth / 2.0;
        double curve = Math.signum(dirIn.xy().cross(dirOut.xy()));

        Vec2d normalIn  = new Vec2d(-dirIn.y,  dirIn.x);
        Vec2d normalOut = new Vec2d(-dirOut.y, dirOut.x);
        Vec2d normalMed = Vec2d.add(normalIn, normalOut).normalize();

        double cos = normalIn.dot(normalMed);
        double innerext = curve * ext / cos;
        boolean intersects = cos != 0.0 && Double.isFinite(innerext)
                && (innerext * innerext) <= (dirIn.z  * dirIn.z  + ext * ext)
                && (innerext * innerext) <= (dirOut.z * dirOut.z + ext * ext);

        boolean bevel = cos < style.miterAngleLimit;

        Vec3d l0 = new Vec3d(0.0,          0.0, ext);
        Vec3d l1 = new Vec3d(0.0,          ext, ext);
        Vec3d l2 = new Vec3d(0.0,         -ext, ext);
        Vec3d li = new Vec3d(0.0,  ext * curve, ext);
        Vec3d lo = new Vec3d(0.0, -ext * curve, ext);

        Vec3d pCenter = new Vec3d(pos.xy().add(Vec2d.mul(normalMed, style.offset / Math.max(cos, 0.00001))), pos.z);
        Vec3d pInner = new Vec3d(pCenter.xy().add(Vec2d.mul(normalMed, innerext)), pos.z);

        Vec3d pRightB = new Vec3d(Vec2d.mul(normalOut,  ext).add(pCenter.xy()), pos.z);
        Vec3d pLeftB  = new Vec3d(Vec2d.mul(normalOut, -ext).add(pCenter.xy()), pos.z);

        int iRightB = emitter.add(new Vertex(pRightB, l1));
        int iLeftB  = emitter.add(new Vertex(pLeftB,  l2));
        int iInner  = emitter.add(new Vertex(pInner,  li));
        int iCenter = emitter.add(new Vertex(pCenter, l0));

        if (intersects) {                       // filler
            if (curve > 0) {
                emitter.emit(iInner);
                emitter.emit(iCenter);
                emitter.emit(iLeftB);
            } else {
                emitter.emit(iCenter);
                emitter.emit(iInner);
                emitter.emit(iRightB);
            }
            emitter.next();
        }

        if (!bevel) {                           // miter cap
            Vec3d pOuter = new Vec3d(Vec2d.mul(normalMed, -innerext).add(pCenter.xy()), pos.z);
            int iOuter = emitter.add(new Vertex(pOuter, lo));

            if (curve > 0) {
                int iLeftBC = emitter.add(new Vertex(pLeftB, lo));

                emitter.emit(iCenter);
                emitter.emit(iOuter);
                emitter.emit(iLeftBC);
            } else {
                int iRightBC = emitter.add(new Vertex(pRightB, lo));

                emitter.emit(iOuter);
                emitter.emit(iCenter);
                emitter.emit(iRightBC);
            }
        } else {                                // bevel cap
            Vec3d pOuter = new Vec3d(Vec2d.mul(normalMed, -curve * ext * cos).add(pCenter.xy()), pos.z);
            int iOuter = emitter.add(new Vertex(pOuter, lo));

            if (curve > 0) {
                int iLeftBC = emitter.add(new Vertex(pLeftB, lo));

                emitter.emit(iOuter);
                emitter.emit(iLeftBC);
                emitter.emit(iCenter);
            } else {
                int iRightBC = emitter.add(new Vertex(pRightB, lo));

                emitter.emit(iRightBC);
                emitter.emit(iOuter);
                emitter.emit(iCenter);
            }
        }
        emitter.next();

        // connector out
        if (intersects) {
            if (curve > 0) {
                emitter.emit(iInner);
                emitter.emit(iLeftB);
            } else {
                emitter.emit(iRightB);
                emitter.emit(iInner);
            }
        } else {
            emitter.emit(iRightB);
            emitter.emit(iLeftB);
        }
    }

    private void emitBevelHalfJoinEnd(Vec3d pos, Vec3d dirIn, Vec3d dirOut, Style style) {
        double ext = style.linewidth / 2.0;
        double curve = Math.signum(dirIn.xy().cross(dirOut.xy()));

        Vec2d normalIn  = new Vec2d(-dirIn.y,  dirIn.x);
        Vec2d normalOut = new Vec2d(-dirOut.y, dirOut.x);
        Vec2d normalMed = Vec2d.add(normalIn, normalOut).normalize();

        double cos = normalIn.dot(normalMed);
        double innerext = curve * ext / cos;
        boolean intersects = cos != 0.0 && Double.isFinite(innerext)
                && (innerext * innerext) <= (dirIn.z  * dirIn.z  + ext * ext)
                && (innerext * innerext) <= (dirOut.z * dirOut.z + ext * ext);

        boolean bevel = cos < style.miterAngleLimit;

        Vec3d l0 = new Vec3d(0.0,          0.0, ext);
        Vec3d l1 = new Vec3d(0.0,          ext, ext);
        Vec3d l2 = new Vec3d(0.0,         -ext, ext);
        Vec3d li = new Vec3d(0.0,  ext * curve, ext);
        Vec3d lo = new Vec3d(0.0, -ext * curve, ext);

        Vec3d pCenter = new Vec3d(pos.xy().add(Vec2d.mul(normalMed, style.offset / Math.max(cos, 0.00001))), pos.z);
        Vec3d pInner = new Vec3d(pCenter.xy().add(Vec2d.mul(normalMed, innerext)), pos.z);

        Vec3d pRightA = new Vec3d(Vec2d.mul(normalIn,   ext).add(pCenter.xy()), pos.z);
        Vec3d pLeftA  = new Vec3d(Vec2d.mul(normalIn,  -ext).add(pCenter.xy()), pos.z);
        Vec3d pRightB = new Vec3d(Vec2d.mul(normalOut,  ext).add(pCenter.xy()), pos.z);
        Vec3d pLeftB  = new Vec3d(Vec2d.mul(normalOut, -ext).add(pCenter.xy()), pos.z);

        int iRightA = emitter.add(new Vertex(pRightA, l1));
        int iLeftA  = emitter.add(new Vertex(pLeftA,  l2));
        int iInner  = emitter.add(new Vertex(pInner,  li));
        int iCenter = emitter.add(new Vertex(pCenter, l0));

        if (intersects) {
            if (curve > 0) {
                // connector in
                emitter.emit(iInner);
                emitter.emit(iLeftA);
                emitter.next();

                // filler
                emitter.emit(iLeftA);
                emitter.emit(iCenter);
                emitter.emit(iInner);
            } else {
                // connector in
                emitter.emit(iRightA);
                emitter.emit(iInner);
                emitter.next();

                // filler
                emitter.emit(iRightA);
                emitter.emit(iInner);
                emitter.emit(iCenter);
            }
            emitter.next();
        } else {
            // connector in
            emitter.emit(iRightA);
            emitter.emit(iLeftA);
            emitter.next();
        }

        if (!bevel) {                           // miter cap
            Vec3d pOuter = new Vec3d(Vec2d.mul(normalMed, -innerext).add(pCenter.xy()), pos.z);
            int iOuter = emitter.add(new Vertex(pOuter, lo));

            if (curve > 0) {
                int iLeftAC = emitter.add(new Vertex(pLeftA, lo));

                emitter.emit(iLeftAC);
                emitter.emit(iOuter);
                emitter.emit(iCenter);
            } else {
                int iRightAC = emitter.add(new Vertex(pRightA, lo));

                emitter.emit(iRightAC);
                emitter.emit(iCenter);
                emitter.emit(iOuter);
            }
        } else {                                // bevel cap
            Vec3d pOuter = new Vec3d(Vec2d.mul(normalMed, -curve * ext * cos).add(pCenter.xy()), pos.z);
            int iOuter = emitter.add(new Vertex(pOuter, lo));

            if (curve > 0) {
                int iLeftAC = emitter.add(new Vertex(pLeftA, lo));

                emitter.emit(iLeftAC);
                emitter.emit(iOuter);
                emitter.emit(iCenter);
            } else {
                int iRightAC = emitter.add(new Vertex(pRightA, lo));
                int iRightBC = emitter.add(new Vertex(pRightB, lo));

                emitter.emit(iOuter);
                emitter.emit(iRightAC);
                emitter.emit(iCenter);
            }
        }
    }

    private void emitRoundHalfJoinBegin(Vec3d pos, Vec3d dirIn, Vec3d dirOut, Style style) {
        double ext = style.linewidth / 2.0;
        double curve = Math.signum(dirIn.xy().cross(dirOut.xy()));

        Vec2d normalIn  = new Vec2d(-dirIn.y,  dirIn.x);
        Vec2d normalOut = new Vec2d(-dirOut.y, dirOut.x);
        Vec2d normalMed = Vec2d.add(normalIn, normalOut).normalize();

        double cos = normalIn.dot(normalMed);
        double sin = normalIn.cross(normalMed);
        double innerext = curve * ext / cos;
        boolean intersects = cos != 0.0 && Double.isFinite(innerext)
                && (innerext * innerext) <= (dirIn.z  * dirIn.z  + ext * ext)
                && (innerext * innerext) <= (dirOut.z * dirOut.z + ext * ext);

        boolean miter = 1 < 2 * cos;

        Vec3d l0 = new Vec3d(0.0,          0.0, ext);
        Vec3d l1 = new Vec3d(0.0,          ext, ext);
        Vec3d l2 = new Vec3d(0.0,         -ext, ext);
        Vec3d li = new Vec3d(0.0,  ext * curve, ext);

        Vec3d pCenter = new Vec3d(pos.xy().add(Vec2d.mul(normalMed, style.offset / Math.max(cos, 0.00001))), pos.z);
        Vec3d pInner = new Vec3d(pCenter.xy().add(Vec2d.mul(normalMed, innerext)), pos.z);

        Vec3d pRightB = new Vec3d(Vec2d.mul(normalOut,  ext).add(pCenter.xy()), pos.z);
        Vec3d pLeftB  = new Vec3d(Vec2d.mul(normalOut, -ext).add(pCenter.xy()), pos.z);

        int iRightB = emitter.add(new Vertex(pRightB, l1));
        int iLeftB  = emitter.add(new Vertex(pLeftB,  l2));
        int iInner  = emitter.add(new Vertex(pInner,  li));
        int iCenter = emitter.add(new Vertex(pCenter, l0));

        if (intersects) {                       // filler
            if (curve > 0) {
                emitter.emit(iInner);
                emitter.emit(iCenter);
                emitter.emit(iLeftB);
            } else {
                emitter.emit(iCenter);
                emitter.emit(iInner);
                emitter.emit(iRightB);
            }
            emitter.next();
        }

        if (miter) {                            // miter cage
            Vec3d pOuter = new Vec3d(Vec2d.mul(normalMed, -innerext).add(pCenter.xy()), pos.z);
            Vec3d lx = new Vec3d((float) (sin * -innerext), (float) (-ext * curve), (float) ext);
            int iOuter = emitter.add(new Vertex(pOuter, lx));

            if (curve > 0) {
                emitter.emit(iCenter);
                emitter.emit(iOuter);
                emitter.emit(iLeftB);
            } else {
                emitter.emit(iOuter);
                emitter.emit(iCenter);
                emitter.emit(iRightB);
            }
        } else {                                // bevel cage
            Vec3d lo = new Vec3d(1.5 * cos * ext, -1.5 * sin * ext, ext);
            Vec3d lx = new Vec3d(            ext,     -curve * ext, ext);

            Vec3d pOuter   = new Vec3d(Vec2d.mul(normalMed, -ext * 1.5 * curve).add(pCenter.xy()), pos.z);
            Vec3d pOuterBC = new Vec3d(Vec2d.mul(normalOut, -curve * ext).sub(dirOut.xy().mul(ext)).add(pCenter.xy()), pos.z);

            int iOuter   = emitter.add(new Vertex(pOuter,   lo));
            int iOuterBC = emitter.add(new Vertex(pOuterBC, lx));

            if (curve > 0) {
                emitter.emit(iOuter);
                emitter.emit(iOuterBC);
                emitter.emit(iCenter);
                emitter.emit(iLeftB);
            } else {
                emitter.emit(iOuter);
                emitter.emit(iCenter);
                emitter.emit(iOuterBC);
                emitter.emit(iRightB);
            }
        }
        emitter.next();

        // connector out
        if (intersects) {
            if (curve > 0) {
                emitter.emit(iInner);
                emitter.emit(iLeftB);
            } else {
                emitter.emit(iRightB);
                emitter.emit(iInner);
            }
        } else {
            emitter.emit(iRightB);
            emitter.emit(iLeftB);
        }
    }

    private void emitRoundHalfJoinEnd(Vec3d pos, Vec3d dirIn, Vec3d dirOut, Style style) {
        double ext = style.linewidth / 2.0;
        double curve = Math.signum(dirIn.xy().cross(dirOut.xy()));

        Vec2d normalIn  = new Vec2d(-dirIn.y,  dirIn.x);
        Vec2d normalOut = new Vec2d(-dirOut.y, dirOut.x);
        Vec2d normalMed = Vec2d.add(normalIn, normalOut).normalize();

        double cos = normalIn.dot(normalMed);
        double sin = normalIn.cross(normalMed);
        double innerext = curve * ext / cos;
        boolean intersects = cos != 0.0 && Double.isFinite(innerext)
                && (innerext * innerext) <= (dirIn.z  * dirIn.z  + ext * ext)
                && (innerext * innerext) <= (dirOut.z * dirOut.z + ext * ext);

        boolean miter = 1 < 2 * cos;

        Vec3d l0 = new Vec3d(0.0,          0.0, ext);
        Vec3d l1 = new Vec3d(0.0,          ext, ext);
        Vec3d l2 = new Vec3d(0.0,         -ext, ext);
        Vec3d li = new Vec3d(0.0,  ext * curve, ext);

        Vec3d pCenter = new Vec3d(pos.xy().add(Vec2d.mul(normalMed, style.offset / Math.max(cos, 0.00001))), pos.z);
        Vec3d pInner = new Vec3d(pCenter.xy().add(Vec2d.mul(normalMed, innerext)), pos.z);

        Vec3d pRightA = new Vec3d(Vec2d.mul(normalIn,   ext).add(pCenter.xy()), pos.z);
        Vec3d pLeftA  = new Vec3d(Vec2d.mul(normalIn,  -ext).add(pCenter.xy()), pos.z);

        int iRightA = emitter.add(new Vertex(pRightA, l1));
        int iLeftA  = emitter.add(new Vertex(pLeftA,  l2));
        int iInner  = emitter.add(new Vertex(pInner,  li));
        int iCenter = emitter.add(new Vertex(pCenter, l0));

        if (intersects) {
            if (curve > 0) {
                // connector in
                emitter.emit(iInner);
                emitter.emit(iLeftA);
                emitter.next();

                // filler
                emitter.emit(iLeftA);
                emitter.emit(iCenter);
                emitter.emit(iInner);
            } else {
                // connector in
                emitter.emit(iRightA);
                emitter.emit(iInner);
                emitter.next();

                // filler
                emitter.emit(iRightA);
                emitter.emit(iInner);
                emitter.emit(iCenter);
            }
            emitter.next();
        } else {
            // connector in
            emitter.emit(iRightA);
            emitter.emit(iLeftA);
            emitter.next();
        }

        if (miter) {                            // miter cage
            Vec3d pOuter = new Vec3d(Vec2d.mul(normalMed, -innerext).add(pCenter.xy()), pos.z);
            Vec3d lx = new Vec3d((float) (sin * -innerext), (float) (-ext * curve), (float) ext);
            int iOuter = emitter.add(new Vertex(pOuter, lx));

            if (curve > 0) {
                emitter.emit(iLeftA);
                emitter.emit(iOuter);
                emitter.emit(iCenter);
            } else {
                emitter.emit(iRightA);
                emitter.emit(iCenter);
                emitter.emit(iOuter);
            }
        } else {                                // bevel cage
            Vec3d lo = new Vec3d(1.5 * cos * ext, -1.5 * sin * ext, ext);
            Vec3d lx = new Vec3d(            ext,     -curve * ext, ext);

            Vec3d pOuter   = new Vec3d(Vec2d.mul(normalMed, -ext * 1.5 * curve).add(pCenter.xy()), pos.z);
            Vec3d pOuterAC = new Vec3d(Vec2d.mul(normalIn,  -curve * ext).add( dirIn.xy().mul(ext)).add(pCenter.xy()), pos.z);

            int iOuter   = emitter.add(new Vertex(pOuter,   lo));
            int iOuterAC = emitter.add(new Vertex(pOuterAC, lx));

            if (curve > 0) {
                emitter.emit(iLeftA);
                emitter.emit(iOuterAC);
                emitter.emit(iCenter);
                emitter.emit(iOuter);
            } else {
                emitter.emit(iRightA);
                emitter.emit(iCenter);
                emitter.emit(iOuterAC);
                emitter.emit(iOuter);
            }
        }
    }


    public static Vec2d normal(Vec3d dir) {
        return new Vec2d(-dir.y, dir.x);
    }

    public static Vec3d dirvec2d(Vec3d a, Vec3d b) {
        double x = b.x - a.x;
        double y = b.y - a.y;
        double len = Math.hypot(x, y);

        return new Vec3d(x/len, y/len, len);
    }

    public static Vec3d dirvec2d(Vec2d a, Vec2d b) {
        double x = b.x - a.x;
        double y = b.y - a.y;
        double len = Math.hypot(x, y);

        return new Vec3d(x/len, y/len, len);
    }
}
