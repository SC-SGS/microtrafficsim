package microtrafficsim.core.shortestpath;

import java.util.Queue;

/**
 * <p>
 * This interface allows using a shortest path algorithm without knowing the
 * algorithm. You don't have to know very much about the nodes and edges,
 * because they just have to implement {@link ShortestPathNode} and
 * {@link ShortestPathEdge}. Therefore you can implement this interface in many
 * algorithms and graph types.
 * </p>
 * <p>
 * You don't need a graph because dependencies about nodes and edges are saved
 * in them, not in a graph structure.
 * </p>
 * <p>
 * To serve flexibility, the used edge type extending {@link ShortestPathEdge},
 * that are part of the shortest path, is given as a generic type.
 * </p>
 *
 * @author Jan-Oliver Schmidt, Dominic Parga Cacheiro
 */
public interface ShortestPathAlgorithm {
	
	/**
	 * 
	 * 
	 * @return A sequence of edges, starting from the start node to
	 *         the end node.
	 */
	public Queue<? extends ShortestPathEdge> findShortestPath(ShortestPathNode start, ShortestPathNode end);
}