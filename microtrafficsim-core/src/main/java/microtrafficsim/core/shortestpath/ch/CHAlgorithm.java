package microtrafficsim.core.shortestpath.ch;

import microtrafficsim.core.shortestpath.ShortestPathAlgorithm;
import microtrafficsim.core.shortestpath.ShortestPathEdge;
import microtrafficsim.core.shortestpath.ShortestPathNode;

import java.util.*;
import java.util.function.Consumer;

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

    private ShortestPathAlgorithm preprocessingSPA, shortestPathAlgorithm;
    private HashMap<ShortestPathNode, PriorityNode> priorityNodes;
    private PriorityQueue<PriorityNode> priorityQueue;


    /**
     * Standard constructor setting its parameter to the given ones.
     *
     * @param shortestPathAlgorithm This shortest path algorithm is used for finding shortcuts during and after the
     *                              preprocessing phase.
     * @param nodes A collection of all nodes is needed to reach all nodes for assessing.
     */
    public CHAlgorithm(ShortestPathAlgorithm shortestPathAlgorithm, HashSet<ShortestPathNode> nodes) {
        this(shortestPathAlgorithm, shortestPathAlgorithm, nodes);
    }

    /**
     * Standard constructor setting its parameter to the given ones.
     *
     * @param preprocessingSPA This shortest path algorithm is used for finding shortcuts during the preprocessing phase
     * @param shortestPathAlgorithm This shortest path algorithm is used for finding shortcuts after the preprocessing
     *                              phase
     * @param nodes A collection of all nodes is needed to reach all nodes for assessing.
     */
    public CHAlgorithm(ShortestPathAlgorithm preprocessingSPA,
                       ShortestPathAlgorithm shortestPathAlgorithm,
                       HashSet<ShortestPathNode> nodes) {
        this.preprocessingSPA = preprocessingSPA;
        this.shortestPathAlgorithm = shortestPathAlgorithm;

        priorityNodes = new HashMap<>();
        for (ShortestPathNode node : nodes) {
            priorityNodes.put(node, new PriorityNode(node));
        }

        priorityQueue = new PriorityQueue<>();
    }

    /**
     * <p>
     * Basic idea:<br>
     * Finding a total order < among all nodes and execute the following procedure ("contraction") per node u: <br>
     * <br>
     * foreach (v, u) in E with u > v do <br>
     * . . foreach (u, w) in E with w > u do <br>
     * . . . . if v->u->w "may be" the only shortest path from v to w then <br>
     * . . . . . . add (v, w) to E with w(v, w) := w(v, u) + w(u, w) <br>
     * . . . . fi <br>
     * .  . od <br>
     * od
     *
     * <p>
     * Thus, the preprocessing consists of multiple steps: <br>
     * &bull preassess nodes <br>
     * &bull execute procedure above for each node and reassess affected nodes after each outer iteration step <br>
     *
     * <p>
     * This method can be called multiple times.
     */
    @Override
    public void preprocess() {
        // temporary for preprocessing
        final HashMap<PriorityNode, Set<Shortcut>> leavingEdges, incomingEdges;
        leavingEdges = new HashMap<>();
        incomingEdges = new HashMap<>();

        // fill priority nodes with ("atomic") shortcuts; for more information, see class Shortcut
        for (PriorityNode origin : priorityNodes.values()) {
            leavingEdges.put(origin, new HashSet<>());
            incomingEdges.put(origin, new HashSet<>());

            for (ShortestPathEdge edge : origin.node.getLeavingEdges(null)) {
                // create shortcut
                Shortcut shortcut = new Shortcut();
                shortcut.push(edge);
                // add leaving shortcut
                origin.addShortcut(shortcut);
                leavingEdges.get(origin).add(shortcut);
                // add incoming edge to destination for for every leaving edge
                priorityNodes.get(edge.getDestination()).addShortcut(shortcut);
                incomingEdges.get(origin).add(shortcut);
            }
        }

        // defining assess function
        Consumer<PriorityNode> assessFunction = node -> {
            node.priority = 0;
            node.priority -= leavingEdges.size() + incomingEdges.size();
        };

        // preassessing all nodes
        for (PriorityNode node : priorityNodes.values()) {
            assessFunction.accept(node);
            priorityQueue.add(node);
        }

        // contract all nodes
        while (!priorityQueue.isEmpty()) {
            PriorityNode u = priorityQueue.poll();
            if (u.isDirty) {
                assessFunction.accept(u);
                priorityQueue.add(u);
            } else {
                // contraction
                // foreach (v, u) in E with u > v do
                for (Shortcut incoming : incomingEdges.get(u)) {
                    if (u.priority > priorityNodes.get(incoming.getOrigin()).priority) {
                        // foreach (u, w) in E with w > u do
                        for (Shortcut leaving : leavingEdges.get(u)) {
                            if (u.priority < priorityNodes.get(leaving.getDestination()).priority) {
//                                preprocessingSPA.findShortestPath();
                            }
                        }
                    }
                }
            }
        }

        // reset
        priorityQueue.clear();
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
