package microtrafficsim.math.random.distributions.impl;

import microtrafficsim.math.random.distributions.RandomGenerator;

import java.util.Random;

/**
 * Just a wrapper class for Javas {@code Random} class ensuring seed resetting.
 *
 * @author Dominic Parga Cacheiro
 */
public class JavaRandom implements RandomGenerator {

    private long seed;
    private Random random;
    private static volatile long seedUniquifier = 8682522807148012L;

    /**
     * <p>
     * Calls {@link #JavaRandom(long)} using {@code seedUniquifier + System.nanoTime()}.
     *
     * <p>
     * Modelled after the OpenJDK Random implementation.
     */
    public JavaRandom() {
        this(++seedUniquifier + System.nanoTime());
    }

    public JavaRandom(long seed) {
        setSeed(seed);
    }

    /*
    |=====================|
    | (i) RandomGenerator |
    |=====================|
    */
    /**
     * Implementation: Calls {@link #setSeed(long)} using current seed
     */
    @Override
    public void reset() {
        setSeed(seed);
    }

    @Override
    public void setSeed(long seed) {
        this.seed = seed;
        random = new Random(seed);
    }

    @Override
    public long getSeed() {
        return seed;
    }

    @Override
    public int nextInt() {
        return random.nextInt();
    }

    @Override
    public float nextFloat() {
        return random.nextFloat();
    }
}
