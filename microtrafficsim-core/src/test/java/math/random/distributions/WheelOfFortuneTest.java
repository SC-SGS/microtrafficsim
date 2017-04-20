package math.random.distributions;

import microtrafficsim.math.random.distributions.WheelOfFortune;
import microtrafficsim.math.random.distributions.impl.BasicWheelOfFortune;
import microtrafficsim.math.random.distributions.impl.Random;
import microtrafficsim.utils.Descriptor;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * This class tests {@link BasicWheelOfFortune}
 *
 * @author Dominic Parga Cacheiro
 */
public class WheelOfFortuneTest {
    private final int fillCount = 100;
    private final int checkCount = 10000;

    private WheelOfFortune<Descriptor<Integer>> wheelOfFortune;
    private final HashMap<Integer, Descriptor<Integer>> references;


    public WheelOfFortuneTest() {
        references = new HashMap<>();
    }


    @Before
    public void init() {
        wheelOfFortune = new BasicWheelOfFortune<>(new Random());
        references.clear();
    }

    @Test
    public void testAddAndSizeAndUpdate() {

        /* check if empty is correct */
        assertEquals(0, wheelOfFortune.size());
        assertTrue(wheelOfFortune.isEmpty());


        /* fill wheel with elements containing an unique description */
        long id = System.nanoTime();
        for (int weight = 1; weight <= fillCount; weight++) {

            Descriptor<Integer> descriptor = new Descriptor<>(weight, "" + (id++));
            wheelOfFortune.add(descriptor, weight);

            // remember the weight for this object
            references.put(weight, descriptor);

            assertEquals(weight, wheelOfFortune.size());
        }

        assertWheel();


        /* update all elements in the wheel with a weight equal to the old one plus an offset */
        for (int weight = 1; weight <= fillCount; weight++) {

            Descriptor<Integer> reference = references.get(weight);
            int newWeight = weight + fillCount;
            wheelOfFortune.update(reference, newWeight);

            assertEquals(fillCount, wheelOfFortune.size());
        }

        assertWheel();
    }

    public void testIncWeight() {
        // todo
    }

    public void testDecWeight() {
        // todo
    }

    public void testRemove() {
        // todo
    }

    public void testClear() {
        // todo
    }

    public void testIsEmpty() {
        // todo
    }

    public void testNextObject() {
        // todo
    }

    public void testNextObject1() {
        // todo
    }


    /*
    |=======|
    | utils |
    |=======|
    */
    private void assertWheel() {
        for (int i = 0; i < checkCount; i++) {
            Descriptor<Integer> actual = wheelOfFortune.nextObject();
            Descriptor<Integer> expected = references.get(actual.getObj());
            assertEquals(expected.getDescription(), actual.getDescription());
            assertEquals(expected, actual);
            assertTrue(expected == actual);
        }
    }
}