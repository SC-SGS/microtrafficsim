package microtrafficsim.core.logic.routes;

import microtrafficsim.core.logic.nodes.Node;
import microtrafficsim.core.logic.streets.DirectedEdge;
import microtrafficsim.core.shortestpath.ShortestPathEdge;

/**
 * @author Dominic Parga Cacheiro
 */
public interface Route extends Iterable<DirectedEdge> {
    default boolean isMonitored() {
        return false;
    }

    default void setMonitored(boolean isMonitored) {

    }

    int getSpawnDelay();

    void setSpawnDelay(int spawnDelay);

    Node getOrigin();

    Node getDestination();


    Route clone();

    boolean isEmpty();

    int size();

    DirectedEdge peek();

    DirectedEdge pop();
}
