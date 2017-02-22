package utils.collections;

import microtrafficsim.build.BuildSetup;
import microtrafficsim.math.random.distributions.impl.Random;
import microtrafficsim.utils.collections.skiplist.PrioritySkipList;
import microtrafficsim.utils.collections.skiplist.SkipList;
import microtrafficsim.utils.logging.EasyMarkableLogger;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;

import java.util.*;

import static org.junit.Assert.*;

/**
 * Tests implementations of {@link SkipList}
 *
 * @author Dominic Parga Cacheiro
 */
public class TestPrioritySkipList implements TestSkipList {

    private static final Logger logger = new EasyMarkableLogger(TestPrioritySkipList.class);
    static {
        BuildSetup.DEBUG_ENABLED = true;
    }

    private final Random      random    = new Random(8685533621925346L);
    private final int         fillCount = 1000;
    private SkipList<Integer> skipList;

    @Before
    public void prepareForTest() {
        resetAttributes();
    }

    private void resetAttributes() {
        random.reset();
        skipList = new PrioritySkipList<>();
        logger.debug("skip list seed = " + skipList.getSeed());
    }

    @SafeVarargs
    private final void addRandomly(int n, Collection<Integer>... collections) {
        for (int i = 0; i < n; i++) {
            int next = random.nextInt();
            for (Collection<Integer> c : collections)
                c.add(next);
        }
    }

    /*
    |============|
    | test cases |
    |============|
    */
    @Override
    @Test
    public void testGetIndex() {

        /* check before adding */
        for (int i = 0; i < fillCount; i++)
            assertEquals(null, skipList.get(i));

        /* add */
        List<Integer> expected = new ArrayList<>();
        List<Integer> remain   = new ArrayList<>();
        List<Integer> remove   = new ArrayList<>();
        int halfFillCount = fillCount / 2;
        addRandomly(halfFillCount, skipList, expected, remain);
        addRandomly(fillCount - halfFillCount, skipList, expected, remove);

        /* check after adding, before moving */
        expected.sort(Integer::compareTo);
        for (int index = 0; index < expected.size(); index++) {
            Integer i = expected.get(index);
            assertEquals(i, skipList.get(index));
            assertEquals(i, skipList.get(index + expected.size()));
            assertEquals(i, skipList.get(index - expected.size()));
        }

        /* remove */
        for (Integer i : remove)
            skipList.remove(i);

        /* check after removing */
        remain.sort(Integer::compareTo);
        for (Integer i : remain)
            assertEquals(i, skipList.get(i));
    }

    @Override
    @Test
    public void testGetObj() {

        /* check before adding */
        for (int i = 0; i < fillCount; i++)
            assertEquals(null, skipList.get((Integer) i));

        /* add */
        List<Integer> expected = new ArrayList<>();
        List<Integer> remain   = new ArrayList<>();
        List<Integer> remove   = new ArrayList<>();
        int halfFillCount = fillCount / 2;
        addRandomly(halfFillCount, skipList, expected, remain);
        addRandomly(fillCount - halfFillCount, skipList, expected, remove);

        /* check after adding, before moving */
        expected.sort(Integer::compareTo);
        for (Integer i : expected)
            assertEquals(i, skipList.get(i));

        /* remove */
        for (Integer i : remove)
            skipList.remove(i);

        /* check after removing */
        remain.sort(Integer::compareTo);
        for (Integer i : remain)
            assertEquals(i, skipList.get(i));
    }

    @Override
    @Test
    public void testAddObj() {
        testGetObj();
    }

    @Override
    @Test
    public void testOfferObj() {
        List<Integer> expected = new ArrayList<>();
        addRandomly(fillCount, expected);
        for (int i : expected)
            skipList.offer(i);

        expected.sort(Integer::compareTo);
        for (Integer i : expected)
            assertEquals(i, skipList.get(i));
    }

    @Override
    @Test
    public void testRemove() {

        List<Integer> expected = new ArrayList<>();
        addRandomly(fillCount, skipList, expected);
        expected.sort(Integer::compareTo);

        while (!expected.isEmpty()) {
            int expectedRemove = expected.remove(0);
            int actualRemove   = skipList.remove();
            assertEquals(expectedRemove, actualRemove);
            checkForWeakEquality(expected, skipList);
        }

        try {
            skipList.remove();
            fail(msgNoSuchElementException("remove()", true));
        } catch (NoSuchElementException ignored) {}
    }

    @Override
    @Test
    public void testRemoveObj() {

        List<Integer> expected = new ArrayList<>();
        addRandomly(fillCount, skipList, expected);
        expected.sort(Integer::compareTo);

        while (!expected.isEmpty()) {
            int rdmIdx = random.nextInt(expected.size());
            Integer removed = expected.remove(rdmIdx);
            assertTrue(skipList.remove(removed));
            checkForWeakEquality(expected, skipList);
        }
    }

    @Override
    @Test
    public void testRemoveIndex() {

        List<Integer> expected = new ArrayList<>();
        addRandomly(fillCount, skipList, expected);
        expected.sort(Integer::compareTo);

        while (!expected.isEmpty()) {
            int rdmIdx = random.nextInt(expected.size());
            int removed = expected.remove(rdmIdx);
            assertTrue(skipList.remove(removed));
            checkForWeakEquality(expected, skipList);
        }
    }

    @Override
    @Test
    public void testPoll() {

        List<Integer> expected = new ArrayList<>();
        addRandomly(fillCount, skipList, expected);
        expected.sort(Integer::compareTo);

        while (!expected.isEmpty()) {
            int expectedRemove = expected.remove(0);
            int actualRemove   = skipList.poll();
            assertEquals(expectedRemove, actualRemove);
            checkForWeakEquality(expected, skipList);
        }

        try {
            assertEquals(null, skipList.poll());
        } catch (Exception ignored) {
            fail(msgNoSuchElementException("poll()", false));
        }
    }

    @Override
    @Test
    public void testElement() {

        try {
            assertEquals(null, skipList.element());
            fail(msgNoSuchElementException("element()", true));
        } catch (Exception ignored) {
        }

        PriorityQueue<Integer> expected = new PriorityQueue<>();
        for (int i = 0; i < fillCount; i++) {
            int next = random.nextInt();
            expected.add(next);
            skipList.add(next);
            assertEquals(expected.element(), skipList.element());
            assertEquals(expected.size(), skipList.size());
        }
    }

    @Override
    @Test
    public void testPeek() {

        try {
            assertEquals(null, skipList.peek());
        } catch (Exception ignored) {
            fail(msgNoSuchElementException("peek()", false));
        }

        PriorityQueue<Integer> expected = new PriorityQueue<>();
        for (int i = 0; i < fillCount; i++) {
            int next = random.nextInt();
            expected.add(next);
            skipList.add(next);
            assertEquals(expected.peek(), skipList.peek());
            assertEquals(expected.size(), skipList.size());
        }
    }

    @Override
    @Test
    public void testSize() {

        /* check size after adding */
        for (int i = 0; i < fillCount; i++) {
            assertEquals(i, skipList.size());
            skipList.add(i);
        }
        assertEquals(fillCount, skipList.size());
        skipList.clear();


        /* check size after clearing */
        for (int i = 0; i < fillCount; i++) {
            assertEquals(i, skipList.size());
            skipList.add(i);
        }


        /* check size after removing */
        assertEquals(fillCount, skipList.size());
        for (int i = fillCount - 1; i > fillCount / 2; i--) {
            skipList.remove(i);
            assertEquals(i, skipList.size());
        }
        for (int i = fillCount / 2; i >= 0; i--) {
            skipList.remove((Integer) i);
            assertEquals(i, skipList.size());
        }
    }

    @Override
    @Test
    public void testIsEmpty() {

        assertTrue(skipList.isEmpty());

        for (int i = 0; i < fillCount; i++) {
            skipList.add(random.nextInt());
            assertFalse(skipList.isEmpty());
        }

        skipList.clear();
        assertTrue(skipList.isEmpty());
    }

    @Override
    @Test
    public void testContainsObj() {

        assertFalse(skipList.contains(fillCount));

        for (int i = 0; i < fillCount; i++) {
            int next = random.nextInt();
            skipList.add(next);
            assertTrue(skipList.contains(next));
            assertFalse(skipList.contains(fillCount));
        }

        assertFalse(skipList.contains(fillCount));
    }

    @Override
    @Test
    public void testIterator() {

        List<Integer> expected = new ArrayList<>();
        addRandomly(fillCount, skipList, expected);
        expected.sort(Integer::compareTo);

        Iterator<Integer> expectedIterator = expected.iterator();
        Iterator<Integer> queueSetIterator = skipList.iterator();
        while (expectedIterator.hasNext()) {
            assertTrue(queueSetIterator.hasNext());
            assertEquals(expectedIterator.next(), queueSetIterator.next());
        }

        assertFalse(queueSetIterator.hasNext());
    }

    @Override
    @Test
    public void testToArrayWithoutParameter() {

        List<Integer> expected = new ArrayList<>();
        addRandomly(fillCount, skipList, expected);
        expected.sort(Integer::compareTo);

        Object[] expectedArray = expected.toArray();
        Object[] queueSetArray = skipList.toArray();
        checkForStrongEquality(expectedArray, queueSetArray);
    }

    @Override
    @Test
    public void testToArrayWithParameter() {

        List<Integer> expected = new ArrayList<>();
        addRandomly(fillCount, skipList, expected);
        expected.sort(Integer::compareTo);

        Integer[] expectedArray = new Integer[expected.size()];
        expectedArray = expected.toArray(expectedArray);
        Integer[] queueSetArray;


        for (int i = -1; i < 2; i++) {
            queueSetArray = new Integer[fillCount + i * fillCount / 2];
            queueSetArray = skipList.toArray(queueSetArray);
            if (i == 1)
                checkForWeakEquality(expectedArray, queueSetArray, expectedArray.length);
            else
                checkForStrongEquality(expectedArray, queueSetArray);
        }
    }

    @Override
    @Test
    public void testContainsAll() {

        List<Integer> containedList    = new ArrayList<>();
        List<Integer> notContainedList = new ArrayList<>();
        int halfFillCount = fillCount / 2;

        addRandomly(halfFillCount, containedList, skipList);
        addRandomly(random.nextInt(fillCount), notContainedList);

        assertTrue(skipList.containsAll(containedList));
        assertFalse(skipList.containsAll(notContainedList));

        addRandomly(fillCount - halfFillCount, skipList);
        assertTrue(skipList.containsAll(containedList));
        assertFalse(skipList.containsAll(notContainedList));
    }

    @Override
    @Test
    public void testAddAll() {

        // one list contains all integer that should be added to the queueset
        List<Integer> addingList = new ArrayList<>();
        List<Integer> expected = new ArrayList<>();
        // remember list state to guarantee calling removeAll() does not change the list
        List<Integer> addingBackup = new ArrayList<>();

        int halfFillCount = fillCount / 2;

        // this loop is important to guarantee that after calling removeAll, adding is still correct (hence a
        // data structure can sort its elements)
        for (int k = 0; k < 2; k++) {
            // empty filled lists
            addingList.clear();
            addingBackup.clear();

            // fill lists
            addRandomly(halfFillCount, expected, skipList);
            addRandomly(fillCount - halfFillCount, addingList, addingBackup);
            checkIsNotEmpty(skipList);

            checkForWeakEquality(expected, skipList);

            expected.addAll(addingList);
            skipList.addAll(addingList);

            checkForWeakEquality(expected, skipList);
            checkForStrongEquality(addingBackup, addingList);
        }
    }

    @Override
    @Test
    public void testRemoveAll() {

        // one list contains all integer that should be removed from the queueset, the other list of remaining
        // integers is needed for testing after removing
        List<Integer> removingList = new ArrayList<>();
        List<Integer> remainingList = new ArrayList<>();
        // remember list state to guarantee calling removeAll() does not change the list
        List<Integer> removingListBackup = new ArrayList<>();

        int halfFillCount = fillCount / 2;

        // this loop is important to guarantee that after calling removeAll, adding is still correct (hence a
        // data structure can sort its elements)
        for (int k = 0; k < 2; k++) {
            // empty filled lists
            removingList.clear();
            removingListBackup.clear();

            // fill lists
            addRandomly(halfFillCount, removingList, removingListBackup, skipList);
            addRandomly(fillCount - halfFillCount, remainingList, skipList);
            checkIsNotEmpty(skipList);

            assertEquals(removingList.size() + remainingList.size(), skipList.size());
            skipList.removeAll(removingList);

            checkForWeakEquality(remainingList, skipList);
            checkForStrongEquality(removingListBackup, removingList);
        }
    }

    @Override
    @Test
    public void testRetainAll() {

        List<Integer> list = new ArrayList<>();
        // remember list state to guarantee calling removeAll() does not change the list
        List<Integer> backup = new ArrayList<>();
        int halfFillCount = fillCount / 2;

        // this loop is important to guarantee that after calling retainAll, adding is still correct (hence a
        // data structure can sort its elements)
        for (int k = 0; k < 2; k++) {
            // fill both lists, but fill skipList with more elements
            // note: due to randomness, a bad seed could add two identical numbers
            addRandomly(halfFillCount, list, backup, skipList);
            addRandomly(fillCount - halfFillCount, skipList);
            assertFalse(list.size() == skipList.size());

            // call retain and check for equal elements
            skipList.retainAll(list);
            checkForWeakEquality(list, skipList);
            checkForStrongEquality(backup, list);
        }
    }

    @Override
    @Test
    public void testClear() {

        skipList.clear();
        checkIsEmpty(skipList);

        addRandomly(fillCount, skipList);
        checkIsNotEmpty(skipList);

        skipList.clear();
        checkIsEmpty(skipList);
    }

    @Override
    @Test
    public void testEquals() {

        /* "self" */
        SkipList<Integer> expected = new PrioritySkipList<>();
        assertEquals(expected, skipList);

        addRandomly(fillCount, expected, skipList);
        assertEquals(expected, skipList);


        /* others */
        subTestNotEquals(new LinkedList<>());
        subTestNotEquals(new HashSet<>());
        subTestNotEquals(new PriorityQueue<>());
    }

    @Override
    @Test
    public void testForNoDoubles() {

        Set<Integer> correctSet = new HashSet<>();
        for (int i = 0; i < fillCount; i++) {
            skipList.add(i);
            skipList.add(i);
            correctSet.add(i);
        }

        checkForWeakEquality(correctSet, skipList);
    }

    /*
    |=======|
    | utils |
    |=======|
    */
    /**
     * Checks whether the given collection is empty. Because {@code isEmpty()} and {@code size()} has to be checked
     * itself, this method asserts true if at least one of both conditions is true ({@code isEmpty()} or
     * {@code size() == 0}).
     */
    private void checkIsEmpty(Collection<?> collection) {
        assertTrue(collection.isEmpty() || collection.size() == 0);
    }

    /**
     * Checks whether the given collection is not empty. Because {@code isEmpty()} and {@code size()} has to be checked
     * itself, this method asserts true if at least one of both conditions is true ({@code !isEmpty()} or
     * {@code size() > 0}).
     */
    private void checkIsNotEmpty(Collection<?> collection) {
        assertTrue(!collection.isEmpty() || collection.size() > 0);
    }

    /**
     * Checks for same size and whether all all elements in {@code original} are contained in {@code copy}.
     */
    private void checkForWeakEquality(Collection<?> expected, Collection<?> actual) {
        assertEquals(expected.size(), actual.size());
        assertTrue(actual.containsAll(expected));
        assertTrue(expected.containsAll(actual)); // needed
    }

    /**
     * Checks with {@link Object#equals(Object)}, checks for same object indices and checks for same size.
     */
    private void checkForStrongEquality(List<Integer> expected, List<Integer> actual) {
        assertEquals(expected.size(), actual.size());
        for (int i = 0; i < expected.size(); i++)
            assertEquals(expected.get(i), actual.get(i));
    }

    /**
     * Checks with {@link Object#equals(Object)} and checks for same object indices.
     */
    private void checkForWeakEquality(Object[] expected, Object[] actual, int maxIdx) {
        for (int i = 0; i < maxIdx; i++)
            assertEquals(expected[i], actual[i]);
    }

    /**
     * Checks with {@link Object#equals(Object)}, checks for same object indices and checks for same length.
     */
    private void checkForStrongEquality(Object[] expected, Object[] actual) {
        assertEquals(expected.length, actual.length);
        for (int i = 0; i < expected.length; i++)
            assertEquals(expected[i], actual[i]);
    }

    private <C extends Collection<Integer>> void subTestNotEquals(C collection) {
        resetAttributes();
        assertNotEquals(skipList, collection);

        addRandomly(fillCount, collection, skipList);
        assertNotEquals(skipList, collection);
    }

    private String msgNoSuchElementException(String methodName, boolean exceptionExpected) {

        String msg = "SkipList does ";
        if (exceptionExpected)
            msg += "not ";
        msg += "throw exception calling " + methodName + " when empty.";

        return msg;
    }
}