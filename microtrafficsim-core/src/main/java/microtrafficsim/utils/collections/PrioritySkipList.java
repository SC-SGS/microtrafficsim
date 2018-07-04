package microtrafficsim.utils.collections;

import microtrafficsim.math.random.distributions.impl.Random;
import microtrafficsim.utils.strings.builder.BasicStringBuilder;
import microtrafficsim.utils.strings.builder.LevelStringBuilder;
import microtrafficsim.utils.strings.builder.StringBuilder;

import java.util.*;

/**
 * <p>
 * This implementation of {@code SkipList} is implemented as a sorted linked list (not by Java).
 * It compares elements using their {@link Comparable natural order} per default.
 *
 * <p>
 * This class does NOT store duplicates. In case two objects have the same priority, they are compared using
 * {@link Long#compare(long, long) Long.compare(o1.hashCode(), o2.hashCode())}. Only if this comparison returns 0,
 * two objects are interpreted as duplicates.
 *
 * <p>
 * This implementation is not thread-safe.
 *
 * @author Dominic Parga Cacheiro
 */
public class PrioritySkipList<E> implements SkipList<E> {
    private         Skipnode<E>           head;
    private final   Random                random;
    protected final Comparator<? super E> comparator;
    private         int                   size;

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

        /**
         * <p>
         * This method is used to compare two objects in this class. A subclass has access to {@code my protected final
         * comparator}.
         *
         * <p>
         * Default implementation compares in this order:<br>
         * &bull; if ({@code my comparator != null}) {@literal ->} cast objects to {@code E} and use the comparator<br>
         * &bull; else {@literal ->} cast objects to {@link Comparable Comparable<? super E>} and use
         * {@link Comparable#compareTo(Object) compareTo(E e)}<br>
         * &bull; if ({@code cmp-result == 0}) {@literal ->} use
         * {@link Long#compare(long, long) Long.compare(o1.hashCode(), o2.hashCode())}
         */
        if (comparator != null) {
            this.comparator = (o1, o2) -> {
                int cmp = comparator.compare(o1, o2);

                if (cmp == 0)
                    cmp = Long.compare(o1.hashCode(), o2.hashCode());

                return cmp;
            };
        } else {
            this.comparator = (o1, o2) -> {
                Comparable<? super E> e1 = (Comparable<? super E>) o1;
                int cmp = e1.compareTo(o2);

                if (cmp == 0)
                    cmp = Long.compare(o1.hashCode(), o2.hashCode());

                return cmp;
            };
        }
    }

    @Override
    public String toString() {
        StringBuilder builder = new BasicStringBuilder();

        /* values */
        builder.appendln("VALUES STORED IN THIS SKIP LIST IN ASCENDING (PRIORITY) ORDER");
        Iterator<Skipnode<E>> iterator = new AscendingNodeIterator(head);
        builder.append("<HEAD>");
        builder.append(head);
        builder.append("</HEAD>");
        while (iterator.hasNext()) {
            builder.appendln();
            builder.append(iterator.next());
        }

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
        return find(index).value;
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
        return equal(infimum.value, obj) ? infimum.value : null;
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
                if (equal(newNode.value, infimum.value))
                    return false;

            Skipnode<E> supremum = infimum.tower.getNext();


            /* init for tower level 0 */
            infimum.tower.setNext(newNode);
            supremum.tower.setPrev(newNode);
            newNode.tower.add(infimum, supremum);
            newNode.tower.addLinkLength(1);

            createAndFillTower(infimum, newNode, supremum);
        }

        size++;
        return true;
    }

    @Override
    public boolean offer(E e) {
        return add(e);
    }

    @Override
    public E remove(int index) {
        return removeExistingNode(find(index)).value;
    }

    @Override
    public boolean remove(Object obj) {
        Skipnode<E> infimum = findInfimumOf(obj);
        if (infimum == head)
            return false;
        if (equal(infimum.value, obj)) {
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
        return removeExistingNode(head.tower.getNext()).value;
    }

    @Override
    public E element() {
        if (isEmpty())
            throw new NoSuchElementException(isEmptyMsg());
        return head.tower.getNext().value;
    }

    @Override
    public E peek() {
        if (isEmpty())
            return null;
        return head.tower.getNext().value;
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
        return equal(infimum.value, obj);
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
     * <p>
     * Creates a new PrioritySkipList with my seed and comparator and adds all elements of my list to this new list
     * if these elements are equal to an element of the given collection. So the runtime complexity is
     * in {@code O(m * log(n))} where {@code m == collection.size()} and {@code n == this.size()}. {@code O(logn)} is
     * determined by the runtime of {@link #get(Object) get(E)} and is expected with high probability.
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
        head = new Skipnode<>(null);
        head.tower.add(head, head);
        head.tower.addLinkLength(0);
        size = 0;
    }

    /*
    |=======|
    | utils |
    |=======|
    */
    private Skipnode<E> findInfimumOf(Object obj) {

        int towerLevel = head.tower.getHeight();
        Skipnode<E> curNode = head;
        Skipnode<E> nextNode;

        // from highest level to bottom level
        while (towerLevel >= 0) {
            nextNode = curNode.tower.getNext(towerLevel);
            if (nextNode == head) {
                towerLevel--;
                continue;
            }

            int cmp = comparator.compare(nextNode.value, (E) obj);
            if (cmp > 0)
                towerLevel--;
            else if (cmp == 0)
                return nextNode;
            else
                curNode = nextNode;
        }

        return curNode;
    }

    /**
     * Claims the index.
     *
     * @return head if the list is empty
     */
    private Skipnode<E> find(int index) {

        if (isEmpty())
            return head;
        index = claimIndex(index);

        int towerLevel = head.tower.getHeight();
        Skipnode<E> curNode = head;

        while (towerLevel >= 0) {
            int newIndex = index - curNode.tower.getLinkLength(towerLevel);
            if (newIndex >= 0) {
                index = newIndex;
                curNode = curNode.tower.getNext(towerLevel);
            } else
                towerLevel--;
        }
        return curNode;
    }

    private int countLinkLength(Skipnode<E> from, Skipnode<E> to, int towerLevel) {

        Skipnode<E> current = from;
        int linkLength = 0;
        while (current != to) {
            linkLength += current.tower.getLinkLength(towerLevel);
            current = current.tower.getNext(towerLevel);
        }

        return linkLength;
    }

    /**
     * Updates all nodes BEFORE the concerned node (e.g. the new node or next of a removed one)
     */
    private void updateLinkLengths(Skipnode<E> concernedNode) {

        // simple implementation but iterates twice over some nodes
        // Skipnode<E> from = concernedNode.tower.getPrev();
        // for (int towerLevel = 1; towerLevel <= head.tower.getHeight(); towerLevel++) {
        // // go back until there is a node with expected tower level to update
        // while (from.tower.getHeight() < towerLevel)
        // from = from.tower.getPrev(towerLevel - 1);
        //
        // // destination is next of current from (with correct level
        // Skipnode<E> to = from.tower.getNext(towerLevel);
        // int linkLength = countLinkLength(from, to, towerLevel - 1);
        // from.tower.setLinkLength(towerLevel, linkLength);
        // }

        // midFrom is the first node that should be updated
        Skipnode<E> midFrom = concernedNode.tower.getPrev();
        for (int towerLevel = 1; towerLevel <= head.tower.getHeight(); towerLevel++) {
            // copy "midFrom" into "from";
            // "from" is used to find the next node with expected tower level to update.
            // A new variable "from" is used because "midFrom" has to be remembered in cases, where you have to
            // calculate from "midFrom" to "to" as mentioned here:
            //
            // | from | -----------------------?------------------------> | to |
            // | from | -1-> | node | -1-> | midFrom | -1-> | node | -1-> | to |
            Skipnode<E> from = midFrom;

            int linkLength = 0;
            // go back from "midFrom" to "from" and count link lengths
            while (from.tower.getHeight() < towerLevel) {
                from        = from.tower.getPrev(towerLevel - 1);
                linkLength += from.tower.getLinkLength(towerLevel - 1);
            }

            // "to" is next of current "from" (with correct level)
            Skipnode<E> to = from.tower.getNext(towerLevel);
            // count remaining link lengths from "midFrom" to "to"
            linkLength += countLinkLength(midFrom, to, towerLevel - 1);
            from.tower.setLinkLength(towerLevel, linkLength);
            // update midFrom for next loop run
            midFrom = from;
        }
    }

    /**
     * Removes the given node. In worst case, you could remove nodes with equal tower size and the runtime complexity
     * would be in O(n). Preventing this, a new tower for the given node's successor is calculated as well.
     *
     * @param node that will be removed from this list
     * @return given node (just for practical purposes)
     */
    private Skipnode<E> removeExistingNode(Skipnode<E> node) {

        removeTower(node, 0);
        cleanupHead();
        updateLinkLengths(node);
        size--;

        /* recalculate tower */
        if (node.tower.getHeight() == head.tower.getHeight()) {
            Skipnode<E> next = node.tower.getNext();
            if (next != head) {
                removeTower(next, 1);
                cleanupHead();
                updateLinkLengths(node);
                createAndFillTower(next.tower.getPrev(), next, next.tower.getNext());
            }
        }

        return node;
    }

    /**
     * Does <b>not</b> update {@code size} if {@code bottomTowerLevel} is 0.
     *
     * @param node
     * @param bottomTowerLevel if 0, this node is fully removed from the list
     */
    private void removeTower(Skipnode<E> node, int bottomTowerLevel) {

        /* set links */
        for (int towerLevel = bottomTowerLevel; towerLevel <= node.tower.getHeight(); towerLevel++) {
            Skipnode<E> next = node.tower.getNext(towerLevel);
            Skipnode<E> prev = node.tower.getPrev(towerLevel);
            prev.tower.setNext(towerLevel, next);
            next.tower.setPrev(towerLevel, prev);
        }

        /* remove tower levels if node is not fully removed */
        if (bottomTowerLevel > 0)
            while (node.tower.getHeight() > 0)
                node.tower.removeHighest();
    }

    /**
     * <p>
     * Throws the coin ({@link #throwCoinUntilFalse()}) and fills the tower at level 1 and above if coin count is
     * greater than 0.
     *
     * <p>
     * Important note:<br>
     * This method does <b>only add</b> a tower to a node without tower. If the parameter {@code newNode} already has
     * a tower, this method does not work correctly.
     */
    private void createAndFillTower(Skipnode<E> infimum, Skipnode<E> node, Skipnode<E> supremum) {
        int towerHeight = throwCoinUntilFalse();
        for (int towerLevel = 1; towerLevel <= towerHeight; towerLevel++) {

                /* set infimum's pointer */
            while (infimum.tower.getHeight() < towerLevel) {
                // if head: increase tower height
                if (infimum == head) {
                    infimum.tower.add(head, head);
                    infimum.tower.addLinkLength(0);
                }
                // if not head: jump to previous element
                else
                    infimum = infimum.tower.getPrev(towerLevel - 1);
            }
            infimum.tower.setNext(towerLevel, node);


                /* set supremum's pointer */
            while (supremum.tower.getHeight() < towerLevel) {
                // if head: increase tower height
                if (supremum == head) {
                    supremum.tower.add(head, head);
                    infimum.tower.addLinkLength(0);
                }
                // if not head: jump to next element
                else
                    supremum = supremum.tower.getNext(towerLevel - 1);
            }
            supremum.tower.setPrev(towerLevel, node);


                /* set new node's pointer */
            node.tower.add(infimum, supremum);
            int nextLinkLength = countLinkLength(node, supremum, towerLevel - 1);
            node.tower.addLinkLength(nextLinkLength);
        }
        updateLinkLengths(node);
    }

    private static int coinCount;
    public static void main(String[] args) {

        SkipList<Integer> skipList = new PrioritySkipList<>();
        coinCount = 2;
        skipList.add(0);
        coinCount = 0;
        skipList.add(1);
        coinCount = 1;
        skipList.add(2);
        skipList.remove(1);
        System.out.println(skipList);
    }

    private boolean equal(Object o1, Object o2) {
        return comparator.compare((E) o1, (E) o2) == 0;
    }

    private int throwCoinUntilFalse() {

        int counter = 0;
        while (random.nextBoolean())
            counter++;
        return counter;
    }

    private void cleanupHead() {
        while (head.tower.getHeight() > 0 && head.tower.getNext(head.tower.getHeight()) == head)
            head.tower.removeHighest();
    }

    private int claimIndex(int index) {
        index = index % size;
        if (index < 0)
            index += size;
        return index;
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

        AscendingIterator(Skipnode<E> startNode) {
            currentNode = startNode;
        }

        @Override
        public boolean hasNext() {
            return currentNode.tower.getNext() != head;
        }

        @Override
        public E next() {
            currentNode = currentNode.tower.getNext();
            return currentNode.value;
        }
    }

    private class DescendingIterator implements Iterator<E> {

        private Skipnode<E> currentNode;

        DescendingIterator(Skipnode<E> startNode) {
            currentNode = startNode;
        }

        @Override
        public boolean hasNext() {
            return currentNode.tower.getPrev() != head;
        }

        @Override
        public E next() {
            currentNode = currentNode.tower.getPrev();
            return currentNode.value;
        }
    }

    private class AscendingNodeIterator implements Iterator<Skipnode<E>> {

        private Skipnode<E> currentNode;

        AscendingNodeIterator(Skipnode<E> startNode) {
            currentNode = startNode;
        }

        @Override
        public boolean hasNext() {
            return currentNode.tower.getNext() != head;
        }

        @Override
        public Skipnode<E> next() {
            currentNode = currentNode.tower.getNext();
            return currentNode;
        }
    }


    /*
    |=====================|
    | sub data structures |
    |=====================|
    */
    /**
     * @author Dominic Parga Cacheiro
     */
    private class Tower<E> {
        private ArrayList<Skipnode<E>> skipnodes;
        private ArrayList<Integer>     linkLengths;

        public Tower() {
            // (1) for every element this tower has to store two links: next and prev
            // (2) due to geometric distribution, this tower has to store more than 1 element (2 links) in every 4th case
            skipnodes = new ArrayList<>(2);
            // only (2) holds
            linkLengths = new ArrayList<>(1);
        }

        @Override
        public String toString() {
            LevelStringBuilder builder = new LevelStringBuilder();
            builder.appendln("<" + getClass().getSimpleName() + ">");
            builder.incLevel();

            for (int towerLevel = getHeight(); towerLevel >= 0; towerLevel--) {
                builder.appendln("| |-" + getLinkLength(towerLevel) + "->");
            }

            builder.decLevel();
            builder.appendln("</" + getClass().getSimpleName() + ">");
            return builder.toString();
        }


        /**
         * Important note: the height of the tower describes the count of all links in this node without the node itself.
         * E.g. if all towers would have a height of 0, the skip list would be a doubled linked list.
         *
         * @return the size of the tower containing all links to other nodes.
         */
        public int getHeight() {
            return skipnodes.size() / 2 - 1;
        }


        public void add(Skipnode<E> prev, Skipnode<E> next) {
            // order is important due to idx; see getNextIdx(int towerLevel)
            skipnodes.add(next);
            skipnodes.add(prev);
        }

        public void addLinkLength(int linkLength) {
            linkLengths.add(linkLength);
        }

        /**
         * Removes the highest link (next + prev) of this tower
         */
        public void removeHighest() {
            skipnodes.remove(skipnodes.size() - 1);
            skipnodes.remove(skipnodes.size() - 1);

            linkLengths.remove(linkLengths.size() - 1);
        }

        public void clear() {
            skipnodes.clear();
            linkLengths.clear();
        }


        public Skipnode<E> getNext() {
            return getNext(0);
        }

        public Skipnode<E> getNext(int towerLevel) {
            return skipnodes.get(getNextIdx(towerLevel));
        }

        public void setNext(Skipnode<E> next) {
            setNext(0, next);
        }

        public void setNext(int towerLevel, Skipnode<E> next) {
            skipnodes.set(getNextIdx(towerLevel), next);
        }

        public Skipnode<E> getPrev() {
            return getPrev(0);
        }

        public Skipnode<E> getPrev(int towerLevel) {
            return skipnodes.get(getPrevIdx(towerLevel));
        }

        public void setPrev(Skipnode<E> prev) {
            setPrev(0, prev);
        }

        public void setPrev(int towerLevel, Skipnode<E> prev) {
            skipnodes.set(getPrevIdx(towerLevel), prev);
        }


        public int getLinkLength(int towerLevel) {
            return linkLengths.get(towerLevel);
        }

        public void setLinkLength(int towerLevel, int linkLength) {
            linkLengths.set(towerLevel, linkLength);
        }


        /*
        |=======|
        | utils |
        |=======|
        */
        private int getNextIdx(int towerLevel) {
            return 2 * towerLevel;
        }

        private int getPrevIdx(int towerLevel) {
            return 2 * towerLevel + 1;
        }
    }

    /**
     * @author Dominic Parga Cacheiro
     */
    private class Skipnode<E> {
        public final E        value;
        public final Tower<E> tower;


        public Skipnode(E value) {
            this.value = value;
            tower      = new Tower<>();
        }


        @Override
        public String toString() {
            LevelStringBuilder builder = new LevelStringBuilder();
            builder.appendln("<" + getClass().getSimpleName() + ">");
            builder.incLevel();

            builder.appendln("value:");
            builder.appendln(value == null ? "null" : value.toString());
            builder.appendln(tower);

            builder.decLevel();
            builder.appendln("</" + getClass().getSimpleName() + ">");
            return builder.toString();
        }

        /**
         * This method could be needed for the default comparator in a skiplist using hashcode (e.g. {@link PrioritySkipList}).
         *
         * @return hashcode of the stored value
         */
        @Override
        public int hashCode() {
            return value.hashCode();
        }
    }
}
