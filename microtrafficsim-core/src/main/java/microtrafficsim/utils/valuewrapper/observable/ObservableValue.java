package microtrafficsim.utils.valuewrapper.observable;

import microtrafficsim.utils.valuewrapper.IConcurrentValue;

/**
 * @author Dominic Parga Cacheiro
 *
 * @param <T> The observed value's type
 * @param <A> The type of the argument in {@link GenericObserver#update(GenericObservable, Object)}
 */
public final class ObservableValue<T, A>
        extends GenericObservable<A>
        implements IObservableValue<T>, IConcurrentValue<T>{

    private T t;
    public final A id;

    /**
     * Calls this(null)
     */
    public ObservableValue() {
        this(null);
    }

    /**
     * @param id null is allowed, but has to be catched in {@link GenericObserver#update(GenericObservable, Object)}
     */
    public ObservableValue(A id) {
        this.id = id;
    }

    public T get() {
        synchronized (this) {
            return t;
        }
    }

    public void set(T t) {
        synchronized (this) {
            this.t = t;
        }
        setChanged();
        notifyObservers(id);
    }

    @Override
    public synchronized boolean equals(Object obj) {
        if (t == null)
            return obj == null;
        return t.equals(obj);
    }
}