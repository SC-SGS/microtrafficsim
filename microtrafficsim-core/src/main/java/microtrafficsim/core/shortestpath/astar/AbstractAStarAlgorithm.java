package microtrafficsim.core.shortestpath.astar;

import microtrafficsim.core.shortestpath.*;

import java.util.*;

/**
 * This class represents an abstract A* algorithm. It is abstract because you
 * have to extend "getEdgeWeight(@IDijkstrableEdge edge)" and
 * "estimate({@link ShortestPathEdge} edge)". Therefore you are just allowed to use
 * positive edge weights.
 * 
 * In case you want to implement Dijkstra's algorithm, you have to extend this
 * class and implement getEdgeWeight(...) and estimate(...) should return 0.
 *
 * IShortestPathAlgorithm serves
 * "findShortestPath(IDijkstrableNode start, IDijkstrableNode end)"
 *
 * @author Jan-Oliver Schmidt, Dominic Parga Cacheiro
 */
public abstract class AbstractAStarAlgorithm implements ShortestPathAlgorithm {

	private HashSet<ShortestPathNode> visitedNodes;
	private HashMap<ShortestPathNode, EdgeWeightTuple> predecessors;
	private PriorityQueue<WeightedNode> queue;

	/**
	 * <p>
	 * The A* algorithm uses a node A from the priority queue for actualizing
	 * (if necessary) the weight of each node B, that is a destination of an
	 * edge starting in A. For this, it needs the current weight of A plus the
	 * weight of the mentioned edge.
	 * </p>
	 * 
	 * <p>
	 * Invariants:<br>
	 * This estimation has to be positive (or 0).
	 * </p>
	 * 
	 * @param edge
	 *            The edge that leaves the currently visited node of the A*
	 *            algorithm.
	 * @return Edge weight.
	 */
	protected abstract <T extends ShortestPathEdge> float getEdgeWeight(T edge);

	/**
	 * <p>
	 * The A* algorithm uses a node A from the priority queue for actualizing
	 * (if necessary) the weight of each node B, that is a destination of an
	 * edge starting in A. In addition, the A* algorithm estimates the distance
	 * from this destination to the end of the route to calculate the shortest
	 * path faster.
	 * </p>
	 * <p>
	 * Invariants:<br>
	 * 1) This estimation must be lower or equal to the real shortest path from
	 * destination to the route's end. So it is not allowed to be more
	 * pessimistic than the correct shortest path. Otherwise, it is not
	 * guaranteed, that the A* algorithm returns correct results.<br>
	 * 2) This estimation has to be positive or 0.
	 * </p>
	 * 
	 * @param destination
	 *            The destination node of an edge leaving the currently visited
	 *            node of the A* algorithm.
	 * @param routeDestination
	 *            The end of the route => Last node of the shortest path.
	 * @return Estimation for the shortest path from destination to the end.
	 *         E.g. return 0 for Dijkstra's algorithm or the linear distance as
	 *         usual used approximation.
	 */
	protected abstract <T extends ShortestPathNode> float estimate(T destination, T routeDestination);

	/**
	 * Standard constructor.
	 */
	public AbstractAStarAlgorithm() {
		visitedNodes = new HashSet<>();
		predecessors = new HashMap<>();
		queue = new PriorityQueue<>();
	}

	// |============================|
	// | (i) IShortestPathAlgorithm |
	// |============================|
	@Override
	public Queue<? extends ShortestPathEdge> findShortestPath(ShortestPathNode start, ShortestPathNode end) {
		
		LinkedList<ShortestPathEdge> shortestPath = new LinkedList<>();
		if (start != end) {
			// INIT (the same as in the while-loop below)
			// this is needed to guarantee that each node in the queue has a
			// predecessor
			// => no if-condition if it has a predecessor
			WeightedNode origin = new WeightedNode(start, 0f, estimate(start, end));
			visitedNodes.add(origin.node);
			// iterate over all leaving edges
			Iterator<ShortestPathEdge> leavingEdges = origin.node.getLeavingEdges(null);
			while (leavingEdges.hasNext()) {
				ShortestPathEdge edge = leavingEdges.next();
				ShortestPathNode dest = edge.getDestination();
				float g = origin.g + getEdgeWeight(edge);

				// update predecessors
				EdgeWeightTuple p = predecessors.get(dest);
				if (p == null) {
					predecessors.put(dest, new EdgeWeightTuple(g, edge));
				} else {
					if (p.weight > g) {
						p.weight = g;
						p.edge = edge;
					}
				}

				// push new node into priority queue
				if (!visitedNodes.contains(dest)) {
					queue.add(new WeightedNode(dest, g, estimate(dest, end)));
				}
			}
	
			// ALGORITHM
			// now: each node in the queue has a predecessor
			while (!queue.isEmpty()) {
				origin = queue.poll();
	
				if (!visitedNodes.contains(origin.node)) {
					// if shortest path to end is already found
					if (origin.node == end) {
						// create shortest path
						ShortestPathNode curNode = end;
	
						while (curNode != start) {
							ShortestPathEdge curEdge = predecessors.get(curNode).edge;
							// "unchecked" cast is checked
							shortestPath.addFirst(curEdge);
							curNode = curEdge.getOrigin();
						}
	
						break;
					}
	
					visitedNodes.add(origin.node);
	
					// iterate over all leaving edges
					leavingEdges = origin.node.getLeavingEdges(predecessors.get(origin.node).edge);
					while (leavingEdges.hasNext()) {
						ShortestPathEdge edge = leavingEdges.next();
						ShortestPathNode dest = edge.getDestination();
						float g = origin.g + getEdgeWeight(edge);
						
						// update predecessors
						EdgeWeightTuple p = predecessors.get(dest);
						if (p == null) {
							predecessors.put(dest, new EdgeWeightTuple(g, edge));
						} else {
							if (p.weight > g) {
								p.weight = g;
								p.edge = edge;
							}
						}
						
						// push new node into priority queue
						if (!visitedNodes.contains(dest)) {
							queue.add(new WeightedNode(dest, g, estimate(dest, end)));
						}
					}
				}
			}
		}
		
		// refresh
		visitedNodes.clear();
		predecessors.clear();
		queue.clear();

		return shortestPath;
	}
}
