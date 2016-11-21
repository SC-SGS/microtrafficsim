package microtrafficsim.core.logic;

import microtrafficsim.core.shortestpath.ShortestPathEdge;
import microtrafficsim.core.shortestpath.ShortestPathNode;

import java.util.*;


/**
 * This class is just used as a wrapper for a {@link Stack} of {@link ShortestPathEdge}s containing start and end node.
 *
 * @author Dominic Parga Cacheiro
 */
public class Route<N extends ShortestPathNode> extends Stack<ShortestPathEdge> {

    private N                start;
    private N                end;

    /**
     * Default constructor.
     *
     * @param start Start node of this route.
     * @param end End node of this route (on the ground of the stack)
     */
    public Route(N start, N end) {
        this.start = start;
        this.end   = end;
    }

    public N getStart() {
        return start;
    }

    public N getEnd() {
        return end;
    }

    public int calcLength() {
        Iterator<ShortestPathEdge> iter = iterator();

        int len = 0;
        while (iter.hasNext()) {
            len = len + iter.next().getLength();
        }

        return len;
    }
}