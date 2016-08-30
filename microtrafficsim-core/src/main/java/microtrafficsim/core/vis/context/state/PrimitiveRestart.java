package microtrafficsim.core.vis.context.state;

import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GL2GL3;


/**
 * OpenGL primitive restart index state.
 *
 * @author Maximilian Luz
 */
public class PrimitiveRestart {

    private boolean enabled;
    private int     index;

    /**
     * Constructs a new {@code PrimitiveRestart} state.
     */
    public PrimitiveRestart() {
        this.enabled = false;
        this.index   = 0;
    }


    /**
     * Enables the use of the primitive restart index.
     *
     * @param gl the {@code GL2GL3}-Object of the OpenGL context.
     */
    public void enable(GL2GL3 gl) {
        if (enabled) return;
        gl.glEnable(GL2.GL_PRIMITIVE_RESTART);
        enabled = true;
    }

    /**
     * Disables the use of the primitive restart index.
     *
     * @param gl the {@code GL2GL3}-Object of the OpenGL context.
     */
    public void disable(GL2GL3 gl) {
        if (!enabled) return;
        enabled = false;
        gl.glDisable(GL2.GL_PRIMITIVE_RESTART);
    }

    /**
     * Checks if the primitive restart index is enabled.
     *
     * @return {@code true} if the primitive restart index is enabled.
     */
    public boolean isEnabled() {
        return enabled;
    }


    /**
     * Set the primitive restart index.
     *
     * @param gl    the {@code GL2GL3}-Object of the OpenGL context.
     * @param index the new primitive restart index.
     */
    public void setIndex(GL2GL3 gl, int index) {
        if (this.index == index) return;

        gl.glPrimitiveRestartIndex(index);
        this.index = index;
    }

    /**
     * Returns the currently used primitive restart index.
     *
     * @return the currently used primitive restart index.
     */
    public int getIndex() {
        return index;
    }
}
