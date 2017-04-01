package microtrafficsim.core.shortestpath.astar;

import microtrafficsim.core.shortestpath.ShortestPathEdge;
import microtrafficsim.core.shortestpath.ShortestPathNode;
import microtrafficsim.math.HaversineDistanceCalculator;


/**
 * Functions to create various bidirectional A* algorithms.
 *
 * @author Dominic Parga Cacheiro, Maximilian Luz
 */
public class BidirectionalAStars {
    private BidirectionalAStars() {}

    /**
     * Create a distance-based bidirectional Dijkstra algorithm, returning the shortest path.
     *
     * @return Standard implementation of Dijkstra's algorithm for calculating the shortest (not necessarily fastest)
     * path using {@link ShortestPathEdge#getLength()}. As Dijkstra's algorithm does not employ a cost estimation
     * heuristic, it is set to constant zero.
     */
    public static <N extends ShortestPathNode<E>, E extends ShortestPathEdge<N>> BidirectionalAStar<N, E>
        shortestPathDijkstra()
    {
        return new BidirectionalAStar<>(
                edge -> (double) edge.getLength(),
                (node, dest) -> 0.0
        );
    }

    /**
     * Create a distance-based bidirectional A* algorithm, returning the shortest path.
     *
     * @return Standard implementation of the A* algorithm for calculating the shortest (not necessarily fastest)
     * path using {@link ShortestPathEdge#getLength()} and the linear distance, calculated using
     * {@link HaversineDistanceCalculator}, as heuristic.
     */
    public static <N extends ShortestPathNode<E>, E extends ShortestPathEdge<N>> BidirectionalAStar<N, E>
        shortestPathAStar(double metersPerCell)
    {
        return new BidirectionalAStar<>(
                edge -> (double) edge.getLength(),
                (node, dest) -> (HaversineDistanceCalculator.getDistance(node.getCoordinate(), dest.getCoordinate())
                        / metersPerCell)
        );
    }

    /**
     * Create a time-based bidirectional A* algorithm, returning the fastest path.
     *
     * @return Standard implementation of the A* algorithm for calculating the shortest (not necessarily fastest)
     * path using {@link ShortestPathEdge#getLength()} and the linear distance, calculated using
     * {@link HaversineDistanceCalculator}, as heuristic.
     */
    public static <N extends ShortestPathNode<E>, E extends ShortestPathEdge<N>> BidirectionalAStar<N, E>
        fastestPathAStar(double metersPerCell, double maxCellsPerSec) {
        return new BidirectionalAStar<>(
                ShortestPathEdge::getTimeCostMillis,
                (destination, routeDestination) ->
                        // after HaversineDistance/metersPerCell: result in cells
                        // BUT results for estimation should be in milliseconds
                        // => take maximum speed = 6 cell/s
                        // => 1000 ms / (6 cells) * ? m / (7.5 m/cell)
                        // => 1000 / 6 * distance / 7.5 ms
                        (1000 / maxCellsPerSec * (int) (HaversineDistanceCalculator.getDistance(
                                destination.getCoordinate(), routeDestination.getCoordinate()) / metersPerCell))
        );
    }
}
