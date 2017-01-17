package microtrafficsim.utils.id;


/**
 * Simple implementation of the {@code LongGenerator} returning IDs. This is implementation
 * works by simply incrementing an internal value synchronized and thus is
 * thread-safe. The first ID is 0 per default.
 *
 * @author Dominic Parga Cacheiro
 */
public class ConcurrentLongIDGenerator implements LongGenerator {

    private long id;

    public ConcurrentLongIDGenerator() {
        this(0);
    }

    public ConcurrentLongIDGenerator(long initialID) {
        id = initialID;
    }

    @Override
    public synchronized long next() {
        long returnID = id;
        id            = id + 1;
        return returnID;
    }
}