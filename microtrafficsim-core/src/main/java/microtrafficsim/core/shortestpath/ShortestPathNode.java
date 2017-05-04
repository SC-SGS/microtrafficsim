package microtrafficsim.core.shortestpath;

import microtrafficsim.core.map.Mappable;

import java.util.Set;


/**
 * This interface guarantees a unified access to needed node functions for a shortest path algorithm.
 *
 * @author Jan-Oliver Schmidt, Dominic Parga Cacheiro
 */
public interface ShortestPathNode<E extends ShortestPathEdge> extends Mappable {

    /**
     * @param incoming The leaving edges are depending on the incoming edge.
     *                 Therefore, this parameter is needed.
     * @return A set of all edges that are leaving this node
     */
    Set<? extends E> getLeavingEdges(E incoming);

    /**
     * @return A set of all edges that are coming in this node
     */
    Set<? extends E> getIncomingEdges();
}