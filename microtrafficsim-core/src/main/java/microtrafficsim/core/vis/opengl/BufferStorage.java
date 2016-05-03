package microtrafficsim.core.vis.opengl;

import com.jogamp.opengl.GL;


public class BufferStorage {
	
	public static BufferStorage create(GL gl, int target) {
		int[] obj = { -1 };
		gl.glGenBuffers(1, obj, 0);
		
		return new BufferStorage(target, obj[0]);
	}
	
	
	public final int target;
	public final int handle;
	
	public BufferStorage(int target, int handle) {
		this.target = target;
		this.handle = handle;
	}
	
	public void dispose(GL gl) {
		int[] obj = { handle };
		gl.glDeleteBuffers(1, obj, 0);
	}
	
	
	public void bind(GL gl) {
		gl.glBindBuffer(target, handle);
	}
	
	public void unbind(GL gl) {
		gl.glBindBuffer(target, 0);
	}
}
