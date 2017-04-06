package microtrafficsim.core.shortestpath;


/**
 * This interface guarantees a unified access to needed edge functions for a shortest path algorithm.
 *
 * @author Jan-Oliver Schmidt, Dominic Parga Cacheiro
 */
public interface ShortestPathEdge<N> {

    /**
     * @return Length of this edge in a certain unit.
     */
    int getLength();

    /**
     * This method is for weights using time instead of length (in milliseconds). E.g. a motorway
     * could be passed faster than a shorter road, where you have to drive more
     * slowly.
     *
     * @return Time to pass this edge in milliseconds
     */
    double getTimeCostMillis();

    N getOrigin();
    N getDestination();
}