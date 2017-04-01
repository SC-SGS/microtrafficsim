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
 * This class represents an A* algorithm. You can use the constructor for own implementations of the weight and
 * estimation function, but you can also look at {@link AStars} for various constructor-functions using selected cost
 * and heuristic functions.
 *
 * @author Jan-Oliver Schmidt, Dominic Parga Cacheiro, Maximilian Luz
 */
public class AStar<N extends ShortestPathNode<E>, E extends ShortestPathEdge<N>> implements ShortestPathAlgorithm<N, E> {

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
    public AStar(ToDoubleFunction<? super E> edgeWeightFunction,
                 ToDoubleBiFunction<? super N, ? super N> estimationFunction) {
        this.edgeWeightFunction = edgeWeightFunction;
        this.estimationFunction = estimationFunction;
    }


    /*
    |===========================|
    | (i) ShortestPathAlgorithm |
    |===========================|
    */
    @Override
    public void findShortestPath(N start, N end, Stack<? super E> shortestPath) {
        if (start == end) return;

        HashMap<N, WeightedNode<N, E>> visitedNodes = new HashMap<>();
        PriorityQueue<WeightedNode<N, E>> queue = new PriorityQueue<>();
        queue.add(new WeightedNode<>(start, null, null, 0f, estimationFunction.applyAsDouble(start, end)));

        while (!queue.isEmpty()) {
            WeightedNode<N, E> current = queue.poll();

            if (current.node == end) { // shortest path found
                // create shortest path
                while (current.predecessor != null) {
                    shortestPath.push(current.predecessor);
                    current = visitedNodes.get(current.predecessor.getOrigin());
                }

                return;
            }

            if (visitedNodes.keySet().contains(current.node))
                continue;

            visitedNodes.put(current.node, current);

            // iterate over all leaving edges
            for (E leaving : current.node.getLeavingEdges(current.predecessor)) {
                N dest = leaving.getDestination();
                double g = current.g + edgeWeightFunction.applyAsDouble(leaving);

                // push new node into priority queue
                if (!visitedNodes.keySet().contains(dest))
                    queue.add(new WeightedNode<>(dest, leaving, null, g, estimationFunction.applyAsDouble(dest, end)));
            }
        }
    }
}
