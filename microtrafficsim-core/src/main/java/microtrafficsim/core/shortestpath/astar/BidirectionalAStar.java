package microtrafficsim.core.shortestpath.astar;

import microtrafficsim.core.shortestpath.ShortestPathAlgorithm;
import microtrafficsim.core.shortestpath.ShortestPathEdge;
import microtrafficsim.core.shortestpath.ShortestPathNode;

import java.util.*;
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
    private HashSet<ShortestPathNode> forwardVisitedNodes, backwardVisitedNodes;
    private HashMap<ShortestPathNode, EdgeWeightTuple> predecessors, successors;
    private PriorityQueue<WeightedNode> forwardQueue, backwardQueue;
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
        // forward
        forwardVisitedNodes = new HashSet<>();
        predecessors = new HashMap<>();
        forwardQueue = new PriorityQueue<>();

        // backward
        backwardVisitedNodes = new HashSet<>();
        successors = new HashMap<>();
        backwardQueue = new PriorityQueue<>();

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
    /**
     * This method is not needed in this algorithm and thus its empty.
     */
    @Override
    public void preprocess() {

    }

    @Override
    public void findShortestPath(ShortestPathNode start, ShortestPathNode end, Stack<ShortestPathEdge> shortestPath) {
        if (start != end) {
            // INIT FORWARDS (the same as in the while-loop below)
            // this is needed because the first node has no predecessors in this algorithm
            float estimation = estimationFunction.apply(start, end);
            WeightedNode origin = new WeightedNode(start, 0f, estimation);
            forwardVisitedNodes.add(origin.node);
            // iterate over all leaving edges
            for (ShortestPathEdge edge : origin.node.getLeavingEdges(null)) {
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
                if (!forwardVisitedNodes.contains(dest)) {
                    forwardQueue.add(new WeightedNode(dest, g, estimationFunction.apply(dest, end)));
                }
            }

            // INIT BACKWARDS (the same as in the while-loop below)
            // this is needed because the last node has no successors in this algorithm
            WeightedNode destination = new WeightedNode(end, 0f, estimation);
            backwardVisitedNodes.add(destination.node);
            // iterate over all incoming edges
            for (ShortestPathEdge edge : destination.node.getIncomingEdges()) {
                ShortestPathNode orig = edge.getOrigin();
                float            g    = destination.g + edgeWeightFunction.apply(edge);

                // update predecessors
                EdgeWeightTuple p = successors.get(orig);
                if (p == null) {
                    successors.put(orig, new EdgeWeightTuple(g, edge));
                } else {
                    if (p.weight > g) {
                        p.weight = g;
                        p.edge   = edge;
                    }
                }

                // push new node into priority queue
                if (!backwardVisitedNodes.contains(orig)) {
                    backwardQueue.add(new WeightedNode(orig, g, estimationFunction.apply(start, orig)));
                }
            }

            // ALGORITHM
            // now: each node in the queues has a predecessor/successor
            // while at least one queue is not empty
            ShortestPathNode meetingNode = null;
            while (!(forwardQueue.isEmpty() && backwardQueue.isEmpty())) {
                // one step FORWARDS
                if (!forwardQueue.isEmpty()) {
                    origin = forwardQueue.poll();

                    if (!forwardVisitedNodes.contains(origin.node)) {
                        // if shortest path to end is already found
                        if (backwardVisitedNodes.contains(origin.node)) {
                            meetingNode = origin.node;
                            break;
                        }

                        forwardVisitedNodes.add(origin.node);

                        // iterate over all leaving edges
                        for (ShortestPathEdge edge : origin.node.getLeavingEdges(predecessors.get(origin.node).edge)) {
                            ShortestPathNode dest = edge.getDestination();
                            float g = origin.g + edgeWeightFunction.apply(edge);

                            // update predecessors
                            EdgeWeightTuple p = predecessors.get(dest);
                            if (p == null) {
                                predecessors.put(dest, new EdgeWeightTuple(g, edge));
                            } else {
                                if (p.weight > g) {
                                    p.weight = g;
                                    p.edge = edge;
                                }
                            }

                            // push new node into priority queue
                            if (!forwardVisitedNodes.contains(dest)) {
                                forwardQueue.add(new WeightedNode(dest, g, estimationFunction.apply(dest, end)));
                            }
                        }
                    }
                }

                // one step BACKWARDS
                if (!backwardQueue.isEmpty()) {
                    destination = backwardQueue.poll();

                    if (!backwardVisitedNodes.contains(destination.node)) {
                        // if shortest path to start is already found
                        if (forwardVisitedNodes.contains(destination.node)) {
                            meetingNode = destination.node;
                            break;
                        }

                        backwardVisitedNodes.add(destination.node);

                        // iterate over all incoming edges
                        for (ShortestPathEdge edge : destination.node.getIncomingEdges()) {
                            ShortestPathNode orig = edge.getOrigin();
                            float g = destination.g + edgeWeightFunction.apply(edge);

                            // update successors
                            EdgeWeightTuple p = successors.get(orig);
                            if (p == null) {
                                successors.put(orig, new EdgeWeightTuple(g, edge));
                            } else {
                                if (p.weight > g) {
                                    p.weight = g;
                                    p.edge = edge;
                                }
                            }

                            // push new node into priority queue
                            if (!backwardVisitedNodes.contains(orig)) {
                                backwardQueue.add(new WeightedNode(orig, g, estimationFunction.apply(start, orig)));
                            }
                        }
                    }
                }
            }

            if (meetingNode != null) {
                // create shortest path

                // BACKWARDS
                Stack<ShortestPathEdge> bin = new Stack<>();
                ShortestPathNode curNode = meetingNode;
                while (curNode != end) {
                    ShortestPathEdge curEdge = successors.get(curNode).edge;
                    bin.push(curEdge);
                    curNode = curEdge.getDestination();
                }
                while (!bin.isEmpty()) {
                    shortestPath.push(bin.pop());
                }

                // FORWARDS
                curNode = meetingNode;
                while (curNode != start) {
                    ShortestPathEdge curEdge = predecessors.get(curNode).edge;
                    shortestPath.push(curEdge);
                    curNode = curEdge.getOrigin();
                }
            }
        }

        // refresh FORWARDS
        forwardVisitedNodes.clear();
        predecessors.clear();
        forwardQueue.clear();
        // refresh BACKWARDS
        backwardVisitedNodes.clear();
        successors.clear();
        backwardQueue.clear();
    }
}