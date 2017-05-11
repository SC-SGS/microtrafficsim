package microtrafficsim.core.simulation.builder;

/**
 * This exception is thrown if a route should be assigned to a vehicle, but the route is not defined in the given
 * scenario.
 *
 * @author Dominic Parga Cacheiro
 */
public class RouteIsNotDefinedException extends Exception {
    public RouteIsNotDefinedException() {
        super();
    }

    public RouteIsNotDefinedException(String message) {
        super(message);
    }

    public RouteIsNotDefinedException(String message, Throwable cause) {
        super(message, cause);
    }

    public RouteIsNotDefinedException(Throwable cause) {
        super(cause);
    }

    protected RouteIsNotDefinedException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
