package microtrafficsim.core.map;


/**
 * Groups multiple {@code FeaturePrimitive} objects of the same type to a named
 * group.
 *
 * @param <T> the type of the grouped objects
 * @author Maximilian Luz
 */
public class Feature<T extends FeaturePrimitive> {

    private final String name;
    private final Class<T> type;
    private T[] data;

    /**
     * Constructs a new feature with the given name, type and data.
     *
     * @param name the name of the feature.
     * @param type the type of the feature.
     * @param data the data contained in the future.
     */
    public Feature(String name, Class<T> type, T[] data) {
        this.name = name;
        this.type = type;
        this.data = data;
    }


    /**
     * Returns the name of this feature.
     *
     * @return the name of this feature.
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the type of this feature.
     *
     * @return the type of this feature.
     */
    public Class<T> getType() {
        return type;
    }

    /**
     * Returns the descriptor describing this feature.
     *
     * @return the descriptor describing this feature.
     */
    public FeatureDescriptor getDescriptor() {
        return new FeatureDescriptor(name, type);
    }

    /**
     * Returns the data of this feature.
     *
     * @return the data of this feature.
     */
    public T[] getData() {
        return data;
    }
}
