package microtrafficsim.utils.collections;

import microtrafficsim.math.random.distributions.impl.Random;

import java.util.*;

/**
 * <p>
 * The difference to a {@link PrioritySkipList} is the fact that this class checks duplicates only by their hashcode.
 * This class uses a skip list as priority queue (sorting the objects by their priority + hashcode).
 * For finding a duplicate only by its hashcode, this class uses a {@link HashMap} to remember an object stored in the
 * skip list only by its hashcode. Thus if you change the priority or hashcode of an object in the queue, it could
 * not be found anymore.
 *
 * <p>Important note: <br>
 * Hence this class uses a {@link HashMap}, {@link Object#equals(Object)} is used in case two objects has two
 * identical hashcodes. Be careful <b>not</b> to use the object's priority to calculate {@code equality}; otherwise
 * this class cannot detect objects correctly.
 *
 * <p>
 * This implementation is not thread-safe.
 *
 * @author Dominic Parga Cacheiro
 */
public class PrioritySkipListSet<E> implements SkipList<E> {
    private PrioritySkipList<E> priorityQueue;
    private TreeMap<E, E>  storedValues;


    /**
     * Calls {@link #PrioritySkipListSet(long, Comparator) PrioritySkipList(Random.createSeed(), null)}
     */
    public PrioritySkipListSet() {
        priorityQueue = new PrioritySkipList<>();
        storedValues  = new TreeMap<>();
    }

    /**
     Calls {@link #PrioritySkipListSet(long, Comparator) PrioritySkipList(seed, null)}
     */
    public PrioritySkipListSet(long seed) {
        priorityQueue = new PrioritySkipList<>(seed);
        storedValues = new TreeMap<>();
    }

    /**
     * Calls {@link #PrioritySkipListSet(long, Comparator) PrioritySkipList(Random.createSeed(), comparator)}
     */
    public PrioritySkipListSet(Comparator<? super E> comparator) {
        priorityQueue = new PrioritySkipList<>(comparator);
        storedValues = new TreeMap<>(comparator);
    }

    /**
     * Default constructor.
     *
     * @param seed used for instance of {@link Random}
     * @param comparator for the priority; if null, this list uses the element's {@link Comparable natural order}
     *
     * @see Random#createSeed()
     */
    public PrioritySkipListSet(long seed, Comparator<? super E> comparator) {
        priorityQueue = new PrioritySkipList<>(seed, comparator);
        storedValues = new TreeMap<>(comparator);
    }


    @Override
    public long getSeed() {
        return priorityQueue.getSeed();
    }

    /**
     * @return the element at the specified index regarding the priority queue
     */
    @Override
    public E get(int index) {
        return priorityQueue.get(index);
    }

    /**
     * @return the removed element at the specified index regarding the priority queue
     */
    @Override
    public E remove(int index) {
        E e = priorityQueue.remove(index);
        storedValues.remove(e);
        return e;
    }

    /**
     * @return The object stored in the priority queue with hashcode equal to the given object.
     */
    @Override
    public E get(Object obj) {
        return priorityQueue.get(storedValues.get(obj));
    }

    @Override
    public Iterator<E> iteratorAsc() {
        return priorityQueue.iteratorAsc();
    }

    @Override
    public Iterator<E> iteratorDesc() {
        return priorityQueue.iteratorDesc();
    }

    /**
     * Checks whether the given object is in this collection (via hashcode). If so, the old value is replaced by the
     * given one.
     */
    @Override
    public boolean add(E e) {

        // get old value and remove it
        E storedValue = storedValues.get(e);
        if (storedValue != null)
            priorityQueue.remove(storedValue);

        // add new value
        storedValues.put(e, e);
        return priorityQueue.add(e);
    }

    @Override
    public boolean offer(E e) {
        return add(e);
    }

    @Override
    public E remove() {
        E storedValue = priorityQueue.remove();
        storedValues.remove(storedValue);
        return storedValue;
    }

    @Override
    public E poll() {
        E storedValue = priorityQueue.poll();
        if (storedValue != null)
            storedValues.remove(storedValue);
        return storedValue;
    }

    @Override
    public E element() {
        return priorityQueue.element();
    }

    @Override
    public E peek() {
        return priorityQueue.peek();
    }

    @Override
    public int size() {
        return priorityQueue.size();
    }

    @Override
    public boolean isEmpty() {
        return priorityQueue.isEmpty();
    }

    /**
     * Checks whether the given object is contained in this collection using only the object's hashcode.
     */
    @Override
    public boolean contains(Object obj) {
        return storedValues.containsKey(obj);
    }

    @Override
    public Iterator<E> iterator() {
        return priorityQueue.iterator();
    }

    @Override
    public Object[] toArray() {
        return priorityQueue.toArray();
    }

    @Override
    public <T> T[] toArray(T[] a) {
        return priorityQueue.toArray(a);
    }

    /**
     * Removes the given object by comparing only the object's hashcode.
     */
    @Override
    public boolean remove(Object obj) {
        E storedValue = storedValues.remove(obj);
        return priorityQueue.remove(storedValue);
    }

    /**
     * Retains all objects in this collection depending only on their hashcodes
     */
    @Override
    public boolean retainAll(Collection<?> collection) {

        int oldSize = size();

        // update priority queue
        priorityQueue.clear();
        for (Object obj : collection)
            priorityQueue.add(storedValues.get(obj));

        // update set
        storedValues.clear();
        for (E e : priorityQueue)
            storedValues.put(e, e);

        return priorityQueue.size() != oldSize;
    }

    @Override
    public void clear() {
        priorityQueue.clear();
        storedValues.clear();
    }
}
