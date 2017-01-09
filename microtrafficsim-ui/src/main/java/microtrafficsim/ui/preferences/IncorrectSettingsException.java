package microtrafficsim.ui.preferences;


/**
 * Helps throwing the same exception multiple times for similar reasons.
 *
 * @author Dominic Parga Cacheiro
 */
public class IncorrectSettingsException extends Exception {
    private StringBuilder msgBuilder;

    public IncorrectSettingsException() {
        msgBuilder = new StringBuilder();
    }

    public void appendToMessage(String str) {
        msgBuilder.append(str);
    }

    @Override
    public String getMessage() {
        return msgBuilder.toString();
    }
}
