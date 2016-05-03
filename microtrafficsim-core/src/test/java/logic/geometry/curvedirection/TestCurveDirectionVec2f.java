package logic.geometry.curvedirection;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import microtrafficsim.core.logic.Direction;
import microtrafficsim.math.Geometry;
import microtrafficsim.math.Vec2f;

/**
 * This class tests the method
 * mictrotrafficsim.math.Geometry.calcCurveDirection(Vec2f p, Vec2f q, Vec2f r).
 * 
 * @author Dominic Parga Cacheiro
 */
public class TestCurveDirectionVec2f {

	private Vec2f p, q, r, s, xninf, xpinf, ninf;

	@Before
	public void setup() {

		// pqr is right curve
		// rqp is left curve
		// pqs is collinear

		p = new Vec2f(0, 0);
		q = new Vec2f(1.7f, 2.3f);
		r = new Vec2f(3.14f, 1.61f);
		s = new Vec2f(2 * 1.7f, 2 * 2.3f);

		xninf = new Vec2f(Float.NEGATIVE_INFINITY, 0f);
		xpinf = new Vec2f(Float.POSITIVE_INFINITY, 0f);
		ninf = new Vec2f(Float.NEGATIVE_INFINITY, Float.NEGATIVE_INFINITY);
	}

	@Test
	public void checkPositiveInfinity() {
		assertEquals(Direction.RIGHT, Geometry.calcCurveDirection(p, q, xpinf));
		assertEquals(Direction.LEFT, Geometry.calcCurveDirection(p, xpinf, q));
		assertEquals(null, Geometry.calcCurveDirection(xpinf, p, q));
	}

	@Test
	public void checkNegativeInfinity() {
		assertEquals(Direction.LEFT, Geometry.calcCurveDirection(p, q, xninf));
		assertEquals(Direction.RIGHT, Geometry.calcCurveDirection(p, xninf, q));
		assertEquals(null, Geometry.calcCurveDirection(xninf, p, q));

		assertEquals(null, Geometry.calcCurveDirection(ninf, p, q));
	}

	@Test
	public void checkMixedInfinity() {
		assertEquals(null, Geometry.calcCurveDirection(xninf, q, xpinf));
		assertEquals(null, Geometry.calcCurveDirection(xninf, q, xninf));
		assertEquals(null, Geometry.calcCurveDirection(xpinf, q, xninf));

		assertEquals(null, Geometry.calcCurveDirection(xninf, p, xpinf));
		assertEquals(null, Geometry.calcCurveDirection(xninf, p, xninf));
		assertEquals(null, Geometry.calcCurveDirection(xpinf, p, xninf));
		assertEquals(null, Geometry.calcCurveDirection(xpinf, xpinf, xninf));
	}

	@Test
	public void checkLeftDirection() {
		assertEquals(Direction.LEFT, Geometry.calcCurveDirection(r, q, p));
	}

	@Test
	public void checkCollinearDirection() {
		assertEquals(Direction.COLLINEAR, Geometry.calcCurveDirection(p, q, s));
	}

	@Test
	public void checkRightDirection() {
		assertEquals(Direction.RIGHT, Geometry.calcCurveDirection(p, q, r));
	}
}