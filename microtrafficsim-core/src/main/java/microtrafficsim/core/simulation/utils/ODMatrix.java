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

    /**
     * Increments the value for the given origin/destination pair.
     *
     * @param origin Route's origin
     * @param destination Route's destination
     */
    void inc(Node origin, Node destination);

    /**
     * Decrements the value for the given origin/destination pair. It should check whether the value gets
     * 0 to save memory in this case.
     *
     * @param origin Route's origin
     * @param destination Route's destination
     */
    void dec(Node origin, Node destination);

    /**
     * Sets the value for the given origin/destination pair to the given count. If count <= 0, nothing should be done.
     *
     * @param count The new value for the given origin/destination pair; should be > 0
     * @param origin Route's origin
     * @param destination Route's destination
     */
    void set(int count, Node origin, Node destination);

    /**
     * @param origin Route's origin
     * @param destination Route's destination
     * @return Number of routes with this origin and this destination
     */
    int get(Node origin, Node destination);
}
