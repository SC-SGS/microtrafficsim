package microtrafficsim.core.vis.opengl.utils;

import com.jogamp.opengl.*;


/**
 * Debugging utilities for OpenGL/JOGL.
 *
 * @author Maximilian Luz
 */
public class DebugUtils {
    private DebugUtils() {}

    /**
     * Enables the debug-mode for the specified drawable. The debug-mode will automatically test for and report
     * errors as exceptions.
     *
     * @param drawable the {@code GLAutoDrawable} for which the debug-mode should be enabled.
     */
    public static void setDebugGL(GLAutoDrawable drawable) {
        if (drawable.getGL().isGL4bc())
            drawable.setGL(new DebugGL4bc(drawable.getGL().getGL4bc()));
        else if (drawable.getGL().isGL4())
            drawable.setGL(new DebugGL4(drawable.getGL().getGL4()));
        else if (drawable.getGL().isGL3bc())
            drawable.setGL(new DebugGL3bc(drawable.getGL().getGL3bc()));
        else if (drawable.getGL().isGL3())
            drawable.setGL(new DebugGL3(drawable.getGL().getGL3()));
        else if (drawable.getGL().isGL2())
            drawable.setGL(new DebugGL2(drawable.getGL().getGL2()));
    }
}
