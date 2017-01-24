package microtrafficsim.utils.id;

import microtrafficsim.math.random.distributions.impl.ResettableRandom;

/**
 * Basic (not concurrent) implementation of {@link LongGenerator} using an object of {@link ResettableRandom} to create
 * seeds
 * for
 * every call of {@link #next()}.
 *
 * @author Dominic Parga Cacheiro
 */
public class BasicSeedGenerator implements LongGenerator {

    private ResettableRandom seeds;

    /**
     * @param seed This seed is used to initialize the instance of {@link ResettableRandom}, which generates seeds for
     *             every call
     *             of {@link #next()}.
     */
    public BasicSeedGenerator(long seed) {
        seeds = new ResettableRandom(seed);
    }

    /**
     * @return seeds.nextLong(), where seeds is an object of {@link ResettableRandom} initialized in the constructor of
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
