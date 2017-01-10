package microtrafficsim.utils.concurrency.delegation;

import microtrafficsim.utils.concurrency.executorservices.FixedThreadPool;
import microtrafficsim.utils.concurrency.interruptsafe.InterruptSafeExecutors;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

/**
 * <p>
 * This ThreadDelegator is called static, because it creates lists of elements until no element is left, only then it
 * starts working the tasks off concurrently.
 *
 * @author Dominic Parga Cacheiro
 */
public class StaticThreadDelegator implements ThreadDelegator {

    private final ExecutorService pool;

    public StaticThreadDelegator(int nThreads) {
        this.pool = InterruptSafeExecutors.newFixedThreadPool(nThreads);
    }

    /**
     * <p>
     * This method executes the given task on every element in this iterator using a thread pool. At first, all
     * tasks containing a list of a certain number of elements (<=> {@code elementCount} are created.
     */
    public <T> void doTask(Consumer<T> elementTask, Iterator<T> iter, int elementCount) throws InterruptedException {

        LinkedList<Future<Void>> futures = new LinkedList<>();

        while (iter.hasNext() && !Thread.currentThread().isInterrupted()) {
            if (Thread.interrupted())
                throw new InterruptedException();

            // fill current thread's vehicle list
            ArrayList<T> list = new ArrayList<>(elementCount);
            int c = 0;
            while (c++ < elementCount && iter.hasNext()) {
                list.add(iter.next());
            }

            // let a thread work off the list
            futures.add(pool.submit(() -> {
                for (T t : list) {
                    if (Thread.interrupted())
                        throw new CancellationException();

                    elementTask.accept(t);
                }

                return null;
            }));
        }

        try {                                   // try to wait on all futures
            for (Future<Void> future : futures) {
                try {
                    future.get();
                } catch (CancellationException | ExecutionException e) {
                    // NOTE: we do not cancel any tasks before this loop, so there should be no CancellationExceptions
                    throw new RuntimeException(e);
                }
            }
        } catch (InterruptedException e) {      // cancel if interrupted, interrupt worker-threads
            for (Future<Void> future : futures) {
                future.cancel(true);
            }

            for (Future<Void> future : futures) {
                try {
                    future.get();
                } catch (Throwable t) {
                    // ignore any exceptions here, as we intend to cancel this task, regardless of the outcome
                }
            }

            throw e;                            // propagate the interrupt
        }
    }
}
