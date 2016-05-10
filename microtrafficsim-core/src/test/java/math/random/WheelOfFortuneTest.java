package math.random;

import microtrafficsim.core.map.Coordinate;
import microtrafficsim.math.HaversineDistanceCalculator;
import microtrafficsim.math.random.WheelOfFortune;
import org.junit.Test;

import static org.junit.Assert.assertEquals;


/**
 * Test for {@link WheelOfFortune}.
 *
 * big TODO
 *
 * @author Dominic Parga Cacheiro
 */
public class WheelOfFortuneTest {

    @Test
    public void tinyDistanceTest() {

    }

    private void test(Coordinate a, Coordinate b, float distance, float error) {
        assertEquals(HaversineDistanceCalculator.getDistance(a, b), distance, distance * error);
    }
}
