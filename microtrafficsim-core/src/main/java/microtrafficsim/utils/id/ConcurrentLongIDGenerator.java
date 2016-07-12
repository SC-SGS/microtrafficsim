package microtrafficsim.utils.id;


/**
 * Simple implementation of the {@code LongIDGenerator}. This is implementation
 * works by simply incrementing an internal value synchronized and thus is
 * thread-safe.
 *
 * @author Dominic Parga Cacheiro
 */
public class ConcurrentLongIDGenerator implements LongIDGenerator {

    private long id;

    public ConcurrentLongIDGenerator() {
        this.id = 0;
    }


    @Override
    public synchronized long next() {
        long returnID = id;
        id            = id + 1;
        return returnID;
    }
}