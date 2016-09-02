package microtrafficsim.core.shortestpath.ch;

import microtrafficsim.core.shortestpath.ShortestPathAlgorithm;
import microtrafficsim.core.shortestpath.ShortestPathEdge;
import microtrafficsim.core.shortestpath.ShortestPathNode;
import microtrafficsim.core.shortestpath.astar.AStarAlgorithm;

import java.util.Stack;

/**
 * <p>
 * This class uses contraction hierarchies for finding the shortest path. This technique contracts nodes in a specified,
 * total order and adds shortcut edges to ensure correctness and faster path finding. Thus, the contraction has to be
 * done once, before any shortest path shall be found.
 * <p>
 * The original diploma thesis from Robert Geisberger in 2008 is used for this implementation. Javadocs cite it.
 *
 * @author Dominic Parga Cacheiro
 */
public class CHAlgorithm implements ShortestPathAlgorithm {

    private ShortestPathAlgorithm preprocessingSPA;

    public CHAlgorithm() {
        preprocessingSPA = AStarAlgorithm.createShortestWayDijkstra();
    }

    /**
     * <p>
     * Basic idea:<br>
     * Finding a total order < among all nodes and execute the following procedure per node: <br>
     * <br>
     * foreach (v, u) in E with u > v do <br>
     * . . foreach (u, w) in E with w > u do <br>
     * . . . . if < v, u, w > "may be" the only shortest path from v to w then <br>
     * . . . . . . add (v, w) to E with w(v, w) := w(v, u) + w(u, w) <br>
     * . . . . fi <br>
     * .  . od <br>
     * od
     *
     * <p>
     * Thus, the preprocessing consists of multiple steps: <br>
     * &bull preassess nodes <br>
     * &bull execute procedure above for each node and reassess affected nodes after each contraction <br>
     */
    @Override
    public void preprocess() {

    }


    /**
     * This method is implemented with a bidirectional dijkstra, which uses the previously added shortcut edges of the
     * preprocessing for faster path finding.
     *
     * @param start First node of the shortest path
     * @param end Last node of the shortest path
     * @param shortestPath This data structure gets NOT cleared but filled with the edges of the shortest path, that is
     *                     calculated in this method.
     */
    @Override
    public void findShortestPath(ShortestPathNode start, ShortestPathNode end, Stack<ShortestPathEdge> shortestPath) {

    }
}
