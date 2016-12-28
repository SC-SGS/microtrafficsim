package microtrafficsim.utils.datacollection;


/**
 * This interface serves functionality for saving {@link Data} assigned to a {@link Tag}.
 *
 * @author Dominic Parga Cacheiro
 */
public interface DataCollector<T> extends Iterable<T> {
    /**
     * A bundle is a collection of {@link Data} assigned to the given tag.
     *
     * @param tag {@code Data} can be added under this {@code Tag}.
     * @return The return value and its meaning can be defined by subclasses, e.g. whether the addition was successful
     */
    boolean addBundle(String tag);

    /**
     * The given object of type {@code T} is added to this collection under the given tag after getting wrapped by a
     * {@link Data}.
     *
     * @param tag {@code Data} can be added under this {@code Tag}.
     * @param t The object that should be saved in this collection.
     */
    void put(String tag, T t);

    /**
     * Returns the size of the collection assigned to the given tag or 0, if there is not such a tag.
     *
     * @param tag {@code Data} can be added under this {@code Tag} with other methods.
     * @return the size of the collection assigned to the given tag; or 0, if there is not such a tag or this collection
     * is empty.
     */
    int size(String tag);
}
