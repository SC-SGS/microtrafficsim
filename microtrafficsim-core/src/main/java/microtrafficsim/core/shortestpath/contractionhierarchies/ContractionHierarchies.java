package microtrafficsim.core.shortestpath.contractionhierarchies;

import microtrafficsim.core.shortestpath.ShortestPathAlgorithm;
import microtrafficsim.core.shortestpath.ShortestPathEdge;
import microtrafficsim.core.shortestpath.ShortestPathGraph;
import microtrafficsim.core.shortestpath.ShortestPathNode;

import java.util.Stack;

/**
 * @author Dominic Parga Cacheiro
 */
public class ContractionHierarchies implements ShortestPathAlgorithm {

    private final ShortestPathAlgorithm basicSPA;
    private final ShortestPathGraph     graph;
    private boolean                     isPreprocessed;

    /**
     * @param basicSPA This {@code ShortestPathAlgorithm} is the basic algorithm used in this implementation of
     *                 contraction hierarchies.
     */
    public ContractionHierarchies(ShortestPathAlgorithm basicSPA, ShortestPathGraph graph) {
        this.basicSPA  = basicSPA;
        this.graph     = graph;
        isPreprocessed = false;
    }

    /**
     * @return Whether this algorithm has already calculate shortcuts.
     */
    @Override
    public boolean isPreprocessed() {
        return isPreprocessed;
    }

    @Override
    public void preprocess() {

        isPreprocessed = false;

        if (!basicSPA.isPreprocessed())
            basicSPA.preprocess();

        // todo impl start
        // give all nodes initial proto-priority
        // loop {
        //     select next node depending on contraction-priority
        //     give selected node fix priority
        //     update proto-priority of neighbors
        // }
        // todo impl end

        isPreprocessed = true;
    }

    @Override
    public void findShortestPath(ShortestPathNode start, ShortestPathNode end, Stack<ShortestPathEdge> shortestPath) {
        basicSPA.findShortestPath(start, end, shortestPath);
    }
}
