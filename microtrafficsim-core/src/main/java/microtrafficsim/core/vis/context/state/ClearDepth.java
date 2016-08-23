package microtrafficsim.core.vis.context.state;

import com.jogamp.opengl.GL;


/**
 * OpenGL clear-depth state.
 *
 * @author Maximilian Luz
 */
public class ClearDepth {

    private double value;

    /**
     * Constructs a new {@code ClearDepth} state.
     */
    public ClearDepth() {
        this.value = 1.0f;
    }

    /**
     * Sets the clear-depth.
     *
     * @param gl    the {@code GL}-Object of the OpenGL context.
     * @param value the new clear-depth value.
     */
    public void set(GL gl, double value) {
        if (this.value == value) return;

        gl.glClearDepth(value);
        this.value = value;
    }

    /**
     * Returns the current clear-depth.
     *
     * @return the current clear-depth.
     */
    public double get() {
        return value;
    }
}
