package microtrafficsim.math.random;

/**
 * @author Dominic Parga Cacheiro
 */
public interface Seeded {

    /**
     * @return Currently used seed
     */
    long getSeed();

    /**
     * Sets the seed to the given one and {@code resets} this class.
     */
    void setSeed(long seed);
}
