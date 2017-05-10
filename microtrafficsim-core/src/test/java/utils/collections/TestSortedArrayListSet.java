package utils.collections;

import microtrafficsim.utils.collections.QueueSet;
import microtrafficsim.utils.collections.SkipList;
import microtrafficsim.utils.collections.SortedArrayListSet;

import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Tests implementations of {@link SkipList}
 *
 * @author Dominic Parga Cacheiro
 */
public class TestSortedArrayListSet extends AbstractTestQueueSet {
    public TestSortedArrayListSet() {
        super(SortedArrayListSet::new);
    }

    @Override
    public void testEquals() {
        /* "self" */
        QueueSet<Integer> expected = new SortedArrayListSet<>();
        assertEquals(expected, queueSet);

        addRandomly(fillCount, expected, queueSet);
        assertEquals(expected, queueSet);


        /* others */
        subTestEquals(new LinkedList<>());
        subTestNotEquals(new HashSet<>());
        subTestNotEquals(new PriorityQueue<>());
    }

    @Override
    public void testGetIndex() {
        /* check before adding */
        checkIsEmpty(queueSet);

        /* add */
        List<Integer> expected = new ArrayList<>();
        List<Integer> remain   = new ArrayList<>();
        List<Integer> remove   = new ArrayList<>();
        int halfFillCount = fillCount / 2;
        addRandomly(halfFillCount, queueSet, expected, remain);
        addRandomly(fillCount - halfFillCount, queueSet, expected, remove);

        /* check after adding, before moving */
        expected.sort(Integer::compareTo);
        for (int index = 0; index < expected.size(); index++) {
            Integer i = expected.get(index);
            assertEquals(i, queueSet.get(index));

            try {
                queueSet.get(index + expected.size());
                assertTrue("Should throw " + IndexOutOfBoundsException.class.getSimpleName(), false);
            } catch (IndexOutOfBoundsException e) {
                assertTrue(true);
            }
            try {
                queueSet.get(index - expected.size());
                assertTrue("Should throw " + IndexOutOfBoundsException.class.getSimpleName(), false);
            } catch (IndexOutOfBoundsException e) {
                assertTrue(true);
            }
        }

        /* remove */
        for (Integer i : remove)
            queueSet.remove(i);

        /* check after removing */
        remain.sort(Integer::compareTo);
        for (Integer i : remain)
            assertEquals(i, queueSet.get(i));
    }
}