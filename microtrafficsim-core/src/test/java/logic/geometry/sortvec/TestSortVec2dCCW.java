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
public class TestSortVec2dCCW {

    /* general */
    private int               sortRepetationCount = 1000;
    private LinkedList<Vec2d> cwSortedVec2d;
    private ArrayList<Vec2d>  mixedVec2d;

    /* different for each test case */
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

        /* counterclockwise */
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
        zero = new Vec2d(0.7427612, -0.6695564);    // right bottom

        ccwSortedVec2d.add(zero);
        ccwSortedVec2d.add(new Vec2d(0.886666, 0.4624104));    // right top
        ccwSortedVec2d.add(new Vec2d(-0.76506644, 0.6439514));    // left top
        ccwSortedVec2d.add(new Vec2d(-0.9005878, -0.43467405));    // left bottom
    }

    /*
    |=======|
    | setup |
    |=======|
    */
    @Before
    public void setup() {
        ccwSortedVec2d = new LinkedList<>();
        cwSortedVec2d = new LinkedList<>();
    }

    @After
    public void testCW() {

        /* setup cw */
        for (Vec2d v : ccwSortedVec2d)
            cwSortedVec2d.add(v);
        cwSortedVec2d.poll();
        cwSortedVec2d.add(zero);
        Collections.reverse(cwSortedVec2d);

        mixedVec2d = new ArrayList<>(ccwSortedVec2d);
        Collections.shuffle(mixedVec2d);

        /* assertion */
        for (int i = 0; i < sortRepetationCount; i++) {
            Queue<Vec2d> sortedCW = Geometry.sortClockwiseAsc(zero, mixedVec2d, true);

            for (Vec2d v : cwSortedVec2d)
                assertEquals(v, sortedCW.poll());
        }
    }

    @After
    public void testCCW() {
        mixedVec2d = new ArrayList<>(ccwSortedVec2d);
        Collections.shuffle(mixedVec2d);

        boolean notFailed = true;
        for (int i = 0; i < sortRepetationCount; i++) {
            Queue<Vec2d> sortedCCW = Geometry.sortClockwiseAsc(zero, mixedVec2d, false);

            for (Vec2d v : ccwSortedVec2d)
                assertEquals(v, sortedCCW.poll());
        }
    }
}