package microtrafficsim.utils.valuewrapper.observable;

/**
 * @author Dominic Parga Cacheiro
 *
 * @param <T> The observed value's type
 */
public interface IObservableValue<T> {

    T get();
    void set(T t);
}
