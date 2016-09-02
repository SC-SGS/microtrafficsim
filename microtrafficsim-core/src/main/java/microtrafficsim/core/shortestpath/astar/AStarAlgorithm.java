package microtrafficsim.core.shortestpath.astar;

import microtrafficsim.core.shortestpath.ShortestPathAlgorithm;
import microtrafficsim.core.shortestpath.ShortestPathEdge;
import microtrafficsim.core.shortestpath.ShortestPathNode;
import microtrafficsim.math.HaversineDistanceCalculator;

import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;


/**
 * This class represents an abstract A* algorithm. It is abstract because you
 * have to extend "getEdgeWeight(@IDijkstrableEdge edge)" and
 * "estimate({@link ShortestPathEdge} edge)". Therefore you are just allowed to use
 * positive edge weights.
 * <p>
 * In case you want to implement Dijkstra's algorithm, you have to extend this
 * class and implement getEdgeWeight(...) and estimate(...) should return 0.
 * <p>
 * IShortestPathAlgorithm serves
 * "findShortestPath(IDijkstrableNode start, IDijkstrableNode end)"
 *
 * @author Jan-Oliver Schmidt, Dominic Parga Cacheiro
 */
public class AStarAlgorithm implements ShortestPathAlgorithm {

    private HashSet<ShortestPathNode> visitedNodes;
    private HashMap<ShortestPathNode, EdgeWeightTuple> predecessors;
    private PriorityQueue<WeightedNode> queue;
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
    public AStarAlgorithm(Function<ShortestPathEdge, Float> edgeWeightFunction,
                          BiFunction<ShortestPathNode, ShortestPathNode, Float> estimationFunction) {
        visitedNodes = new HashSet<>();
        predecessors = new HashMap<>();
        queue        = new PriorityQueue<>();

        this.edgeWeightFunction = edgeWeightFunction;
        this.estimationFunction = estimationFunction;
    }

    /**
     * @return Standard implementation of Dijkstra's algorithm for calculating the shortest (not necessarily fastest)
     * path using {@link ShortestPathEdge#getLength()}
     */
    public static AStarAlgorithm createShortestWayDijkstra() {
        return new AStarAlgorithm(
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
        if (start != end) {
            // INIT (the same as in the while-loop below)
            // this is needed to guarantee that each node in the queue has a
            // predecessor
            // => no if-condition if it has a predecessor
            WeightedNode origin = new WeightedNode(start, 0f, estimationFunction.apply(start, end));
            visitedNodes.add(origin.node);
            // iterate over all leaving edges
            Iterator<ShortestPathEdge> leavingEdges = origin.node.getLeavingEdges(null);
            while (leavingEdges.hasNext()) {
                ShortestPathEdge edge = leavingEdges.next();
                ShortestPathNode dest = edge.getDestination();
                float            g    = origin.g + edgeWeightFunction.apply(edge);

                // update predecessors
                EdgeWeightTuple p = predecessors.get(dest);
                if (p == null) {
                    predecessors.put(dest, new EdgeWeightTuple(g, edge));
                } else {
                    if (p.weight > g) {
                        p.weight = g;
                        p.edge   = edge;
                    }
                }

                // push new node into priority queue
                if (!visitedNodes.contains(dest)) {
                    queue.add(new WeightedNode(dest, g, estimationFunction.apply(dest, end)));
                }
            }

            // ALGORITHM
            // now: each node in the queue has a predecessor
            while (!queue.isEmpty()) {
                origin = queue.poll();

                if (!visitedNodes.contains(origin.node)) {
                    // if shortest path to end is already found
                    if (origin.node == end) {
                        // create shortest path
                        ShortestPathNode curNode = end;

                        while (curNode != start) {
                            ShortestPathEdge curEdge = predecessors.get(curNode).edge;
                            shortestPath.push(curEdge);
                            curNode = curEdge.getOrigin();
                        }

                        break;
                    }

                    visitedNodes.add(origin.node);

                    // iterate over all leaving edges
                    leavingEdges = origin.node.getLeavingEdges(predecessors.get(origin.node).edge);
                    while (leavingEdges.hasNext()) {
                        ShortestPathEdge edge = leavingEdges.next();
                        ShortestPathNode dest = edge.getDestination();
                        float            g    = origin.g + edgeWeightFunction.apply(edge);

                        // update predecessors
                        EdgeWeightTuple p = predecessors.get(dest);
                        if (p == null) {
                            predecessors.put(dest, new EdgeWeightTuple(g, edge));
                        } else {
                            if (p.weight > g) {
                                p.weight = g;
                                p.edge   = edge;
                            }
                        }

                        // push new node into priority queue
                        if (!visitedNodes.contains(dest)) {
                            queue.add(new WeightedNode(dest, g, estimationFunction.apply(dest, end)));
                        }
                    }
                }
            }
        }

        // refresh
        visitedNodes.clear();
        predecessors.clear();
        queue.clear();
    }
}
