package microtrafficsim.core.shortestpath.ch;

import microtrafficsim.core.shortestpath.ShortestPathAlgorithm;
import microtrafficsim.core.shortestpath.ShortestPathEdge;
import microtrafficsim.core.shortestpath.ShortestPathNode;

import java.util.Stack;

/**
 * This class uses contraction hierarchies for finding the shortest path. This technique contracts nodes in a specified
 * order and adds shortcut edges to ensure correctness and faster path finding. Thus, the contraction has to be done
 * once, before any shortest path shall be found.
 *
 * @author Dominic Parga Cacheiro
 */
public class CHAlgorithm implements ShortestPathAlgorithm {

    @Override
    public void preprocess() {

    }

    @Override
    public void findShortestPath(ShortestPathNode start, ShortestPathNode end, Stack<ShortestPathEdge> shortestPath) {

    }
}
