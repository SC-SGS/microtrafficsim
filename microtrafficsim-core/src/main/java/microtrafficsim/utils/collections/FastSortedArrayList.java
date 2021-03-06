package microtrafficsim.utils.collections;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;

/**
 * <p>
 * Is an {@code ArrayList} keeping all elements sorted and implements {@code Queue}. This class stores the values
 * directly and sorts them if needed.
 *
 * <p>
 * Which method sorts and which one doesn't is described in their JavaDoc. In general, the list gets sorted in every
 * method where: <br>
 * &bull; runtime could be improved by a sorted list (e.g. {@link #get(Object)} or {@link #contains(Object)}) <br>
 * &bull; the identity/order of this list is relevant (e.g. {@link #equals(Object)}, {@link #poll()}) <br>
 * It is not sorted for description methods ({@link #toString()}), for {@link #listIterator() list iterators} and
 * getter
 * using indices ({@link #get(int)}).<br>
 * <br>
 * You can sort the list calling {@link #sort()}. It only sorts the list if it's order is "dirty" (e.g. after calling
 * {@link #add(Object)}).
 *
 * @author Dominic Parga Cacheiro
 */
public class FastSortedArrayList<E> extends ArrayList<E> implements Queue<E> {
    private Comparator<? super E> comparator;
    private boolean isDirty = true;

    public FastSortedArrayList() {
        super();
        initComparator(null);
    }

    public FastSortedArrayList(Comparator<? super E> comparator) {
        super();
        initComparator(comparator);
    }

    public FastSortedArrayList(Collection<? extends E> c) {
        this(c, null);
    }

    public FastSortedArrayList(Collection<? extends E> collection, Comparator<? super E> comparator) {
        this(collection.size(), comparator);
        addAll(collection);
    }

    public FastSortedArrayList(int initialCapacity) {
        this(initialCapacity, null);
    }

    public FastSortedArrayList(int initialCapacity, Comparator<? super E> comparator) {
        super(initialCapacity);
        initComparator(comparator);
    }

    private void initComparator(Comparator<? super E> comparator) {
        if (comparator != null)
            this.comparator = comparator::compare;
        else
            this.comparator = (o1, o2) -> ((Comparable<? super E>) o1).compareTo(o2);
    }


    /**
     * Does {@link #sort() sort}
     */
    @Override
    public boolean equals(Object o) {
        sort();
        if (o instanceof FastSortedArrayList)
            ((FastSortedArrayList) o).sort();
        return super.equals(o);
    }

    /**
     * Does {@link #sort() sort}
     */
    @Override
    public int hashCode() {
        sort();
        return super.hashCode();
    }

    /**
     * Does not {@link #sort() sort}
     */
    @Override
    public String toString() {
        return super.toString();
    }


    /**
     * <p>
     * Does {@link #sort() sort}
     *
     * <p>
     * @return true if the specified object can be found. This does not mean the found element is
     * {@link #equals(Object) equal} to the searched object, but the same attributes concerning the specified
     * comparator.
     */
    @Override
    public boolean contains(Object o) {
        return binarySearch((E) o) >= 0;
    }

    /**
     * todo worst case is still O(n) if all elements are equal
     *
     * <p>
     * Does {@link #sort() sort}
     */
    @Override
    public int indexOf(Object o) {
        int index = binarySearch((E) o);
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
     * <p>
     * Does {@link #sort() sort}
     */
    @Override
    public int lastIndexOf(Object o) {
        int index = binarySearch((E) o);
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

    /**
     * Does not {@link #sort() sort}
     */
    @Override
    public boolean add(E e) {
        isDirty = true;
        return super.add(e);
    }

    /**
     * Does {@link #sort() sort}
     */
    @Override
    public boolean remove(Object o) {
        int index = indexOf(o);
        if (index < 0)
            return false;
        return super.remove(get(index));
    }

    /**
     * Does not {@link #sort() sort}
     */
    @Override
    public boolean addAll(Collection<? extends E> c) {
        boolean changed = false;
        for (E e : c)
            changed |= add(e);
        return changed;
    }

    /**
     * Does {@link #sort() sort}
     */
    @Override
    public boolean containsAll(Collection<?> c) {
        for (Object obj : c)
            if (!contains(obj))
                return false;
        return true;
    }

    /**
     * Does {@link #sort() sort}
     */
    public E get(Object obj) {
        int index = indexOf(obj);
        if (index < 0)
            return null;
        return get(index);
    }

    /**
     * Does not {@link #sort() sort}
     */
    @Override
    public Object clone() {
        return super.clone();
    }

    /**
     * Does not {@link #sort() sort}
     */
    @Override
    public Object[] toArray() {
        return super.toArray();
    }

    /**
     * Does not {@link #sort() sort}
     */
    @Override
    public <T> T[] toArray(T[] a) {
        return super.toArray(a);
    }

    /**
     * Does not {@link #sort() sort}
     */
    @Override
    public E get(int index) {
        return super.get(index);
    }

    public E getFirst() {
        if (isEmpty())
            return null;
        sort();
        return get(0);
    }

    public E getLast() {
        if (isEmpty())
            return null;
        sort();
        return get(size() - 1);
    }

    /**
     * Does not {@link #sort() sort}
     */
    @Override
    public E remove(int index) {
        return super.remove(index);
    }

    /**
     * Does not {@link #sort() sort}
     */
    @Override
    protected void removeRange(int fromIndex, int toIndex) {
        super.removeRange(fromIndex, toIndex);
    }

    /**
     * Does not {@link #sort() sort}
     */
    @Override
    public List<E> subList(int fromIndex, int toIndex) {
        return super.subList(fromIndex, toIndex);
    }


    /*
    |=======|
    | Queue |
    |=======|
    */
    /**
     * Does not {@link #sort() sort}
     */
    @Override
    public boolean offer(E e) {
        return add(e);
    }

    /**
     * Does {@link #sort() sort}
     */
    @Override
    public E remove() {
        if (isEmpty())
            throw new NoSuchElementException(isEmptyMsg());
        sort();
        return remove(0);
    }

    /**
     * Does {@link #sort() sort}
     */
    @Override
    public E poll() {
        if (isEmpty())
            return null;
        sort();
        return remove(0);
    }

    /**
     * Does {@link #sort() sort}
     */
    @Override
    public E element() {
        if (isEmpty())
            throw new NoSuchElementException(isEmptyMsg());
        sort();
        return get(0);
    }

    /**
     * Does {@link #sort() sort}
     */
    @Override
    public E peek() {
        return getFirst();
    }


    /*
    |==========|
    | iterator |
    |==========|
    */
    /**
     * Does not {@link #sort() sort}
     */
    @Override
    public ListIterator<E> listIterator(int index) {
        return super.listIterator(index);
    }

    /**
     * Does not {@link #sort() sort}
     */
    @Override
    public ListIterator<E> listIterator() {
        return super.listIterator();
    }

    /**
     * Does {@link #sort() sort}
     */
    @Override
    public void forEach(Consumer<? super E> action) {
        sort();
        super.forEach(action);
    }

    /**
     * Does {@link #sort() sort}
     */
    @Override
    public Spliterator<E> spliterator() {
        sort();
        return super.spliterator();
    }

    /**
     * Does {@link #sort() sort}
     */
    @Override
    public Stream<E> stream() {
        sort();
        return super.stream();
    }

    /**
     * Does {@link #sort() sort}
     */
    @Override
    public Stream<E> parallelStream() {
        sort();
        return super.stream();
    }

    /**
     * Does {@link #sort() sort}
     */
    @Override
    public Iterator<E> iterator() {
        sort();
        return new AscendingIterator();
    }

    /**
     * Does {@link #sort() sort}
     */
    public Iterator<E> descendingIterator() {
        sort();
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
     * Does {@link #sort() sort}
     *
     * @see Collections#binarySearch(List, Object, Comparator)
     */
    protected int binarySearch(E e) {
        sort();
        return Collections.binarySearch(this, e, comparator);
    }


    /**
     * <p>
     * Uses Java's {@link ArrayList#sort(Comparator) list sort algorithm}.
     *
     * <p>
     * Does {@code sort} if needed
     */
    public void sort() {
        if (isDirty) {
            super.sort(comparator);
            isDirty = false;
        }
    }


    /*
    |=============|
    | unsupported |
    |=============|
    */
    /**
     * ATTENTION: Does NOT {@link #sort() sort} before or after insertion
     */
    @Override
    public E set(int index, E element) {
        isDirty = true;
        return super.set(index, element);
    }

    /**
     * ATTENTION: Does NOT {@link #sort() sort} before or after insertion
     */
    @Override
    public void add(int index, E element) {
        isDirty = true;
        super.add(index, element);
    }

    /**
     * ATTENTION: Does NOT {@link #sort() sort} before or after insertion
     */
    @Override
    public boolean addAll(int index, Collection<? extends E> c) {
        boolean changed = super.addAll(index, c);
        isDirty |= changed;
        return changed;
    }

    /**
     * ATTENTION: Does NOT {@link #sort() sort} before or after insertion
     */
    @Override
    public void replaceAll(UnaryOperator<E> operator) {
        isDirty = true;
        super.replaceAll(operator);
    }

    /**
     * @throws UnsupportedOperationException due to total order of this collection
     */
    @Override
    public void sort(Comparator<? super E> c) {
        throw unsupportedOperationException();
    }
}
