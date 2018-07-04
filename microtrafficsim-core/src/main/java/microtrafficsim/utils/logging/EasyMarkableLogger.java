package microtrafficsim.utils.logging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;

/**
 * <p>
 * This class uses a {@link Logger logger} of the {@link LoggerFactory} for its implementation, but serves attributes
 * for deciding whether the method should log or not, both locally and statically. <br>
 * {@code level is enabled means level is statically enabled and locally enabled}
 *
 * <p>
 * Possible attributes: trace, debug, info, warn, error
 *
 * <p>
 * Per default, the logger level is set to trace, all local attributes are true and all static attributes above debug
 * (info, warn and error) are true. This means, per default, this logger logs info, warn and error messages.
 *
 * @author Dominic Parga Cacheiro
 */
public class EasyMarkableLogger implements Logger {
    public Logger logger;
    public final LoggingLevel TRACE = LoggingLevel.createLevel(LoggingLevel.Type.TRACE);
    public final LoggingLevel DEBUG = LoggingLevel.createLevel(LoggingLevel.Type.DEBUG);
    public final LoggingLevel INFO = LoggingLevel.createLevel(LoggingLevel.Type.INFO);
    public final LoggingLevel WARN = LoggingLevel.createLevel(LoggingLevel.Type.WARN);
    public final LoggingLevel ERROR = LoggingLevel.createLevel(LoggingLevel.Type.ERROR);


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

    public static void setEnabledGlobally(boolean trace, boolean debug, boolean info, boolean warn, boolean error) {
        LoggingLevel.setEnabledGlobally(trace, debug, info, warn, error);
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
        return TRACE.isEnabled() && logger.isTraceEnabled();
    }

    @Override
    public void trace(String msg) {
        if (isTraceEnabled())
            logger.trace(msg);
    }

    @Override
    public void trace(String format, Object arg) {
        if (isTraceEnabled())
            logger.trace(format, arg);
    }

    @Override
    public void trace(String format, Object arg1, Object arg2) {
        if (isTraceEnabled())
            logger.trace(format, arg1, arg2);
    }

    @Override
    public void trace(String format, Object... arguments) {
        if (isTraceEnabled())
            logger.trace(format, arguments);
    }

    @Override
    public void trace(String msg, Throwable t) {
        if (isTraceEnabled())
            logger.trace(msg, t);
    }

    @Override
    public boolean isTraceEnabled(Marker marker) {
        return TRACE.isEnabled() && logger.isTraceEnabled(marker);
    }

    @Override
    public void trace(Marker marker, String msg) {
        if (isTraceEnabled())
            logger.trace(marker, msg);
    }

    @Override
    public void trace(Marker marker, String format, Object arg) {
        if (isTraceEnabled())
            logger.trace(marker, format, arg);
    }

    @Override
    public void trace(Marker marker, String format, Object arg1, Object arg2) {
        if (isTraceEnabled())
            logger.trace(marker, format, arg1, arg2);
    }

    @Override
    public void trace(Marker marker, String format, Object... argArray) {
        if (isTraceEnabled())
            logger.trace(marker, format, argArray);
    }

    @Override
    public void trace(Marker marker, String msg, Throwable t) {
        if (isTraceEnabled())
            logger.trace(marker, msg, t);
    }

    @Override
    public boolean isDebugEnabled() {
        return DEBUG.isEnabled() && logger.isDebugEnabled();
    }

    @Override
    public void debug(String msg) {
        if (isDebugEnabled())
            logger.debug(msg);
    }

    @Override
    public void debug(String format, Object arg) {
        if (isDebugEnabled())
            logger.debug(format, arg);
    }

    @Override
    public void debug(String format, Object arg1, Object arg2) {
        if (isDebugEnabled())
            logger.debug(format, arg1, arg2);
    }

    @Override
    public void debug(String format, Object... arguments) {
        if (isDebugEnabled())
            logger.debug(format, arguments);
    }

    @Override
    public void debug(String msg, Throwable t) {
        if (isDebugEnabled())
            logger.debug(msg, t);
    }

    @Override
    public boolean isDebugEnabled(Marker marker) {
        return DEBUG.isEnabled() && logger.isDebugEnabled(marker);
    }

    @Override
    public void debug(Marker marker, String msg) {
        if (isDebugEnabled())
            logger.debug(marker, msg);
    }

    @Override
    public void debug(Marker marker, String format, Object arg) {
        if (isDebugEnabled())
            logger.debug(marker, format, arg);
    }

    @Override
    public void debug(Marker marker, String format, Object arg1, Object arg2) {
        if (isDebugEnabled())
            logger.debug(marker, format, arg1, arg2);
    }

    @Override
    public void debug(Marker marker, String format, Object... arguments) {
        if (isDebugEnabled())
            logger.debug(marker, format, arguments);
    }

    @Override
    public void debug(Marker marker, String msg, Throwable t) {
        if (isDebugEnabled())
            logger.debug(marker, msg, t);
    }

    @Override
    public boolean isInfoEnabled() {
        return INFO.isEnabled() && logger.isInfoEnabled();
    }

    @Override
    public void info(String msg) {
        if (isInfoEnabled())
            logger.info(msg);
    }

    @Override
    public void info(String format, Object arg) {
        if (isInfoEnabled())
            logger.info(format, arg);
    }

    @Override
    public void info(String format, Object arg1, Object arg2) {
        if (isInfoEnabled())
            logger.info(format, arg1, arg2);
    }

    @Override
    public void info(String format, Object... arguments) {
        if (isInfoEnabled())
            logger.info(format, arguments);
    }

    @Override
    public void info(String msg, Throwable t) {
        if (isInfoEnabled())
            logger.info(msg, t);
    }

    @Override
    public boolean isInfoEnabled(Marker marker) {
        return INFO.isEnabled() && logger.isInfoEnabled(marker);
    }

    @Override
    public void info(Marker marker, String msg) {
        if (isInfoEnabled())
            logger.info(marker, msg);
    }

    @Override
    public void info(Marker marker, String format, Object arg) {
        if (isInfoEnabled())
            logger.info(marker, format, arg);
    }

    @Override
    public void info(Marker marker, String format, Object arg1, Object arg2) {
        if (isInfoEnabled())
            logger.info(marker, format, arg1, arg2);
    }

    @Override
    public void info(Marker marker, String format, Object... arguments) {
        if (isInfoEnabled())
            logger.info(marker, format, arguments);
    }

    @Override
    public void info(Marker marker, String msg, Throwable t) {
        if (isInfoEnabled())
            logger.info(marker, msg, t);
    }

    @Override
    public boolean isWarnEnabled() {
        return WARN.isEnabled() && logger.isWarnEnabled();
    }

    @Override
    public void warn(String msg) {
        if (isWarnEnabled())
            logger.warn(msg);
    }

    @Override
    public void warn(String format, Object arg) {
        if (isWarnEnabled())
            logger.warn(format, arg);
    }

    @Override
    public void warn(String format, Object... arguments) {
        if (isWarnEnabled())
            logger.warn(format, arguments);
    }

    @Override
    public void warn(String format, Object arg1, Object arg2) {
        if (isWarnEnabled())
            logger.warn(format, arg1, arg2);
    }

    @Override
    public void warn(String msg, Throwable t) {
        if (isWarnEnabled())
            logger.warn(msg, t);
    }

    @Override
    public boolean isWarnEnabled(Marker marker) {
        return WARN.isEnabled() && logger.isWarnEnabled(marker);
    }

    @Override
    public void warn(Marker marker, String msg) {
        if (isWarnEnabled())
            logger.warn(marker, msg);
    }

    @Override
    public void warn(Marker marker, String format, Object arg) {
        if (isWarnEnabled())
            logger.warn(marker, format, arg);
    }

    @Override
    public void warn(Marker marker, String format, Object arg1, Object arg2) {
        if (isWarnEnabled())
            logger.warn(marker, format, arg1, arg2);
    }

    @Override
    public void warn(Marker marker, String format, Object... arguments) {
        if (isWarnEnabled())
            logger.warn(marker, format, arguments);
    }

    @Override
    public void warn(Marker marker, String msg, Throwable t) {
        if (isWarnEnabled())
            logger.warn(marker, msg, t);
    }

    @Override
    public boolean isErrorEnabled() {
        return ERROR.isEnabled() && logger.isErrorEnabled();
    }

    @Override
    public void error(String msg) {
        if (isErrorEnabled())
            logger.error(msg);
    }

    @Override
    public void error(String format, Object arg) {
        if (isErrorEnabled())
            logger.error(format, arg);
    }

    @Override
    public void error(String format, Object arg1, Object arg2) {
        if (isErrorEnabled())
            logger.error(format, arg1, arg2);
    }

    @Override
    public void error(String format, Object... arguments) {
        if (isErrorEnabled())
            logger.error(format, arguments);
    }

    @Override
    public void error(String msg, Throwable t) {
        if (isErrorEnabled())
            logger.error(msg, t);
    }

    @Override
    public boolean isErrorEnabled(Marker marker) {
        return ERROR.isEnabled() && logger.isErrorEnabled(marker);
    }

    @Override
    public void error(Marker marker, String msg) {
        if (isErrorEnabled())
            logger.error(marker, msg);
    }

    @Override
    public void error(Marker marker, String format, Object arg) {
        if (isErrorEnabled())
            logger.error(marker, format, arg);
    }

    @Override
    public void error(Marker marker, String format, Object arg1, Object arg2) {
        if (isErrorEnabled())
            logger.error(marker, format, arg1, arg2);
    }

    @Override
    public void error(Marker marker, String format, Object... arguments) {
        if (isErrorEnabled())
            logger.error(marker, format, arguments);
    }

    @Override
    public void error(Marker marker, String msg, Throwable t) {
        if (isErrorEnabled())
            logger.error(marker, msg, t);
    }
}
