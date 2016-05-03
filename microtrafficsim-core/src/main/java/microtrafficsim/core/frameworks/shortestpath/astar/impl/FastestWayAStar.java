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
public class FastestWayAStar extends AbstractAStarAlgorithm {

	private float metersPerCell;

	public FastestWayAStar(float metersPerCell) {
		super();
		this.metersPerCell = metersPerCell;		
	}
	
	@Override
	protected <T extends IDijkstrableEdge> float getEdgeWeight(T edge) {
		return edge.getTimeCostMillis();
	}

	@Override
	protected <T extends IDijkstrableNode> float estimate(T destination, T routeDestination) {
		// after HaversineDistance/metersPerCell: result in cells
		// BUT results should be in milliseconds
		// => take minimum speed = 5 cell/s
		// => 1000ms = 1s
		return 200 * (int)(
                HaversineDistanceCalculator.getDistance(
                        destination.getCoordinate(),
                        routeDestination.getCoordinate()
                ) / metersPerCell
        );
	}
}
