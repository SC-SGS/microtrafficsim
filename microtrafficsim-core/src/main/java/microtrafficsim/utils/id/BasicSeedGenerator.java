package microtrafficsim.utils.id;

import microtrafficsim.math.random.Seeded;
import microtrafficsim.math.random.distributions.impl.Random;

/**
 * Basic (not concurrent) implementation of {@link LongGenerator} using an object of {@link Random} to create
 * seeds
 * for
 * every call of {@link #next()}.
 *
 * @author Dominic Parga Cacheiro
 */
public class BasicSeedGenerator implements LongGenerator, Seeded {

    private Random seeds;

    /**
     * @param seed This seed is used to initialize the instance of {@link Random}, which generates seeds for
     *             every call
     *             of {@link #next()}.
     */
    public BasicSeedGenerator(long seed) {
        seeds = new Random(seed);
    }

    @Override
    public void setSeed(long seed) {
        seeds.setSeed(seed);
    }

    @Override
    public long getSeed() {
        return seeds.getSeed();
    }

    /**
     * @return seeds.nextLong(), where seeds is an object of {@link Random} initialized in the constructor of
     * this class
     */
    @Override
    public long next() {
        return seeds.nextLong();
    }

    @Override
    public void reset() {
        seeds.reset();
    }
}
