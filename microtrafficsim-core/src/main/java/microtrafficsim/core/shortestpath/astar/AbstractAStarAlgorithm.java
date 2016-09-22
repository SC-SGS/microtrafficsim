package microtrafficsim.core.shortestpath.astar;

import microtrafficsim.core.shortestpath.ShortestPathAlgorithm;
import microtrafficsim.core.shortestpath.ShortestPathEdge;
import microtrafficsim.core.shortestpath.ShortestPathNode;

import java.util.*;


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
public abstract class AbstractAStarAlgorithm implements ShortestPathAlgorithm {

    /**
     * <p>
     * The A* algorithm uses a node A from the priority queue for actualizing
     * (if necessary) the weight of each node B, that is a destination of an
     * edge starting in A. For this, it needs the current weight of A plus the
     * weight of the mentioned edge.
     * </p>
     * <p>
     * <p>
     * Invariants:<br>
     * This estimation has to be positive (or 0).
     * </p>
     *
     * @param edge The edge that leaves the currently visited node of the A*
     *             algorithm.
     * @return Edge weight.
     */
    protected abstract <T extends ShortestPathEdge> float getEdgeWeight(T edge);

    /**
     * <p>
     * The A* algorithm uses a node A from the priority queue for actualizing
     * (if necessary) the weight of each node B, that is a destination of an
     * edge starting in A. In addition, the A* algorithm estimates the distance
     * from this destination to the end of the route to calculate the shortest
     * path faster.
     * </p>
     * <p>
     * Invariants:<br>
     * 1) This estimation must be lower or equal to the real shortest path from
     * destination to the route's end. So it is not allowed to be more
     * pessimistic than the correct shortest path. Otherwise, it is not
     * guaranteed, that the A* algorithm returns correct results.<br>
     * 2) This estimation has to be positive or 0.
     * </p>
     *
     * @param destination      The destination node of an edge leaving the currently visited
     *                         node of the A* algorithm.
     * @param routeDestination The end of the route => Last node of the shortest path.
     * @return Estimation for the shortest path from destination to the end.
     * E.g. return 0 for Dijkstra's algorithm or the linear distance as
     * usual used approximation.
     */
    protected abstract <T extends ShortestPathNode> float estimate(T destination, T routeDestination);

    // |============================|
    // | (i) IShortestPathAlgorithm |
    // |============================|
    @Override
    public Queue<? extends ShortestPathEdge> findShortestPath(ShortestPathNode start, ShortestPathNode end) {
        PriorityQueue<Node> queue = new PriorityQueue<>();
        HashMap<ShortestPathNode, Node> visited = new HashMap<>();

        Node origin = new Node(start, null, 0, estimate(start, end));
        queue.add(origin);

        while (!queue.isEmpty()) {
            Node current = queue.poll();

            if (current.node == end) {     // found the shortest path
                LinkedList<ShortestPathEdge> result = new LinkedList<>();

                while (current.predecessor != null) {
                    result.addFirst(current.predecessor);
                    current = visited.get(current.predecessor.getOrigin());
                }

                return result;
            }

            if (visited.keySet().contains(current.node))
                continue;

            visited.put(current.node, current);
            for (ShortestPathEdge leaving : current.node.getLeavingEdges(current.predecessor)) {
                ShortestPathNode next = leaving.getDestination();

                if (!visited.keySet().contains(next))
                    queue.add(new Node(next, leaving, current.cost + getEdgeWeight(leaving), estimate(next, end)));
            }
        }

        System.out.println("no way found from " + start.toString() + " to " + end.toString());
        return new LinkedList<>();
    }
    /*
     * Note: the algorithm above is actually not quite correct: The leaving edges depend on the incoming edge, so
     * if a shortest-path would lead through a certain node from a certain edge that enables different outgoing edges
     * than the evaluation of said node before, and such an outgoing edge is only accessible from the incoming edge used
     * at the second evaluation, the algorithm would not provide a correct answer, because the node will not be
     * evaluated two (or more) times.
     *
     * The correct behaviour could be restored by storing visited nodes in combination with outgoing edges.
     */


    /**
     * Node for the A* algorithm.
     */
    public static class Node implements Comparable<Node> {
        final ShortestPathNode node;
        final ShortestPathEdge predecessor;
        final float            cost;            // cost from start to this node
        final float            weight;          // weight = cost + estimate

        /**
         * Standard constructor.
         *
         * @param node        the {@link ShortestPathNode} to be wrapped.
         * @param predecessor the edge leading to this node.
         * @param cost        the actual cost from the start to this node.
         * @param estimate    the estimated cost to reach the destination from this node.
         */
        Node(ShortestPathNode node, ShortestPathEdge predecessor, float cost, float estimate) {
            this.node        = node;
            this.predecessor = predecessor;
            this.cost        = cost;
            this.weight      = cost + estimate;
        }

        @Override
        public int compareTo(Node other) {
            if (weight < other.weight) return -1;
            if (weight > other.weight) return 1;
            return 0;
        }
    }
}
