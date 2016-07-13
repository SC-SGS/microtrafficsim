package microtrafficsim.core.shortestpath.impl;

import microtrafficsim.core.shortestpath.ShortestPathEdge;
import microtrafficsim.core.shortestpath.ShortestPathNode;
import microtrafficsim.core.shortestpath.astar.AbstractAStarAlgorithm;
import microtrafficsim.math.HaversineDistanceCalculator;


/**
 * This class extends the abstract A* algorithm for defining the edge weights
 * and heuristic function meeting the Dijkstra's algorithms conditions.
 *
 * @author Jan-Oliver Schmidt, Dominic Parga Cacheiro
 */
public class FastestWayAStar extends AbstractAStarAlgorithm {

    private float metersPerCell;

    public FastestWayAStar(float metersPerCell) {
        super();
        this.metersPerCell = metersPerCell;
    }

    @Override
    protected <T extends ShortestPathEdge> float getEdgeWeight(T edge) {
        return edge.getTimeCostMillis();
    }

    @Override
    protected <T extends ShortestPathNode> float estimate(T destination, T routeDestination) {
        // after HaversineDistance/metersPerCell: result in cells
        // BUT results for estimation should be in milliseconds
        // => take maximum speed = 6 cell/s
        // => 1000 ms / (6 cells) * ? m / (7.5 m/cell)
        // => 1000 / 6 * ? / 7.5 ms
        return 1000 / 6 * (int) (HaversineDistanceCalculator.getDistance(
                destination.getCoordinate(), routeDestination.getCoordinate())
                / metersPerCell);
    }
}
