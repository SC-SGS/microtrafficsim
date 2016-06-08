package microtrafficsim.utils.concurrency;

import java.util.concurrent.*;

// TODO: infer documentation from ThreadPoolExecutor


public class InteruptSafeThreadPoolExecutor extends ThreadPoolExecutor {

    public InteruptSafeThreadPoolExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit,
                                          BlockingQueue<Runnable> workQueue) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue);
    }

    public InteruptSafeThreadPoolExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit,
                                          BlockingQueue<Runnable> workQueue, ThreadFactory threadFactory) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, threadFactory);
    }

    public InteruptSafeThreadPoolExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit,
                                          BlockingQueue<Runnable> workQueue, RejectedExecutionHandler handler) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, handler);
    }

    public InteruptSafeThreadPoolExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit,
                                          BlockingQueue<Runnable> workQueue, ThreadFactory threadFactory, RejectedExecutionHandler handler) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, threadFactory, handler);
    }


    @Override
    protected <V> RunnableFuture<V> newTaskFor(Callable<V> task) {
        return new InterruptSafeFutureTask<V>(task);
    }

    @Override
    protected <V> RunnableFuture<V> newTaskFor(Runnable task, V result) {
        return new InterruptSafeFutureTask<V>(Executors.callable(task, result));
    }
}
