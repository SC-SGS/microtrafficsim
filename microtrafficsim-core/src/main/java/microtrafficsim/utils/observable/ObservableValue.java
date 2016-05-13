package microtrafficsim.utils.observable;

/**
 * @author Dominic Parga Cacheiro
 *
 * @param <T> The observed value's type
 * @param <A> The type of the argument in {@link GenericObserver#update(GenericObservable, Object)}
 */
public final class ObservableValue<T, A> extends GenericObservable<A> {

    private T t;
    public final A id;

    /**
     *
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
}