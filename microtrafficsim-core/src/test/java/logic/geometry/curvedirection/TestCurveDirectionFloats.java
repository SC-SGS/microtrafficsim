package logic.geometry.curvedirection;

import microtrafficsim.core.logic.Direction;
import microtrafficsim.math.Geometry;
import microtrafficsim.math.Vec2f;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;


/**
 * This class tests the method
 * mictrotrafficsim.math.Geometry.calcCurveDirection(float p.x, float p.y, float q.x,
 * float q.y, float r.x, float r.y).
 *
 * @author Dominic Parga Cacheiro
 */
public class TestCurveDirectionFloats {
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
        ninf  = new Vec2f(Float.NEGATIVE_INFINITY, Float.NEGATIVE_INFINITY);
    }

    @Test
    public void checkPositiveInfinity() {
        assertEquals(Direction.RIGHT, Geometry.calcCurveDirection(p.x, p.y, q.x, q.y, xpinf.x, xpinf.y));
        assertEquals(Direction.LEFT, Geometry.calcCurveDirection(p.x, p.y, xpinf.x, xpinf.y, q.x, q.y));
        assertEquals(null, Geometry.calcCurveDirection(xpinf.x, xpinf.y, p.x, p.y, q.x, q.y));
    }

    @Test
    public void checkNegativeInfinity() {
        assertEquals(Direction.LEFT, Geometry.calcCurveDirection(p.x, p.y, q.x, q.y, xninf.x, xninf.y));
        assertEquals(Direction.RIGHT, Geometry.calcCurveDirection(p.x, p.y, xninf.x, xninf.y, q.x, q.y));
        assertEquals(null, Geometry.calcCurveDirection(xninf.x, xninf.y, p.x, p.y, q.x, q.y));

        assertEquals(null, Geometry.calcCurveDirection(ninf.x, ninf.y, p.x, p.y, q.x, q.y));
    }

    @Test
    public void checkMixedInfinity() {
        assertEquals(null, Geometry.calcCurveDirection(xninf.x, xninf.y, q.x, q.y, xpinf.x, xpinf.y));
        assertEquals(null, Geometry.calcCurveDirection(xninf.x, xninf.y, q.x, q.y, xninf.x, xninf.y));
        assertEquals(null, Geometry.calcCurveDirection(xpinf.x, xpinf.y, q.x, q.y, xninf.x, xninf.y));

        assertEquals(null, Geometry.calcCurveDirection(xninf.x, xninf.y, p.x, p.y, xpinf.x, xpinf.y));
        assertEquals(null, Geometry.calcCurveDirection(xninf.x, xninf.y, p.x, p.y, xninf.x, xninf.y));
        assertEquals(null, Geometry.calcCurveDirection(xpinf.x, xpinf.y, p.x, p.y, xninf.x, xninf.y));
        assertEquals(null, Geometry.calcCurveDirection(xpinf.x, xpinf.y, xpinf.x, xpinf.y, xninf.x, xninf.y));
    }

    @Test
    public void checkLeftDirection() {
        assertEquals(Direction.LEFT, Geometry.calcCurveDirection(r.x, r.y, q.x, q.y, p.x, p.y));
    }

    @Test
    public void checkCollinearDirection() {
        assertEquals(Direction.COLLINEAR, Geometry.calcCurveDirection(p.x, p.y, q.x, q.y, s.x, s.y));
    }

    @Test
    public void checkRightDirection() {
        assertEquals(Direction.RIGHT, Geometry.calcCurveDirection(p.x, p.y, q.x, q.y, r.x, r.y));
    }
}