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

    /**
     * @see java.util.Random#nextBytes(byte[])
     */
    public byte nextByte() {
        byte[] b = new byte[1];
        random.nextBytes(b);
        return b[0];
    }

    /**
     * @see java.util.Random#nextBytes(byte[])
     */
    public byte[] nextByte(int count) {
        byte[] b = new byte[count];
        random.nextBytes(b);
        return b;
    }

    /**
     * @see java.util.Random#nextBytes(byte[])
     */
    public void nextByte(byte[] b) {
        random.nextBytes(b);
    }

    /**
     * @see java.util.Random#nextInt()
     */
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

    /**
     * @see java.util.Random#nextLong()
     */
    @Override
    public long nextLong() {
        return random.nextLong();
    }

    /**
     * @see java.util.Random#nextFloat()
     */
    @Override
    public float nextFloat() {
        return random.nextFloat();
    }
}
