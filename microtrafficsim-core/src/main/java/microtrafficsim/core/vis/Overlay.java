package microtrafficsim.core.vis;

import microtrafficsim.core.vis.context.RenderContext;
import microtrafficsim.core.vis.view.View;


public interface Overlay {

	void init(RenderContext context, View view);
	void dispose(RenderContext context);
	void resize(RenderContext context, View view);
	void display(RenderContext context, View view, MapBuffer map);
	
	void enable();
	void disable();
	boolean isEnabled();

	class MapBuffer {
		public final int fbo;
		public final int color;
		public final int depth;

		public MapBuffer(int fbo, int color, int depth) {
			this.fbo = fbo;
			this.color = color;
			this.depth = depth;
		}
	}
}
