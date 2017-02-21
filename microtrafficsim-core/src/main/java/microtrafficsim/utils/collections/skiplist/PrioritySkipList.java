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
public class PrioritySkipList<T> implements SkipList<T> {

    // todo wikipedia says, the runtime complexity could be repaired in cases of O(n)
    // todo store link width for get(index) in O(logn)

    private SkipNode<T>                 head;
    private final Random                random;
    private final Comparator<? super T> comparator;
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
    public PrioritySkipList(Comparator<? super T> comparator) {
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
    public PrioritySkipList(long seed, Comparator<? super T> comparator) {
        clear();
        random          = new Random(seed);
        this.comparator = comparator;
    }

    @Override
    public String toString() {
        StringBuilder builder = new BasicStringBuilder();

        Iterator<SkipNode<T>> iterator = new AscendingNodeIterator(head);
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
     * @return the stored value equal to the given one using the defined comparator; null, if the element is not
     * contained in this list
     */
    @Override
    public T get(Object obj) {
        if (isEmpty())
            return null;
        SkipNode<T> infimum = findInfimumOf(obj);
        return equal(infimum.getValue(), obj) ? infimum.getValue() : null;
    }

    @Override
    public boolean add(T t) {

        if (t == null)
            return false;
        else {
            /* find position for new node and check whether it already is in this list */
            SkipNode<T> newNode = new SkipNode<>(t);
            SkipNode<T> infimum = findInfimumOf(t);

            // do nothing if element is already in this list
            if (infimum != head)
                if (equal(newNode.getValue(), infimum.getValue()))
                    return false;

            SkipNode<T> supremum = infimum.getNext();


            /* set neighbours */
            infimum.setNext(newNode);
            newNode.setPrev(infimum);
            newNode.setNext(supremum);
            supremum.setPrev(newNode);


            /* create tower */
            int towerHeight = throwCoinUntilFalse();

            /* fill tower */
            for (int towerLevel = 1; towerLevel <= towerHeight; towerLevel++) {

                /* set infimum's pointer */
                while (infimum.getTowerHeight() < towerLevel) {
                    // if head: increase tower height
                    if (infimum == head)
                        infimum.addToTower(head, head);
                    // if not head: jump to previous element
                    else
                        infimum = infimum.getPrev(towerLevel - 1);
                }
                infimum.setNext(newNode, towerLevel);


                /* set supremum's pointer */
                while (supremum.getTowerHeight() < towerLevel) {
                    // if head: increase tower height
                    if (supremum == head)
                        supremum.addToTower(head, head);
                    // if not head: jump to next element
                    else
                        supremum = supremum.getNext(towerLevel - 1);
                }
                supremum.setPrev(newNode, towerLevel);


                /* set new node's pointer */
                newNode.addToTower(infimum, supremum);
            }
        }

        size++;
        return true;
    }

    @Override
    public boolean offer(T t) {
        return add(t);
    }

    @Override
    public boolean remove(Object obj) {

        SkipNode<T> infimum = findInfimumOf(obj);
        if (infimum == head)
            return false;
        if (equal(infimum.getValue(), obj)) {
            removeExistingNode(infimum);
            return true;
        }
        return false;
    }

    @Override
    public T remove() {
        if (isEmpty())
            throw new NoSuchElementException(isEmptyMsg());
        return poll();
    }

    @Override
    public T poll() {
        SkipNode<T> first = head.getNext();
        removeExistingNode(first);
        return first.getValue();
    }

    @Override
    public T element() {
        if (isEmpty())
            throw new NoSuchElementException(isEmptyMsg());
        return head.getNext().getValue();
    }

    @Override
    public T peek() {
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
        SkipNode<T> infimum = findInfimumOf(obj);
        if (infimum == head)
            return false;
        return equal(infimum.getValue(), obj);
    }

    @Override
    public Iterator<T> iterator() {
        return iteratorAsc();
    }

    @Override
    public Iterator<T> iteratorAsc() {
        return new AscendingIterator(head);
    }

    @Override
    public Iterator<T> iteratorDesc() {
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
    public boolean retainAll(Collection<?> c) {

        // fill new list with all elements of me equal to an element of c
        PrioritySkipList<T> newList = new PrioritySkipList<>(getSeed(), comparator);
        for (Object obj : c)
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
        head = new SkipNode<>();
        size = 0;
    }

    /*
    |=======|
    | utils |
    |=======|
    */
    private SkipNode<T> findInfimumOf(Object value) {

        int curTowerLevel = head.getTowerHeight();
        SkipNode<T> curNode = head;
        SkipNode<T> nextNode;

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
            return comparator.compare((T) o1, (T) o2);

        Comparable<? super T> t1 = (Comparable<? super T>) o1;
        return t1.compareTo((T) o2);
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

    private void removeExistingNode(SkipNode<T> node) {

        for (int towerLevel = 0; towerLevel <= node.getTowerHeight(); towerLevel++) {
            SkipNode<T> next = node.getNext(towerLevel);
            SkipNode<T> prev = node.getPrev(towerLevel);
            prev.setNext(next, towerLevel);
            next.setPrev(prev, towerLevel);
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
    private class AscendingIterator implements Iterator<T> {

        private SkipNode<T> currentNode;

        public AscendingIterator(SkipNode<T> startNode) {
            currentNode = startNode;
        }

        @Override
        public boolean hasNext() {
            return currentNode.getNext() != head;
        }

        @Override
        public T next() {
            currentNode = currentNode.getNext();
            return currentNode.getValue();
        }
    }

    private class DescendingIterator implements Iterator<T> {

        private SkipNode<T> currentNode;

        public DescendingIterator(SkipNode<T> startNode) {
            currentNode = startNode;
        }

        @Override
        public boolean hasNext() {
            return currentNode.getPrev() != head;
        }

        @Override
        public T next() {
            currentNode = currentNode.getPrev();
            return currentNode.getValue();
        }
    }

    private class AscendingNodeIterator implements Iterator<SkipNode<T>> {

        private SkipNode<T> currentNode;

        public AscendingNodeIterator(SkipNode<T> startNode) {
            currentNode = startNode;
        }

        @Override
        public boolean hasNext() {
            return currentNode.getNext() != head;
        }

        @Override
        public SkipNode<T> next() {
            currentNode = currentNode.getNext();
            return currentNode;
        }
    }
}