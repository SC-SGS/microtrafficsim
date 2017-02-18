package microtrafficsim.utils.collections;

import java.util.*;

/**
 * This data structure works like a {@code PriorityQueue}. The difference is: a {@code QueueSet} detects double
 * elements in O
 * (logn) (due to set) and removes the old one in O(logn).
 *
 * @author Dominic Parga Cacheiro
 */
public interface PriorityQueueSet<E> extends Queue<E>, Set<E> {

    @Override
    default boolean add(E e) {
        if (offer(e))
            return true;
        throw new IllegalStateException("Element could not be added.");
    }

    @Override
    default E remove() {
        if (isEmpty())
            throw new NoSuchElementException("This QueueSet is empty.");
        return poll();
    }

    @Override
    default E element() {
        if (isEmpty())
            throw new NoSuchElementException("This QueueSet is empty.");
        return peek();
    }

    @Override
    default boolean containsAll(Collection<?> c) {
        for (Object obj : c)
            if (!contains(obj))
                return false;
        return true;
    }

    @Override
    default boolean addAll(Collection<? extends E> c) {
        boolean changed = false;
        for (E e : c)
            changed |= add(e);
        return changed;
    }

    @Override
    default boolean removeAll(Collection<?> c) {
        boolean changed = false;
        for (Object obj : c)
            changed |= remove(obj);
        return changed;
    }
}
