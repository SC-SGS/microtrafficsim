package microtrafficsim.utils.concurrency.executorservices.impl;

import microtrafficsim.utils.concurrency.executorservices.OrderedTaskExecutor;

import java.util.*;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Basic implementation of {@code OrderedTaskExecutor} using a single {@link Thread}.
 *
 * @author Dominic Parga Cacheiro
 */
public class SingleThreadedOrderedTaskExecutor implements OrderedTaskExecutor {

    private final Queue<Runnable> tasks;
    private final ReentrantLock lock;

    public SingleThreadedOrderedTaskExecutor() {
        tasks = new LinkedList<>();
        lock  = new ReentrantLock();

        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                if (lock.tryLock()) {
                    while (hasTask())
                        nextTask().run();
                    lock.unlock();
                }
            }
        }, 0, 100);
    }

    private synchronized Runnable nextTask() {
        return tasks.poll();
    }

    /*
    |=========================|
    | (i) OrderedTaskExecutor |
    |=========================|
    */
    @Override
    public synchronized boolean hasTask() {
        return !tasks.isEmpty();
    }

    @Override
    public synchronized void add(Runnable task) {
        tasks.add(task);
    }
}
