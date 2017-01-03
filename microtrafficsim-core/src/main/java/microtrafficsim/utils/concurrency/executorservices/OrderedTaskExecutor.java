package microtrafficsim.utils.concurrency.executorservices;

import java.util.PriorityQueue;

/**
 * Defines an executor working a queue of tasks off. If this queue is empty, this executor will yield instead of
 * shutdown. The order of this queue is FIFO per default.
 *
 * @author Dominic Parga Cacheiro
 */
public interface OrderedTaskExecutor {

    boolean hasTask();

    void add(Runnable task);
}
