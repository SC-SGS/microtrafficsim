package microtrafficsim.utils.collections.skiplist;

import java.util.*;

/**
 * <p>
 * This data structure works like a {@code PriorityQueue}. The difference is: a {@code QueueSet} detects double
 * elements in O(logn) (due to set) and removes the old one in O(logn). All given runtime complexities are expected
 * (but "with high probability").
 *
 * <p>
 * Imporant note: Double elements are recognized by their priority determined by a comparator (or their natural
 * order). The advantage over a simple set is the access to a certain index in O(logn). You can give a comparator
 * comparing an object's hashcode to the skip list and have a random object of the list in O(logn).
 *
 * @author Dominic Parga Cacheiro
 */
public interface SkipList<E> extends Queue<E>, Set<E> {


    long getSeed();

    E get(int index);

    E remove(int index);

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
