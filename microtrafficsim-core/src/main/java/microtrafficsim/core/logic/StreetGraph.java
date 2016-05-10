package microtrafficsim.core.logic;

import microtrafficsim.core.logic.vehicles.AbstractVehicle;

import java.util.HashSet;
import java.util.Iterator;

/**
 * This graph just saves all @Node#s and all @DirectedEdge#s in a @HashSet. All
 * dependencies between nodes and edges are saved in these classes, not in this
 * graph.
 *
 * @author Jan-Oliver Schmidt, Dominic Parga Cacheiro
 */
public class StreetGraph {

	private HashSet<Node> nodes;
	private HashSet<DirectedEdge> edges;
	public final float minLat, maxLat, minLon, maxLon;

	/**
	 * Just a standard constructor.
	 */
	public StreetGraph(float minLat, float maxLat, float minLon, float maxLon) {
		this.minLat = minLat;
		this.maxLat = maxLat;
		this.minLon = minLon;
		this.maxLon = maxLon;
		nodes = new HashSet<>();
		edges = new HashSet<>();
	}

	public int getNumberOfNodes() {
		return nodes.size();
	}

	/**
	 * @return Iterator over all nodes
	 */
	public Iterator<Node> getNodeIterator() {
		return nodes.iterator();
	}
	
	// |==================|
	// | change structure |
	// |==================|
	/**
	 * Adds the {@link Node}s of the given {@link DirectedEdge} to the node set
	 * and the edge to the edge set.
	 *
	 * @param edge
	 *            This edge will be added to the graph.
	 */
	public void registerEdgeAndNodes(DirectedEdge edge) {
		nodes.add(edge.getOrigin());
		nodes.add(edge.getDestination());
		edges.add(edge);
	}

	/**
	 * Calculates the edge indices for all nodes in this graph.
	 * <p>
	 * IMPORTANT:<br>
	 * This method must be called after all nodes and edges are added to the
	 * graph. Otherwise some edges would have no index and crossing logic would
	 * be unpredictable.
	 * </p>
	 */
	public void calcEdgeIndicesPerNode() {
        nodes.forEach(Node::calculateEdgeIndices);
	}

	@Override
	public String toString() {
		String output = "|==========================================|\n";
		output += "| Graph\n";
		output += "|==========================================|\n";

		output += "> Nodes\n";
		for (Node node : nodes) {
			output += node + "\n";
		}

		output += "> Edges\n";
		for (DirectedEdge edge : edges) {
			output += edge + "\n";
		}

		return output;
	}

	/**
	 * This method just calls "vehicle.spawn()". For more information, see
	 * {@link AbstractVehicle}.{@link AbstractVehicle#spawn spawn()}.
	 * 
	 * @param vehicle
	 *            This vehicle should be added to the StreetGraph.
	 * @return True, if spawning was successful; False if despawned.
	 */
	public boolean addVehicle(AbstractVehicle vehicle) {
		return vehicle.register();
	}
}