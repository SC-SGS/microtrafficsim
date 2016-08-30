package microtrafficsim.core.logic;

import microtrafficsim.core.shortestpath.ShortestPathEdge;

import java.util.*;


public class Route extends Stack<ShortestPathEdge> {

    private Node                start;
    private Node                end;

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