package logic.geometry.sortvec;

import microtrafficsim.math.Geometry;
import microtrafficsim.math.Vec2d;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.*;

import static org.junit.Assert.assertEquals;

/**
 * <p>
 * This class tests {@link Geometry#sortClockwiseAsc(Vec2d, Collection, boolean)}.
 *
 * <p>
 * NOTE: You have to setup the expected vectors in counter clockwise direction.
 *
 * @author Dominic Parga Cacheiro
 */
public class TestSortVec2dCCW extends AbstractTestSortVec2 {
    private int sortRepetationCount = 1000;

    private LinkedList<Vec2d> ccwSortedVec2d;
    private Vec2d             zero;


    /*
    |============|
    | test cases |
    |============|
    */
    @Test
    public void differentFloatSizes() {
        zero = new Vec2d(0.1f, 100f);

        ccwSortedVec2d.add(zero);
        ccwSortedVec2d.add(new Vec2d(-42.424242f, 24.242424f));    // left top
        ccwSortedVec2d.add(new Vec2d(-42.424242f, 24.242424f));    // left top
        ccwSortedVec2d.add(new Vec2d(-42.424242f, -0));            // left
        ccwSortedVec2d.add(new Vec2d(-20, -30));                   // bottom left
        ccwSortedVec2d.add(Vec2d.mul(zero, -1));                         // bottom
        ccwSortedVec2d.add(new Vec2d(12345, -20000f));             // right bottom
        ccwSortedVec2d.add(new Vec2d(103f, 0.0001f));              // right
        ccwSortedVec2d.add(new Vec2d(1111111f, 1111111f));         // top right
        ccwSortedVec2d.add(new Vec2d(0.2f, 100f));                 // top, right from zero
    }

    @Test
    public void plusCrossroadScenario() {
        zero = new Vec2d(0.7427612f, -0.6695564f);    // right bottom

        ccwSortedVec2d.add(zero);
        ccwSortedVec2d.add(new Vec2d(0.886666f, 0.4624104f));    // right top
        ccwSortedVec2d.add(new Vec2d(-0.76506644f, 0.6439514f));    // left top
        ccwSortedVec2d.add(new Vec2d(-0.9005878f, -0.43467405f));    // left bottom
    }

    @Test
    public void pointSymmetricCrossTest0() {
        zero = new Vec2d(-0.001f, 0.001f);

        ccwSortedVec2d.add(zero);
        ccwSortedVec2d.add(new Vec2d(-0.001f, -0.001f));
        ccwSortedVec2d.add(new Vec2d( 0.000f, -0.001f));
        ccwSortedVec2d.add(new Vec2d( 0.001f, -0.001f));
        ccwSortedVec2d.add(new Vec2d( 0.001f,  0.001f));
        ccwSortedVec2d.add(new Vec2d( 0.000f,  0.001f));
    }

    @Test
    public void pointSymmetricCrossTest0Normalized() {
        zero = new Vec2d(-0.001f, 0.001f).normalize();

        ccwSortedVec2d.add(zero);
        ccwSortedVec2d.add(new Vec2d(-0.001f, -0.001f).normalize());
        ccwSortedVec2d.add(new Vec2d( 0.000f, -0.001f).normalize());
        ccwSortedVec2d.add(new Vec2d( 0.001f, -0.001f).normalize());
        ccwSortedVec2d.add(new Vec2d( 0.001f,  0.001f).normalize());
        ccwSortedVec2d.add(new Vec2d( 0.000f,  0.001f).normalize());
    }

    @Test
    public void pointSymmetricCrossTest1() {
        zero = new Vec2d(-0.001f, -0.001f);

        ccwSortedVec2d.add(zero);
        ccwSortedVec2d.add(new Vec2d( 0.000f, -0.001f));
        ccwSortedVec2d.add(new Vec2d( 0.001f, -0.001f));
        ccwSortedVec2d.add(new Vec2d( 0.001f,  0.001f));
        ccwSortedVec2d.add(new Vec2d( 0.000f,  0.001f));
        ccwSortedVec2d.add(new Vec2d(-0.001f,  0.001f));
    }

    @Test
    public void zeroAndMinusZero() {
        zero = new Vec2d(0f, 1f);

        ccwSortedVec2d.add(zero);
        ccwSortedVec2d.add(new Vec2d( -0f,  1f ));
        ccwSortedVec2d.add(new Vec2d(  1f,  0f));
        ccwSortedVec2d.add(new Vec2d(  1f, -0f));
    }


    /*
    |=======|
    | setup |
    |=======|
    */
    @Before
    public void setup() {
        ccwSortedVec2d = new LinkedList<>();
    }

    @After
    public void testCW() {
        /* setup cw */
        LinkedList<Vec2d> cwSortedVec2d = new LinkedList<>();
        for (Vec2d v : ccwSortedVec2d)
            cwSortedVec2d.add(v);
        for (int i = 0; i < cwSortedVec2d.size(); i++) {
            Vec2d tmp = cwSortedVec2d.poll();
            cwSortedVec2d.add(tmp);

            if (!cwSortedVec2d.peek().equals(zero))
                break;
        }
        Collections.reverse(cwSortedVec2d);


        /* assertion */
        for (int i = 0; i < sortRepetationCount; i++)
            testSorting(cwSortedVec2d, true);
    }

    @After
    public void testCCW() {
        for (int i = 0; i < sortRepetationCount; i++)
            testSorting(ccwSortedVec2d, false);
    }

    private void testSorting(List<Vec2d> expected, boolean cw) {
        List<Vec2d> mixedVec2d = shuffleCorrectly(expected);
        Queue<Vec2d> actualSorted = Geometry.sortClockwiseAsc(zero, mixedVec2d, cw);

        for (Vec2d v : expected)
            assertEquals(v, actualSorted.poll());
    }
}
