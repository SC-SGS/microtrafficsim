package logic.geometry.sortvec;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Queue;

import org.junit.Before;
import org.junit.Test;

import microtrafficsim.math.Geometry;
import microtrafficsim.math.Vec2f;

/**
 * This class tests the method
 * mictrotrafficsim.math.Geometry.sortClockwiseAsc(...).
 * 
 * @author Dominic Parga Cacheiro
 */
public class TestSortVecCCW {

	private ArrayList<Vec2f> ccwSortedVectors;
	private ArrayList<Vec2f> cwSortedVectors;
	private ArrayList<Vec2f> mixedVectors;
	private Vec2f zero;

	@Before
	public void setup() {

		zero = new Vec2f(0.1f, 100f);
		ccwSortedVectors = new ArrayList<>(9);

		ccwSortedVectors.add(new Vec2f(-42.424242f, 24.242424f)); // top
																	// left
		ccwSortedVectors.add(new Vec2f(-42.424242f, -0)); // left
		ccwSortedVectors.add(new Vec2f(-20, -30)); // bottom left
		ccwSortedVectors.add(Vec2f.mul(zero, -1)); // bottom
		ccwSortedVectors.add(new Vec2f(12345, -20000f)); // right bottom
		ccwSortedVectors.add(new Vec2f(103f, 0.0001f)); // right
		ccwSortedVectors.add(new Vec2f(1111111f, 1111111f)); // top right
		ccwSortedVectors.add(new Vec2f(0.2f, 100f)); // top, right from zero

		cwSortedVectors = new ArrayList<>(ccwSortedVectors);
		Collections.reverse(cwSortedVectors);
		cwSortedVectors.add(0, zero);
		ccwSortedVectors.add(0, zero);

		mixedVectors = new ArrayList<>(ccwSortedVectors);
		Collections.shuffle(mixedVectors);
	}

	@Test
	public void testSort() {
		Queue<Vec2f> sortedCW = Geometry.sortClockwiseAsc(zero, mixedVectors, true);
		Queue<Vec2f> sortedCCW = Geometry.sortClockwiseAsc(zero, mixedVectors, false);

		// Vec2f bottom = Vec2f.mul(zero, -1);
		// float dot = Vec2f.dot(zero, bottom) / (zero.len() * bottom.len());
		// double alpha = Math.acos(dot);
		// System.out.println(dot);
		// if (Direction.RIGHT == Geometry.calcCurveDirection(new Vec2f(), zero,
		// Vec2f.add(zero, bottom)))
		// alpha = 2 * Math.PI - alpha;

		int idx = 0;
		while (!sortedCW.isEmpty() && !sortedCCW.isEmpty()) {
			System.out.println(sortedCCW.peek());
			assertEquals(ccwSortedVectors.get(idx), sortedCCW.poll());
			assertEquals(cwSortedVectors.get(idx), sortedCW.poll());
			idx++;
		}
	}
}