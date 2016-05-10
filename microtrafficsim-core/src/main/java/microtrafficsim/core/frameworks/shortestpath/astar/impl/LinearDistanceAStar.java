package microtrafficsim.core.frameworks.shortestpath.astar.impl;

import microtrafficsim.core.frameworks.shortestpath.astar.AbstractAStarAlgorithm;
import microtrafficsim.core.frameworks.shortestpath.IDijkstrableEdge;
import microtrafficsim.core.frameworks.shortestpath.IDijkstrableNode;
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
	protected <T extends IDijkstrableEdge> float getEdgeWeight(T edge) {
		return edge.getLength();
	}

	@Override
	protected <T extends IDijkstrableNode> float estimate(T destination, T routeDestination) {
		return (float) HaversineDistanceCalculator.getDistance(destination.getCoordinate(), routeDestination.getCoordinate()) / metersPerCell;
	}
}
