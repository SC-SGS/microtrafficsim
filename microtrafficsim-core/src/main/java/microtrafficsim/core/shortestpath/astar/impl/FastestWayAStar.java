package microtrafficsim.core.shortestpath.astar.impl;

import microtrafficsim.core.shortestpath.ShortestPathEdge;
import microtrafficsim.core.shortestpath.astar.AStarAlgorithm;
import microtrafficsim.math.HaversineDistanceCalculator;


/**
 * This class extends the abstract A* algorithm for defining the edge weights
 * and heuristic function meeting the Dijkstra's algorithms conditions.
 *
 * @author Jan-Oliver Schmidt, Dominic Parga Cacheiro
 */
public class FastestWayAStar extends AStarAlgorithm {

    public FastestWayAStar(float metersPerCell) {
        super(
                ShortestPathEdge::getTimeCostMillis,
                (destination, routeDestination) ->
                        // after HaversineDistance/metersPerCell: result in cells
                        // BUT results for estimation should be in milliseconds
                        // => take maximum speed = 6 cell/s
                        // => 1000 ms / (6 cells) * ? m / (7.5 m/cell)
                        // => 1000 / 6 * distance / 7.5 ms
                        (float) (1000 / 6 * (int) (HaversineDistanceCalculator.getDistance(
                                destination.getCoordinate(),
                                routeDestination.getCoordinate()) / metersPerCell))
        );
    }
}
