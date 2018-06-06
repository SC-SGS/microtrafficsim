package logic.crossinglogic.automatic;

import microtrafficsim.core.logic.nodes.IndicesCalculator;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;


/**
 * This class tests {@link IndicesCalculator#leftmostIndexInMatching(byte, byte, byte, byte, byte)}
 *
 * @author Dominic Parga Cacheiro
 */
public class TestLeftmostIndex {
    // String s1, s2, d, e;
    private byte origin_s1, destination_s1, origin_s2, destination_s2, origin_d, destination_d, origin_e, destination_e,
            indicesPerNode, origin_wtf, destination_wtf;

    @Before
    public void setup() {
        // s1 = "234";
        // s2 = "3401";
        // d = "01";
        // e = "40";
        // wtf = "340";

        origin_s1      = 2;
        destination_s1 = 4;
        origin_s2      = 3;
        destination_s2 = 1;
        origin_d       = 0;
        destination_d  = 1;
        origin_e       = 4;
        destination_e  = 0;
        indicesPerNode = 5;

        origin_wtf      = 3;
        destination_wtf = 0;
    }

    @Test
    public void testMethod() {

        assertEquals(origin_s2, IndicesCalculator.leftmostIndexInMatching(origin_s1, destination_s1, origin_s2,
                                                                          destination_s2, indicesPerNode));
        assertEquals(origin_s2, IndicesCalculator.leftmostIndexInMatching(origin_s2, destination_s2, origin_s1,
                                                                          destination_s1, indicesPerNode));
        assertEquals(IndicesCalculator.NO_MATCH,
                     IndicesCalculator.leftmostIndexInMatching(origin_s1, destination_s1, origin_d, destination_d,
                                                               indicesPerNode));
        assertEquals(IndicesCalculator.NO_MATCH,
                     IndicesCalculator.leftmostIndexInMatching(origin_d, destination_d, origin_s1, destination_s1,
                                                               indicesPerNode));
        assertEquals(origin_e, IndicesCalculator.leftmostIndexInMatching(origin_s1, destination_s1, origin_e,
                                                                         destination_e, indicesPerNode));
        assertEquals(origin_e, IndicesCalculator.leftmostIndexInMatching(origin_e, destination_e, origin_s1,
                                                                         destination_s1, indicesPerNode));

        assertEquals(origin_d, IndicesCalculator.leftmostIndexInMatching(origin_s2, destination_s2, origin_d,
                                                                         destination_d, indicesPerNode));
        assertEquals(origin_d, IndicesCalculator.leftmostIndexInMatching(origin_d, destination_d, origin_s2,
                                                                         destination_s2, indicesPerNode));
        assertEquals(origin_e, IndicesCalculator.leftmostIndexInMatching(origin_s2, destination_s2, origin_e,
                                                                         destination_e, indicesPerNode));
        assertEquals(origin_e, IndicesCalculator.leftmostIndexInMatching(origin_e, destination_e, origin_s2,
                                                                         destination_s2, indicesPerNode));

        assertEquals(origin_d, IndicesCalculator.leftmostIndexInMatching(origin_d, destination_d, origin_e,
                                                                         destination_e, indicesPerNode));
        assertEquals(origin_d, IndicesCalculator.leftmostIndexInMatching(origin_e, destination_e, origin_d,
                                                                         destination_d, indicesPerNode));

        // wtf
        assertEquals(origin_e, IndicesCalculator.leftmostIndexInMatching(origin_e, destination_e, origin_wtf,
                                                                         destination_wtf, indicesPerNode));
        assertEquals(origin_e, IndicesCalculator.leftmostIndexInMatching(origin_wtf, destination_wtf, origin_e,
                                                                         destination_e, indicesPerNode));
    }
}
