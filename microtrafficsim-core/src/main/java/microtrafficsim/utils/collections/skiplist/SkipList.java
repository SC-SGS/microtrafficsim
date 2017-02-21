package microtrafficsim.utils.collections.skiplist;

import java.util.*;

/**
 * This data structure works like a {@code PriorityQueue}. The difference is: a {@code QueueSet} detects double
 * elements in O(logn) (due to set) and removes the old one in O(logn).
 *
 * @author Dominic Parga Cacheiro
 */
public interface SkipList<T> extends Queue<T>, Set<T> {

    // todo get(index)

    long getSeed();

    T get(Object obj);

    Iterator<T> iteratorAsc();

    Iterator<T> iteratorDesc();

    @Override
    default boolean containsAll(Collection<?> c) {
        for (Object obj : c)
            if (!contains(obj))
                return false;
        return true;
    }

    @Override
    default boolean addAll(Collection<? extends T> c) {
        boolean changed = false;
        for (T t : c)
            changed |= add(t);
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
