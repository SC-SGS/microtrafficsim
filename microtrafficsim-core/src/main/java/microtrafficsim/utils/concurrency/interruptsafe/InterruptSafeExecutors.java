package microtrafficsim.utils.concurrency.interruptsafe;

import java.util.concurrent.*;


// TODO: infer documentation from Executors

/**
 * A variety of interrupt-safe thread-pool-executors.
 *
 * @author Maximilian Luz
 */
public class InterruptSafeExecutors {
    private InterruptSafeExecutors() {}

    /**
     * Creates a new, interrupt-safe, fixed-size thread-pool
     *
     * @param nThreads the number of threads the created thread-pool should contain.
     * @return the created thread-pool as {@code ExecutorService}.
     */
    public static ExecutorService newFixedThreadPool(int nThreads) {
        return new InterruptSafeThreadPoolExecutor(nThreads, nThreads, 0L, TimeUnit.MILLISECONDS,
                                                   new LinkedBlockingQueue<>());
    }

    /**
     * Creates a new interrupt-safe, fixed-size thread-pool
     *
     * @param nThreads      the number of threads the created thread-pool should contain.
     * @param threadFactory the factory used to create the threads.
     * @return the created thread-pool as {@code ExecutorService}.
     */
    public static ExecutorService newFixedThreadPool(int nThreads, ThreadFactory threadFactory) {
        return new InterruptSafeThreadPoolExecutor(nThreads, nThreads, 0L, TimeUnit.MILLISECONDS,
                                                   new LinkedBlockingQueue<>(), threadFactory);
    }

    /**
     * Creates a new interrupt-safe, cached thread-pool.
     *
     * @return the created thread-pool as {@code ExecutorService}.
     */
    public static ExecutorService newCachedThreadPool() {
        return new InterruptSafeThreadPoolExecutor(0, Integer.MAX_VALUE, 60L, TimeUnit.SECONDS,
                                                   new SynchronousQueue<>());
    }

    /**
     * Creates a new interrupt-safe, cached thread-pool.
     *
     * @param threadFactory the factory used to create the threads.
     * @return the created thread-pool as {@code ExecutorService}.
     */
    public static ExecutorService newCachedThreadPool(ThreadFactory threadFactory) {
        return new InterruptSafeThreadPoolExecutor(0, Integer.MAX_VALUE, 60L, TimeUnit.SECONDS,
                                                   new SynchronousQueue<>(), threadFactory);
    }
}
