package microtrafficsim.utils.resources;

import java.io.IOException;


/**
 * Resource exception, indicating a failure to access or interact with a resource.
 *
 * @author Maximilian Luz
 */
public class ResourceException extends IOException {
    private static final long serialVersionUID = 2537380971878927752L;


    /**
     * Creates a new {@code ResourceException} without a detail-message.
     * @see IOException#IOException()
     */
    public ResourceException() {
        super();
    }

    /**
     * Creates a new {@code ResourceException} with the specified message.
     * @param message the error detail-message.
     * @see IOException#IOException(String)
     */
    public ResourceException(String message) {
        super(message);
    }

    /**
     * Creates a new {@code ResourceException} with the specified message and cause.
     * @param message the error detail-message.
     * @param cause   the cause of the error.
     * @see IOException#IOException(String, Throwable)
     */
    public ResourceException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Creates a new {@code ResourceException} with the specified cause. Inherits the detail-message of the cause.
     * @param cause   the cause of the error.
     * @see IOException#IOException(Throwable)
     */
    public ResourceException(Throwable cause) {
        super(cause);
    }
}
