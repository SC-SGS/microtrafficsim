package microtrafficsim.utils.collections;

import java.util.Collection;
import java.util.Iterator;
import java.util.Queue;
import java.util.Set;

/**
 * @author Dominic Parga Cacheiro
 */
public interface QueueSet<E> extends Queue<E>, Set<E> {
    E get(int index);

    E remove(int index);

    E get(Object obj);

    Iterator<E> iteratorAsc();

    Iterator<E> iteratorDesc();

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
