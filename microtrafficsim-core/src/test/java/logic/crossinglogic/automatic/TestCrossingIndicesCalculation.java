package logic.crossinglogic.automatic;

import microtrafficsim.core.logic.nodes.IndicesCalculator;
import org.junit.Test;

import static org.junit.Assert.assertEquals;


/**
 * This class tests {@link IndicesCalculator#areIndicesCrossing(byte, byte, byte, byte)}
 *
 * @author Dominic Parga Cacheiro
 */
public class TestCrossingIndicesCalculation {

    private final boolean crossing = true, not_crossing = false;
    private byte          origin1, destination1, origin2, destination2;

    /**
     * Ordinary crossroad with 4 roads (incoming and leaving, so 8
     * {@code DirectedEdge}s)
     */
    @Test
    public void testOrdinaryCrossroad() {
        // two left curves, not crossing
        origin1      = 2;
        destination1 = 7;
        origin2      = 6;
        destination2 = 3;
        assertEquals(
                not_crossing,
                IndicesCalculator.areIndicesCrossing(origin1, destination1, origin2, destination2));
        // same switched
        assertEquals(
                not_crossing,
                IndicesCalculator.areIndicesCrossing(origin2, destination2, origin1, destination1));

        // one left curve, one right curve, not crossing
        origin1      = 4;
        destination1 = 5;
        origin2      = 6;
        destination2 = 3;
        assertEquals(
                not_crossing,
                IndicesCalculator.areIndicesCrossing(origin1, destination1, origin2, destination2));
        // same switched
        assertEquals(
                not_crossing,
                IndicesCalculator.areIndicesCrossing(origin2, destination2, origin1, destination1));

        // one left curve, one right curve, crossing (same destination)
        origin1      = 6;
        destination1 = 7;
        origin2      = 2;
        destination2 = 7;
        assertEquals(
                crossing,
                IndicesCalculator.areIndicesCrossing(origin1, destination1, origin2, destination2));
        // same switched
        assertEquals(
                crossing,
                IndicesCalculator.areIndicesCrossing(origin2, destination2, origin1, destination1));

        // two right curves, not crossing
        origin1      = 6;
        destination1 = 7;
        origin2      = 2;
        destination2 = 3;
        assertEquals(
                not_crossing,
                IndicesCalculator.areIndicesCrossing(origin1, destination1, origin2, destination2));
        // same switched
        assertEquals(
                not_crossing,
                IndicesCalculator.areIndicesCrossing(origin2, destination2, origin1, destination1));

        // two straight, crossing
        origin1      = 0;
        destination1 = 3;
        origin2      = 6;
        destination2 = 1;
        assertEquals(
                crossing,
                IndicesCalculator.areIndicesCrossing(origin1, destination1, origin2, destination2));
        // same switched
        assertEquals(
                crossing,
                IndicesCalculator.areIndicesCrossing(origin2, destination2, origin1, destination1));

        // two straight, crossing
        origin1      = 0;
        destination1 = 3;
        origin2      = 2;
        destination2 = 5;
        assertEquals(
                crossing,
                IndicesCalculator.areIndicesCrossing(origin1, destination1, origin2, destination2));
        // same switched
        assertEquals(
                crossing,
                IndicesCalculator.areIndicesCrossing(origin2, destination2, origin1, destination1));

        // two straight, not crossing
        origin1      = 0;
        destination1 = 3;
        origin2      = 4;
        destination2 = 7;
        assertEquals(
                not_crossing,
                IndicesCalculator.areIndicesCrossing(origin1, destination1, origin2, destination2));
        // same switched
        assertEquals(
                not_crossing,
                IndicesCalculator.areIndicesCrossing(origin2, destination2, origin1, destination1));

        // two straight, not crossing
        origin1      = 2;
        destination1 = 5;
        origin2      = 6;
        destination2 = 1;
        assertEquals(
                not_crossing,
                IndicesCalculator.areIndicesCrossing(origin1, destination1, origin2, destination2));
        // same switched
        assertEquals(
                not_crossing,
                IndicesCalculator.areIndicesCrossing(origin2, destination2, origin1, destination1));

        // one straight, one right, crossing
        origin1      = 0;
        destination1 = 1;
        origin2      = 6;
        destination2 = 1;
        assertEquals(
                crossing,
                IndicesCalculator.areIndicesCrossing(origin1, destination1, origin2, destination2));
        // same switched
        assertEquals(
                crossing,
                IndicesCalculator.areIndicesCrossing(origin2, destination2, origin1, destination1));

        // one straight, one left, crossing
        origin1      = 4;
        destination1 = 1;
        origin2      = 6;
        destination2 = 1;
        assertEquals(
                crossing,
                IndicesCalculator.areIndicesCrossing(origin1, destination1, origin2, destination2));
        // same switched
        assertEquals(
                crossing,
                IndicesCalculator.areIndicesCrossing(origin2, destination2, origin1, destination1));
    }

    /**
     * Three roads => 6 {@code DirectedEdge}s
     */
    @Test
    public void testMergingCrossroad() {
        // one left curve, one right curve, crossing (same destination)
        origin1      = 0;
        destination1 = 3;
        origin2      = 2;
        destination2 = 3;
        assertEquals(
                crossing,
                IndicesCalculator.areIndicesCrossing(origin1, destination1, origin2, destination2));
        // same switched
        assertEquals(
                crossing,
                IndicesCalculator.areIndicesCrossing(origin2, destination2, origin1, destination1));

        // one straight, one left curve, crossing
        origin1      = 0;
        destination1 = 3;
        origin2      = 2;
        destination2 = 5;
        assertEquals(
                crossing,
                IndicesCalculator.areIndicesCrossing(origin1, destination1, origin2, destination2));
        // same switched
        assertEquals(
                crossing,
                IndicesCalculator.areIndicesCrossing(origin2, destination2, origin1, destination1));

        // two right curves, not crossing
        origin1      = 2;
        destination1 = 3;
        origin2      = 4;
        destination2 = 5;
        assertEquals(
                not_crossing,
                IndicesCalculator.areIndicesCrossing(origin1, destination1, origin2, destination2));
        // same switched
        assertEquals(
                not_crossing,
                IndicesCalculator.areIndicesCrossing(origin2, destination2, origin1, destination1));

        // one straight, one right curve, not crossing
        origin1      = 0;
        destination1 = 1;
        origin2      = 2;
        destination2 = 3;
        assertEquals(
                not_crossing,
                IndicesCalculator.areIndicesCrossing(origin1, destination1, origin2, destination2));
        // same switched
        assertEquals(
                not_crossing,
                IndicesCalculator.areIndicesCrossing(origin2, destination2, origin1, destination1));
    }
}