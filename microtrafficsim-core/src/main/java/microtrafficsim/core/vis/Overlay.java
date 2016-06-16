package microtrafficsim.core.vis;

import com.jogamp.newt.event.KeyEvent;
import com.jogamp.newt.event.MouseEvent;
import microtrafficsim.core.vis.context.RenderContext;
import microtrafficsim.core.vis.view.OrthographicView;
import microtrafficsim.core.vis.view.View;


public interface Overlay {

	void setView(OrthographicView view);

	void init(RenderContext context);
	void dispose(RenderContext context);
	void resize(RenderContext context);
	void display(RenderContext context, MapBuffer map);
	
	void enable();
	void disable();
	boolean isEnabled();

	default KeyListener getKeyListeners() { return null; }
	default MouseListener getMouseListener() { return null; };


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

	interface KeyListener {
		default boolean keyPressed(KeyEvent e)  { return  false; }
		default boolean keyReleased(KeyEvent e) { return  false; }
	}

	interface MouseListener {
		default boolean mouseClicked(MouseEvent e)    { return false; }
		default boolean mousePressed(MouseEvent e)    { return false; }
		default boolean mouseReleased(MouseEvent e)   { return false; }
		default boolean mouseMoved(MouseEvent e)      { return false; }
		default boolean mouseDragged(MouseEvent e)    { return false; }
		default boolean mouseWheelMoved(MouseEvent e) { return false; }
		default boolean mouseEntered(MouseEvent e) 	  { return false; }
		default boolean mouseExited(MouseEvent e)     { return false; }
	}
}
