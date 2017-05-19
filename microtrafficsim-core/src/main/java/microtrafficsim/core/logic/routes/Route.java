package microtrafficsim.core.logic.routes;

import microtrafficsim.core.logic.nodes.Node;
import microtrafficsim.core.shortestpath.ShortestPathEdge;

/**
 * @author Dominic Parga Cacheiro
 */
public interface Route extends Iterable<ShortestPathEdge<Node>> {
    int getSpawnDelay();

    void setSpawnDelay(int spawnDelay);

    Node getOrigin();

    Node getDestination();


    Route clone();

    boolean isEmpty();

    int size();

    ShortestPathEdge<Node> peek();

    ShortestPathEdge<Node> pop();
}
