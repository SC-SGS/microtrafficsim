package microtrafficsim.core.simulation.utils;

import microtrafficsim.core.logic.Node;
import microtrafficsim.utils.collections.Triple;
import microtrafficsim.utils.datacollection.Bundle;
import microtrafficsim.utils.datacollection.Data;
import microtrafficsim.utils.datacollection.Tag;

import java.util.HashMap;
import java.util.Iterator;

/**
 * A basic implementation of ODMatrix using a HashMap to save memory.
 *
 * @author Dominic Parga Cacheiro
 */
public class SparseODMatrix implements ODMatrix {

    private HashMap<Node, HashMap<Node, Integer>> matrix;

    /**
     * Default constructor
     */
    public SparseODMatrix() {
        this.matrix = new HashMap<>();
    }

    /*
    |==============|
    | (i) ODMatrix |
    |==============|
    */

    @Override
    public void inc(Node origin, Node destination) {

        HashMap<Node, Integer> tmp = matrix.get(origin);
        // if origin is unknown => put in 1
        if (tmp == null) {
            tmp = new HashMap<>();
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
        HashMap<Node, Integer> tmp = matrix.get(origin);
        // if origin is unknown => do nothing
        if (tmp != null) {
            Integer count = tmp.get(destination);
            // if destination is unknown for this origin => do nothing
            if (count != null)
                if (count <= 1) {
                    tmp.remove(destination);
                    if (tmp.isEmpty())
                        matrix.remove(origin);
                } else
                    tmp.put(destination, count - 1);
        }
    }

    @Override
    public void set(int count, Node origin, Node destination) {

        HashMap<Node, Integer> tmp = matrix.get(origin);

        if (tmp == null) {
            tmp = new HashMap<>();
            matrix.put(origin, tmp);
        }
        tmp.put(destination, count);
    }

    @Override
    public int get(Node origin, Node destination) {

        HashMap<Node, Integer> tmp = matrix.get(origin);

        if (tmp == null)
            return 0;

        Integer count = tmp.get(destination);
        if (count == null)
            return 0;

        return count;
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

                // if and not while, because this class guarantees that every entry has children
                if (current_destinations == null || !current_destinations.hasNext()) {
                    origin = origins.next();
                    current_destinations = matrix.get(origin).keySet().iterator();
                }

                Node dest = current_destinations.next();
                return new Triple<>(origin, dest, matrix.get(origin).get(dest));
            }
        };
    }
}
