package microtrafficsim.core.vis.context.exceptions;

import microtrafficsim.core.vis.context.RenderContext;


public class UncaughtContextException extends RuntimeException {
    private RenderContext context;

    public UncaughtContextException(RenderContext context) {
        super();
        this.context = context;
    }

    public UncaughtContextException(RenderContext context, String message) {
        super(message);
        this.context = context;
    }

    public UncaughtContextException(RenderContext context, String message, Throwable cause) {
        super(message, cause);
        this.context = context;
    }

    public UncaughtContextException(RenderContext context, Throwable cause) {
        super(cause);
        this.context = context;
    }

    public RenderContext getContext() {
        return context;
    }
}
