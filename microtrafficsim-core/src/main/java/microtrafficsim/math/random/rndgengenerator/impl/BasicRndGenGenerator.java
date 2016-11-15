package microtrafficsim.math.random.rndgengenerator.impl;

import microtrafficsim.math.random.rndgengenerator.RndGenGenerator;

import java.util.Random;

/**
 * Basic (not concurrent) implementation of {@link RndGenGenerator} using an object of {@link Random} to create seeds for every call of
 * {@link #next()}.
 *
 * @author Dominic Parga Cacheiro
 */
public class BasicRndGenGenerator implements RndGenGenerator {

    private Random seeds;

    /**
     * @param seed This seed is used to initialize the instance of {@link Random}, which generates seeds for every call
     *             of {@link #next()}.
     */
    public BasicRndGenGenerator(long seed) {
        seeds = new Random(seed);
    }

    /**
     * @return new Random(seeds.nextLong()), where seeds is an object of {@link Random} initialized in the constructor
     * of this class
     */
    @Override
    public Random next() {
        return new Random(seeds.nextLong());
    }
}
