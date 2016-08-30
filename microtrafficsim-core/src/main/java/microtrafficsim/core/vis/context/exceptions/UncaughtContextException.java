package microtrafficsim.core.vis.context.exceptions;

import microtrafficsim.core.vis.context.RenderContext;


/**
 * Wrapper-exception for an uncaught exception that occurred on a {@code RenderContext}.
 *
 * @author Maximilian Luz
 */
public class UncaughtContextException extends RuntimeException {
    private RenderContext context;


    /**
     * Constructs a new {@code UncaughtContextException}.
     *
     * @param context the context the exception occured.
     */
    public UncaughtContextException(RenderContext context) {
        super();
        this.context = context;
    }

    /**
     * Constructs a new {@code UncaughtContextException}.
     *
     * @param context the context the exception occured.
     * @param message the message of the exception.
     */
    public UncaughtContextException(RenderContext context, String message) {
        super(message);
        this.context = context;
    }

    /**
     * Constructs a new {@code UncaughtContextException}.
     *
     * @param context the context the exception occured.
     * @param message the message of the exception.
     * @param cause   the cause of this exception.
     */
    public UncaughtContextException(RenderContext context, String message, Throwable cause) {
        super(message, cause);
        this.context = context;
    }

    /**
     * Constructs a new {@code UncaughtContextException}.
     *
     * @param context the context the exception occured.
     * @param cause   the cause of this exception.
     */
    public UncaughtContextException(RenderContext context, Throwable cause) {
        super(cause);
        this.context = context;
    }

    /**
     * Returns the context on which the exception occurred.
     *
     * @return the context on which the exception occurred.
     */
    public RenderContext getContext() {
        return context;
    }
}
