package microtrafficsim.utils.concurrency;

import microtrafficsim.utils.functional.Procedure;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Creates one thread for executing only one task using {@link #requestAnExecutionThread(Procedure)}. If you want to
 * create another thread with another task, the old one has to finish its task or has to be interrupted first using
 * {@link #requestAnInterruptingThread(Procedure)}.
 *
 * @author Dominic Parga Cacheiro
 */
public class SingleExecutionThreadSupplier implements ExecutionThreadSupplier {
    private final AtomicBoolean isBusy;
    private final AtomicBoolean isInterrupting;


    public SingleExecutionThreadSupplier() {
        this(new AtomicBoolean(false));
    }

    /**
     * Sets the internal used {@link AtomicBoolean boolean reference} to the given one. This can be used to sync
     * different {@link SingleExecutionThreadSupplier controllers} by using same lock attributes.
     *
     * @param isBusy
     */
    public SingleExecutionThreadSupplier(AtomicBoolean isBusy) {
        this.isBusy = isBusy;
        isInterrupting = new AtomicBoolean(false);
    }


    @Override
    public synchronized Thread requestAnExecutionThread(Procedure procedure) {
        Thread thread = null;
        if (isBusy.compareAndSet(false, true)) {
            thread = new Thread(procedure::invoke);
        }

        return thread;
    }

    @Override
    public synchronized Thread requestAnInterruptingThread(Procedure procedure) {
        Thread thread = null;
        if (isBusy.get()) {
            if (isInterrupting.compareAndSet(false, true)) {
                thread = new Thread(() -> {
                    procedure.invoke();
                    isInterrupting.set(false);
                });
            }
        }

        return thread;
    }

    public synchronized Thread tryStartingExecutionThread(Procedure procedure) {
        Thread thread = requestAnExecutionThread(procedure);
        if (thread != null)
            thread.start();
        return thread;
    }

    public synchronized Thread tryStartingInterruptionThread(Procedure procedure) {
        Thread thread = requestAnInterruptingThread(procedure);
        if (thread != null)
            thread.start();
        return thread;
    }

    @Override
    public synchronized boolean isBusy() {
        return isBusy.get();
    }

    @Override
    public synchronized void finishedProcedureExecution() {
        isBusy.set(false);
    }
}
