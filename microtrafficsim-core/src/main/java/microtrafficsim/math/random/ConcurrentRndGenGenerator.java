package microtrafficsim.math.random;

import java.util.Random;

/**
 * Basic and concurrent implementation of {@link RndGenGenerator} using an object of {@link Random} to create seeds for every call of
 * {@link #next()}.
 *
 * @author Dominic Parga Cacheiro
 */
public class ConcurrentRndGenGenerator implements RndGenGenerator {

    private Random seeds;

    /**
     * @param seed This seed is used to initialize the instance of {@link Random}, which generates seeds for every call
     *             of {@link #next()}.
     */
    public ConcurrentRndGenGenerator(long seed) {
        seeds = new Random(seed);
    }

    /**
     * @return new Random(seeds.nextLong()), where seeds is an object of {@link Random} initialized in the constructor
     * of this class
     */
    @Override
    public synchronized Random next() {
        return new Random(seeds.nextLong());
    }
}
