package microtrafficsim.utils.valuewrapper.observable;

import java.util.Observable;
import java.util.Observer;
import java.util.Vector;

/**
 * Just a copy of {@link java.util.Observable} implementing a generic argument in {@link #notifyObservers(Object)} for
 * the argument of {@link Observer#update(Observable, Object)}.
 *
 * @author Dominic Parga Cacheiro
 */
public class GenericObservable<T> {
    private boolean changed = false;
    private Vector<GenericObserver<T>> obs;

    /** Construct an Observable with zero Observers. */
    public GenericObservable() {
        obs = new Vector<>();
    }

    /**
     * See {@link Observable#addObserver(Observer)}
     */
    public synchronized void addObserver(GenericObserver<T> o) {
        if (o == null)
            throw new NullPointerException();
        if (!obs.contains(o)) {
            obs.addElement(o);
        }
    }

    /**
     * See {@link Observable#deleteObserver(Observer)}
     */
    public synchronized void deleteObserver(GenericObserver<T> o) {
        obs.removeElement(o);
    }

    /**
     * See {@link Observable#notifyObservers()}
     */
    public void notifyObservers() {
        notifyObservers(null);
    }

    /**
     * See {@link Observable#notifyObservers(Object)}
     */
    public void notifyObservers(T arg) {
        /*
         * a temporary array buffer, used as a snapshot of the state of
         * current Observers.
         */
        Object[] arrLocal;

        synchronized (this) {
            /* We don't want the Observer doing callbacks into
             * arbitrary code while holding its own Monitor.
             * The code where we extract each Observable from
             * the Vector and store the state of the Observer
             * needs synchronization, but notifying observers
             * does not (should not).  The worst result of any
             * potential race-condition here is that:
             * 1) a newly-added Observer will miss a
             *   notification in progress
             * 2) a recently unregistered Observer will be
             *   wrongly notified when it doesn't care
             */
            if (!changed)
                return;
            arrLocal = obs.toArray();
            clearChanged();
        }

        for (int i = arrLocal.length-1; i>=0; i--)
            ((GenericObserver<T>)arrLocal[i]).update(this, arg);
    }

    /**
     * See {@link Observable#deleteObservers()}
     */
    public synchronized void deleteObservers() {
        obs.removeAllElements();
    }

    /**
     * See {@link Observable#setChanged()}
     */
    protected synchronized void setChanged() {
        changed = true;
    }

    /**
     * See {@link Observable#clearChanged()}
     */
    protected synchronized void clearChanged() {
        changed = false;
    }

    /**
     * See {@link Observable#hasChanged()}
     */
    public synchronized boolean hasChanged() {
        return changed;
    }

    /**
     * See {@link Observable#countObservers()}
     */
    public synchronized int countObservers() {
        return obs.size();
    }
}
