package microtrafficsim.utils.collections;

import java.util.*;
import java.util.function.UnaryOperator;

/**
 * Is an {@code ArrayList} keeping all elements sorted and implements {@code Queue}.
 *
 * @author Dominic Parga Cacheiro
 */
public class SortedArrayList<E> extends ArrayList<E> implements Queue<E> {
    private Comparator<? super E> comparator;

    public SortedArrayList() {
        super();
        initComparator(null);
    }

    public SortedArrayList(Comparator<? super E> comparator) {
        super();
        initComparator(comparator);
    }

    public SortedArrayList(Collection<? extends E> c) {
        this(c, null);
    }

    public SortedArrayList(Collection<? extends E> collection, Comparator<? super E> comparator) {
        super();
        initComparator(comparator);
        addAll(collection);
    }

    public SortedArrayList(int initialCapacity) {
        this(initialCapacity, null);
    }

    public SortedArrayList(int initialCapacity, Comparator<? super E> comparator) {
        super(initialCapacity);
        initComparator(comparator);
    }

    private void initComparator(Comparator<? super E> comparator) {
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
        return super.toString();
    }


    /**
     * @return true if the specified object can be found. This does not mean the found element is
     * {@link #equals(Object) equal} to the searched object, but the same attributes concerning the specified
     * comparator.
     */
    @Override
    public boolean contains(Object o) {
        return binarySearch(o) >= 0;
    }

    /**
     * todo worst case is still O(n) if all elements are equal
     *
     * @param o
     * @return
     */
    @Override
    public int indexOf(Object o) {
        int index = binarySearch(o);
        if (index >= 0) {
            while (index > 0) {
                int cmp = comparator.compare(get(index), get(index - 1));

                if (cmp == 0) index--;
                else break;
            }
            return index;
        } else {
            return -1;
        }
    }

    /**
     * todo worst case is still O(n) if all elements are equal
     *
     * @param o
     * @return
     */
    @Override
    public int lastIndexOf(Object o) {
        int index = binarySearch(o);
        if (index >= 0) {
            while (index < size() - 1) {
                int cmp = comparator.compare(get(index), get(index + 1));

                if (cmp == 0) index++;
                else break;
            }
            return index;
        } else {
            return -1;
        }
    }

    @Override
    public boolean add(E e) {
        int index = searchInsertionPoint(e);
        super.add(index, e);
        return true;
    }

    @Override
    public boolean remove(Object o) {
        int index = indexOf(o);
        if (index < 0)
            return false;
        return super.remove(get(index));
    }

    @Override
    public boolean addAll(Collection<? extends E> c) {
        boolean changed = false;
        for (E e : c)
            changed |= add(e);
        return changed;
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        for (Object obj : c)
            if (!contains(obj))
                return false;
        return true;
    }

    public E get(Object obj) {
        int index = indexOf(obj);
        if (index < 0)
            return null;
        return get(index);
    }

    /*
    |=======|
    | Queue |
    |=======|
    */
    @Override
    public boolean offer(E e) {
        return add(e);
    }

    @Override
    public E remove() {
        if (isEmpty())
            throw new NoSuchElementException(isEmptyMsg());
        return remove(0);
    }

    @Override
    public E poll() {
        if (isEmpty())
            return null;
        return remove(0);
    }

    @Override
    public E element() {
        if (isEmpty())
            throw new NoSuchElementException(isEmptyMsg());
        return get(0);
    }

    @Override
    public E peek() {
        if (isEmpty())
            return null;
        return get(0);
    }


    /*
    |==========|
    | iterator |
    |==========|
    */
    @Override
    public Iterator<E> iterator() {
        return new AscendingIterator();
    }

    public Iterator<E> iteratorAsc() {
        return new AscendingIterator();
    }

    public Iterator<E> iteratorDesc() {
        return new DescendingIterator();
    }

    private class AscendingIterator implements Iterator<E> {
        private int idx = 0;

        @Override
        public boolean hasNext() {
            return idx < size();
        }

        @Override
        public E next() {
            int tmp = idx;
            E e = get(idx);
            idx = tmp + 1;
            return e;
        }
    }

    private class DescendingIterator implements Iterator<E> {
        private int idx = size() - 1;

        @Override
        public boolean hasNext() {
            return idx >= 0;
        }

        @Override
        public E next() {
            int tmp = idx;
            E e = get(idx);
            idx = tmp - 1;
            return e;
        }
    }


    /*
    |=======|
    | utils |
    |=======|
    */
    protected UnsupportedOperationException unsupportedOperationException() {
        return new UnsupportedOperationException("Not supported due to total order of this collection.");
    }

    protected String isEmptyMsg() {
        return "This " + getClass().getSimpleName() + " is empty.";
    }

    /**
     * @see Collections#binarySearch(List, Object, Comparator)
     */
    protected int binarySearch(Object o) {
        return Collections.binarySearch(this, (E) o, comparator);
    }

    protected int searchInsertionPoint(Object o) {
        int index = binarySearch(o);
        return index < 0 ? -(index + 1) : index;
    }



    /*
    |=============|
    | unsupported |
    |=============|
    */
    /**
     * @throws UnsupportedOperationException due to total order of this collection.
     */
    @Override
    public E set(int index, E element) {
        throw unsupportedOperationException();
    }

    /**
     * @throws UnsupportedOperationException due to total order of this collection.
     */
    @Override
    public void add(int index, E element) {
        throw unsupportedOperationException();
    }

    /**
     * @throws UnsupportedOperationException due to total order of this collection.
     */
    @Override
    public boolean addAll(int index, Collection<? extends E> c) {
        throw unsupportedOperationException();
    }

    /**
     * @throws UnsupportedOperationException due to total order of this collection
     */
    @Override
    public void replaceAll(UnaryOperator<E> operator) {
        throw unsupportedOperationException();
    }

    /**
     * @throws UnsupportedOperationException due to total order of this collection
     */
    @Override
    public void sort(Comparator<? super E> c) {
        throw unsupportedOperationException();
    }
}
