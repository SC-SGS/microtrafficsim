package microtrafficsim.core.shortestpath.impl;

import microtrafficsim.core.shortestpath.astar.AbstractAStarAlgorithm;
import microtrafficsim.core.shortestpath.ShortestPathEdge;
import microtrafficsim.core.shortestpath.ShortestPathNode;

/**
 * This class extends the abstract A* algorithm for defining the edge weights
 * and heuristic function meeting the Dijkstra's algorithms conditions.
 *
 * @author Dominic Parga Cacheiro
 */
public class ShortestWayDijkstra extends AbstractAStarAlgorithm {

	@Override
	protected <T extends ShortestPathEdge> float getEdgeWeight(T edge) {
		return edge.getLength();
	}

	@Override
	protected <T extends ShortestPathNode> float estimate(T destination, T routeDestination) {
		return 0;
	}
}
