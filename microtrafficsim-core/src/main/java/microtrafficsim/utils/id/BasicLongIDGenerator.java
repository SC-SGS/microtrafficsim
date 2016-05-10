package microtrafficsim.utils.id;


/**
 * Simple implementation of the {@code LongIDGenerator}.
 * This is implementation works by simply incrementing an internal value and
 * thus is not thread-safe.
 *
 * @author Maximilian Luz
 */
public class BasicLongIDGenerator implements LongIDGenerator {
	
	private long id;
	
	public BasicLongIDGenerator() {
		this.id = 0;
	}

	
	@Override
	public long next() {
		return id++;
	}
}
