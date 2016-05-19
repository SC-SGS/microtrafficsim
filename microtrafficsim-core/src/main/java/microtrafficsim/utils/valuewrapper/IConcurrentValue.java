package microtrafficsim.utils.valuewrapper;

/**
 * This interface is thread safe.
 *
 * @author Dominic Parga Cacheiro
 */
public interface IConcurrentValue<T> {

    T get();

    void set(T t);
}
