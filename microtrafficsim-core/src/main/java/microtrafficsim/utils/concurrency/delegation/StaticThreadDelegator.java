package microtrafficsim.utils.concurrency.delegation;

import microtrafficsim.utils.concurrency.executorservices.FixedThreadPool;

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

    public StaticThreadDelegator(ExecutorService pool) {
        this.pool = pool;
    }

    public StaticThreadDelegator(int nThreads) {
        this(new FixedThreadPool(nThreads));
    }

    /**
     * <p>
     * This method executes the given task on every element in this iterator using a thread pool. At first, all
     * tasks containing a list of a certain number of elements (<=> {@code elementCount} are created.
     */
    public <T> void doTask(Consumer<T> elementTask, Iterator<T> iter, int elementCount) throws InterruptedException {

        LinkedList<Callable<Object>> tasks = new LinkedList<>();
        final Thread currentThread = Thread.currentThread();
        final AtomicBoolean isInterrupted = new AtomicBoolean(false);

        while (iter.hasNext()) {
            if (Thread.interrupted())
                throw new InterruptedException();

            // fill current thread's vehicle list
            ArrayList<T> list = new ArrayList<>(elementCount);
            int c = 0;
            while (c++ < elementCount && iter.hasNext()) {
                if (Thread.interrupted())
                    throw new InterruptedException();

                list.add(iter.next());
            }
            // let a thread work off the list
            tasks.add(Executors.callable(() -> {
                for (T t : list) {
                    isInterrupted.compareAndSet(false, currentThread.isInterrupted());
                    if (isInterrupted.get())
                        break;
                    elementTask.accept(t);
                }
            }));
        }

        // waiting for finishing the threads
        pool.invokeAll(tasks);

        if (isInterrupted.get())
            throw new InterruptedException();
    }
}
