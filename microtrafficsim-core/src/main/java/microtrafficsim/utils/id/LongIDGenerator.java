package microtrafficsim.utils.id;


/**
 * Simple interface for a ID generator returning long-values as ID.
 *
 * @author Maximilian Luz
 */
public interface LongIDGenerator {

    /**
     * Returns a new ID. Details to the uniqueness of this value are left to the
     * implementing method.
     *
     * @return the next ID.
     */
    long next();
}
