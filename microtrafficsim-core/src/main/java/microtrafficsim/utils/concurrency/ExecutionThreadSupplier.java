package microtrafficsim.utils.concurrency;

import microtrafficsim.utils.functional.Procedure;

/**
 * Serves methods for requesting an execution thread executing a given procedure under the condition that no other
 * created thread is currently running or in creation process.
 *
 * @author Dominic Parga Cacheiro
 */
public interface ExecutionThreadSupplier {

    /**
     * Creates a thread executing the given procedure. The thread is NOT started yet. If the preferences frame is busy,
     * nothing is done. If the procedure has finished, it should call {@link #finishedProcedureExecution()}.
     *
     * @return The thread able to execute the procedure; null if busy
     */
    Thread requestAnExecutionThread(Procedure procedure);

    /**
     * Creates a thread executing the given procedure. The thread is NOT started yet. If the preferences frame is
     * NOT busy, nothing is done.
     *
     * @return The thread able to execute the procedure; null if busy
     */
    Thread requestAnInterruptingThread(Procedure procedure);

    /**
     * @return whether this supplier has created a thread that has not finished yet
     */
    boolean isBusy();

    /**
     * This method just sets this to {@code not busy}.
     */
    void finishedProcedureExecution();
}
