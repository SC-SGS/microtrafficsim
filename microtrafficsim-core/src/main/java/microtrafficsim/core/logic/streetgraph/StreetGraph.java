package microtrafficsim.core.logic.streetgraph;

import microtrafficsim.core.logic.nodes.Node;
import microtrafficsim.core.logic.streets.DirectedEdge;
import microtrafficsim.core.simulation.builder.MapInitializer;
import microtrafficsim.core.simulation.builder.impl.StreetGraphInitializer;

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
public class StreetGraph implements Graph {

    private final float           minLat, maxLat, minLon, maxLon;
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
    |===========|
    | (i) Graph |
    |===========|
    */
    @Override
    public float getMinLat() {
        return minLat;
    }

    @Override
    public float getMaxLat() {
        return maxLat;
    }

    @Override
    public float getMinLon() {
        return minLon;
    }

    @Override
    public float getMaxLon() {
        return maxLon;
    }

    @Override
    public Set<Node> getNodes() {
        return Collections.unmodifiableSet(nodes);
    }

    @Override
    public Set<DirectedEdge> getEdges() {
        return Collections.unmodifiableSet(edges);
    }

    @Override
    public void registerEdgeAndNodes(DirectedEdge edge) {
        nodes.add(edge.getOrigin());
        nodes.add(edge.getDestination());
        edges.add(edge);
    }


    /*
    |====================|
    | (i) MapInitializer |
    |====================|
    */
    @Override
    public Graph postprocessFreshGraph(Graph protoGraph, long seed) {
        return initializer.postprocessFreshGraph(protoGraph, seed);
    }

    @Override
    public Graph postprocessGraph(Graph protoGraph, long seed) {
        return initializer.postprocessGraph(protoGraph, seed);
    }
}