package microtrafficsim.utils.concurrency.interruptsafe;

import java.util.concurrent.*;


/**
 * A interrupt-safe {@link ThreadPoolExecutor}.
 *
 * @author Maximilian Luz
 */
public class InterruptSafeThreadPoolExecutor extends ThreadPoolExecutor {

    /**
     * Constructs a new {@code InterruptSafeThreadPoolExecutor} with the given properties.
     *
     * @param corePoolSize    the core (targeted) pool-size.
     * @param maximumPoolSize the maximum pool-size.
     * @param keepAliveTime   the (minimum) time to keep idle threads alive.
     * @param unit            the unit of the time-value.
     * @param workQueue       the queue used for holding the tasks given to the created {@code ThreadPoolExecutor}.
     */
    public InterruptSafeThreadPoolExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit,
                                           BlockingQueue<Runnable> workQueue) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue);
    }

    /**
     * Constructs a new {@code InterruptSafeThreadPoolExecutor} with the given properties.
     *
     * @param corePoolSize    the core (targeted) pool-size.
     * @param maximumPoolSize the maximum pool-size.
     * @param keepAliveTime   the (minimum) time to keep idle threads alive.
     * @param unit            the unit of the time-value.
     * @param workQueue       the queue used for holding the tasks given to the created {@code ThreadPoolExecutor}.
     * @param threadFactory   the factory used to create the threads.
     */
    public InterruptSafeThreadPoolExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit,
                                           BlockingQueue<Runnable> workQueue, ThreadFactory threadFactory) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, threadFactory);
    }

    /**
     * Constructs a new {@code InterruptSafeThreadPoolExecutor} with the given properties.
     *
     * @param corePoolSize    the core (targeted) pool-size.
     * @param maximumPoolSize the maximum pool-size.
     * @param keepAliveTime   the (minimum) time to keep idle threads alive.
     * @param unit            the unit of the time-value.
     * @param workQueue       the queue used for holding the tasks given to the created {@code ThreadPoolExecutor}.
     * @param handler         the handler specifying what happens when a task is rejected.
     */
    public InterruptSafeThreadPoolExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit,
                                           BlockingQueue<Runnable> workQueue, RejectedExecutionHandler handler) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, handler);
    }

    /**
     * Constructs a new {@code InterruptSafeThreadPoolExecutor} with the given properties.
     *
     * @param corePoolSize    the core (targeted) pool-size.
     * @param maximumPoolSize the maximum pool-size.
     * @param keepAliveTime   the (minimum) time to keep idle threads alive.
     * @param unit            the unit of the time-value.
     * @param workQueue       the queue used for holding the tasks given to the created {@code ThreadPoolExecutor}.
     * @param threadFactory   the factory used to create the threads.
     * @param handler         the handler specifying what happens when a task is rejected.
     */
    public InterruptSafeThreadPoolExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit,
                                           BlockingQueue<Runnable> workQueue, ThreadFactory threadFactory,
                                           RejectedExecutionHandler handler) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, threadFactory, handler);
    }


    @Override
    protected <V> RunnableFuture<V> newTaskFor(Callable<V> task) {
        return new InterruptSafeFutureTask<>(task);
    }

    @Override
    protected <V> RunnableFuture<V> newTaskFor(Runnable task, V result) {
        return new InterruptSafeFutureTask<>(Executors.callable(task, result));
    }
}
