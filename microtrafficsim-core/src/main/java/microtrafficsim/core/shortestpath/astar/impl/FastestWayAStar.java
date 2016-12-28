package microtrafficsim.core.shortestpath.astar.impl;

import microtrafficsim.core.shortestpath.ShortestPathEdge;
import microtrafficsim.core.shortestpath.astar.AStar;
import microtrafficsim.math.HaversineDistanceCalculator;


/**
 * This class extends the A* algorithm for defining the edge weights and heuristic function meeting the Dijkstra's
 * algorithms conditions. The extension causes that the found way is the fastest one.
 *
 * @author Jan-Oliver Schmidt, Dominic Parga Cacheiro
 */
public class FastestWayAStar extends AStar {

    /**
     * @param metersPerCell We use cells in our traffic simulation, but the distance is calculated in meters.
     * @param maxCellsPerS Because this is the fastest, not the shortest way, the cells have to be converted to
     *                  milliseconds.
     */
    public FastestWayAStar(float metersPerCell, int maxCellsPerS) {
        super(
                ShortestPathEdge::getTimeCostMillis,
                (destination, routeDestination) ->
                        // after HaversineDistance/metersPerCell: result in cells
                        // BUT results for estimation should be in milliseconds
                        // => take maximum speed = 6 cell/s
                        // => 1000 ms / (6 cells) * ? m / (7.5 m/cell)
                        // => 1000 / 6 * distance / 7.5 ms
                        (float) (1000 / maxCellsPerS * (int) (HaversineDistanceCalculator.getDistance(
                                destination.getCoordinate(),
                                routeDestination.getCoordinate()) / metersPerCell))
        );
    }
}
