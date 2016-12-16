package microtrafficsim.build;


import microtrafficsim.utils.logging.EasyMarkableLogger;

/**
 * Debug switches. This class has to be
 *
 * @author Maximilian Luz, Dominic Parga Cacheiro
 */
public abstract class BuildSetup {

    /**
     * <p>
     * Initializes attributes of other classes: <br>
     * &bull {@link EasyMarkableLogger}
     */
    public static void init() {
        EasyMarkableLogger.TRACE_ENABLED = false;
        EasyMarkableLogger.DEBUG_ENABLED = false;
        EasyMarkableLogger.INFO_ENABLED = true;
        EasyMarkableLogger.WARN_ENABLED = true;
        EasyMarkableLogger.ERROR_ENABLED = true;
    }

    /**
     * Enable/disable debug instructions for the visualization.
     */
    public static final boolean DEBUG_CORE_VIS = true;

    /**
     * Enable/disable debug instructions for the visualization.
     */
    public static final boolean DEBUG_CORE_PARSER = true;
}
