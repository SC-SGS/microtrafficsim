package microtrafficsim.core.shortestpath.astar;

import microtrafficsim.core.shortestpath.ShortestPathAlgorithm;
import microtrafficsim.core.shortestpath.ShortestPathEdge;
import microtrafficsim.core.shortestpath.ShortestPathNode;

import java.util.HashMap;
import java.util.PriorityQueue;
import java.util.Stack;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * This class represents an bidirectional A* algorithm. You can use the constructor for own implementations of the
 * weight and estimation function, but you can also use {@link #createShortestWayDijkstra()} for a standard
 * implementation of Dijkstra's algorithm (bidirectional).
 *
 * @author Dominic Parga Cacheiro
 */
public class BidirectionalAStar implements ShortestPathAlgorithm {
    private final Function<ShortestPathEdge, Float> edgeWeightFunction;
    private final BiFunction<ShortestPathNode, ShortestPathNode, Float> estimationFunction;

    /**
     * Standard constructor which sets its edge weight and estimation function to the given ones. This constructor
     * should only be used if you want to define the weight and estimation function on your own. There is a factory
     * method in this class for an implementation of Dijkstra's algorithm.
     *
     * @param edgeWeightFunction The A* algorithm uses a node N from the priority queue for actualizing (if necessary)
     *                           the weight of each node D, that is a destination of an edge starting in N. For this,
     *                           it needs the current weight of N plus the weight of the mentioned edge. <br>
     *                           <p>
     *                           Invariants: <br>
     *                           All edge weights has to be >= 0
     * @param estimationFunction In addition, the A* algorithm estimates the distance from this destination D to the end
     *                           of the route to find the shortest path faster by reducing the search area. <br>
     *                           <p>
     *                           Invariants: <br>
     *                           1) This estimation must be lower or equal to the real shortest path from destination
     *                           to the route's end. So it is not allowed to be more pessimistic than the correct
     *                           shortest path. Otherwise, it is not guaranteed, that the A* algorithm returns correct
     *                           results. <br>
     *                           2) This estimation has to be >= 0
     *
     */
    public BidirectionalAStar(Function<ShortestPathEdge, Float> edgeWeightFunction,
                              BiFunction<ShortestPathNode, ShortestPathNode, Float> estimationFunction) {
        this.edgeWeightFunction = edgeWeightFunction;
        this.estimationFunction = estimationFunction;
    }

    /**
     * @return Standard implementation of Dijkstra's algorithm for calculating the shortest (not necessarily fastest)
     * path using {@link ShortestPathEdge#getLength()}
     */
    public static BidirectionalAStar createShortestWayDijkstra() {
        return new BidirectionalAStar(
                edge -> (float)edge.getLength(),
                (destination, routeDestination) -> 0f
        );
    }

    /*
    |===========================|
    | (i) ShortestPathAlgorithm |
    |===========================|
    */
    @Override
    public void findShortestPath(ShortestPathNode start, ShortestPathNode end, Stack<ShortestPathEdge> shortestPath) {

        if (start == end)
            return;

        /*
        |================|
        | INITIALIZATION |
        |================|
        */
        // both
        float estimation = estimationFunction.apply(start, end);
        // forward
        HashMap<ShortestPathNode, WeightedNode> forwardVisitedNodes = new HashMap<>();
        PriorityQueue<WeightedNode>             forwardQueue        = new PriorityQueue<>();
        forwardQueue.add(new WeightedNode(start, null, null, 0f, estimation));
        // backward
        HashMap<ShortestPathNode, WeightedNode> backwardVisitedNodes = new HashMap<>();
        PriorityQueue<WeightedNode>             backwardQueue        = new PriorityQueue<>();
        backwardQueue.add(new WeightedNode(end, null, null, 0f, estimation));

        /*
        |================|
        | LOOP/ALGORITHM |
        |================|
        */
        WeightedNode meetingNode = null;
        // while at least one is not empty
        while (!(forwardQueue.isEmpty() && backwardQueue.isEmpty())) {
            // one step forwards
            if (!forwardQueue.isEmpty()) {
                WeightedNode current = forwardQueue.poll();

                WeightedNode backwardsCurrent = backwardVisitedNodes.get(current.node);
                if (backwardsCurrent != null) { // shortest path found
                    meetingNode = current;
                    meetingNode.successor = backwardsCurrent.successor;
                    break;
                }

                if (!forwardVisitedNodes.keySet().contains(current.node)) {
                    forwardVisitedNodes.put(current.node, current);

                    // iterate over all leaving edges
                    for (ShortestPathEdge leaving : current.node.getLeavingEdges(current.predecessor)) {
                        ShortestPathNode dest = leaving.getDestination();
                        float g = current.g + edgeWeightFunction.apply(leaving);

                        // push new node into priority queue
                        if (!forwardVisitedNodes.keySet().contains(dest))
                            forwardQueue.add(
                                    new WeightedNode(dest, leaving, null, g, estimationFunction.apply(dest, end)));
                    }
                }
            }

            // one step backwards
            if (!backwardQueue.isEmpty()) {
                WeightedNode current = backwardQueue.poll();

                WeightedNode forwardsCurrent = forwardVisitedNodes.get(current.node);
                if (forwardsCurrent != null) { // shortest path found
                    meetingNode = current;
                    meetingNode.predecessor = forwardsCurrent.predecessor;
                    break;
                }

                if (!backwardVisitedNodes.keySet().contains(current.node)) {
                    backwardVisitedNodes.put(current.node, current);

                    // iterate over all incoming edges
                    for (ShortestPathEdge incoming : current.node.getIncomingEdges()) {
                        ShortestPathNode orig = incoming.getOrigin();
                        float g = current.g + edgeWeightFunction.apply(incoming);

                        // push new node into priority queue
                        if (!backwardVisitedNodes.keySet().contains(orig))
                            backwardQueue.add(
                                    new WeightedNode(orig, null, incoming, g, estimationFunction.apply(start, orig)));
                    }
                }
            }
        }


        /*
        |===============================|
        | CREATE SHORTEST PATH IF FOUND |
        |===============================|
        */
        if (meetingNode == null)
            return;
        Stack<ShortestPathEdge> bin = new Stack<>();
        // create shortest path - last part
        WeightedNode current = meetingNode;
        while (current.successor != null) {
            bin.push(current.successor);
            current = backwardVisitedNodes.get(current.successor.getDestination());
        }
        while (!bin.isEmpty()) {
            shortestPath.push(bin.pop());
        }
        // create shortest path - first part
        current = meetingNode;
        while (current.predecessor != null) {
            shortestPath.push(current.predecessor);
            current = forwardVisitedNodes.get(current.predecessor.getOrigin());
        }
    }
}