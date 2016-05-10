package microtrafficsim.core.vis;

import microtrafficsim.core.vis.context.RenderContext;


public interface Renderer {
    void init(RenderContext context);
    void dispose(RenderContext context);
    void display(RenderContext context);
    void reshape(RenderContext context, int x, int y, int width, int height);
}
