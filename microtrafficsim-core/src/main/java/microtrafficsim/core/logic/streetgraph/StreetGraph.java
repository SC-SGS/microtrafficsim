package microtrafficsim.core.logic.streetgraph;

import microtrafficsim.core.logic.nodes.Node;
import microtrafficsim.core.logic.streets.DirectedEdge;
import microtrafficsim.core.map.Bounds;
import microtrafficsim.math.random.distributions.impl.Random;
import microtrafficsim.utils.id.BasicSeedGenerator;
import microtrafficsim.utils.strings.builder.LevelStringBuilder;

import java.util.*;


/**
 * This graph just saves all {@link Node}s and all {@link DirectedEdge}s in a {@link HashSet}. All
 * dependencies between nodes and edges are saved in these classes, not in this
 * graph. This class is just a container for them with some functions for interaction.
 *
 * @author Jan-Oliver Schmidt, Dominic Parga Cacheiro, Maximilian Luz
 */
public class StreetGraph implements Graph {

    private GraphGUID                   guid;
    private Bounds                      bounds;
    private HashMap<Integer, Node>         nodes;
    private HashMap<Integer, DirectedEdge> edges;
    private long                        seed;

    /**
     * Just a standard constructor.
     *
     * @param bounds the bounds enclosing the excerpt represented in this graph.
     */
    public StreetGraph(Bounds bounds) {
        this.bounds = bounds;
        this.nodes  = new HashMap<>();
        this.edges  = new HashMap<>();
        this.seed   = Random.createSeed();
    }

    @Override
    public String toString() {
        LevelStringBuilder output = new LevelStringBuilder()
                .setLevelSubString("    ")
                .setLevelSeparator(System.lineSeparator())
                .append(StreetGraph.class.toString())
                .append(":GUID_hash=" + guid.hashCode())
                .appendln(": {").incLevel();

        output.appendln("Nodes: {").incLevel();
        nodes.values().forEach(output::appendln);
        output.decLevel().appendln("}");

        output.appendln("Edges: {").incLevel();
        edges.values().forEach(output::appendln);
        output.decLevel().appendln("}");

        return output.decLevel().append("}").toString();
    }


    @Override
    public GraphGUID getGUID() {
        return guid;
    }

    @Override
    public GraphGUID updateGraphGUID() {
        guid = GraphGUID.from(this);
        return guid;
    }

    @Override
    public Bounds getBounds() {
        return bounds;
    }


    @Override
    public Map<Integer, Node> getNodeMap() {
        return Collections.unmodifiableMap(nodes);
    }

    @Override
    public Map<Integer, DirectedEdge> getEdgeMap() {
        return Collections.unmodifiableMap(edges);
    }

    @Override
    public Set<Node> getNodes() {
        return new HashSet<>(nodes.values());
    }

    @Override
    public Set<DirectedEdge> getEdges() {
        return new HashSet<>(edges.values());
    }

    @Override
    public void addNode(Node node) {
        nodes.put(node.hashCode(), node);
    }

    @Override
    public void addEdge(DirectedEdge edge) {
        edges.put(edge.hashCode(), edge);
    }


    @Override
    public void setSeed(long seed) {
        BasicSeedGenerator gen = new BasicSeedGenerator(seed);
        for (Node node : nodes.values()) {
            node.setSeed(gen.next());
        }

        this.seed = seed;
    }

    @Override
    public long getSeed() {
        return seed;
    }
}