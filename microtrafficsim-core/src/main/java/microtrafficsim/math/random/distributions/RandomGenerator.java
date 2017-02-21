package microtrafficsim.math.random.distributions;

import microtrafficsim.math.random.Seeded;
import microtrafficsim.math.random.distributions.impl.Random;
import microtrafficsim.utils.Resettable;

/**
 * Serves functionality for general use of {@link Random} or similar, because Java is not able to do this.
 *
 * @author Dominic Parga Cacheiro
 */
public interface RandomGenerator extends Resettable, Seeded {

    /*
    |================|
    | (i) Resettable |
    |================|
    */
    /**
     * Should reset this {@code RandomGenerator} to initialization state. This means it can return the same numbers
     * in the same order as already returned.
     */
    @Override
    void reset();

    boolean nextBoolean();

    byte nextByte();

    byte[] nextByte(int count);

    void nextByte(byte[] b);

    /**
     * @return next integer due to the currently used seed
     */
    int nextInt();

    /**
     * @return an integer between 0 (inclusive) and bound (exclusive)
     */
    int nextInt(int bound);

    long nextLong();

    /**
     * @return next float due to the currently used seed
     */
    float nextFloat();
}
