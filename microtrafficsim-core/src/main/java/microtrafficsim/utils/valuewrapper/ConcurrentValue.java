package microtrafficsim.utils.valuewrapper;

/**
 * This class is thread safe.
 *
 * @author Dominic Parga Cacheiro
 */
public class ConcurrentValue<T> implements IConcurrentValue<T> {

    private T t;

    /**
     * Default value: null
     */
    public ConcurrentValue() {
        this(null);
    }

    public ConcurrentValue(T t) {
        this.t = t;
    }

    public synchronized T get() {
        return t;
    }

    public synchronized void set(T t) {
        this.t = t;
    }

    /*
    |============|
    | (c) Object |
    |============|
    */
    @Override
    public synchronized boolean equals(Object obj) {
        if (t == null)
            return obj == null;
        return t.equals(obj);
    }
}
