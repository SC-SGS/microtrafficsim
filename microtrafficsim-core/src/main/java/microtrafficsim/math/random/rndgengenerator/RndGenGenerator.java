package microtrafficsim.math.random.rndgengenerator;

import java.util.Random;

/**
 * Simple interface defining a functionality for returning {@link Random} objects.
 *
 * @author Dominic Parga Cacheiro
 */
public interface RndGenGenerator {

    /**
     * Returns a new {@link Random} object. Details to the uniqueness of this value are left to the
     * implementing method.
     *
     * @return the next {@link Random} object
     */
    Random next();
}
