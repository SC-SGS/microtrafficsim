package microtrafficsim.utils.valuewrapper;

/**
 * This interface is used, if a final variable should be set later than in a constructor, but still only once.
 *
 * @author Dominic Parga Cacheiro
 */
public interface ILazyFinalValue<T> {

    T get();

    /**
     * @param t
     * @return true <=> setting value was successfully
     * @throws LazyFinalException if value is already set
     */
    void set(T t);

    /**
     * @return Whether this value is already set. This class can detect, if a value was set to null.
     */
    boolean isAlreadySet();
}
