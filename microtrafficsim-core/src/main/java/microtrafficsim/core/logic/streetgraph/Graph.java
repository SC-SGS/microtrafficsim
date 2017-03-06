package microtrafficsim.core.logic.streetgraph;

import microtrafficsim.core.logic.nodes.Node;
import microtrafficsim.core.logic.streets.DirectedEdge;
import microtrafficsim.core.shortestpath.ShortestPathGraph;
import microtrafficsim.core.simulation.builder.MapInitializer;
import microtrafficsim.utils.Resettable;
import microtrafficsim.core.map.Bounds;

import java.util.Set;

/**
 * @author Dominic Parga Cacheiro
 */
public interface Graph extends MapInitializer, Resettable, ShortestPathGraph {

    /**
     * Adds the {@link Node}s of the given {@link DirectedEdge} to the node set
     * and the edge to the edge set.
     *
     * @param edge This edge will be added to the graph.
     */
    void registerEdgeAndNodes(DirectedEdge edge);

    Bounds getBounds();

    /*
    |=======================|
    | (i) ShortestPathGraph |
    |=======================|
    */
    /**
     * @return instance of {@link java.util.Collections.UnmodifiableSet} of the nodes in this graph
     */
    @Override
    Set<Node> getNodes();

    /**
     * @return instance of {@link java.util.Collections.UnmodifiableSet} of the edges in this graph
     */
    @Override
    Set<DirectedEdge> getEdges();

    /*
    |=================================|
    | extension to (i) MapInitializer |
    |=================================|
    */
    /**
     * Calls {@link #postprocessFreshGraph(Graph, long) postprocessFreshGraph(this, seed)}
     */
    default Graph postprocessFresh(long seed) {
        return postprocessFreshGraph(this, seed);
    }

    /**
     * Calls {@link #postprocessGraph(Graph, long) postprocessGraph(this, seed)}
     */
    default Graph postprocess(long seed) {
        return postprocessGraph(this, seed);
    }

    /*
    |================|
    | (i) Resettable |
    |================|
    */
    /**
     * This method resets the nodes and edges of the {@code graph}.
     */
    @Override
    default void reset() {
        getNodes().forEach(Node::reset);
        getEdges().forEach(DirectedEdge::reset);
    }
}
