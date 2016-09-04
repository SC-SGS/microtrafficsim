package microtrafficsim.core.shortestpath.ch;

import microtrafficsim.core.shortestpath.ShortestPathEdge;
import microtrafficsim.core.shortestpath.ShortestPathNode;
import microtrafficsim.utils.hashing.FNVHashBuilder;

import java.util.Stack;

/**
 * <p>
 * This class represents a collection of edges as described in the diploma thesis of contraction hierarchies by
 * Robert Geisberger from 2008. In addition, we allow atomic shortcuts for an uniform implementation, which represents
 * standard edges.
 *
 * This class supports some stack functionality like push and pop.
 *
 * @author Dominic Parga Cacheiro
 */
class Shortcut implements ShortestPathEdge {
    private ShortestPathNode origin, destination;
    private Stack<ShortestPathEdge> stack;
    private int length;
    private float timeCostMillis;

    /**
     * Standard constructor.
     */
    public Shortcut() {
        length = 0;
        timeCostMillis = 0;
        stack = new Stack<>();
    }

    /**
     * @return true <=> this shortcut represents exactly 1 edge
     */
    public boolean isAtomic() {
        return stack.size() == 1;
    }

    /*
    |=======|
    | Stack |
    |=======|
    */
    public ShortestPathEdge push(ShortestPathEdge edge) {
        if (stack.isEmpty()) {
            destination = edge.getDestination();
            origin = edge.getOrigin();
            length = edge.getLength();
            timeCostMillis = edge.getTimeCostMillis();
        } else {
            origin = edge.getOrigin();
            length += edge.getLength();
            timeCostMillis += edge.getTimeCostMillis();
        }

        return stack.push(edge);
    }

    public synchronized ShortestPathEdge pop() {
        ShortestPathEdge item = stack.pop();

        if (stack.isEmpty()) {
            destination = null;
            origin = null;
            length = 0;
            timeCostMillis = 0;
        } else {
            origin = item.getDestination();
            length -= item.getLength();
            timeCostMillis -= item.getTimeCostMillis();
        }

        return item;
    }

    /*
    |======================|
    | (i) ShortestPathEdge |
    |======================|
    */
    @Override
    public int getLength() {
        return length;
    }

    @Override
    public float getTimeCostMillis() {
        return timeCostMillis;
    }

    @Override
    public ShortestPathNode getOrigin() {
        return origin;
    }

    @Override
    public ShortestPathNode getDestination() {
        return destination;
    }
}
