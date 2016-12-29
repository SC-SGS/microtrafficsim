package microtrafficsim.utils.collections;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;


/**
 * Array list that keeps elements sorted.
 *
 * @author Maximilian Luz
 */
public class SortedArrayList<T> extends ArrayList<T> {

    private final Comparator<? super T> comparator;


    public SortedArrayList() {
        super();
        comparator = null;
    }

    public SortedArrayList(Comparator<? super T> comparator) {
        super();
        this.comparator = comparator;
    }

    public SortedArrayList(Collection<? extends T> collection) {
        super(collection);
        this.comparator = null;
        this.sort(null);
    }

    public SortedArrayList(Collection<? extends T> collection, Comparator<? super T> comparator) {
        super(collection);
        this.comparator = comparator;
        this.sort(comparator);
    }

    public SortedArrayList(int initialCapacity) {
        super(initialCapacity);
        this.comparator = null;
    }

    public SortedArrayList(int initialCapacity, Comparator<? super T> comparator) {
        super(initialCapacity);
        this.comparator = comparator;
    }


    public Comparator<? super T> comparator() {
        return comparator;
    }

    @Override
    public boolean add(T t) {
        int index = Collections.binarySearch(this, t, comparator);
        this.add(index < 0 ? -(index + 1) : index, t);
        return true;
    }

    @Override
    public boolean addAll(Collection<? extends T> collection) {
        for (T elem : collection)
            add(elem);

        return !collection.isEmpty();
    }

    public void sort() {
        this.sort(comparator);
    }
}
