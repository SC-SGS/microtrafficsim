package microtrafficsim.utils.progressable;


/**
 * This interface helps getting updates if progress is done.
 *
 * @author Dominic Parga Cacheiro
 */
public interface ProgressListener {
    void didProgress(int currentInPercent);
}