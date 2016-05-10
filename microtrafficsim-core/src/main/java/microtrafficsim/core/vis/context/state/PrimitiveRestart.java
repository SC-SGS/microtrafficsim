package microtrafficsim.core.vis.context.state;

import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GL2GL3;


public class PrimitiveRestart {
	
	private boolean enabled;
	private int index;
	
	
	public PrimitiveRestart() {
		this.enabled = false;
		this.index = 0;
	}
	
	
	public void enable(GL2GL3 gl) {
		if (enabled) return;
		gl.glEnable(GL2.GL_PRIMITIVE_RESTART);
		enabled = true;
	}
	
	public void disable(GL2GL3 gl) {
		if (!enabled) return;
		enabled = false;
		gl.glDisable(GL2.GL_PRIMITIVE_RESTART);
	}
	
	public boolean isEnabled() {
		return enabled;
	}
	
	
	public void setIndex(GL2GL3 gl, int index) {
		if (this.index == index) return;
		
		gl.glPrimitiveRestartIndex(index);
		this.index = index;
	}
	
	public int getIndex() {
		return index;
	}
}
