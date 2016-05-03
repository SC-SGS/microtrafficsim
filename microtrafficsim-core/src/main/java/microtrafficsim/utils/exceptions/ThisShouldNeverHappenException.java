package microtrafficsim.utils.exceptions;


public class ThisShouldNeverHappenException extends RuntimeException {

    public ThisShouldNeverHappenException() {
        super();
    }

    public ThisShouldNeverHappenException(String message) {
        super(message);
    }

    public ThisShouldNeverHappenException(String message, Throwable cause) {
        super(message, cause);
    }

    public ThisShouldNeverHappenException(Throwable cause) {
        super(cause);
    }

    protected ThisShouldNeverHappenException(String message, Throwable cause,
                                             boolean enableSuppression,
                                             boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
