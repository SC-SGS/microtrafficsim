package microtrafficsim.core.logic;

import microtrafficsim.core.shortestpath.ShortestPathEdge;

import java.util.*;


/**
 * This class is just used as a wrapper for a {@link Stack} of {@link ShortestPathEdge}s containing start and end node.
 *
 * @author Dominic Parga Cacheiro
 */
public class Route extends Stack<ShortestPathEdge> {

    private Node                start;
    private Node                end;

    /**
     * Default constructor.
     *
     * @param start Start node of this route.
     * @param end End node of this route (on the ground of the stack)
     */
    public Route(Node start, Node end) {
        this.start = start;
        this.end   = end;
    }

    public Node getStart() {
        return start;
    }

    public Node getEnd() {
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