package microtrafficsim.core.vis.context.state;

import com.jogamp.opengl.GL;


public class ClearDepth {
	
	private double value;
	
	public ClearDepth() {
		this.value = 1.0f;
	}
	
	public void set(GL gl, double value) {
		if (this.value == value) return;
		
		gl.glClearDepth(value);
		this.value = value;
	}
	
	public double get() {
		return value;
	}
}
