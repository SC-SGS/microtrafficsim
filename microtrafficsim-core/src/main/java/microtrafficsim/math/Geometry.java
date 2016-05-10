package microtrafficsim.math;

import microtrafficsim.core.logic.Direction;
import microtrafficsim.core.map.Coordinate;

import java.util.*;

/**
 * This class implements methods for calculating characteristics of vectors.
 * 
 * @author Dominic Parga Cacheiro
 */
public class Geometry {

	/**
	 * This method calculates the direction of the curve starting in p, passing
	 * q and ending in r. In special cases it returns null (e.g. if all vectors
	 * together contains more than one infinity value).
	 * 
	 * @param p
	 *            start of the curve
	 * @param q
	 *            middle point of the curve
	 * @param r
	 *            last point of the curve
	 * @return Direction.LEFT if p->q->r describes a left turn; Direction.RIGHT
	 *         if p->q->r describes a right turn; Direction.COLLINEAR if p->q->r
	 *         are collinear (= on the same line)
	 */
	public static Direction calcCurveDirection(Vec2f p, Vec2f q, Vec2f r) {
		float vp = Vec2f.sub(q, p).cross(Vec2f.sub(r, p));

		if (vp == 0f)
			return Direction.COLLINEAR;
		if (vp < 0f)
			return Direction.RIGHT;
		if (vp > 0f)
			return Direction.LEFT;
		return null;
	}

	/**
	 * This method calculates the direction of the curve starting in p, passing
	 * q and ending in r. In special cases it returns null (e.g. if all vectors
	 * together contains more than one infinity value).
	 * 
	 * @param p
	 *            start of the curve
	 * @param q
	 *            middle point of the curve
	 * @param r
	 *            last point of the curve
	 * @return Direction.LEFT if p->q->r describes a left turn; Direction.RIGHT
	 *         if p->q->r describes a right turn; Direction.COLLINEAR if p->q->r
	 *         are collinear (= on the same line)
	 */
	public static Direction calcCurveDirection(Coordinate p, Coordinate q, Coordinate r) {
		double vp = ((q.lon - p.lon) * (r.lat - p.lat) - (r.lon - p.lon) * (q.lat - p.lat));

		if (vp == 0f)
			return Direction.COLLINEAR;
		if (vp < 0f)
			return Direction.RIGHT;
		if (vp > 0f)
			return Direction.LEFT;
		return null;
	}

	/**
	 * This method calculates the direction of the curve starting in p, passing
	 * q and ending in r. In special cases it returns null (e.g. if all vectors
	 * together contains more than one infinity value).
	 * 
	 * @param px
	 *            start of the curve - horizontal
	 * @param py
	 *            start of the curve - vertical
	 * @param qx
	 *            middle point of the curve - horizontal
	 * @param qy
	 *            middle point of the curve - vertical
	 * @param rx
	 *            last point of the curve - horizontal
	 * @param ry
	 *            last point of the curve - vertical
	 * @return Direction.LEFT if p->q->r describes a left turn; Direction.RIGHT
	 *         if p->q->r describes a right turn; Direction.COLLINEAR if p->q->r
	 *         are collinear (= on the same line)
	 */
	public static Direction calcCurveDirection(float px, float py, float qx, float qy, float rx, float ry) {
		float vp = ((qx - px) * (ry - py) - (rx - px) * (qy - py));

		if (vp == 0f)
			return Direction.COLLINEAR;
		if (vp < 0f)
			return Direction.RIGHT;
		if (vp > 0f)
			return Direction.LEFT;
		return null;
	}

	/**
	 * This method returns the given vectors sorted (counter-)clockwise
	 * ascending. The @Vec2f zero stands for 0 degrees.
	 * 
	 * @param zero
	 *            Stands for 0 degrees
	 * @param vectors
	 *            Will be sorted
	 * @param clockwise
	 *            if true => clockwise ascending; if false => counter clockwise
	 *            ascending
	 * @return A queue containing the given vectors sorted
	 */
	public static Queue<Vec2f> sortClockwiseAsc(Vec2f zero, Collection<Vec2f> vectors, boolean clockwise) {

		Direction direction = clockwise ? Direction.LEFT : Direction.RIGHT;

		HashMap<Vec2f, Double> alphas = new HashMap<>();
		for (Vec2f v : vectors) {
			float dot = (Vec2f.dot(zero, v) / (zero.len() * v.len()));
			double alpha = Math.acos(dot);
			if (direction == calcCurveDirection(new Vec2f(), zero, Vec2f.add(zero, v)))
				alpha = 2 * Math.PI - alpha;
			alphas.put(v, alpha);
		}

		LinkedList<Vec2f> sortedList = new LinkedList<>(alphas.keySet());
		Collections.sort(sortedList, (o1, o2) -> {
			double a1 = alphas.get(o1);
			double a2 = alphas.get(o2);
			if (a1 > a2)
				return 1;
			if (a1 < a2)
				return -1;
			return 0;
		});

		return sortedList;
	}
}