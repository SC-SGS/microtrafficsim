package microtrafficsim.core.logic;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;


/**
 * This graph just saves all {@link Node}s and all {@link DirectedEdge}s in a {@link HashSet}. All
 * dependencies between nodes and edges are saved in these classes, not in this
 * graph. This class is just a container for them with some functions for interaction.
 *
 * @author Jan-Oliver Schmidt, Dominic Parga Cacheiro
 */
public class StreetGraph {

    public final float            minLat, maxLat, minLon, maxLon;
    private HashSet<Node>         nodes;
    private HashSet<DirectedEdge> edges;

    /**
     * Just a standard constructor.
     */
    public StreetGraph(float minLat, float maxLat, float minLon, float maxLon) {
        this.minLat = minLat;
        this.maxLat = maxLat;
        this.minLon = minLon;
        this.maxLon = maxLon;
        nodes       = new HashSet<>();
        edges       = new HashSet<>();
    }

    /**
     * @return instance of {@link java.util.Collections.UnmodifiableSet} of the nodes in this graph
     */
    public Set<Node> getNodes() {
        return Collections.unmodifiableSet(nodes);
    }

    // |==================|
    // | change structure |
    // |==================|

    /**
     * Adds the {@link Node}s of the given {@link DirectedEdge} to the node set
     * and the edge to the edge set.
     *
     * @param edge This edge will be added to the graph.
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
        String output = "|==========================================|\n"
            + "| Graph\n"
            + "|==========================================|\n";

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
     * This method resets the nodes and edges of the streetgraph. The "new" streetgraph will NOT be identical to the
     * previous one (e.g. random numbers will be different).
     */
    public void reset() {
        nodes.forEach(Node::reset);
        edges.forEach(DirectedEdge::reset);
    }
}