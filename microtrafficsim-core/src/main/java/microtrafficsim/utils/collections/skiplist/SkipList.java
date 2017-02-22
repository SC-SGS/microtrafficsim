package microtrafficsim.utils.collections.skiplist;

import java.util.Collection;
import java.util.Iterator;
import java.util.Queue;
import java.util.Set;

/**
 * This data structure works like a {@code PriorityQueue}. The difference is: a {@code QueueSet} detects double
 * elements in O(logn) (due to set) and removes the old one in O(logn).
 *
 * @author Dominic Parga Cacheiro
 */
public interface SkipList<E> extends Queue<E>, Set<E> {


    long getSeed();

    E get(int index);

    boolean remove(int index);

    E get(Object obj);

    Iterator<E> iteratorAsc();

    Iterator<E> iteratorDesc();

    @Override
    default boolean containsAll(Collection<?> collection) {
        for (Object obj : collection)
            if (!contains(obj))
                return false;
        return true;
    }

    @Override
    default boolean addAll(Collection<? extends E> collection) {
        boolean changed = false;
        for (E e : collection)
            changed |= add(e);
        return changed;
    }

    @Override
    default boolean removeAll(Collection<?> collection) {
        boolean changed = false;
        for (Object obj : collection)
            changed |= remove(obj);
        return changed;
    }
}
