package microtrafficsim.utils.concurrency.executorservices.impl;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.*;

/**
 * This class uses {@link Executors#newFixedThreadPool(int)} for its implementation, but remembers the number of
 * threads, which this method is called with.
 *
 * @author Dominic Parga Cacheiro
 */
public class FixedThreadPool implements ExecutorService {

    public final int nThreads;
    private final ExecutorService pool;

    public FixedThreadPool(int nThreads) {
        this.nThreads = nThreads;
        pool = Executors.newFixedThreadPool(nThreads);
    }

    /*
    |=====================|
    | (i) ExecutorService |
    |=====================|
    */
    @Override
    public void shutdown() {
        pool.shutdown();
    }

    @Override
    public List<Runnable> shutdownNow() {
        return pool.shutdownNow();
    }

    @Override
    public boolean isShutdown() {
        return pool.isShutdown();
    }

    @Override
    public boolean isTerminated() {
        return pool.isTerminated();
    }

    @Override
    public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
        return pool.awaitTermination(timeout, unit);
    }

    @Override
    public <T> Future<T> submit(Callable<T> task) {
        return pool.submit(task);
    }

    @Override
    public <T> Future<T> submit(Runnable task, T result) {
        return pool.submit(task, result);
    }

    @Override
    public Future<?> submit(Runnable task) {
        return pool.submit(task);
    }

    @Override
    public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks) throws InterruptedException {
        return pool.invokeAll(tasks);
    }

    @Override
    public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit) throws InterruptedException {
        return pool.invokeAll(tasks, timeout, unit);
    }

    @Override
    public <T> T invokeAny(Collection<? extends Callable<T>> tasks) throws InterruptedException, ExecutionException {
        return pool.invokeAny(tasks);
    }

    @Override
    public <T> T invokeAny(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        return pool.invokeAny(tasks, timeout, unit);
    }

    @Override
    public void execute(Runnable command) {
        pool.execute(command);
    }
}
