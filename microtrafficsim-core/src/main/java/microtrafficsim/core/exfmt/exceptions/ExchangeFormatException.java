package microtrafficsim.core.exfmt.exceptions;


/**
 * Base-class for all ExchangeFormat related exceptions.
 *
 * @author Maximilian Luz
 */
public class ExchangeFormatException extends Exception {
    public ExchangeFormatException() {
        super();
    }

    public ExchangeFormatException(String message) {
        super(message);
    }

    public ExchangeFormatException(Throwable cause) {
        super(cause);
    }

    public ExchangeFormatException(String message, Throwable cause) {
        super(message, cause);
    }
}
