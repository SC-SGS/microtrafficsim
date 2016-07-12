package microtrafficsim.utils.concurrency;

import java.util.concurrent.*;


// TODO: infer documentation from Executors

public class InterruptSafeExecutors {
    private InterruptSafeExecutors() {}

    public static ExecutorService newFixedThreadPool(int nThreads) {
        return new InterruptSafeThreadPoolExecutor(nThreads, nThreads, 0L, TimeUnit.MILLISECONDS,
                                                   new LinkedBlockingQueue<>());
    }

    public static ExecutorService newFixedThreadPool(int nThreads, ThreadFactory threadFactory) {
        return new InterruptSafeThreadPoolExecutor(nThreads, nThreads, 0L, TimeUnit.MILLISECONDS,
                                                   new LinkedBlockingQueue<>(), threadFactory);
    }

    public static ExecutorService newCachedThreadPool() {
        return new InterruptSafeThreadPoolExecutor(0, Integer.MAX_VALUE, 60L, TimeUnit.SECONDS,
                                                   new SynchronousQueue<>());
    }

    public static ExecutorService newCachedThreadPool(ThreadFactory threadFactory) {
        return new InterruptSafeThreadPoolExecutor(0, Integer.MAX_VALUE, 60L, TimeUnit.SECONDS,
                                                   new SynchronousQueue<>(), threadFactory);
    }
}
