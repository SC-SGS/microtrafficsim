package microtrafficsim.utils.id;


import microtrafficsim.utils.Resettable;

/**
 * Simple interface for a generator returning long-values (e.g. as id).
 *
 * @author Maximilian Luz, Dominic Parga Cacheiro
 */
public interface LongGenerator extends Resettable {

    /**
     * Returns a new long value. Details to the uniqueness of this value are left to the
     * implementing method.
     *
     * @return the next long.
     */
    long next();
}
