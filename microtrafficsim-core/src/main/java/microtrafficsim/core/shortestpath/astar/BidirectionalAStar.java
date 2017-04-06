package microtrafficsim.core.shortestpath.astar;

import microtrafficsim.core.shortestpath.ShortestPathAlgorithm;
import microtrafficsim.core.shortestpath.ShortestPathEdge;
import microtrafficsim.core.shortestpath.ShortestPathNode;

import java.util.HashMap;
import java.util.PriorityQueue;
import java.util.Stack;
import java.util.function.ToDoubleBiFunction;
import java.util.function.ToDoubleFunction;

/**
 * This class represents an bidirectional A* algorithm. You can use the constructor for own
 * implementations of the weight and estimation function, but you can also look at {@link BidirectionalAStars} for
 * various constructor-functions using selected cost and heuristic functions.
 *
 * <p>
 * Important note:<br>
 * Hence this class is a {@code bidirectional A}*, it uses two priority queues (forward and backward search). For
 * better performance, {@link #findShortestPath(N, N, Stack) findShortestPath(...)}
 * stops searching for the shortest path if <b>at least</b> one queue is empty, <b>not both</b>. This
 * could cause incorrect shortest paths if the forward queue iterates over different edges/nodes than the backward
 * queue (e.g. in {@code contraction hierarchies}).
 *
 * @author Dominic Parga Cacheiro, Maximilian Luz
 */
public class BidirectionalAStar<N extends ShortestPathNode<E>, E extends ShortestPathEdge<N>>
        implements ShortestPathAlgorithm<N, E>
{
    private final ToDoubleFunction<? super E> edgeWeightFunction;
    private final ToDoubleBiFunction<? super N, ? super N> estimationFunction;

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
    public BidirectionalAStar(ToDoubleFunction<? super E> edgeWeightFunction,
                              ToDoubleBiFunction<? super N, ? super N> estimationFunction) {
        this.edgeWeightFunction = edgeWeightFunction;
        this.estimationFunction = estimationFunction;
    }


    /*
    |===========================|
    | (i) ShortestPathAlgorithm |
    |===========================|
    */
    /**
     * Important note:<br>
     * Hence this class is a {@code bidirectional A}*, it uses two priority queues (forward and backward search). For
     * better performance, this method stops searching for the shortest path if <b>at least</b> one queue is empty,
     * <b>not both</b>. This could cause incorrect shortest paths if the forward queue iterates over different
     * edges/nodes than the backward queue (e.g. in {@code contraction hierarchies}).
     */
    @Override
    public void findShortestPath(N start, N end, Stack<? super E> result) {
        if (start == end) return;

        /*
        |================|
        | INITIALIZATION |
        |================|
        */
        // both
        double estimation = estimationFunction.applyAsDouble(start, end);

        // forward
        HashMap<N, WeightedNode<N, E>> forwardVisitedNodes = new HashMap<>();
        PriorityQueue<WeightedNode<N, E>> forwardQueue = new PriorityQueue<>();
        forwardQueue.add(new WeightedNode<>(start, null, null, 0.0, estimation));

        // backward
        HashMap<N, WeightedNode<N, E>> backwardVisitedNodes = new HashMap<>();
        PriorityQueue<WeightedNode<N, E>> backwardQueue = new PriorityQueue<>();
        backwardQueue.add(new WeightedNode<>(end, null, null, 0.0, estimation));

        /*
        |================|
        | LOOP/ALGORITHM |
        |================|
        */
        WeightedNode<N, E> meetingNode = null;
        // while at least one is not empty
        while (!forwardQueue.isEmpty() && !backwardQueue.isEmpty()) {
            // one step forwards
            if (!forwardQueue.isEmpty()) {
                WeightedNode<N, E> current = forwardQueue.poll();

                WeightedNode<N, E> backwardsCurrent = backwardVisitedNodes.get(current.node);
                if (backwardsCurrent != null) { // shortest path found
                    meetingNode = current;
                    meetingNode.successor = backwardsCurrent.successor;
                    break;
                }

                if (!forwardVisitedNodes.keySet().contains(current.node)) {
                    forwardVisitedNodes.put(current.node, current);

                    // iterate over all leaving edges
                    for (E leaving : current.node.getLeavingEdges(current.predecessor)) {
                        N dest = leaving.getDestination();
                        double g = current.g + edgeWeightFunction.applyAsDouble(leaving);

                        // push new node into priority queue
                        if (!forwardVisitedNodes.keySet().contains(dest)) {
                            double h = estimationFunction.applyAsDouble(dest, end);
                            forwardQueue.add(new WeightedNode<>(dest, leaving, null, g, h));
                        }
                    }
                }
            }

            // one step backwards
            if (!backwardQueue.isEmpty()) {
                WeightedNode<N, E> current = backwardQueue.poll();

                WeightedNode<N, E> forwardsCurrent = forwardVisitedNodes.get(current.node);
                if (forwardsCurrent != null) { // shortest path found
                    meetingNode = current;
                    meetingNode.predecessor = forwardsCurrent.predecessor;
                    break;
                }

                if (!backwardVisitedNodes.keySet().contains(current.node)) {
                    backwardVisitedNodes.put(current.node, current);

                    // iterate over all incoming edges
                    for (E incoming : current.node.getIncoming()) {
                        N orig = incoming.getOrigin();
                        double g = current.g + edgeWeightFunction.applyAsDouble(incoming);

                        // push new node into priority queue
                        if (!backwardVisitedNodes.keySet().contains(orig)) {
                            double h = estimationFunction.applyAsDouble(start, end);
                            backwardQueue.add(new WeightedNode<>(orig, null, incoming, g, h));
                        }
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

        Stack<E> bin = new Stack<>();
        // create shortest path - last part
        WeightedNode<N, E> current = meetingNode;
        while (current.successor != null) {
            bin.push(current.successor);
            current = backwardVisitedNodes.get(current.successor.getDestination());
        }
        while (!bin.isEmpty()) {
            result.push(bin.pop());
        }
        // create shortest path - first part
        current = meetingNode;
        while (current.predecessor != null) {
            result.push(current.predecessor);
            current = forwardVisitedNodes.get(current.predecessor.getOrigin());
        }
    }
}