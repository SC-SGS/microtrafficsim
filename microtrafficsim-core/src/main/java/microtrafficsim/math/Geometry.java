package microtrafficsim.math;

import microtrafficsim.core.logic.Direction;
import microtrafficsim.core.map.Coordinate;
import microtrafficsim.utils.collections.FastSortedArrayList;

import java.util.*;


/**
 * This class implements methods for calculating characteristics of vectors.
 *
 * @author Dominic Parga Cacheiro
 */
public class Geometry {

    /**
     * Calls {@code calcCurveDirection(p.x, p.y, q.x, q.y, r.x, r.y)}
     *
     * @see #calcCurveDirection(float, float, float, float, float, float)
     */
    public static Direction calcCurveDirection(Vec2f p, Vec2f q, Vec2f r) {
        return calcCurveDirection(p.x, p.y, q.x, q.y, r.x, r.y);
    }

    /**
     * Calls {@code calcCurveDirection(p.x, p.y, q.x, q.y, r.x, r.y)}
     *
     * @see #calcCurveDirection(double, double, double, double, double, double)
     */
    public static Direction calcCurveDirection(Vec2d p, Vec2d q, Vec2d r) {
        return calcCurveDirection(p.x, p.y, q.x, q.y, r.x, r.y);
    }

    /**
     * Calls {@code calcCurveDirection(p.x, p.y, q.x, q.y, r.x, r.y)}
     *
     * @see #calcCurveDirection(double, double, double, double, double, double)
     */
    public static Direction calcCurveDirection(Coordinate p, Coordinate q, Coordinate r) {
        return calcCurveDirection(p.lon, p.lat, q.lon, q.lat, r.lon, r.lat);
    }

    /**
     * This method calculates the direction of the curve starting in p, passing
     * q and ending in r. In special cases it returns null (e.g. if all vectors
     * together contains more than one infinity value).
     *
     * @param px start of the curve - horizontal
     * @param py start of the curve - vertical
     * @param qx middle point of the curve - horizontal
     * @param qy middle point of the curve - vertical
     * @param rx last point of the curve - horizontal
     * @param ry last point of the curve - vertical
     * @return Direction.LEFT if {@literal p->q->r} describes a left turn; Direction.RIGHT
     * if {@literal p->q->r} describes a right turn; Direction.COLLINEAR if {@literal p->q->r}
     * are collinear (= on the same line)
     */
    public static Direction calcCurveDirection(float px, float py, float qx, float qy, float rx, float ry) {
        // float vp = Vec2f.sub(q, p).cross(Vec2f.sub(r, p));
        float vp = ((qx - px) * (ry - py) - (rx - px) * (qy - py));

        if (vp == 0f) return Direction.COLLINEAR;
        if (vp < 0f)  return Direction.RIGHT;
        if (vp > 0f)  return Direction.LEFT;
        return null;
    }

    /**
     * Same as {@link #calcCurveDirection(float, float, float, float, float, float)} using doubles.
     */
    public static Direction calcCurveDirection(double px, double py, double qx, double qy, double rx, double ry) {
        // double vp = Vec2f.sub(q, p).cross(Vec2f.sub(r, p));
        double vp = ((qx - px) * (ry - py) - (rx - px) * (qy - py));

        if (vp == 0f) return Direction.COLLINEAR;
        if (vp < 0f)  return Direction.RIGHT;
        if (vp > 0f)  return Direction.LEFT;
        return null;
    }

    /**
     * <p>
     * This method returns the given vectors sorted (counter-)clockwise
     * ascending. The Vec2f zero stands for 0 degrees.
     *
     * <p>
     * This method supports multiple occurrence of the same vector.
     *
     * @param zero      Stands for 0 degrees
     * @param vectors   Will be sorted
     * @param clockwise if true {@literal ->} clockwise ascending; if false {@literal ->} counter clockwise
     *                  ascending
     * @return A queue containing the given vectors sorted
     */
    public static Queue<Vec2f> sortClockwiseAsc(Vec2f zero, Collection<Vec2f> vectors, boolean clockwise) {
        Direction direction = clockwise ? Direction.LEFT : Direction.RIGHT;

        HashMap<Vec2f, Double> alphas = new HashMap<>();
        for (Vec2f v : vectors) {
            if (alphas.containsKey(v))
                continue;
            double dot = (Vec2f.dot(zero, v) / (zero.len() * v.len()));
            dot = MathUtils.clamp(dot, -1, 1);
            double alpha = Math.acos(dot);
            if (direction == calcCurveDirection(new Vec2f(), zero, Vec2f.add(zero, v)))
                alpha = 2 * Math.PI - alpha;
            alphas.put(v, alpha);
        }

        return new FastSortedArrayList<>(vectors, Comparator.comparingDouble(alphas::get));
    }

    /**
     * <p>
     * This method returns the given vectors sorted (counter-)clockwise
     * ascending. The Vec2f zero stands for 0 degrees.
     *
     * <p>
     * This method supports multiple occurrence of the same vector.
     *
     * @param zero      Stands for 0 degrees
     * @param vectors   Will be sorted
     * @param clockwise if true {@literal ->} clockwise ascending; if false {@literal ->} counter clockwise
     *                  ascending
     * @return A queue containing the given vectors sorted
     */
    public static Queue<Vec2d> sortClockwiseAsc(Vec2d zero, Collection<Vec2d> vectors, boolean clockwise) {
        Direction direction = clockwise ? Direction.LEFT : Direction.RIGHT;

        HashMap<Vec2d, Double> alphas = new HashMap<>();
        for (Vec2d v : vectors) {
            if (alphas.containsKey(v))
                continue;
            double dot = (Vec2d.dot(zero, v) / (zero.len() * v.len()));
            dot = MathUtils.clamp(dot, -1, 1);
            double alpha = Math.acos(dot);
            if (direction == calcCurveDirection(new Vec2d(), zero, Vec2d.add(zero, v)))
                alpha = 2 * Math.PI - alpha;
            alphas.put(v, alpha);
        }

        return new FastSortedArrayList<>(vectors, Comparator.comparingDouble(alphas::get));
    }
}
