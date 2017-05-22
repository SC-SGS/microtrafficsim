package logic.geometry.sortvec;

import microtrafficsim.math.Geometry;
import microtrafficsim.math.Vec2f;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.*;

import static org.junit.Assert.assertEquals;

/**
 * <p>
 * This class tests {@link Geometry#sortClockwiseAsc(Vec2f, Collection, boolean)}.
 *
 * <p>
 * NOTE: You have to setup the expected vectors in counter clockwise direction.
 *
 * @author Dominic Parga Cacheiro
 */
public class TestSortVec2fCCW extends AbstractTestSortVec2 {
    private int sortRepetationCount = 1000;

    private LinkedList<Vec2f> ccwSortedVec2f;
    private Vec2f             zero;


    /*
    |============|
    | test cases |
    |============|
    */
    @Test
    public void differentFloatSizes() {
        zero = new Vec2f(0.1f, 100f);

        ccwSortedVec2f.add(zero);
        ccwSortedVec2f.add(new Vec2f(-42.424242f, 24.242424f));    // left top
        ccwSortedVec2f.add(new Vec2f(-42.424242f, 24.242424f));    // left top
        ccwSortedVec2f.add(new Vec2f(-42.424242f, -0));            // left
        ccwSortedVec2f.add(new Vec2f(-20, -30));                   // bottom left
        ccwSortedVec2f.add(Vec2f.mul(zero, -1));                         // bottom
        ccwSortedVec2f.add(new Vec2f(12345, -20000f));             // right bottom
        ccwSortedVec2f.add(new Vec2f(103f, 0.0001f));              // right
        ccwSortedVec2f.add(new Vec2f(1111111f, 1111111f));         // top right
        ccwSortedVec2f.add(new Vec2f(0.2f, 100f));                 // top, right from zero
    }

    @Test
    public void plusCrossroadScenario() {
        zero = new Vec2f(0.7427612f, -0.6695564f);    // right bottom

        ccwSortedVec2f.add(zero);
        ccwSortedVec2f.add(new Vec2f(0.886666f, 0.4624104f));    // right top
        ccwSortedVec2f.add(new Vec2f(-0.76506644f, 0.6439514f));    // left top
        ccwSortedVec2f.add(new Vec2f(-0.9005878f, -0.43467405f));    // left bottom
    }

    @Test
    public void pointSymmetricCrossTest0() {
        zero = new Vec2f(-0.001f, 0.001f);

        ccwSortedVec2f.add(zero);
        ccwSortedVec2f.add(new Vec2f(-0.001f, -0.001f));
        ccwSortedVec2f.add(new Vec2f( 0.000f, -0.001f));
        ccwSortedVec2f.add(new Vec2f( 0.001f, -0.001f));
        ccwSortedVec2f.add(new Vec2f( 0.001f,  0.001f));
        ccwSortedVec2f.add(new Vec2f( 0.000f,  0.001f));
    }

    @Test
    public void pointSymmetricCrossTest0Normalized() {
        zero = new Vec2f(-0.001f, 0.001f).normalize();

        ccwSortedVec2f.add(zero);
        ccwSortedVec2f.add(new Vec2f(-0.001f, -0.001f).normalize());
        ccwSortedVec2f.add(new Vec2f( 0.000f, -0.001f).normalize());
        ccwSortedVec2f.add(new Vec2f( 0.001f, -0.001f).normalize());
        ccwSortedVec2f.add(new Vec2f( 0.001f,  0.001f).normalize());
        ccwSortedVec2f.add(new Vec2f( 0.000f,  0.001f).normalize());
    }

    @Test
    public void pointSymmetricCrossTest1() {
        zero = new Vec2f(-0.001f, -0.001f);

        ccwSortedVec2f.add(zero);
        ccwSortedVec2f.add(new Vec2f( 0.000f, -0.001f));
        ccwSortedVec2f.add(new Vec2f( 0.001f, -0.001f));
        ccwSortedVec2f.add(new Vec2f( 0.001f,  0.001f));
        ccwSortedVec2f.add(new Vec2f( 0.000f,  0.001f));
        ccwSortedVec2f.add(new Vec2f(-0.001f,  0.001f));
    }

    @Test
    public void zeroAndMinusZero() {
        zero = new Vec2f(0f, 1f);

        ccwSortedVec2f.add(zero);
        ccwSortedVec2f.add(new Vec2f( -0f,  1f ));
        ccwSortedVec2f.add(new Vec2f(  1f,  0f));
        ccwSortedVec2f.add(new Vec2f(  1f, -0f));
    }


    /*
    |=======|
    | setup |
    |=======|
    */
    @Before
    public void setup() {
        ccwSortedVec2f = new LinkedList<>();
    }

    @After
    public void testCW() {
        /* setup cw */
        LinkedList<Vec2f> cwSortedVec2f = new LinkedList<>();
        for (Vec2f v : ccwSortedVec2f)
            cwSortedVec2f.add(v);
        for (int i = 0; i < cwSortedVec2f.size(); i++) {
            Vec2f tmp = cwSortedVec2f.poll();
            cwSortedVec2f.add(tmp);

            if (!cwSortedVec2f.peek().equals(zero))
                break;
        }
        Collections.reverse(cwSortedVec2f);


        /* assertion */
        for (int i = 0; i < sortRepetationCount; i++)
            testSorting(cwSortedVec2f, true);
    }

    @After
    public void testCCW() {
        for (int i = 0; i < sortRepetationCount; i++)
            testSorting(ccwSortedVec2f, false);
    }

    private void testSorting(List<Vec2f> expected, boolean cw) {
        List<Vec2f> mixedVec2f = shuffleCorrectly(expected);
        Queue<Vec2f> actualSorted = Geometry.sortClockwiseAsc(zero, mixedVec2f, cw);

        for (Vec2f v : expected)
            assertEquals(v, actualSorted.poll());
    }
}
