package microtrafficsim.core.vis.context.tasks;

import microtrafficsim.core.vis.context.RenderContext;


/**
 * Render-task to be executed on a {@code RenderContext}.
 *
 * @param <V> the return-type of the task.
 * @author Maximilian Luz
 */
public interface RenderTask<V> {

    /**
     * Executes this task.
     *
     * @param context the context this task is executed on.
     * @return the result computed by this task.
     * @throws Exception any exception thrown during execution.
     */
    V execute(RenderContext context) throws Exception;
}
