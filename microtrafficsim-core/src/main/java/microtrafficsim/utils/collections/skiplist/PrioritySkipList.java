package microtrafficsim.utils.collections.skiplist;

import microtrafficsim.math.random.distributions.impl.Random;
import microtrafficsim.utils.strings.builder.BasicStringBuilder;
import microtrafficsim.utils.strings.builder.StringBuilder;

import java.util.*;

/**
 * <p>
 * This implementation of {@code SkipList} is implemented as a sorted linked list (not by Java).
 * It compares elements using their {@link Comparable natural order} per default.
 *
 * <p>
 * This implementation is not thread-safe.
 *
 * @author Dominic Parga Cacheiro
 */
public class PrioritySkipList<E> implements SkipList<E> {

    // todo wikipedia says, the runtime complexity could be repaired in cases of O(n)
    // todo store link width for get(index) in O(logn)

    private Skipnode<E> head;
    private final Random                random;
    private final Comparator<? super E> comparator;
    private int                         size;

    /**
     * Calls {@link #PrioritySkipList(long, Comparator) PrioritySkipList(Random.createSeed(), null)}
     */
    public PrioritySkipList() {
        this(Random.createSeed(), null);
    }

    /**
     Calls {@link #PrioritySkipList(long, Comparator) PrioritySkipList(seed, null)}
     */
    public PrioritySkipList(long seed) {
        this(seed, null);
    }

    /**
     * Calls {@link #PrioritySkipList(long, Comparator) PrioritySkipList(Random.createSeed(), comparator)}
     */
    public PrioritySkipList(Comparator<? super E> comparator) {
        this(Random.createSeed(), comparator);
    }

    /**
     * Default constructor.
     *
     * @param seed used for instance of {@link Random}
     * @param comparator for the priority; if null, this list uses the element's {@link Comparable natural order}
     *
     * @see Random#createSeed()
     */
    public PrioritySkipList(long seed, Comparator<? super E> comparator) {
        clear();
        random          = new Random(seed);
        this.comparator = comparator;
    }

    @Override
    public String toString() {
        StringBuilder builder = new BasicStringBuilder();

        Iterator<Skipnode<E>> iterator = new AscendingNodeIterator(head);
        while (iterator.hasNext())
            builder.append(iterator.next());

        return builder.toString();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this)
            return true;


        if (!(obj instanceof SkipList))
            return false;

        Collection<?> c = (Collection<?>) obj;
        if (c.size() != size)
            return false;

        try {
            return containsAll(c);
        } catch (Exception unused) {
            return false;
        }
    }

    @Override
    public long getSeed() {
        return random.getSeed();
    }

    /**
     * @param index This index is allowed to be out of range and interpreted like out of range. E.g. -1 gives the last
     *              element of the list.
     * @return the stored value at the specified index; null, if the element is not contained in this list
     */
    @Override
    public E get(int index) {
        if (isEmpty())
            return null;
        // todo
        return null;
    }

    /**
     * @return the stored value equal to the given one using the defined comparator; null, if the element is not
     * contained in this list
     */
    @Override
    public E get(Object obj) {
        if (isEmpty())
            return null;
        Skipnode<E> infimum = findInfimumOf(obj);
        return equal(infimum.getValue(), obj) ? infimum.getValue() : null;
    }

    @Override
    public boolean add(E e) {

        if (e == null)
            return false;
        else {
            /* find position for new node and check whether it already is in this list */
            Skipnode<E> newNode = new Skipnode<>(e);
            Skipnode<E> infimum = findInfimumOf(e);

            // do nothing if element is already in this list
            if (infimum != head)
                if (equal(newNode.getValue(), infimum.getValue()))
                    return false;

            Skipnode<E> supremum = infimum.getNext();


            /* create and fill tower */
            int towerHeight = throwCoinUntilFalse();

            for (int towerLevel = 0; towerLevel <= towerHeight; towerLevel++) {

                /* set infimum's pointer */
                while (infimum.getTowerHeight() < towerLevel) {
                    // if head: increase tower height
                    if (infimum == head)
                        infimum.addToTower(head, head);
                    // if not head: jump to previous element
                    else
                        infimum = infimum.getPrev(towerLevel - 1);
                }
                infimum.setNext(towerLevel, newNode);


                /* set supremum's pointer */
                while (supremum.getTowerHeight() < towerLevel) {
                    // if head: increase tower height
                    if (supremum == head)
                        supremum.addToTower(head, head);
                    // if not head: jump to next element
                    else
                        supremum = supremum.getNext(towerLevel - 1);
                }
                supremum.setPrev(towerLevel, newNode);


                /* set new node's pointer */
                newNode.addToTower(infimum, supremum);
            }
        }

        size++;
        return true;
    }

    @Override
    public boolean offer(E e) {
        return add(e);
    }

    @Override
    public boolean remove(Object obj) {

        Skipnode<E> infimum = findInfimumOf(obj);
        if (infimum == head)
            return false;
        if (equal(infimum.getValue(), obj)) {
            removeExistingNode(infimum);
            return true;
        }
        return false;
    }

    @Override
    public E remove() {
        if (isEmpty())
            throw new NoSuchElementException(isEmptyMsg());
        return poll();
    }

    @Override
    public E poll() {
        Skipnode<E> first = head.getNext();
        removeExistingNode(first);
        return first.getValue();
    }

    @Override
    public E element() {
        if (isEmpty())
            throw new NoSuchElementException(isEmptyMsg());
        return head.getNext().getValue();
    }

    @Override
    public E peek() {
        if (isEmpty())
            return null;
        return head.getNext().getValue();
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public boolean isEmpty() {
        return size == 0;
    }

    @Override
    public boolean contains(Object obj) {
        Skipnode<E> infimum = findInfimumOf(obj);
        if (infimum == head)
            return false;
        return equal(infimum.getValue(), obj);
    }

    @Override
    public Iterator<E> iterator() {
        return iteratorAsc();
    }

    @Override
    public Iterator<E> iteratorAsc() {
        return new AscendingIterator(head);
    }

    @Override
    public Iterator<E> iteratorDesc() {
        return new DescendingIterator(head);
    }

    @Override
    public Object[] toArray() {
        ArrayList<Object> list = new ArrayList<>(size);
        for (Object obj : this)
            list.add(obj);
        return list.toArray();
    }

    @Override
    public <T> T[] toArray(T[] a) {
        ArrayList<Object> list = new ArrayList<>(size);
        for (Object obj : this)
            list.add(obj);
        return list.toArray(a);
    }

    /**
     * This method calls {@link ArrayList#retainAll(Collection)} and sorts the list afterwards by calling
     * {@link ArrayList#sort(Comparator)} using the comparator defined by this {@code QueueSet}.
     */
    @Override
    public boolean retainAll(Collection<?> collection) {

        // fill new list with all elements of me equal to an element of c
        PrioritySkipList<E> newList = new PrioritySkipList<>(getSeed(), comparator);
        for (Object obj : collection)
            newList.add(get(obj));

        boolean changed = newList.size != size;
        // update me
        this.head = newList.head;
        this.size = newList.size;

        return changed;
    }

    /**
     * Just resets the head node. All other nodes are still existing in memory. Garbage Collection does remove them.
     */
    @Override
    public void clear() {
        head = new Skipnode<>();
        head.addToTower(head, head);
        size = 0;
    }

    /*
    |=======|
    | utils |
    |=======|
    */
    private Skipnode<E> findInfimumOf(Object value) {

        int curTowerLevel = head.getTowerHeight();
        Skipnode<E> curNode = head;
        Skipnode<E> nextNode;

        // from highest level to bottom level
        while (curTowerLevel >= 0) {
            nextNode = curNode.getNext(curTowerLevel);
            if (nextNode == head) {
                curTowerLevel--;
                continue;
            }

            int cmp = compare(nextNode.getValue(), value);
            if (cmp > 0)
                curTowerLevel--;
            else if (cmp == 0)
                return nextNode;
            else
                curNode = nextNode;
        }

        return curNode;
    }

    @SuppressWarnings("unchecked")
    private int compare(Object o1, Object o2) {
        if (comparator != null)
            return comparator.compare((E) o1, (E) o2);

        Comparable<? super E> e1 = (Comparable<? super E>) o1;
        return e1.compareTo((E) o2);
    }

    private boolean equal(Object o1, Object o2) {
        return compare(o1, o2) == 0;
    }

    private int throwCoinUntilFalse() {

        int counter = 0;
        while (random.nextBoolean())
            counter++;
        return counter;
    }

    private void removeExistingNode(Skipnode<E> node) {

        for (int towerLevel = 0; towerLevel <= node.getTowerHeight(); towerLevel++) {
            Skipnode<E> next = node.getNext(towerLevel);
            Skipnode<E> prev = node.getPrev(towerLevel);
            prev.setNext(towerLevel, next);
            next.setPrev(towerLevel, prev);
        }

        cleanupHead();
        size--;
    }

    private void cleanupHead() {
        while (head.getTowerHeight() > 0 && head.getNext(head.getTowerHeight()) == head)
            head.removeHighest();
    }

    /*
    |====================|
    | exception messages |
    |====================|
    */
    private String isEmptyMsg() {
        return "This " + getClass().getSimpleName() + " is empty.";
    }

    /*
    |==========|
    | iterator |
    |==========|
    */
    private class AscendingIterator implements Iterator<E> {

        private Skipnode<E> currentNode;

        public AscendingIterator(Skipnode<E> startNode) {
            currentNode = startNode;
        }

        @Override
        public boolean hasNext() {
            return currentNode.getNext() != head;
        }

        @Override
        public E next() {
            currentNode = currentNode.getNext();
            return currentNode.getValue();
        }
    }

    private class DescendingIterator implements Iterator<E> {

        private Skipnode<E> currentNode;

        public DescendingIterator(Skipnode<E> startNode) {
            currentNode = startNode;
        }

        @Override
        public boolean hasNext() {
            return currentNode.getPrev() != head;
        }

        @Override
        public E next() {
            currentNode = currentNode.getPrev();
            return currentNode.getValue();
        }
    }

    private class AscendingNodeIterator implements Iterator<Skipnode<E>> {

        private Skipnode<E> currentNode;

        public AscendingNodeIterator(Skipnode<E> startNode) {
            currentNode = startNode;
        }

        @Override
        public boolean hasNext() {
            return currentNode.getNext() != head;
        }

        @Override
        public Skipnode<E> next() {
            currentNode = currentNode.getNext();
            return currentNode;
        }
    }
}