package microtrafficsim.core.vis.glui.renderer;

import microtrafficsim.core.vis.context.RenderContext;
import microtrafficsim.core.vis.opengl.shader.ShaderCompileException;
import microtrafficsim.core.vis.opengl.shader.ShaderLinkException;

import java.io.IOException;


public interface Batch {
    void initialize(RenderContext context) throws Exception;
    void dispose(RenderContext context);
    void display(RenderContext context) throws Exception;
}
