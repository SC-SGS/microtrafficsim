package microtrafficsim.utils.logging;

import microtrafficsim.build.BuildSetup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;

/**
 * <p>
 * This class uses a logger of the {@link LoggerFactory} for its implementation, but serves public static
 * attributes for deciding whether the method should log or not. These attributes are considered in calculating
 * whether logging for a certain marker is allowed or not.
 *
 * <p>
 * Per default, all attributes are true.
 *
 * @author Dominic Parga Cacheiro
 */
public class EasyMarkableLogger implements Logger{

    private Logger logger;
    public static boolean
            TRACE_ENABLED = true;
    public static boolean DEBUG_ENABLED = true;
    public static boolean INFO_ENABLED = true;
    public static boolean WARN_ENABLED = true;
    public static boolean ERROR_ENABLED = true;

    /**
     * Default constructor
     *
     * @param name is used in {@link LoggerFactory#getLogger(String)}
     */
    public EasyMarkableLogger(String name) {
        logger = LoggerFactory.getLogger(name);
    }

    /**
     * Default constructor
     *
     * @param clazz is used in {@link LoggerFactory#getLogger(Class)}
     */
    public EasyMarkableLogger(Class<?> clazz) {
        logger = LoggerFactory.getLogger(clazz);
    }

    /*
    |============|
    | (i) Logger |
    |============|
    */
    @Override
    public String getName() {
        return logger.getName();
    }

    @Override
    public boolean isTraceEnabled() {
        return TRACE_ENABLED && logger.isTraceEnabled();
    }

    @Override
    public void trace(String msg) {
        if (TRACE_ENABLED)
            logger.trace(msg);
    }

    @Override
    public void trace(String format, Object arg) {
        if (TRACE_ENABLED)
            logger.trace(format, arg);
    }

    @Override
    public void trace(String format, Object arg1, Object arg2) {
        if (TRACE_ENABLED)
            logger.trace(format, arg1, arg2);
    }

    @Override
    public void trace(String format, Object... arguments) {
        if (TRACE_ENABLED)
            logger.trace(format, arguments);
    }

    @Override
    public void trace(String msg, Throwable t) {
        if (TRACE_ENABLED)
            logger.trace(msg, t);
    }

    @Override
    public boolean isTraceEnabled(Marker marker) {
        return TRACE_ENABLED && logger.isTraceEnabled(marker);
    }

    @Override
    public void trace(Marker marker, String msg) {
        if (TRACE_ENABLED)
            logger.trace(marker, msg);
    }

    @Override
    public void trace(Marker marker, String format, Object arg) {
        if (TRACE_ENABLED)
            logger.trace(marker, format, arg);
    }

    @Override
    public void trace(Marker marker, String format, Object arg1, Object arg2) {
        if (TRACE_ENABLED)
            logger.trace(marker, format, arg1, arg2);
    }

    @Override
    public void trace(Marker marker, String format, Object... argArray) {
        if (TRACE_ENABLED)
            logger.trace(marker, format, argArray);
    }

    @Override
    public void trace(Marker marker, String msg, Throwable t) {
        if (TRACE_ENABLED)
            logger.trace(marker, msg, t);
    }

    @Override
    public boolean isDebugEnabled() {
        return DEBUG_ENABLED && logger.isDebugEnabled();
    }

    @Override
    public void debug(String msg) {
        if (DEBUG_ENABLED)
            logger.debug(msg);
    }

    @Override
    public void debug(String format, Object arg) {
        if (DEBUG_ENABLED)
            logger.debug(format, arg);
    }

    @Override
    public void debug(String format, Object arg1, Object arg2) {
        if (DEBUG_ENABLED)
            logger.debug(format, arg1, arg2);
    }

    @Override
    public void debug(String format, Object... arguments) {
        if (DEBUG_ENABLED)
            logger.debug(format, arguments);
    }

    @Override
    public void debug(String msg, Throwable t) {
        if (DEBUG_ENABLED)
            logger.debug(msg, t);
    }

    @Override
    public boolean isDebugEnabled(Marker marker) {
        return DEBUG_ENABLED && logger.isDebugEnabled(marker);
    }

    @Override
    public void debug(Marker marker, String msg) {
        if (DEBUG_ENABLED)
            logger.debug(marker, msg);
    }

    @Override
    public void debug(Marker marker, String format, Object arg) {
        if (DEBUG_ENABLED)
            logger.debug(marker, format, arg);
    }

    @Override
    public void debug(Marker marker, String format, Object arg1, Object arg2) {
        if (DEBUG_ENABLED)
            logger.debug(marker, format, arg1, arg2);
    }

    @Override
    public void debug(Marker marker, String format, Object... arguments) {
        if (DEBUG_ENABLED)
            logger.debug(marker, format, arguments);
    }

    @Override
    public void debug(Marker marker, String msg, Throwable t) {
        if (DEBUG_ENABLED)
            logger.debug(marker, msg, t);
    }

    @Override
    public boolean isInfoEnabled() {
        return INFO_ENABLED && logger.isInfoEnabled();
    }

    @Override
    public void info(String msg) {
        if (INFO_ENABLED)
            logger.info(msg);
    }

    @Override
    public void info(String format, Object arg) {
        if (INFO_ENABLED)
            logger.info(format, arg);
    }

    @Override
    public void info(String format, Object arg1, Object arg2) {
        if (INFO_ENABLED)
            logger.info(format, arg1, arg2);
    }

    @Override
    public void info(String format, Object... arguments) {
        if (INFO_ENABLED)
            logger.info(format, arguments);
    }

    @Override
    public void info(String msg, Throwable t) {
        if (INFO_ENABLED)
            logger.info(msg, t);
    }

    @Override
    public boolean isInfoEnabled(Marker marker) {
        return INFO_ENABLED && logger.isInfoEnabled(marker);
    }

    @Override
    public void info(Marker marker, String msg) {
        if (INFO_ENABLED)
            logger.info(marker, msg);
    }

    @Override
    public void info(Marker marker, String format, Object arg) {
        if (INFO_ENABLED)
            logger.info(marker, format, arg);
    }

    @Override
    public void info(Marker marker, String format, Object arg1, Object arg2) {
        if (INFO_ENABLED)
            logger.info(marker, format, arg1, arg2);
    }

    @Override
    public void info(Marker marker, String format, Object... arguments) {
        if (INFO_ENABLED)
            logger.info(marker, format, arguments);
    }

    @Override
    public void info(Marker marker, String msg, Throwable t) {
        if (INFO_ENABLED)
            logger.info(marker, msg, t);
    }

    @Override
    public boolean isWarnEnabled() {
        return WARN_ENABLED && logger.isWarnEnabled();
    }

    @Override
    public void warn(String msg) {
        if (WARN_ENABLED)
            logger.warn(msg);
    }

    @Override
    public void warn(String format, Object arg) {
        if (WARN_ENABLED)
            logger.warn(format, arg);
    }

    @Override
    public void warn(String format, Object... arguments) {
        if (WARN_ENABLED)
            logger.warn(format, arguments);
    }

    @Override
    public void warn(String format, Object arg1, Object arg2) {
        if (WARN_ENABLED)
            logger.warn(format, arg1, arg2);
    }

    @Override
    public void warn(String msg, Throwable t) {
        if (WARN_ENABLED)
            logger.warn(msg, t);
    }

    @Override
    public boolean isWarnEnabled(Marker marker) {
        return WARN_ENABLED && logger.isWarnEnabled(marker);
    }

    @Override
    public void warn(Marker marker, String msg) {
        if (WARN_ENABLED)
            logger.warn(marker, msg);
    }

    @Override
    public void warn(Marker marker, String format, Object arg) {
        if (WARN_ENABLED)
            logger.warn(marker, format, arg);
    }

    @Override
    public void warn(Marker marker, String format, Object arg1, Object arg2) {
        if (WARN_ENABLED)
            logger.warn(marker, format, arg1, arg2);
    }

    @Override
    public void warn(Marker marker, String format, Object... arguments) {
        if (WARN_ENABLED)
            logger.warn(marker, format, arguments);
    }

    @Override
    public void warn(Marker marker, String msg, Throwable t) {
        if (WARN_ENABLED)
            logger.warn(marker, msg, t);
    }

    @Override
    public boolean isErrorEnabled() {
        return ERROR_ENABLED && logger.isErrorEnabled();
    }

    @Override
    public void error(String msg) {
        if (ERROR_ENABLED)
            logger.error(msg);
    }

    @Override
    public void error(String format, Object arg) {
        if (ERROR_ENABLED)
            logger.error(format, arg);
    }

    @Override
    public void error(String format, Object arg1, Object arg2) {
        if (ERROR_ENABLED)
            logger.error(format, arg1, arg2);
    }

    @Override
    public void error(String format, Object... arguments) {
        if (ERROR_ENABLED)
            logger.error(format, arguments);
    }

    @Override
    public void error(String msg, Throwable t) {
        if (ERROR_ENABLED)
            logger.error(msg, t);
    }

    @Override
    public boolean isErrorEnabled(Marker marker) {
        return ERROR_ENABLED && logger.isErrorEnabled(marker);
    }

    @Override
    public void error(Marker marker, String msg) {
        if (ERROR_ENABLED)
            logger.error(marker, msg);
    }

    @Override
    public void error(Marker marker, String format, Object arg) {
        if (ERROR_ENABLED)
            logger.error(marker, format, arg);
    }

    @Override
    public void error(Marker marker, String format, Object arg1, Object arg2) {
        if (ERROR_ENABLED)
            logger.error(marker, format, arg1, arg2);
    }

    @Override
    public void error(Marker marker, String format, Object... arguments) {
        if (ERROR_ENABLED)
            logger.error(marker, format, arguments);
    }

    @Override
    public void error(Marker marker, String msg, Throwable t) {
        if (ERROR_ENABLED)
            logger.error(marker, msg, t);
    }
}
