package microtrafficsim.math.random;

/**
 * @author Dominic Parga Cacheiro
 */
public interface Seeded {

    /**
     * Sets the seed to the given one and {@code resets} this class.
     */
    void setSeed(long seed);

    /**
     * @return Currently used seed
     */
    long getSeed();
}
