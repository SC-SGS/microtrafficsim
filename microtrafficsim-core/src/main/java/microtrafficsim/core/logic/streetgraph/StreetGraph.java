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
    private GraphGUID guid;
    private Bounds bounds;
    private TreeMap<Node.Key, Node> nodes;
    private TreeMap<DirectedEdge.Key, DirectedEdge> edges;
    private long seed;

    /**
     * Just a standard constructor.
     *
     * @param bounds the bounds enclosing the excerpt represented in this graph.
     */
    public StreetGraph(Bounds bounds) {
        this.bounds = bounds;
        this.nodes  = new TreeMap<>();
        this.edges  = new TreeMap<>();
        this.seed   = Random.createSeed();
    }

    @Override
    public String toString() {
        LevelStringBuilder builder = new LevelStringBuilder()
                .setLevelSubString("    ")
                .setLevelSeparator(System.lineSeparator());

        builder.appendln("<" + getClass().getSimpleName() + ">").incLevel(); {
            builder.appendln("GUID hash = " + guid.hashCode());
            builder.appendln();
            builder.appendln();
            nodes.values().forEach(builder::appendln);
            builder.appendln();
            builder.appendln();
            edges.values().forEach(builder::appendln);
        } builder.decLevel().append("</" + getClass().getSimpleName() + ">");

        return builder.toString();
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
    public Map<Node.Key, Node> getNodeMap() {
        return Collections.unmodifiableMap(nodes);
    }

    @Override
    public Map<DirectedEdge.Key, DirectedEdge> getEdgeMap() {
        return Collections.unmodifiableMap(edges);
    }

    @Override
    public TreeSet<Node> getNodes() {
        return new TreeSet<>(nodes.values());
    }

    @Override
    public TreeSet<DirectedEdge> getEdges() {
        return new TreeSet<>(edges.values());
    }

    @Override
    public void addNode(Node node) {
        nodes.put(node.key(), node);
    }

    @Override
    public void addEdge(DirectedEdge edge) {
        edges.put(edge.key(), edge);
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