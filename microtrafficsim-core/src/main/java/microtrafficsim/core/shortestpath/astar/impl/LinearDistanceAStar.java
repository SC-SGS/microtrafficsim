package microtrafficsim.core.shortestpath.astar.impl;

import microtrafficsim.core.shortestpath.astar.AStarAlgorithm;
import microtrafficsim.math.HaversineDistanceCalculator;


/**
 * This class extends the abstract A* algorithm for defining the edge weights
 * and heuristic function meeting the Dijkstra's algorithms conditions.
 *
 * @author Jan-Oliver Schmidt, Dominic Parga Cacheiro
 */
public class LinearDistanceAStar extends AStarAlgorithm {

    public LinearDistanceAStar(float metersPerCell) {
        super(
                edge -> (float)edge.getLength(),
                (destination, routeDestination) -> (float) HaversineDistanceCalculator.getDistance(
                        destination.getCoordinate(), routeDestination.getCoordinate()) / metersPerCell
        );
    }
}
