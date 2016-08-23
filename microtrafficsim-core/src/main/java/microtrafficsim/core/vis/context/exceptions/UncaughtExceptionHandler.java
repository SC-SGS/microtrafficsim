package microtrafficsim.core.vis.context.exceptions;

import microtrafficsim.core.vis.context.RenderContext;


/**
 * Exception-handler for uncaught exceptions on a {@code RenderContext}.
 *
 * @author Maximilian Luz
 */
public interface UncaughtExceptionHandler {

    /**
     * Handles an uncaught exception.
     *
     * @param context the context the exception was thrown on.
     * @param cause   the uncaught exception.
     */
    void uncaughtException(RenderContext context, Throwable cause);
}
