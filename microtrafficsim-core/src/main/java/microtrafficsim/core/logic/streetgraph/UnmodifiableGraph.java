package microtrafficsim.core.logic.streetgraph;

import microtrafficsim.core.logic.nodes.Node;
import microtrafficsim.core.logic.streets.DirectedEdge;
import microtrafficsim.core.map.Bounds;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

/**
 * @author Dominic Parga Cacheiro
 */
public class UnmodifiableGraph implements Graph {
    private final Graph graph;


    public UnmodifiableGraph(Graph graph) {
        this.graph = graph;
    }



    @Override
    public long getSeed() {
        return graph.getSeed();
    }

    @Override
    public GraphGUID getGUID() {
        return graph.getGUID();
    }

    /**
     * @throws UnsupportedOperationException
     */
    @Override
    public GraphGUID updateGraphGUID() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Bounds getBounds() {
        return graph.getBounds();
    }

    @Override
    public Map<Node.Key, Node> getNodeMap() {
        return Collections.unmodifiableMap(graph.getNodeMap());
    }

    @Override
    public Map<DirectedEdge.Key, DirectedEdge> getEdgeMap() {
        return Collections.unmodifiableMap(graph.getEdgeMap());
    }

    @Override
    public Set<Node> getNodes() {
        return Collections.unmodifiableSet(graph.getNodes());
    }

    @Override
    public Set<DirectedEdge> getEdges() {
        return Collections.unmodifiableSet(graph.getEdges());
    }

    /**
     * @throws UnsupportedOperationException
     */
    @Override
    public void addNode(Node node) {
        throw new UnsupportedOperationException();
    }

    /**
     * @throws UnsupportedOperationException
     */
    @Override
    public void addEdge(DirectedEdge edge) {
        throw new UnsupportedOperationException();
    }

    /**
     * @throws UnsupportedOperationException
     */
    @Override
    public void setSeed(long seed) {
        throw new UnsupportedOperationException();
    }
}
