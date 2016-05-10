package microtrafficsim.core.frameworks.shortestpath.astar.impl;

import microtrafficsim.core.frameworks.shortestpath.astar.AbstractAStarAlgorithm;
import microtrafficsim.core.frameworks.shortestpath.IDijkstrableEdge;
import microtrafficsim.core.frameworks.shortestpath.IDijkstrableNode;

/**
 * This class extends the abstract A* algorithm for defining the edge weights
 * and heuristic function meeting the Dijkstra's algorithms conditions.
 *
 * @author Dominic Parga Cacheiro
 */
public class ShortestWayDijkstra extends AbstractAStarAlgorithm {

	@Override
	protected <T extends IDijkstrableEdge> float getEdgeWeight(T edge) {
		return edge.getLength();
	}

	@Override
	protected <T extends IDijkstrableNode> float estimate(T destination, T routeDestination) {
		return 0;
	}
}
