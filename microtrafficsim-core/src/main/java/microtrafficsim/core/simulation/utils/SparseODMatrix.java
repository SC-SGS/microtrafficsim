package microtrafficsim.core.simulation.utils;

import microtrafficsim.core.logic.nodes.Node;
import microtrafficsim.utils.collections.Triple;

import java.util.Comparator;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

/**
 * A basic implementation of ODMatrix using a TreeMap to save memory.
 *
 * @author Dominic Parga Cacheiro
 */
public class SparseODMatrix implements ODMatrix {

    private Map<Node, Map<Node, Integer>> matrix;

    /**
     * Default constructor
     */
    public SparseODMatrix() {
        this.matrix = new TreeMap<>(Comparator.comparingLong(Node::hashCode));
    }

    private void remove(Node origin, Node destination) {
        Map<Node, Integer> tmp = matrix.get(origin);
        if (tmp != null) {
            tmp.remove(destination);
            if (tmp.isEmpty())
                matrix.remove(origin);
        }
    }

    /*
    |==============|
    | (i) ODMatrix |
    |==============|
    */
    @Override
    public void inc(Node origin, Node destination) {

        Map<Node, Integer> tmp = matrix.get(origin);
        // if origin is unknown => put in 1
        if (tmp == null) {
            tmp = new TreeMap<>(Comparator.comparingLong(Node::hashCode));
            matrix.put(origin, tmp);
            tmp.put(destination, 1);
        } else {

            Integer count = tmp.get(destination);
            // if destination is unknown for this origin => put in 1
            if (count == null)
                tmp.put(destination, 1);
            else
                tmp.put(destination, count + 1);
        }
    }

    @Override
    public void dec(Node origin, Node destination) {
        Map<Node, Integer> tmp = matrix.get(origin);
        // if origin is unknown => do nothing
        if (tmp != null) {
            Integer count = tmp.get(destination);
            // if destination is unknown for this origin => do nothing
            if (count != null)
                if (count <= 1)
                    remove(origin, destination);
                else
                    tmp.put(destination, count - 1);
        }
    }

    /**
     * If count == 0, the entry is removed from the internal used {@code TreeMap}.
     */
    @Override
    public void set(int count, Node origin, Node destination) {

        if (count < 0)
            throw new IllegalArgumentException("Given count has to be greater than or equal to 0.");

        if (count == 0) {
            remove(origin, destination);
            return;
        }

        Map<Node, Integer> tmp = matrix.get(origin);
        if (tmp == null) {
            tmp = new TreeMap<>(Comparator.comparingLong(Node::hashCode));
            matrix.put(origin, tmp);
        }
        tmp.put(destination, count);
    }

    @Override
    public void add(int count, Node origin, Node destination) {
        Map<Node, Integer> tmp = matrix.get(origin);
        if (tmp != null) {
            Integer oldCount = tmp.get(destination);
            if (oldCount != null)
                count += oldCount;
        }
        set(count, origin, destination);
    }

    @Override
    public int get(Node origin, Node destination) {

        Map<Node, Integer> tmp = matrix.get(origin);

        if (tmp == null)
            return 0;

        Integer count = tmp.get(destination);
        if (count == null)
            return 0;

        return count;
    }

    @Override
    public SparseODMatrix shallowcopy() {
        SparseODMatrix copy = new SparseODMatrix();
        copy.matrix = new TreeMap<>(matrix);
        return copy;
    }

    @Override
    public SparseODMatrix add(ODMatrix odMatrix) {
        SparseODMatrix result = shallowcopy();

        for (Triple<Node, Node, Integer> triple : odMatrix) {
            Node origin = triple.obj0;
            Node destination = triple.obj1;
            int count = triple.obj2;
            result.add(count, origin, destination);
        }

        return result;
    }

    /**
     * @return An iterator with triples of (origin, destination, # of occurrence)
     */
    @Override
    public Iterator<Triple<Node, Node, Integer>> iterator() {
        return new Iterator<Triple<Node, Node, Integer>>() {

            private Iterator<Node>
                    origins              = matrix.keySet().iterator(),
                    current_destinations = null;
            private Node origin;

            @Override
            public boolean hasNext() {

                if (origins.hasNext())
                    return true;

                if (current_destinations == null)
                    return false;

                return current_destinations.hasNext();
            }

            @Override
            public Triple<Node, Node, Integer> next() {

                // "if" instead of "while", because this class guarantees that every entry has children
                if (current_destinations == null || !current_destinations.hasNext()) {
                    origin = origins.next();
                    current_destinations = matrix.get(origin).keySet().iterator();
                }

                Node dest = current_destinations.next();
                return new Triple<>(origin, dest, matrix.get(origin).get(dest));
            }
        };
    }

    /**
     * Re-initializes the internal {@code TreeMap} to ensure, the memory usage is minimal.
     */
    @Override
    public void clear() {
        matrix = new TreeMap<>(Comparator.comparingLong(Node::hashCode));
    }
}
