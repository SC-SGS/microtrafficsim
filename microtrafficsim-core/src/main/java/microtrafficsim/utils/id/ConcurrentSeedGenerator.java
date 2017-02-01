package microtrafficsim.utils.id;


import microtrafficsim.math.random.Seeded;
import microtrafficsim.math.random.distributions.impl.Random;

/**
 * Concurrent implementation of {@link LongGenerator} using an object of {@link Random} to create seeds for every
 * call of {@link #next()}.
 *
 * @author Dominic Parga Cacheiro
 */
public class ConcurrentSeedGenerator implements LongGenerator, Seeded {

    private Random seeds;

    public ConcurrentSeedGenerator() {
        seeds = new Random();
    }

    /**
     * @param seed This seed is used to initialize the instance of {@link Random}, which generates seeds for every call
     *             of {@link #next()}.
     */
    public ConcurrentSeedGenerator(long seed) {
        seeds = new Random(seed);
    }

    /*
    |============|
    | (i) Seeded |
    |============|
    */
    @Override
    public synchronized void setSeed(long seed) {
        seeds.setSeed(seed);
    }

    @Override
    public synchronized long getSeed() {
        return seeds.getSeed();
    }

    /*
    |===================|
    | (i) LongGenerator |
    |===================|
    */
    /**
     * @return seeds.nextLong(), where seeds is an object of {@link Random} initialized in the constructor
     * of this class
     */
    @Override
    public synchronized long next() {
        return seeds.nextLong();
    }

    @Override
    public synchronized void reset() {
        seeds.reset();
    }
}
