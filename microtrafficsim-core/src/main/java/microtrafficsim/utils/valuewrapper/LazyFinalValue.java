package microtrafficsim.utils.valuewrapper;

/**
 * This class is used, if a final variable should be set later than in a constructor, but still only once.
 *
 * @author Dominic Parga Cacheiro
 */
public class LazyFinalValue<T> implements ILazyFinalValue<T>, IConcurrentValue<T> {

    private T t;
    private boolean alreadySet;

    public LazyFinalValue() {
        this(null);
    }

    /**
     * @param init initial value (e.g. a default value)
     */
    public LazyFinalValue(T init) {
        this.t = init;
        alreadySet = false;
    }

    public synchronized T get() {
        return t;
    }

    public synchronized void set(T t) {
        if (!alreadySet) {
            alreadySet = true;
            this.t = t;
        } else
            try {
                throw new LazyFinalException();
            } catch (LazyFinalException e) {
                e.printStackTrace();
            }
    }

    public synchronized boolean isAlreadySet() {
        return alreadySet;
    }

    @Override
    public synchronized boolean equals(Object obj) {
        if (t == null)
            return obj == null;
        return t.equals(obj);
    }
}
