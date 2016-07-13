package microtrafficsim.core.shortestpath.impl;

import microtrafficsim.core.shortestpath.astar.AbstractAStarAlgorithm;
import microtrafficsim.core.shortestpath.ShortestPathEdge;
import microtrafficsim.core.shortestpath.ShortestPathNode;
import microtrafficsim.math.HaversineDistanceCalculator;


/**
 * This class extends the abstract A* algorithm for defining the edge weights
 * and heuristic function meeting the Dijkstra's algorithms conditions.
 *
 * @author Jan-Oliver Schmidt, Dominic Parga Cacheiro
 */
public class LinearDistanceAStar extends AbstractAStarAlgorithm {

    private float metersPerCell;

    public LinearDistanceAStar(float metersPerCell) {
        super();
        this.metersPerCell = metersPerCell;
    }

    @Override
    protected <T extends ShortestPathEdge> float getEdgeWeight(T edge) {
        return edge.getLength();
    }

    @Override
    protected <T extends ShortestPathNode> float estimate(T destination, T routeDestination) {
        return (float) HaversineDistanceCalculator.getDistance(
                destination.getCoordinate(), routeDestination.getCoordinate())
                / metersPerCell;
    }
}
