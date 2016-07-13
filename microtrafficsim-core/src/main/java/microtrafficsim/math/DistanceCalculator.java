package microtrafficsim.math;

import microtrafficsim.core.map.Coordinate;


/**
 * Functional interface for distance calculation of two Coordinates.
 * Coordinate dates depend on the implementation.
 *
 * @author Maximilian Luz
 */
public interface DistanceCalculator {

    /**
     * Calculate the distance between the two given Coordinates.
     *
     * @param a the first coordinate.
     * @param b the second coordinate.
     * @return the distance between {@code a} and {@code b}.
     */
    double getDistance(Coordinate a, Coordinate b);
}
