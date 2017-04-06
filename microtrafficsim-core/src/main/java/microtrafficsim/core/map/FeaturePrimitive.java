package microtrafficsim.core.map;


/**
 * Base-class for all feature primitives.
 *
 * @author Maximilian Luz
 */
public abstract class FeaturePrimitive {
    public long id;

    /**
     * Constructs a new {@code FuturePrimitive} with the given id.
     *
     * @param id the id of the created {@code FuturePrimitive}.
     */
    public FeaturePrimitive(long id) {
        this.id = id;
    }
}
