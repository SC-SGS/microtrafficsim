package microtrafficsim.core.vis.context.exceptions;

import microtrafficsim.core.vis.context.RenderContext;


public interface UncaughtExceptionHandler {
    void uncaughtException(RenderContext context, Throwable cause);
}
