package microtrafficsim.core.shortestpath.astar.impl;

import microtrafficsim.core.shortestpath.astar.BidirectionalAStar;
import microtrafficsim.math.HaversineDistanceCalculator;


/**
 * This class extends the bidirectional A* algorithm for defining the edge weights and heuristic function meeting the
 * Dijkstra's algorithms conditions. The found way is the shortest one.
 *
 * @author Jan-Oliver Schmidt, Dominic Parga Cacheiro
 */
public class LinearDistanceBidirectionalAStar extends BidirectionalAStar {

    public LinearDistanceBidirectionalAStar(float metersPerCell) {
        super(
                edge -> (float)edge.getLength(),
                (destination, routeDestination) -> (float) HaversineDistanceCalculator.getDistance(
                        destination.getCoordinate(), routeDestination.getCoordinate()) / metersPerCell
        );
    }
}
