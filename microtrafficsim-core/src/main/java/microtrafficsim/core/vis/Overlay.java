package microtrafficsim.core.vis;

import microtrafficsim.core.vis.context.RenderContext;
import microtrafficsim.core.vis.view.View;


public interface Overlay {
	void init(RenderContext context, View view);
	void dispose(RenderContext context);
	void resize(RenderContext context, View view);
	void display(RenderContext context, View view);
	
	void enable();
	void disable();
	boolean isEnabled();
}
