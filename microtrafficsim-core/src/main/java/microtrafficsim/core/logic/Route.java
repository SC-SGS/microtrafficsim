package microtrafficsim.core.logic;

import microtrafficsim.core.shortestpath.ShortestPathEdge;
import microtrafficsim.core.shortestpath.ShortestPathNode;
import microtrafficsim.utils.strings.builder.LevelStringBuilder;

import java.util.Stack;


/**
 * This class is just used as a wrapper for a {@link Stack} of {@link ShortestPathEdge}s containing start and end node.
 *
 * @author Dominic Parga Cacheiro
 */
public class Route<NodeType extends ShortestPathNode> extends Stack<ShortestPathEdge> {

    private NodeType start;
    private NodeType end;

    /**
     * Default constructor.
     *
     * @param start Start node of this route.
     * @param end End node of this route (on the ground of the stack)
     */
    public Route(NodeType start, NodeType end) {
        this.start = start;
        this.end   = end;
    }

    @Override
    public synchronized String toString() {
        LevelStringBuilder strBuilder = new LevelStringBuilder();
        strBuilder.appendln("<route>");
        strBuilder.incLevel();

        if (isEmpty())
            strBuilder.appendln("Route is empty.");
        else {
            strBuilder.appendln("start = " + start.toString());
            strBuilder.appendln("end   = " + end.toString());
            strBuilder.appendln("size  = " + size());
        }

        strBuilder.decLevel();
        strBuilder.appendln("<\\route>");
        return strBuilder.toString();
    }

    public NodeType getStart() {
        return start;
    }

    public NodeType getEnd() {
        return end;
    }
}