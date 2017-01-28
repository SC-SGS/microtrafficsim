package microtrafficsim.math.random.distributions.impl;

import microtrafficsim.math.random.distributions.RandomGenerator;

/**
 * Just a wrapper class for Javas {@code Random} class ensuring seed resetting.
 *
 * @author Dominic Parga Cacheiro
 */
public class Random implements RandomGenerator {

    private long seed;
    private java.util.Random random;
    private static volatile long seedUniquifier = 8682522807148012L;

    /**
     * <p>
     * Calls {@link #Random(long)} using {@code seedUniquifier + System.nanoTime()}.
     *
     * <p>
     * Modelled after the OpenJDK Random implementation.
     */
    public Random() {
        this(++seedUniquifier + System.nanoTime());
    }

    public Random(long seed) {
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
        random = new java.util.Random(seed);
    }

    @Override
    public long getSeed() {
        return seed;
    }

    @Override
    public int nextInt() {
        return random.nextInt();
    }

    /**
     * @see java.util.Random#nextInt(int)
     */
    @Override
    public int nextInt(int bound) {
        return random.nextInt(bound);
    }

    @Override
    public long nextLong() {
        return random.nextLong();
    }

    @Override
    public float nextFloat() {
        return random.nextFloat();
    }
}
