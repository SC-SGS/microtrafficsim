package microtrafficsim.core.shortestpath;

import microtrafficsim.core.map.Mappable;

import java.util.Collection;


/**
 * This interface guarantees a unified access to needed node functions for a shortest path algorithm.
 *
 * @author Jan-Oliver Schmidt, Dominic Parga Cacheiro
 */
public interface ShortestPathNode extends Mappable {

    /**
     * @param incoming The leaving edges are depending on the incoming edge.
     *                 Therefore, this parameter is needed.
     * @return An iterator over all edges that are leaving this node
     */
    Collection<ShortestPathEdge> getLeavingEdges(ShortestPathEdge incoming);
}