package microtrafficsim.core.logic;

import java.util.Collection;
import java.util.Iterator;
import java.util.Queue;


public class Route implements Queue<DirectedEdge> {

    private Node                start;
    private Node                end;
    private Queue<DirectedEdge> path;

    public Route(Node start, Node end, Queue<DirectedEdge> path) {
        this.start = start;
        this.end   = end;
        this.path  = path;
    }

    public Node getStart() {
        return start;
    }

    public Node getEnd() {
        return end;
    }

    public int calcLength() {
        Iterator<DirectedEdge> iter = iterator();

        int len = 0;
        while (iter.hasNext()) {
            len = len + iter.next().getLength();
        }

        return len;
    }

    // |===========|
    // | (i) Queue |
    // |===========|
    @Override
    public int size() {
        return path.size();
    }

    @Override
    public boolean isEmpty() {
        return path.isEmpty();
    }

    @Override
    public boolean contains(Object o) {
        return path.contains(o);
    }

    @Override
    public Iterator<DirectedEdge> iterator() {
        return path.iterator();
    }

    @Override
    public Object[] toArray() {
        return path.toArray();
    }

    @Override
    public <T> T[] toArray(T[] a) {
        return path.toArray(a);
    }

    @Override
    public boolean remove(Object o) {
        return path.remove(o);
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        return path.containsAll(c);
    }

    @Override
    public boolean addAll(Collection<? extends DirectedEdge> c) {
        return path.addAll(c);
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        return path.removeAll(c);
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        return path.retainAll(c);
    }

    @Override
    public void clear() {
        path.clear();
    }

    @Override
    public boolean add(DirectedEdge e) {
        return path.add(e);
    }

    @Override
    public boolean offer(DirectedEdge e) {
        return path.offer(e);
    }

    @Override
    public DirectedEdge remove() {
        return path.remove();
    }

    @Override
    public DirectedEdge poll() {
        return path.poll();
    }

    @Override
    public DirectedEdge element() {
        return path.element();
    }

    @Override
    public DirectedEdge peek() {
        return path.peek();
    }
}