package microtrafficsim.core.shortestpath.astar;

import microtrafficsim.core.shortestpath.ShortestPathEdge;
import microtrafficsim.core.shortestpath.ShortestPathNode;


/**
 * <p>
 * This class supports {@link AStar} (or other AStar algorithms) by saving the relevant
 * weights and predecessors (successors) of the nodes to find the shortest path.
 * This class also implements compareTo, that is comparing the whole weight f = g + h. <br>
 * g: Real weight from the start to this node. <br>
 * h: Estimated weight of the way from this node to the end.
 *
 * @author Dominic Parga Cacheiro
 */
class WeightedNode<N extends ShortestPathNode<E>, E extends ShortestPathEdge<N>> implements Comparable<WeightedNode> {

    final N node;
    final double g;
    final double f;    // f = g + h
    E predecessor;
    E successor;

    /**
     * Standard constructor.
     *
     * @param node {@link ShortestPathNode}
     * @param g    Real weight from the start to this node.
     * @param h    Estimated weight of the way from this node to the end.
     */
    WeightedNode(N node, E predecessor, E successor, double g, double h) {
        this.node = node;
        this.predecessor = predecessor;
        this.successor = successor;
        this.g    = g;
        f         = g + h;
    }

    @Override
    public int hashCode() {
        return node.hashCode();
    }

    @Override
    public String toString() {
        return node.toString() + "; g = " + g + "; f = " + f;
    }

    @Override
    public int compareTo(WeightedNode o) {
        return Double.compare(f, o.f);
    }
}
