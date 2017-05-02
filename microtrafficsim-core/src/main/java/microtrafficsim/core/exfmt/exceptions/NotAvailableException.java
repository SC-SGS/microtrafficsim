package microtrafficsim.core.exfmt.exceptions;

/**
 * Indicates that a component required for the interaction that caused this exception is not available.
 *
 * @author Maximilian Luz
 */
public class NotAvailableException extends ExchangeFormatException {
    public NotAvailableException() {
        super();
    }

    public NotAvailableException(String message) {
        super(message);
    }

    public NotAvailableException(Throwable cause) {
        super(cause);
    }

    public NotAvailableException(String message, Throwable cause) {
        super(message, cause);
    }
}
