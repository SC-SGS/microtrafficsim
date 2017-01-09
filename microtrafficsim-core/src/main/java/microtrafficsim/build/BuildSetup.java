package microtrafficsim.build;


/**
 * Debug switches and similar stuff. Only for programmers.
 *
 * @author Maximilian Luz, Dominic Parga Cacheiro
 */
public abstract class BuildSetup {

    /**
     * Enable/disable trace instructions for logging.
     */
    public static boolean TRACE_ENABLED = false;

    /**
     * Enable/disable debug instructions for logging.
     */
    public static boolean DEBUG_ENABLED = false;

    /**
     * Enable/disable info instructions for logging.
     */
    public static boolean INFO_ENABLED = true;

    /**
     * Enable/disable warn instructions for logging.
     */
    public static boolean WARN_ENABLED = true;

    /**
     * Enable/disable error instructions for logging.
     */
    public static boolean ERROR_ENABLED = true;


    /**
     * Enable/disable debug instructions for the visualization.
     */
    public static final boolean DEBUG_CORE_VIS = true;

    /**
     * Enable/disable debug instructions for the visualization.
     */
    public static final boolean DEBUG_CORE_PARSER = true;
}
