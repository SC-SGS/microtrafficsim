package microtrafficsim.core.logic;

/**
 * Just for unified exception messages.
 *
 * @author Dominic Parga Cacheiro
 */
public class CrossingLogicException extends Exception {

    /**
     * Calls {@code super(message)} with<br>
     * {@code message = "Crossing logic returns 0 where it should not be 0."}
     */
    public CrossingLogicException() {
        super("Crossing logic returns 0 where it should not be 0.");
    }
}
