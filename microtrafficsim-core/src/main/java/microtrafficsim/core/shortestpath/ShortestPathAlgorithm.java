package microtrafficsim.core.shortestpath;

import java.util.Stack;


/**
 * <p>
 * Allows using a shortest path algorithm without knowing the
 * algorithm. You don't have to know very much about the nodes and edges,
 * because they just have to implement {@link ShortestPathNode} and
 * {@link ShortestPathEdge}. Therefore you can implement this interface in many
 * algorithms and graph types.
 * <p>
 * You don't need a graph because dependencies about nodes and edges are saved
 * in them, not in a graph structure.
 * <p>
 * IMPORTANT: Per default, an instance of this interface can be used concurrently.
 *
 * @author Jan-Oliver Schmidt, Dominic Parga Cacheiro
 */
public interface ShortestPathAlgorithm<N extends ShortestPathNode<E>, E extends ShortestPathEdge<N>> {

    /**
     * @return Hence this implementation needs no preprocessing, this method returns true.
     */
    default boolean isPreprocessed() {
        return true;
    }

    /**
     * Is needed for some algorithms for preparations needed for faster shortest path finding. It is implemented
     * empty per default.
     */
    default void preprocess() {}

    /**
     * <pr>
     * Calculates the shortest path and returns a stack containing all edges starting with an edge leaving the given
     * start node and ending with an edge going to the given end node.
     * <p>
     * A stack is chosen because it is useful for dynamic routing: if the next k edges are (however) a bad choice now,
     * you could just pop them until a certain node and you can add another path to it by pushing new edges on the
     * stack.
     * <p>
     * IMPORTANT: This method MUST NOT clear the given stack. In case of dynamic routing, you have to ensure that the
     * data, which is already in the stack, gets not damaged. Furthermore, this method should be callable
     * concurrently per default.
     *
     * @param start First node of the shortest path
     * @param end Last node of the shortest path
     * @param shortestPath This data structure gets NOT cleared, but filled with the edges of the shortest path, that is
     *                     calculated in this method.
     */
    void findShortestPath(N start, N end, Stack<? super E> shortestPath);
}