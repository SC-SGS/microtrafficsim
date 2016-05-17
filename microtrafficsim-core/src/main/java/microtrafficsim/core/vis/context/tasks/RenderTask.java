package microtrafficsim.core.vis.context.tasks;

import microtrafficsim.core.vis.context.RenderContext;


public interface RenderTask<V> {
    V execute(RenderContext context) throws Exception;
}
