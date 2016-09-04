package microtrafficsim.core.shortestpath.ch;

import microtrafficsim.core.map.Coordinate;
import microtrafficsim.core.shortestpath.ShortestPathEdge;
import microtrafficsim.core.shortestpath.ShortestPathNode;

import java.util.HashSet;
import java.util.Set;

/**
 * <p>
 * This class supports {@link CHAlgorithm} by saving the priority of the nodes during preprocessing. This class also
 * implements compareTo, that is simply comparing the priorities of two nodes. <br>
 *
 * @author Dominic Parga Cacheiro
 */
class PriorityNode implements Comparable<PriorityNode>, ShortestPathNode {
    final ShortestPathNode node;
    float priority;
    boolean isDirty;
    final HashSet<ShortestPathEdge> leavingEdges, incomingEdges;

    /**
     * Standard constructor.
     *
     * @param node {@link ShortestPathNode}
     */
    public PriorityNode(ShortestPathNode node) {
        this.node = node;
        priority = 0;
        isDirty = false;

        leavingEdges = new HashSet<>();
        incomingEdges = new HashSet<>();
    }

    /**
     * This method adds the given shortcut to this node's shortcuts: <br>
     * if this node is the shortcut's origin -> leaving shortcut <br>
     * if this node is the shortcut's destination -> incoming shortcut
     *
     * @param shortcut This shortcut is added to this node
     */
    public void addShortcut(Shortcut shortcut) {
        if (shortcut.getOrigin() == this) {
            leavingEdges.add(shortcut);
        } else if (shortcut.getDestination() == this) {
            incomingEdges.add(shortcut);
        }
    }

    @Override
    public int hashCode() {
        return node.hashCode();
    }

    /*
        |======================|
        | (i) ShortestPathNode |
        |======================|
        */
    @Override
    public Set<ShortestPathEdge> getLeavingEdges(ShortestPathEdge incoming) {
        return leavingEdges;
    }

    @Override
    public Set<ShortestPathEdge> getIncomingEdges() {
        return incomingEdges;
    }

    @Override
    public Coordinate getCoordinate() {
        return node.getCoordinate();
    }

    /*
    |================|
    | (i) Comparable |
    |================|
    */
    @Override
    public int compareTo(PriorityNode o) {
        if (priority < o.priority) return -1;
        if (priority > o.priority) return 1;
        return 0;
    }
}
