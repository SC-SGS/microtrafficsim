package microtrafficsim.utils.observable;

import java.util.Observable;
import java.util.Observer;

/**
 * Just a copy of {@link Observer} implementing a generic argument in {@link Observer#update(Observable, Object)}.
 *
 * @author Dominic Parga Cacheiro
 */
public interface GenericObserver<T> {

    /**
     * See {@link Observer}
     */
    void update(GenericObservable<T> o, T arg);
}
