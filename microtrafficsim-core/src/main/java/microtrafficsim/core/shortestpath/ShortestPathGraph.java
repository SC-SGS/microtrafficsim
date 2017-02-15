package microtrafficsim.core.shortestpath;

import java.util.Set;

/**
 * @author Dominic Parga Cacheiro
 */
public interface ShortestPathGraph {

    Set<? extends ShortestPathNode> getNodes();

    Set<? extends ShortestPathEdge> getEdges();
}
