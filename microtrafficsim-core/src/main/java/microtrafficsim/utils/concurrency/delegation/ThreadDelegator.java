package microtrafficsim.utils.concurrency.delegation;

import java.util.Iterator;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * This interface represents a task delegator in multithreaded environments. Implementations should distribute many
 * tasks on threads in a performant way.
 *
 * @author Dominic Parga Cacheiro
 */
public interface ThreadDelegator {

    /**
     *
     * @param elementTask This task is executed for each element in the iterator
     * @param iter Iterator over all elements
     * @param elementCount This is the number of elements that should be executed in one go
     * @param <T> Anything that should be part of this task
     */
    <T> void doTask(Consumer<T> elementTask, Iterator<T> iter, int elementCount);
}
