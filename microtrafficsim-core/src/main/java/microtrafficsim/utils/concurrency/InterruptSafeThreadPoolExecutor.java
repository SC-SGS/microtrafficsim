package microtrafficsim.utils.concurrency;

import java.util.concurrent.*;


// TODO: infer documentation from ThreadPoolExecutor

public class InterruptSafeThreadPoolExecutor extends ThreadPoolExecutor {

    public InterruptSafeThreadPoolExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit,
                                           BlockingQueue<Runnable> workQueue) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue);
    }

    public InterruptSafeThreadPoolExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit,
                                           BlockingQueue<Runnable> workQueue, ThreadFactory threadFactory) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, threadFactory);
    }

    public InterruptSafeThreadPoolExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit,
                                           BlockingQueue<Runnable> workQueue, RejectedExecutionHandler handler) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, handler);
    }

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
