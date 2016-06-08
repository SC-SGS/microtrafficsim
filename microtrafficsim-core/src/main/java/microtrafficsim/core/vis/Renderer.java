package microtrafficsim.core.vis;

import microtrafficsim.core.vis.context.RenderContext;


public interface Renderer {
    void init(RenderContext context) throws Exception;
    void dispose(RenderContext context) throws Exception;
    void display(RenderContext context) throws Exception;
    void reshape(RenderContext context, int x, int y, int width, int height) throws Exception;
}
