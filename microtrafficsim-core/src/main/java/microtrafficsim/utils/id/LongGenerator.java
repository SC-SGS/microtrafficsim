package microtrafficsim.utils.id;


/**
 * Simple interface for a generator returning long-values (e.g. as ID).
 *
 * @author Maximilian Luz, Dominic Parga Cacheiro
 */
public interface LongGenerator {

    /**
     * Returns a new long value. Details to the uniqueness of this value are left to the
     * implementing method.
     *
     * @return the next long.
     */
    long next();
}
