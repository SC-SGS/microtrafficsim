package microtrafficsim.core.shortestpath.contractionhierarchies;

import microtrafficsim.core.shortestpath.ShortestPathNode;

/**
 * @author Dominic Parga Cacheiro
 */
public class ContractionNode {

    public final ShortestPathNode raw;

    public ContractionNode(ShortestPathNode raw) {
        this.raw = raw;
    }
}
