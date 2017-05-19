package utils.collections;

import microtrafficsim.math.random.distributions.impl.Random;
import microtrafficsim.utils.collections.PrioritySkipList;
import microtrafficsim.utils.collections.QueueSet;
import microtrafficsim.utils.collections.SkipList;
import microtrafficsim.utils.logging.EasyMarkableLogger;
import microtrafficsim.utils.logging.LoggingLevel;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;

import java.util.*;
import java.util.function.Supplier;

import static org.junit.Assert.*;

/**
 * Tests implementations of {@link SkipList}
 *
 * @author Dominic Parga Cacheiro
 */
public abstract class AbstractTestQueueSet implements TestQueueSet {

    private static final Logger logger = new EasyMarkableLogger(TestPrioritySkipList.class);
    static {
        LoggingLevel.setEnabledGlobally(false, true, true, true, true);
    }

    private final Random                random    = new Random();
    protected final int                 fillCount = 1000;
    protected QueueSet<Integer>         queueSet;
    private Supplier<QueueSet<Integer>> queueSetFactory;


    public AbstractTestQueueSet(Supplier<QueueSet<Integer>> queueSetFactory) {
        this.queueSetFactory = queueSetFactory;
    }


    @Before
    public void prepareForTest() {
        resetAttributes();
    }

    private void resetAttributes() {
        random.reset();
        queueSet = queueSetFactory.get();
        logger.debug("random seed    = " + random.getSeed());
        if (queueSet instanceof SkipList)
            logger.debug("skip list seed = " + ((SkipList) queueSet).getSeed());
    }

    @SafeVarargs
    protected final void addRandomly(int n, Collection<Integer>... collections) {
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
            assertEquals(null, queueSet.get(i));

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
            assertEquals(i, queueSet.get(index + expected.size()));
            assertEquals(i, queueSet.get(index - expected.size()));
        }

        /* remove */
        for (Integer i : remove)
            queueSet.remove(i);

        /* check after removing */
        remain.sort(Integer::compareTo);
        for (Integer i : remain)
            assertEquals(i, queueSet.get(i));
    }

    @Override
    @Test
    public void testGetObj() {

        /* check before adding */
        for (int i = 0; i < fillCount; i++)
            assertEquals(null, queueSet.get((Integer) i));

        /* add */
        List<Integer> expected = new ArrayList<>();
        List<Integer> remain   = new ArrayList<>();
        List<Integer> remove   = new ArrayList<>();
        int halfFillCount = fillCount / 2;
        addRandomly(halfFillCount, queueSet, expected, remain);
        addRandomly(fillCount - halfFillCount, queueSet, expected, remove);

        /* check after adding, before moving */
        expected.sort(Integer::compareTo);
        for (Integer i : expected)
            assertEquals(i, queueSet.get(i));

        /* remove */
        for (Integer i : remove)
            queueSet.remove(i);

        /* check after removing */
        remain.sort(Integer::compareTo);
        for (Integer i : remain)
            assertEquals(i, queueSet.get(i));
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
            queueSet.offer(i);

        expected.sort(Integer::compareTo);
        for (Integer i : expected)
            assertEquals(i, queueSet.get(i));
    }

    @Override
    @Test
    public void testRemove() {

        List<Integer> expected = new ArrayList<>();
        addRandomly(fillCount, queueSet, expected);
        expected.sort(Integer::compareTo);

        while (!expected.isEmpty()) {
            int expectedRemove = expected.remove(0);
            int actualRemove   = queueSet.remove();
            assertEquals(expectedRemove, actualRemove);
            checkForWeakEquality(expected, queueSet);
        }

        try {
            queueSet.remove();
            fail(msgNoSuchElementException("remove()", true));
        } catch (NoSuchElementException ignored) {}
    }

    @Override
    @Test
    public void testRemoveObj() {

        List<Integer> expected = new ArrayList<>();
        addRandomly(fillCount, queueSet, expected);
        expected.sort(Integer::compareTo);

        while (!expected.isEmpty()) {
            int rdmIdx = random.nextInt(expected.size());
            Integer removed = expected.remove(rdmIdx);
            assertTrue(queueSet.remove(removed));
            checkForWeakEquality(expected, queueSet);
        }
    }

    @Override
    @Test
    public void testRemoveIndex() {

        List<Integer> expected = new ArrayList<>();
        addRandomly(fillCount, queueSet, expected);
        expected.sort(Integer::compareTo);

        while (!expected.isEmpty()) {
            int rdmIdx = random.nextInt(expected.size());
            Integer removed = expected.remove(rdmIdx);
            assertEquals(removed, queueSet.remove(rdmIdx));
            checkForWeakEquality(expected, queueSet);
        }
    }

    @Override
    @Test
    public void testPoll() {

        List<Integer> expected = new ArrayList<>();
        addRandomly(fillCount, queueSet, expected);
        expected.sort(Integer::compareTo);

        while (!expected.isEmpty()) {
            int expectedRemove = expected.remove(0);
            int actualRemove   = queueSet.poll();
            assertEquals(expectedRemove, actualRemove);
            checkForWeakEquality(expected, queueSet);
        }

        try {
            assertEquals(null, queueSet.poll());
        } catch (Exception ignored) {
            fail(msgNoSuchElementException("poll()", false));
        }
    }

    @Override
    @Test
    public void testElement() {

        try {
            assertEquals(null, queueSet.element());
            fail(msgNoSuchElementException("element()", true));
        } catch (Exception ignored) {
        }

        PriorityQueue<Integer> expected = new PriorityQueue<>();
        for (int i = 0; i < fillCount; i++) {
            int next = random.nextInt();
            expected.add(next);
            queueSet.add(next);
            assertEquals(expected.element(), queueSet.element());
            assertEquals(expected.size(), queueSet.size());
        }
    }

    @Override
    @Test
    public void testPeek() {

        try {
            assertEquals(null, queueSet.peek());
        } catch (Exception ignored) {
            fail(msgNoSuchElementException("peek()", false));
        }

        PriorityQueue<Integer> expected = new PriorityQueue<>();
        for (int i = 0; i < fillCount; i++) {
            int next = random.nextInt();
            expected.add(next);
            queueSet.add(next);
            assertEquals(expected.peek(), queueSet.peek());
            assertEquals(expected.size(), queueSet.size());
        }
    }

    @Override
    @Test
    public void testSize() {

        /* check size after adding */
        for (int i = 0; i < fillCount; i++) {
            assertEquals(i, queueSet.size());
            queueSet.add(i);
        }
        assertEquals(fillCount, queueSet.size());
        queueSet.clear();


        /* check size after clearing */
        for (int i = 0; i < fillCount; i++) {
            assertEquals(i, queueSet.size());
            queueSet.add(i);
        }


        /* check size after removing */
        assertEquals(fillCount, queueSet.size());
        for (int i = fillCount - 1; i > fillCount / 2; i--) {
            queueSet.remove(i);
            assertEquals(i, queueSet.size());
        }
        for (int i = fillCount / 2; i >= 0; i--) {
            queueSet.remove((Integer) i);
            assertEquals(i, queueSet.size());
        }
    }

    @Override
    @Test
    public void testIsEmpty() {

        assertTrue(queueSet.isEmpty());

        for (int i = 0; i < fillCount; i++) {
            queueSet.add(random.nextInt());
            assertFalse(queueSet.isEmpty());
        }

        queueSet.clear();
        assertTrue(queueSet.isEmpty());
    }

    @Override
    @Test
    public void testContainsObj() {

        assertFalse(queueSet.contains(fillCount));

        for (int i = 0; i < fillCount; i++) {
            int next = random.nextInt();
            queueSet.add(next);
            assertTrue(queueSet.contains(next));
            assertFalse(queueSet.contains(fillCount));
        }

        assertFalse(queueSet.contains(fillCount));
    }

    @Override
    @Test
    public void testIterator() {

        List<Integer> expected = new ArrayList<>();
        addRandomly(fillCount, queueSet, expected);
        expected.sort(Integer::compareTo);

        Iterator<Integer> expectedIterator = expected.iterator();
        Iterator<Integer> queueSetIterator = queueSet.iterator();
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
        addRandomly(fillCount, queueSet, expected);
        expected.sort(Integer::compareTo);

        Object[] expectedArray = expected.toArray();
        Object[] queueSetArray = queueSet.toArray();
        checkForStrongEquality(expectedArray, queueSetArray);
    }

    @Override
    @Test
    public void testToArrayWithParameter() {

        List<Integer> expected = new ArrayList<>();
        addRandomly(fillCount, queueSet, expected);
        expected.sort(Integer::compareTo);

        Integer[] expectedArray = new Integer[expected.size()];
        expectedArray = expected.toArray(expectedArray);
        Integer[] queueSetArray;


        for (int i = -1; i < 2; i++) {
            queueSetArray = new Integer[fillCount + i * fillCount / 2];
            queueSetArray = queueSet.toArray(queueSetArray);
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

        addRandomly(halfFillCount, containedList, queueSet);
        addRandomly(random.nextInt(fillCount), notContainedList);

        assertTrue(queueSet.containsAll(containedList));
        assertFalse(queueSet.containsAll(notContainedList));

        addRandomly(fillCount - halfFillCount, queueSet);
        assertTrue(queueSet.containsAll(containedList));
        assertFalse(queueSet.containsAll(notContainedList));
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
            addRandomly(halfFillCount, expected, queueSet);
            addRandomly(fillCount - halfFillCount, addingList, addingBackup);
            checkIsNotEmpty(queueSet);

            checkForWeakEquality(expected, queueSet);

            expected.addAll(addingList);
            queueSet.addAll(addingList);

            checkForWeakEquality(expected, queueSet);
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
            addRandomly(halfFillCount, removingList, removingListBackup, queueSet);
            addRandomly(fillCount - halfFillCount, remainingList, queueSet);
            checkIsNotEmpty(queueSet);

            assertEquals(removingList.size() + remainingList.size(), queueSet.size());
            queueSet.removeAll(removingList);

            checkForWeakEquality(remainingList, queueSet);
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
            // fill both lists, but fill queueSet with more elements
            // note: due to randomness, a bad seed could add two identical numbers
            addRandomly(halfFillCount, list, backup, queueSet);
            addRandomly(fillCount - halfFillCount, queueSet);
            assertFalse(list.size() == queueSet.size());

            // call retain and check for equal elements
            queueSet.retainAll(list);
            checkForWeakEquality(list, queueSet);
            checkForStrongEquality(backup, list);
        }
    }

    @Override
    @Test
    public void testClear() {

        queueSet.clear();
        checkIsEmpty(queueSet);

        addRandomly(fillCount, queueSet);
        checkIsNotEmpty(queueSet);

        queueSet.clear();
        checkIsEmpty(queueSet);
    }

    @Override
    @Test
    public void testEquals() {

        /* "self" */
        QueueSet<Integer> expected = new PrioritySkipList<>();
        assertEquals(expected, queueSet);

        addRandomly(fillCount, expected, queueSet);
        assertEquals(expected, queueSet);


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
            queueSet.add(i);
            queueSet.add(i);
            correctSet.add(i);
        }

        checkForWeakEquality(correctSet, queueSet);
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
    protected void checkIsEmpty(Collection<?> collection) {
        assertTrue(collection.isEmpty() || collection.size() == 0);
    }

    /**
     * Checks whether the given collection is not empty. Because {@code isEmpty()} and {@code size()} has to be checked
     * itself, this method asserts true if at least one of both conditions is true ({@code !isEmpty()} or
     * {@code size() > 0}).
     */
    protected void checkIsNotEmpty(Collection<?> collection) {
        assertTrue(!collection.isEmpty() || collection.size() > 0);
    }

    /**
     * Checks for same size and whether all all elements in {@code original} are contained in {@code copy}.
     */
    protected void checkForWeakEquality(Collection<?> expected, Collection<?> actual) {
        assertEquals(expected.size(), actual.size());
        assertTrue(actual.containsAll(expected));
        assertTrue(expected.containsAll(actual)); // needed
    }

    /**
     * Checks with {@link Object#equals(Object)}, checks for same object indices and checks for same size.
     */
    protected void checkForStrongEquality(List<Integer> expected, List<Integer> actual) {
        assertEquals(expected.size(), actual.size());
        for (int i = 0; i < expected.size(); i++)
            assertEquals(expected.get(i), actual.get(i));
    }

    /**
     * Checks with {@link Object#equals(Object)} and checks for same object indices.
     */
    protected void checkForWeakEquality(Object[] expected, Object[] actual, int maxIdx) {
        for (int i = 0; i < maxIdx; i++)
            assertEquals(expected[i], actual[i]);
    }

    /**
     * Checks with {@link Object#equals(Object)}, checks for same object indices and checks for same length.
     */
    protected void checkForStrongEquality(Object[] expected, Object[] actual) {
        assertEquals(expected.length, actual.length);
        for (int i = 0; i < expected.length; i++)
            assertEquals(expected[i], actual[i]);
    }

    protected <C extends Collection<Integer>> void subTestEquals(C collection) {
        resetAttributes();
        assertEquals(queueSet, collection);

        addRandomly(fillCount, collection, queueSet);
        assertNotEquals(queueSet, collection);
    }

    protected <C extends Collection<Integer>> void subTestNotEquals(C collection) {
        resetAttributes();
        assertNotEquals(queueSet, collection);

        addRandomly(fillCount, collection, queueSet);
        assertNotEquals(queueSet, collection);
    }

    protected String msgNoSuchElementException(String methodName, boolean exceptionExpected) {

        String msg = "SkipList does ";
        if (exceptionExpected)
            msg += "not ";
        msg += "throw exception calling " + methodName + " when empty.";

        return msg;
    }
}