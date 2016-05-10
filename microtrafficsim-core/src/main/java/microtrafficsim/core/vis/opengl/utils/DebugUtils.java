package microtrafficsim.core.vis.opengl.utils;

import com.jogamp.opengl.DebugGL2;
import com.jogamp.opengl.DebugGL3;
import com.jogamp.opengl.DebugGL3bc;
import com.jogamp.opengl.DebugGL4;
import com.jogamp.opengl.DebugGL4bc;
import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GL3;
import com.jogamp.opengl.GL3bc;
import com.jogamp.opengl.GL4;
import com.jogamp.opengl.GL4bc;
import com.jogamp.opengl.GLAutoDrawable;


public class DebugUtils {
	private DebugUtils() {}
	
	public static void setDebugGL(GLAutoDrawable drawable) {
		if (drawable.getGL().isGL4bc()) {
			final GL4bc gl4bc = drawable.getGL().getGL4bc();
			drawable.setGL(new DebugGL4bc(gl4bc));
		} else if (drawable.getGL().isGL4()) {
			final GL4 gl4 = drawable.getGL().getGL4();
			drawable.setGL(new DebugGL4(gl4));
		} else if (drawable.getGL().isGL3bc()) {
			final GL3bc gl3bc = drawable.getGL().getGL3bc();
			drawable.setGL(new DebugGL3bc(gl3bc));
		} else if (drawable.getGL().isGL3()) {
			final GL3 gl3 = drawable.getGL().getGL3();
			drawable.setGL(new DebugGL3(gl3));
		} else if (drawable.getGL().isGL2()) {
			final GL2 gl2 = drawable.getGL().getGL2();
			drawable.setGL(new DebugGL2(gl2));
		}
	}

}
