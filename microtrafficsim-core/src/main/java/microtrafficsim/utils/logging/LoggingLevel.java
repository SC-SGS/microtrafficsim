package microtrafficsim.utils.logging;

import ch.qos.logback.classic.Level;

/**
 * @author Dominic Parga Cacheiro
 */
public class LoggingLevel {
    /*
    |======================================|
    | setup level types for static control |
    |======================================|
    */
    public enum Type {
        TRACE(false), DEBUG(false), INFO(true), WARN(true), ERROR(true);

        public boolean isEnabledGlobally;

        Type(boolean isEnabledGlobally) {
            this.isEnabledGlobally = isEnabledGlobally;
        }
    }

    static {
        setSLF4J(Level.TRACE);
    }

    public static void setSLF4J(ch.qos.logback.classic.Level level) {
        ch.qos.logback.classic.Logger root = (ch.qos.logback.classic.Logger) org.slf4j.LoggerFactory.getLogger(ch.qos.logback.classic.Logger.ROOT_LOGGER_NAME);
        root.setLevel(level);
    }


    /*
    |===================|
    | setup local level |
    |===================|
    */
    private boolean isEnabled;
    private final Type type;

    private LoggingLevel(Type type) {
        isEnabled = true;
        this.type = type;
    }

    public static LoggingLevel createLevel(Type type) {
        return new LoggingLevel(type);
    }


    public boolean isEnabled() {
        return isEnabled && type.isEnabledGlobally;
    }

    public void setEnabled(boolean value) {
        isEnabled = value;
    }

    public static void setEnabledGlobally(boolean trace, boolean debug, boolean info, boolean warn, boolean error) {
        Type.TRACE.isEnabledGlobally = trace;
        Type.DEBUG.isEnabledGlobally = debug;
        Type.INFO.isEnabledGlobally  = info;
        Type.WARN.isEnabledGlobally  = warn;
        Type.ERROR.isEnabledGlobally = error;
    }
}