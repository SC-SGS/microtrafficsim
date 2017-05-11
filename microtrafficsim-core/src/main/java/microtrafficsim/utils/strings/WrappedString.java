package microtrafficsim.utils.strings;

/**
 * This class is just a (synchronized) wrapper class for Strings.
 *
 * @author Dominic Parga Cacheiro
 */
public class WrappedString {

    private String str;

    public WrappedString() {
        this.str = "";
    }

    public WrappedString(String str) {
        this.str = str;
    }

    public synchronized String get() {
        return str;
    }

    public synchronized void set(String str) {
        this.str = str;
    }
}