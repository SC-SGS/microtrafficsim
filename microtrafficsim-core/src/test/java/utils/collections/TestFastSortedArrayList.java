package utils.collections;

import microtrafficsim.math.random.distributions.impl.Random;
import microtrafficsim.utils.collections.FastSortedArrayList;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;

import static junit.framework.TestCase.assertFalse;
import static org.junit.Assert.assertEquals;

/**
 * @author Dominic Parga Cacheiro
 */
public class TestFastSortedArrayList {
    // todo
    private final int count = 1000;
    private final Random random = new Random(42);

    private FastSortedArrayList<Integer> actualList;
    private ArrayList<Integer> expectedList = new ArrayList<>(count);


    @Before
    public void init() {
        actualList = new FastSortedArrayList<>();
        expectedList.clear();
        random.reset();
    }

    @Test
    public void testSortOrder() {
        for (int i = 0; i < count; i++) {
            int next = random.nextInt();
            actualList.add(next);
            expectedList.add(next);
        }

        actualList.sort();
        Collections.sort(expectedList);
        for (int i = 0; i < expectedList.size(); i++) {
            assertEquals("Wrong order after filling and sorting.", expectedList.get(i), actualList.get(i));
        }
    }


    @Test
    public void testSortOrderWhenIterating() {
        for (int i = 0; i < count; i++) {
            int next = random.nextInt();
            actualList.add(next);
            expectedList.add(next);
        }

        actualList.sort();
        Collections.sort(expectedList);
        Iterator<Integer> expectedIter = expectedList.iterator();
        for (Integer i : actualList) {
            assertEquals("Wrong order when iterating.", expectedIter.next(), i);
        }

        assertFalse("The tested list does not contain enough elements.", expectedIter.hasNext());
    }

    @Test
    public void testAscIter() {
        for (int i = 0; i < count; i++) {
            int next = random.nextInt();
            actualList.add(next);
            expectedList.add(next);
        }

        actualList.sort();
        Collections.sort(expectedList);

        Iterator<Integer> ascIter = actualList.iteratorAsc();
        Iterator<Integer> expectedIter = expectedList.iterator();
        while (expectedIter.hasNext()) {
            assertEquals("Wrong order when iterating.", expectedIter.next(), ascIter.next());
        }

        assertFalse("The tested list does not contain enough elements.", expectedIter.hasNext());
    }

    @Test
    public void testDescIter() {
        for (int i = 0; i < count; i++) {
            int next = random.nextInt();
            actualList.add(next);
            expectedList.add(next);
        }

        actualList.sort();
        Collections.sort(expectedList);
        Collections.reverse(expectedList);

        Iterator<Integer> descIter = actualList.iteratorDesc();
        Iterator<Integer> expectedIter = expectedList.iterator();
        while (expectedIter.hasNext()) {
            assertEquals("Wrong order when iterating.", expectedIter.next(), descIter.next());
        }

        assertFalse("The tested list does not contain enough elements.", expectedIter.hasNext());
    }
}