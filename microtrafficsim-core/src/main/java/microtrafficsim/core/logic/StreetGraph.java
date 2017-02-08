package microtrafficsim.core.logic;

import microtrafficsim.core.logic.nodes.Node;
import microtrafficsim.core.logic.streets.DirectedEdge;
import microtrafficsim.core.simulation.builder.MapInitializer;
import microtrafficsim.core.simulation.builder.impl.StreetGraphInitializer;
import microtrafficsim.utils.Resettable;

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
public class StreetGraph implements MapInitializer, Resettable {

    public final float            minLat, maxLat, minLon, maxLon;
    private HashSet<Node>         nodes;
    private HashSet<DirectedEdge> edges;
    private MapInitializer        initializer;

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

        initializer = new StreetGraphInitializer();
    }

    /**
     * @return instance of {@link java.util.Collections.UnmodifiableSet} of the nodes in this graph
     */
    public Set<Node> getNodes() {
        return Collections.unmodifiableSet(nodes);
    }

    /**
     * @return instance of {@link java.util.Collections.UnmodifiableSet} of the edges in this graph
     */
    public Set<DirectedEdge> getEdges() {
        return Collections.unmodifiableSet(edges);
    }

    /*
    |==================|
    | change structure |
    |==================|
    */
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


    /*
    |====================|
    | (i) MapInitializer |
    |====================|
    */
    /**
     * Calls {@link #postprocessFreshStreetGraph(StreetGraph, long) postprocessFreshStreetGraph(this, seed)}
     */
    public StreetGraph postprocessFresh(long seed) {
        return postprocessFreshStreetGraph(this, seed);
    }

    /**
     * Calls {@link #postprocessStreetGraph(StreetGraph, long) postprocessStreetGraph(this, seed)}
     */
    public StreetGraph postprocess(long seed) {
        return postprocessStreetGraph(this, seed);
    }

    @Override
    public StreetGraph postprocessFreshStreetGraph(StreetGraph protoGraph, long seed) {
        return initializer.postprocessFreshStreetGraph(protoGraph, seed);
    }

    @Override
    public StreetGraph postprocessStreetGraph(StreetGraph protoGraph, long seed) {
        return initializer.postprocessStreetGraph(protoGraph, seed);
    }


    /*
    |================|
    | (i) Resettable |
    |================|
    */
    /**
     * This method resets the nodes and edges of the streetgraph. The "new" streetgraph will NOT be identical to the
     * previous one (e.g. random numbers will be different).
     */
    @Override
    public void reset() {
        nodes.forEach(Node::reset);
        edges.forEach(DirectedEdge::reset);
    }
}