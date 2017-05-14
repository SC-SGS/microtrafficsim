package microtrafficsim.utils.collections;

import java.util.Collection;
import java.util.Comparator;

/**
 * @author Dominic Parga Cacheiro
 */
public class SortedArrayListSet<E> extends SortedArrayList<E> implements QueueSet<E> {
    public SortedArrayListSet() {
        super();
    }

    public SortedArrayListSet(Comparator<? super E> comparator) {
        super(comparator);
    }

    public SortedArrayListSet(Collection<? extends E> c) {
        super(c);
    }

    public SortedArrayListSet(Collection<? extends E> collection, Comparator<? super E> comparator) {
        super(collection, comparator);
    }

    public SortedArrayListSet(int initialCapacity) {
        super(initialCapacity);
    }

    public SortedArrayListSet(int initialCapacity, Comparator<? super E> comparator) {
        super(initialCapacity, comparator);
    }

    @Override
    public int indexOf(Object o) {
        return binarySearch(o);
    }

    /**
     * todo worst case is still O(n) if all elements are equal
     *
     * @param o
     * @return
     */
    @Override
    public int lastIndexOf(Object o) {
        return binarySearch(o);
    }

    @Override
    public boolean add(E e) {
        if (contains(e))
            return false;
        return super.add(e);
    }
}
