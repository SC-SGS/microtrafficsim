package microtrafficsim.utils.collections;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * @author Dominic Parga Cacheiro
 */
public class IndexableLinkedBand<E> implements Iterable<E> {
    private int currentListNo;
    private final ArrayList<FastSortedArrayList<FullElement>> lists;



    public IndexableLinkedBand(int maxListNo) {
        currentListNo = 0;
        lists = new ArrayList<>(maxListNo);
        for (int i = 0; i < maxListNo; i++) {
            FastSortedArrayList<FullElement> list = new FastSortedArrayList<>((e1, e2) -> Integer.compare(e1.index, e2.index));
            list.add(new EmptyElement(-1));
            lists.add(list);
        }
    }



    public void setCurrentList(int listNo) {
        currentListNo = listNo;
    }


    public E getNext(int index) {
        return getNext(currentListNo, index);
    }

    public E getNext(int listNo, int index) {
        Element supremum = supremumOf(listNo, index);
        if (supremum instanceof EmptyElement) {

        }
        FastSortedArrayList<FullElement> list = lists.get(listNo);
        int indexOf = list.lastIndexOf(new FullElement(null, index));
        if (indexOf < 0) // element is not in list
            return null;
        indexOf++;
        if (indexOf >= list.size()) // element is last element in list
            return null;
        return list.get(indexOf).e;
    }

    public E getPrev(int index) {
        return getPrev(currentListNo, index);
    }

    public E getPrev(int listNo, int index) {
        FastSortedArrayList<FullElement> list = lists.get(listNo);
        int indexOf = list.indexOf(new FullElement(null, index)) - 1;
        if (indexOf < 1) // 0 if first element in list; -1 if index is not in list
            return null;
        return list.get(indexOf).e;
    }

    public E getFirst() {
        return getFirst(currentListNo);
    }

    public E getFirst(int listNo) {
        if (isEmpty(listNo))
            return null;
        return lists.get(listNo).get(1).e;
    }

    public boolean isEmpty() {
        return isEmpty(currentListNo);
    }

    public boolean isEmpty(int listNo) {
        FastSortedArrayList<FullElement> list = lists.get(listNo);
        return list.size() <= 1;
    }

    public E set(E e, int index) {
        return set(e, currentListNo, index);
    }

    public E set(E e, int listNo, int index) {
        // todo runtime could be improved using not-linear search
        return null;
    }

    public E remove(int index) {
        return remove(currentListNo, index);
    }

    public E remove(int listNo, int index) {
        // todo runtime could be improved using not-linear search
        return null;
    }

    public void clear() {
        lists.forEach(list -> {
            list.clear();
            list.add(new EmptyElement(-1));
        });
    }



    private Element supremumOf(int listNo, int index) {
        FastSortedArrayList<FullElement> list = lists.get(listNo);
        Iterator<FullElement> iter = list.iterator();
        FullElement supremum = iter.next(); // every list contains the element of index -1
        while (iter.hasNext()) {
            FullElement next = iter.next();

        }
    }



    @Override
    public Iterator<E> iterator() {
        return iterator(currentListNo);
    }

    public Iterator<E> iterator(int listNo) {
        Iterator<FullElement> iter = lists.get(listNo).iterator();
        return new Iterator<E>() {
            @Override
            public boolean hasNext() {
                return iter.hasNext();
            }

            @Override
            public E next() {
                return iter.next().e;
            }
        };
    }



    private interface Element {}

    private static class HeadElement<E> implements Element {
        private E e;
        private int index;

        public HeadElement(E e, int index) {
            this.e = e;
            this.index = index;
        }
    }

    private static class ContainerElement<E> implements Element {
        private E e;
        private int index;

        public ContainerElement(E e, int index) {
            this.e = e;
            this.index = index;
        }
    }

    private static class EmptyElement implements Element {
        private int index;

        public EmptyElement(int index) {
            this.index = index;
        }
    }
}
