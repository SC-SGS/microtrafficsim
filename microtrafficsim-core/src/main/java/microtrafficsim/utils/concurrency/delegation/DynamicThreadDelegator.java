package microtrafficsim.utils.concurrency.delegation;

import microtrafficsim.utils.concurrency.executorservices.FixedThreadPool;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

/**
 * <p>
 * This ThreadDelegator is called dynamic, because the iterator is worked off dynamically: every task says, take a
 * certain number of elements from the iterator and work them off. After this, take the next elements and do the same.
 *
 * <p>
 * The other way is implemented in {@link StaticThreadDelegator}. This ThreadDelegator is called static, because it
 * creates lists of elements until no element is left, only then it starts working the tasks off.
 *
 * @author Dominic Parga Cacheiro
 */
public class DynamicThreadDelegator implements ThreadDelegator {

    private final FixedThreadPool pool;

    public DynamicThreadDelegator(FixedThreadPool pool) {
        this.pool = pool;
    }

    public DynamicThreadDelegator(int nThreads) {
        this(new FixedThreadPool(nThreads));
    }

    /*
    |=====================|
    | (i) ThreadDelegator |
    |=====================|
    */
    /**
     * <p>
     * This method executes the given task on every element in this iterator using a thread pool. Every thread
     * takes a certain number of elements (<=> {@code packageCount} and executes the task on them.
     */
    @Override
    public <T> void doTask(Consumer<T> elementTask, Iterator<T> iter, int elementCount) {

        ArrayList<Callable<Object>> tasks = new ArrayList<>(pool.nThreads);

        // add this task for every thread
        for (int c = 0; c < pool.nThreads; c++)
            tasks.add(Executors.callable(() -> {
                ArrayList<T> list = new ArrayList<>(elementCount);
                do {
                    for (int i = 0; i < elementCount; i++)
                        synchronized (iter) {
                            if (!iter.hasNext())
                                break;
                            list.add(iter.next());
                        }
                    list.forEach(elementTask);
                } while (iter.hasNext());
            }));

        // waiting for finishing the threads
        try {
            pool.invokeAll(tasks);
        } catch (InterruptedException e) { e.printStackTrace(); }
    }
}
