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
     * Adds the {@link Node}s of the given {@link DirectedEdge} to the node set
     * and the edge to the edge set.
     *
     * @param edge This edge will be added to the graph.
     */
    void registerEdgeAndNodes(DirectedEdge edge);       // TODO: split into multiple methods?


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
     * Sets the seeds of associated nodes based on the given seed. Consider calling {@link #reset()} before
     * executing this method, as this method does not reset this graph.
     *
     * @param seed the seed, typically used to generate subsequent seeds.
     */
    @Override
    void setSeed(long seed);

    /**
     * Resets the nodes and edges of the {@code graph}.
     */
    @Override
    default void reset() {
        getNodes().forEach(Node::reset);
        getEdges().forEach(DirectedEdge::reset);
    }
}
