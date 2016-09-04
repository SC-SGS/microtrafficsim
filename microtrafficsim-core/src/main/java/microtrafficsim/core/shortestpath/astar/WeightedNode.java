package microtrafficsim.core.shortestpath.astar;

import microtrafficsim.core.shortestpath.ShortestPathEdge;
import microtrafficsim.core.shortestpath.ShortestPathNode;


/**
 * <p>
 * This class supports {@link AStar} by saving the relevant
 * weights of the nodes to find the shortest path. This class also implements
 * compareTo, that is comparing the whole weight f = g + h. <br>
 * g: Real weight from the start to this node. <br>
 * h: Estimated weight of the way from this node to the end.
 *
 * @author Dominic Parga Cacheiro
 */
class WeightedNode implements Comparable<WeightedNode> {

    final ShortestPathNode node;
    final float            g;
    final float            f;    // f = g + h
    ShortestPathEdge       predecessor;

    /**
     * Standard constructor.
     *
     * @param node {@link ShortestPathNode}
     * @param g    Real weight from the start to this node.
     * @param h    Estimated weight of the way from this node to the end.
     */
    WeightedNode(ShortestPathNode node, float g, float h) {
        this.node = node;
        this.g    = g;
        f         = g + h;
    }

    @Override
    public int compareTo(WeightedNode o) {
        if (f < o.f) return -1;
        if (f > o.f) return 1;
        return 0;
    }
}
