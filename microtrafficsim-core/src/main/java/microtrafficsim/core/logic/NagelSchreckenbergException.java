package microtrafficsim.core.logic;

/**
 * Represents an exception thrown while executing simulation steps of the Nagel-Schreckenberg-model.
 *
 * @author Dominic Parga Cacheiro
 */
public class NagelSchreckenbergException extends Exception {

    public enum Step {
        accelerate, brake, dawdle, move, didMove
    }

    /**
     * Creates an exception containing "the velocity is less than zero".
     *
     * @param step In this step the exception is thrown
     * @param velocity current velocity
     * @return instance of {@code NagelSchreckenbergException} containing the right message
     */
    public static NagelSchreckenbergException velocityLessThanZero(Step step, int velocity) {
        switch (step) {
            default:
                return new NagelSchreckenbergException(step, "Velocity < 0; actual=" + velocity);
        }
    }

    public NagelSchreckenbergException(Step step, String message) {
        super("Exception occurred in step " + step + ". " + message);
    }
}
