package microtrafficsim.core.logic.streetgraph;

import microtrafficsim.core.map.Bounds;
import microtrafficsim.utils.hashing.FNVHashBuilder;


/**
 * {@code GUID} stands for Globally Unique IDentifier.
 */
public class GraphGUID {
    private final Bounds bounds;
    private final int nodeHash;
    private final int edgeHash;


    public static GraphGUID from(Graph graph) {
        Bounds bounds = new Bounds(graph.getBounds());

        FNVHashBuilder nodes = new FNVHashBuilder();
        graph.getNodes().forEach(nodes::add);

        FNVHashBuilder edges = new FNVHashBuilder();
        graph.getEdges().forEach(nodes::add);

        return new GraphGUID(bounds, nodes.getHash(), edges.getHash());
    }


    public GraphGUID(Bounds bounds, int nodeHash, int edgeHash) {
        this.bounds = bounds;
        this.nodeHash = nodeHash;
        this.edgeHash = edgeHash;
    }

    public Bounds getBounds() {
        return bounds;
    }

    public int getNodeHash() {
        return nodeHash;
    }

    public int getEdgeHash() {
        return edgeHash;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof GraphGUID))
            return false;

        GraphGUID other = (GraphGUID) obj;

        return this.bounds.equals(other.bounds)
                && this.nodeHash == other.nodeHash
                && this.edgeHash == other.edgeHash;
    }

    @Override
    public int hashCode() {
        return new FNVHashBuilder()
                .add(bounds)
                .add(nodeHash)
                .add(edgeHash)
                .getHash();
    }
}
