package microtrafficsim.utils.collections;

import java.util.*;

/**
 * This implementation of {@code PriorityQueueSet} is implemented as a heap.
 *
 * @author Dominic Parga Cacheiro
 */
public class HashPriorityQueueSet<E> implements PriorityQueueSet<E> {

    private final ArrayList<E>    elements;
    private Comparator<? super E> comparator;

    /**
     * Calls {@link #HashPriorityQueueSet(Comparator) HashQueueSet((e1, e2) -> Long.compare(e1.hashCode(), e2.hashCode()))}
     */
    public HashPriorityQueueSet() {
        this((e1, e2) -> Long.compare(e1.hashCode(), e2.hashCode()));
    }

    public HashPriorityQueueSet(Comparator<? super E> comparator) {
        this.comparator = comparator;
        elements = new ArrayList<>();
    }

    @Override
    public boolean equals(Object o) {
        if (o == this)
            return true;

        if (!(o instanceof PriorityQueueSet || o instanceof Queue || o instanceof Set))
            return false;

        Collection<?> c = (Collection<?>) o;
        if (c.size() != size())
            return false;

        try {
            return containsAll(c);
        } catch (Exception unused) {
            return false;
        }
    }

    @Override
    public boolean offer(E e) {
        return false;
    }

    @Override
    public E poll() {
        return null;
    }

    @Override
    public E peek() {
        return null;
    }

    @Override
    public int size() {
        return 0;
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    @Override
    public boolean contains(Object o) {
        return false;
    }

    @Override
    public Iterator<E> iterator() {
        return null;
    }

    @Override
    public Object[] toArray() {
        return new Object[0];
    }

    @Override
    public <T> T[] toArray(T[] a) {
        return null;
    }

    @Override
    public boolean remove(Object o) {
        return false;
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        return false;
    }

    @Override
    public void clear() {

    }
}
