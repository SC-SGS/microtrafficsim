package microtrafficsim.core.logic.streetgraph;

import microtrafficsim.core.logic.nodes.Node;
import microtrafficsim.core.logic.streets.DirectedEdge;
import microtrafficsim.core.map.Bounds;
import microtrafficsim.utils.hashing.FNVHashBuilder;


public class GraphGUID {
    private final Bounds bounds;
    private final int nodes;
    private final int edges;


    public static GraphGUID from(Graph graph) {
        Bounds bounds = new Bounds(graph.getBounds());

        FNVHashBuilder nodes = new FNVHashBuilder();
        for (Node node : graph.getNodes())
            nodes.add(node);

        FNVHashBuilder edges = new FNVHashBuilder();
        for (DirectedEdge edge : graph.getEdges())
            nodes.add(edge);

        return new GraphGUID(bounds, nodes.getHash(), edges.getHash());
    }


    public GraphGUID(Bounds bounds, int nodes, int edges) {
        this.bounds = bounds;
        this.nodes = nodes;
        this.edges = edges;
    }

    public Bounds getBounds() {
        return bounds;
    }

    public int getNodeHash() {
        return nodes;
    }

    public int getEdgeHash() {
        return edges;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof GraphGUID))
            return false;

        GraphGUID other = (GraphGUID) obj;

        return this.bounds.equals(other.bounds)
                && this.nodes == other.nodes
                && this.edges == other.edges;
    }

    @Override
    public int hashCode() {
        return new FNVHashBuilder()
                .add(bounds)
                .add(nodes)
                .add(edges)
                .getHash();
    }
}
