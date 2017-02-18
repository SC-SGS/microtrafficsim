package microtrafficsim.core.shortestpath.contractionhierarchies;

import microtrafficsim.core.shortestpath.ShortestPathNode;

import java.util.PriorityQueue;

/**
 * @author Dominic Parga Cacheiro
 */
public class ContractionNode {

    public final ShortestPathNode raw;
    private PriorityQueue bla;
    /**
     * Is used to build up the forward and backward graph after contraction.
     */
    public int searchPriority;
    /**
     * Is used during contraction phase.
     */
    public int contractionPriority;

    /**
     * Default constructor.
     *
     * @param raw
     */
    public ContractionNode(ShortestPathNode raw) {
        this.raw            = raw;
        searchPriority      = -1;
        contractionPriority = -1;
    }

    /**
     * @return hashcode of the wrapped {@link ShortestPathNode node-instance}
     */
    @Override
    public int hashCode() {
        return raw.hashCode();
    }
}