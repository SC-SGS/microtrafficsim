package microtrafficsim.core.logic.streetgraph;

import microtrafficsim.core.logic.nodes.Node;
import microtrafficsim.core.logic.streets.DirectedEdge;
import microtrafficsim.core.map.Bounds;
import microtrafficsim.math.random.distributions.impl.Random;
import microtrafficsim.utils.id.BasicSeedGenerator;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;


/**
 * This graph just saves all {@link Node}s and all {@link DirectedEdge}s in a {@link HashSet}. All
 * dependencies between nodes and edges are saved in these classes, not in this
 * graph. This class is just a container for them with some functions for interaction.
 *
 * @author Jan-Oliver Schmidt, Dominic Parga Cacheiro, Maximilian Luz
 */
public class StreetGraph implements Graph {

    private Bounds                bounds;
    private HashSet<Node>         nodes;
    private HashSet<DirectedEdge> edges;
    private long                  seed;

    /**
     * Just a standard constructor.
     */
    public StreetGraph(Bounds bounds) {
        this.bounds = bounds;
        this.nodes  = new HashSet<>();
        this.edges  = new HashSet<>();
        this.seed   = Random.createSeed();
    }

    @Override
    public String toString() {
        StringBuilder output = new StringBuilder()
                .append(StreetGraph.class.toString())
                .append(": {\n");

        output.append("    Nodes: {\n");
        for (Node node : nodes) {
            output.append("        ").append(node).append("\n");
        }
        output.append("    }\n");

        output.append("    Edges: {\n");
        for (DirectedEdge edge : edges) {
            output.append("        ").append(edge).append("\n");
        }
        output.append("    }\n");

        return output.append("}").toString();
    }


    @Override
    public Bounds getBounds() {
        return bounds;
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


    @Override
    public void setSeed(long seed) {
        BasicSeedGenerator gen = new BasicSeedGenerator(seed);
        for (Node node : nodes) {
            node.setSeed(gen.next());
        }

        this.seed = seed;
    }

    @Override
    public long getSeed() {
        return seed;
    }
}