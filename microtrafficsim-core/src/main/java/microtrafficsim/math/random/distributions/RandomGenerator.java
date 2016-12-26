package microtrafficsim.math.random.distributions;

import java.util.Random;

/**
 * Serves functionality for general use of {@link Random} or similar, because Java is not able to do this.
 *
 * @author Dominic Parga Cacheiro
 */
public interface RandomGenerator {

    /**
     * Should reset this {@code RandomGenerator} to initialization state. This means it can return the same numbers
     * in the same order as already returned.
     */
    void reset();

    /**
     * Sets the seed to the given one and {@code resets} this class.
     */
    void setSeed(long seed);

    /**
     * @return Currently used seed
     */
    long getSeed();

    /*
    |=========|
    | numbers |
    |=========|
    */
    /**
     * @return next integer due to the currently used seed
     */
    int nextInt();

    /**
     * @return next float due to the currently used seed
     */
    float nextFloat();
}
