package microtrafficsim.core.simulation.utils;

import microtrafficsim.core.logic.nodes.Node;
import microtrafficsim.utils.collections.Triple;

import java.util.Iterator;

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
     * Sets the value for the given origin/destination pair to the given count.
     *
     * @param count the new value for the given origin/destination pair; should be > 0
     * @param origin route's origin
     * @param destination route's destination
     * @throws IllegalArgumentException if count < 0
     */
    void set(int count, Node origin, Node destination);

    /**
     * Adds the given count to the count for the given origin/destination pair. If result <= 0, exception is thrown.
     *
     * @param count this is added to the count for the given origin/destination pair
     * @param origin route's origin
     * @param destination route's destination
     */
    void add(int count, Node origin, Node destination);

    /**
     * @param origin Route's origin
     * @param destination Route's destination
     * @return Number of routes with this origin and this destination
     */
    int get(Node origin, Node destination);

    /**
     * @return An iterator with triples of (origin, destination, # of occurrence)
     */
    @Override
    Iterator<Triple<Node, Node, Integer>> iterator();

    /**
     * @return A matrix containing the (very) same data as this one
     */
    ODMatrix shallowcopy();

    /**
     * <p>
     * Two matrices should be added by adding the count of same elements. This helps by creating mixed
     * origin-destination-definitions. <br>
     * This means: <br>
     * &bull This method is commutative usable.
     * &bull If one matrix contains an element, which is not in the other matrix,
     *
     * @param matrix that should be added to this one
     * @return The result matrix, so this and the given matrix aren't changed
     */
    ODMatrix add(ODMatrix matrix);

    /**
     * Removes all data from this matrix
     */
    void clear();
}
