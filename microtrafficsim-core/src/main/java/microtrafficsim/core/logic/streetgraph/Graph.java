package microtrafficsim.core.logic.streetgraph;

import microtrafficsim.core.logic.nodes.Node;
import microtrafficsim.core.logic.streets.DirectedEdge;
import microtrafficsim.core.map.Bounds;
import microtrafficsim.core.shortestpath.ShortestPathGraph;
import microtrafficsim.math.random.Seeded;
import microtrafficsim.utils.Resettable;

import java.util.Set;


/**
 * Interface for street-graphs.
 *
 * @author Dominic Parga Cacheiro, Maximilian Luz
 */
public interface Graph extends Seeded, Resettable, ShortestPathGraph {

    /**
     * The graph's GUID has to be updated depending on its bounds, nodes and edges using {@link #updateGraphGUID()}.
     *
     * @return The Globally Unique IDentifier of this graph
     */
    GraphGUID getGUID();

    /**
     * Recalculates the graph's GUID depending on its bounds, nodes and edges.
     *
     * @return updated {@code GraphGUID}
     */
    GraphGUID updateGraphGUID();


    /**
     * Returns the bounding rectangle enclosing this graph.
     *
     * @return the bounds of this graph.
     */
    Bounds getBounds();


    /**
     * Returns the nodes of this graph.
     *
     * @return a set containing all nodes of this graph.
     */
    @Override
    Set<Node> getNodes();

    /**
     * Returns the edges of this graph.
     *
     * @return a set containing all edges of this graph.
     */
    @Override
    Set<DirectedEdge> getEdges();


    /**
     * Add the given {@code Node} to this graph. Consider calling {@link #setSeed(long)} afterwards.
     * <p>
     * Note: The specified node is expected to be set up completely, i.e. if a new edge has been created and added to
     * either existing or new nodes, the edge indices of have to be updated by calling {@link Node#updateEdgeIndices()}
     * on both adjacent nodes.
     * <p>
     * Determinism: Please note that adding an edge will break determinism, even if the given seed is the same, thus
     * resulting in a different scenario cycle.
     *
     * @param node the node to add.
     */
    void addNode(Node node);

    /**
     * Add the given {@code Edge} to this graph, does not add any {@code Node}s. To add the {@code Node}s of this edge,
     * call {@link #addNode(Node)}.
     * <p>
     * Note: The specified edge is expected to be set up completely, i.e. if a new edge has been created and added to
     * either existing or new nodes, the edge indices of have to be updated by calling {@link Node#updateEdgeIndices()}
     * on both adjacent nodes.
     * <p>
     * Determinism: Please note that adding an edge will break determinism, even if the given seed is the same, thus
     * resulting in a different scenario cycle.
     *
     * @param edge the edge to add.
     */
    void addEdge(DirectedEdge edge);


    /**
     * Sets the seeds of associated nodes based on the given seed. Consider calling {@link #reset()} before
     * executing this method, as this method does not reset this graph.
     *
     * @param seed the seed, typically used to generate subsequent seeds.
     */
    @Override
    void setSeed(long seed);

    /**
     * Resets all nodes and edges of this graph.
     */
    @Override
    default void reset() {
        getNodes().forEach(Node::reset);
        getEdges().forEach(DirectedEdge::reset);
    }
}
