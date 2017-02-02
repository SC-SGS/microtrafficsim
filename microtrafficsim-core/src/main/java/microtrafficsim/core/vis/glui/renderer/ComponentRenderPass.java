package microtrafficsim.core.vis.glui.renderer;

import microtrafficsim.core.vis.glui.Component;


public interface ComponentRenderPass {
    boolean isActiveFor(Component component);
    BatchBuilder createBuilder();
}
