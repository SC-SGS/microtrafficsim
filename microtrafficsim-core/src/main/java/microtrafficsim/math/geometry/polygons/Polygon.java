package microtrafficsim.math.geometry.polygons;

import microtrafficsim.math.MathUtils;
import microtrafficsim.math.Rect2d;
import microtrafficsim.math.Vec2d;
import microtrafficsim.math.geometry.Lines;
import microtrafficsim.utils.collections.ArrayUtils;


/**
 * Polygonal area, described by an outline and islands.
 *
 * @author Maximilian Luz
 */
public class Polygon {
    public Vec2d[] outline;
    public Vec2d[][] islands;

    /**
     * Creates a new polygon with the given outline. The first vertex should not equal
     * the last vertex.
     *
     * @param outline the outline of the created polygon.
     */
    public Polygon(Vec2d[] outline) {
        this(outline, new Vec2d[0][]);
    }

    /**
     * Creates a new polygon with the given outline and islands.
     *
     * @param outline the outline of the created polygon, the first vertex should not
     *                equal the last vertex.
     * @param islands the islands of the created polygon, the first vertices should not
     *                equal the respective last vertex.
     */
    public Polygon(Vec2d[] outline, Vec2d[][] islands) {
        this.outline = outline;
        this.islands = islands;
    }


    /**
     * Tests whether the given point is contained in this polygon, i. e. it is inside
     * the outline and outside all islands.
     *
     * @param p the point to test for.
     * @return {@code true} iff the given point is inside this polygon.
     */
    public boolean contains(Vec2d p) {
        if (!contains(outline, p))
            return false;

        for (Vec2d[] i : islands)
            if (contains(i, p))
                return false;

        return true;
    }

    /**
     * Normalizes this polygon in place, i. e. restructures the polygon so that the
     * edges of the outline are oriented counter-clockwise, the edges of the islands
     * are oriented clockwise, and, if the area enclosed by the outline is smaller
     * than the area enclosed by the largest island, the outline is swapped with said
     * island.
     *
     * @return this polygon, normalized.
     */
    public Polygon normalize() {
        double[] area = new double[islands.length + 1];

        // calculate all areas
        area[0] = s2area(outline);
        for (int i = 0; i < islands.length; i++)
            area[i + 1] = s2area(islands[i]);

        // make sure the outline is the loop with the largest area
        for (int i = 1; i < area.length; i++) {
            if (Math.abs(area[i]) > Math.abs(area[0])) {
                double tmpA = area[0];
                area[0] = area[i];
                area[i] = tmpA;

                Vec2d[] tmpO = outline;
                outline = islands[i - 1];
                islands[i - 1] = tmpO;
            }
        }

        // make sure polygon-loops are oriented correctly (outline CCW, islands CW)
        if (area[0] > 0)
            ArrayUtils.reverseInPlace(outline);

        for (int i = 1; i < area.length; i++)
            if (area[i] <= 0)
                ArrayUtils.reverseInPlace(islands[i - 1]);

        return this;
    }

    public Rect2d bounds() {
        return Rect2d.from(outline);
    }


    /**
     * Checks if the outline given by the specified array of vertices contains the
     * given point.
     *
     * @param outline the outline against which should be checked.
     * @param p       the point which should be tested.
     * @return {@code true} iff the given point lies inside the area enclosed by
     *         the given outline.
     */
    public static boolean contains(Vec2d[] outline, Vec2d p) {
        /*
         * The point-in-polygon test is calculated using a, for integer values modified,
         * version of the winding number algorithm described in 'A Winding Number and
         * Point-in-Polygon Algorithm' by David G. Alciatore, Dept. of Mechanical
         * Engineering, Colorado State University.
         * (https://www.engr.colostate.edu/~dga/dga/papers/point_in_polygon.pdf)
         */

        int windings = 0;    // actually the doubled number of windings

        double x1 = outline[outline.length - 1].x - p.x;
        double y1 = outline[outline.length - 1].y - p.y;
        for (Vec2d v : outline) {
            double x2 = v.x - p.x;
            double y2 = v.y - p.y;

            if (y1 * y2 < 0) {    // (1) --> (2) crosses the x-axis
                double r = x1
                        + (y1 * (x2 - x1)) / (y1 - y2);    // x-coordinate of intersection of (1) --> (2) and x-axis
                if (r > 0) {                                  // (1) --> (2) crosses positive x-axis
                    if (y1 < 0)
                        windings += 2;
                    else
                        windings -= 2;
                }
            } else if (y1 == 0 && x1 > 0) {    // (1) is on the positive x-axis
                if (y2 > 0)
                    windings += 1;
                else
                    windings -= 1;
            } else if (y2 == 0 && x2 > 0) {    // (2) is on the positive x-axis
                if (y1 < 0)
                    windings += 1;
                else
                    windings -= 1;
            }

            x1 = x2;
            y1 = y2;
        }

        return windings != 0;
    }

    public static boolean intersects(Vec2d[] outline, Rect2d aabb) {
        // check if any point of the polygon is inside of the AABB
        for (Vec2d v : outline) {
            if (aabb.contains(v)) {
                return true;
            }
        }

        // check if any edge of the polygon intersects any edge of the AABB
        Vec2d pa = outline[outline.length - 1];
        for (Vec2d pb : outline) {
            Vec2d b0 = new Vec2d(aabb.xmin, aabb.ymin);
            Vec2d b1 = new Vec2d(aabb.xmin, aabb.ymax);
            Vec2d b2 = new Vec2d(aabb.xmax, aabb.ymax);
            Vec2d b3 = new Vec2d(aabb.xmax, aabb.ymin);

            if (Lines.segmentIntersectsNonCoincidental(pa, pb, b0, b1)) {
                return true;
            } else if (Lines.segmentIntersectsNonCoincidental(pa, pb, b1, b2)) {
                return true;
            } else if (Lines.segmentIntersectsNonCoincidental(pa, pb, b2, b3)) {
                return true;
            } else if (Lines.segmentIntersectsNonCoincidental(pa, pb, b3, b0)) {
                return true;
            }
        }

        return false;
    }


    /**
     * Calculates the signed double area enclosed by the given outline.
     *
     * @param outline the outline describing the area, the first vertex should not
     *                equal the last vertex.
     * @return the signed double area as enclosed by the the outline. This will be
     *         negative, if the outline is counter-clockwise, positive otherwise.
     *         For a self-intersecting outline, the sign will indicate the order of
     *         largest part.
     */
    public static double s2area(Vec2d[] outline) {
        double area = 0;

        Vec2d a = outline[outline.length - 1];
        for (int i = 0; i < outline.length; i++) {
            Vec2d b = outline[i];

            area += (b.x - a.x) * (b.y + a.y);

            a = b;
        }

        return area;
    }

    /**
     * Calculates the absolute area enclosed by the given outline.
     *
     * @param outline the outline describing the area, the first vertex should not
     *                equal the last vertex.
     * @return the absolute area enclosed by the given outline.
     */
    public static double area(Vec2d[] outline) {
        return Math.abs(s2area(outline)) / 2.0;
    }
}
