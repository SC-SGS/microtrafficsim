package microtrafficsim.utils.id;


/**
 * Simple implementation of the {@code LongGenerator} returning IDs.
 * This is implementation works by simply incrementing an internal value and
 * thus is not thread-safe.
 *
 * @author Maximilian Luz
 */
public class BasicLongIDGenerator implements LongGenerator {

    private long id;
    private final long initialID;

    public BasicLongIDGenerator() {
        this(0);
    }

    public BasicLongIDGenerator(long initialID) {
        id = initialID;
        this.initialID = initialID;
    }

    @Override
    public long next() {
        return id++;
    }

    @Override
    public void reset() {
        id = initialID;
    }
}
