package microtrafficsim.core.shortestpath.astar;

import microtrafficsim.core.shortestpath.ShortestPathEdge;
import microtrafficsim.core.shortestpath.ShortestPathNode;
import microtrafficsim.math.HaversineDistanceCalculator;


/**
 * Functions to create various A* algorithms.
 *
 * @author Dominic Parga Cacheiro, Maximilian Luz
 */
public class AStars {
    private AStars() {}

    /**
     * Create a distance-based Dijkstra algorithm, returning the shortest path.
     *
     * @return Standard implementation of Dijkstra's algorithm for calculating the shortest (not necessarily fastest)
     * path using {@link ShortestPathEdge#getLength()}
     */
    public static <N extends ShortestPathNode<E>, E extends ShortestPathEdge<N>> AStar<N, E>
        shortestPathDijkstra()
    {
        return new AStar<>(
                edge -> (double) edge.getLength(),
                (destination, routeDestination) -> 0.0
        );
    }

    /**
     * Create a distance-based A* algorithm, returning the shortest path.
     *
     * @return Standard implementation of the A* algorithm for calculating the shortest (not necessarily fastest)
     * path using {@link ShortestPathEdge#getLength()} and the linear distance, calculated using
     * {@link HaversineDistanceCalculator}, as heuristic.
     */
    public static <N extends ShortestPathNode<E>, E extends ShortestPathEdge<N>> AStar<N, E>
        shortestPathAStar(double metersPerCell)
    {
        return new AStar<>(
                edge -> (double) edge.getLength(),
                (node, dest) -> {
                    double distanceInMeters = HaversineDistanceCalculator.getDistance(
                            node.getCoordinate(), dest.getCoordinate());
                    return distanceInMeters / metersPerCell;
                }
        );
    }

    /**
     * Create a time-based A* algorithm, returning the fastest path.
     *
     * @return Standard implementation of the A* algorithm for calculating the shortest (not necessarily fastest)
     * path using {@link ShortestPathEdge#getLength()} and the linear distance, calculated using
     * {@link HaversineDistanceCalculator}, as heuristic.
     */
    public static <N extends ShortestPathNode<E>, E extends ShortestPathEdge<N>> AStar<N, E>
        fastestPathAStar(double metersPerCell, double maxCellsPerSecond)
    {
        return new AStar<>(
                ShortestPathEdge::getTimeCostMillis,
                (destination, routeDestination) -> {

                    int distanceInCells = (int) (HaversineDistanceCalculator.getDistance(
                            destination.getCoordinate(),
                            routeDestination.getCoordinate()) / metersPerCell);

                    return 1000 * distanceInCells / maxCellsPerSecond;
                });
    }
}
