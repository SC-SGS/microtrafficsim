package microtrafficsim.core.simulation.utils;

import microtrafficsim.core.logic.Node;
import microtrafficsim.utils.collections.Triple;

/**
 * This interface represents an Origin-Destination-Matrix containing all start nodes as key and all destination
 * nodes as values. In addition to the destination nodes, the number of routes with this start/end node is saved.
 *
 * @author Dominic Parga Cacheiro
 */
public interface ODMatrix extends Iterable<Triple<Node, Node, Integer>> {

    void inc(Node origin, Node destination);

    /**
     * This method decrements the value for the given origin/destination pair. It should check whether the value gets
     * 0 to save memory in this case.
     *
     * @param origin
     * @param destination
     */
    void dec(Node origin, Node destination);

    void set(int count, Node origin, Node destination);

    int get(Node origin, Node destination);
}
