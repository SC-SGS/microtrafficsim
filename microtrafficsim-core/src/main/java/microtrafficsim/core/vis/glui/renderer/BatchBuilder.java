package microtrafficsim.core.vis.glui.renderer;

import microtrafficsim.core.vis.context.RenderContext;
import microtrafficsim.core.vis.glui.Component;
import microtrafficsim.math.Mat3d;


public interface BatchBuilder {
    boolean isApplicableFor(Component component, ComponentRenderPass pass);
    BatchBuilder add(Component component, ComponentRenderPass pass, Mat3d transform);
    Batch build(RenderContext context);
}
