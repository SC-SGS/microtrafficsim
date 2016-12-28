package microtrafficsim.core.shortestpath.astar.impl;

import microtrafficsim.core.shortestpath.astar.AStar;
import microtrafficsim.math.HaversineDistanceCalculator;


/**
 * This class extends the A* algorithm for defining the edge weights and heuristic function meeting the Dijkstra's
 * algorithms conditions. The found way is the shortest one.
 *
 * @author Jan-Oliver Schmidt, Dominic Parga Cacheiro
 */
public class LinearDistanceAStar extends AStar {

    public LinearDistanceAStar(float metersPerCell) {
        super(
                edge -> (float)edge.getLength(),
                (destination, routeDestination) -> (float) HaversineDistanceCalculator.getDistance(
                        destination.getCoordinate(), routeDestination.getCoordinate()) / metersPerCell
        );
    }
}
