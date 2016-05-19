package microtrafficsim.utils.valuewrapper;

/**
 * @author Dominic Parga Cacheiro
 */
public class LazyFinalException extends Exception {

    public LazyFinalException() {
        this("LazyFinalValue is already set, but should be set again.");
    }

    public LazyFinalException(String str) {
        super(str);
    }
}
