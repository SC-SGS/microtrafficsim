package logic.geometry.curvedirection;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

import microtrafficsim.core.logic.Direction;
import microtrafficsim.core.map.Coordinate;
import microtrafficsim.math.Geometry;

/**
 * This class tests the method
 * mictrotrafficsim.math.Geometry.calcCurveDirection(Coordinate p, Coordinate q, Coordinate r).
 * 
 * @author Dominic Parga Cacheiro
 */
public class TestCurveDirectionCoordinate {

	private Coordinate p, q, r, s, xninf, xpinf, ninf;

	@Before
	public void setup() {

		// pqr is right curve
		// rqp is left curve
		// pqs is collinear

		p = new Coordinate(0, 0);
		q = new Coordinate(2.3f, 1.7f);
		r = new Coordinate(1.61f, 3.14f);
		s = new Coordinate(2 * 2.3f, 2 * 1.7f);

		xninf = new Coordinate(0f, Float.NEGATIVE_INFINITY);
		xpinf = new Coordinate(0f, Float.POSITIVE_INFINITY);
		ninf = new Coordinate(Float.NEGATIVE_INFINITY, Float.NEGATIVE_INFINITY);
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