package microtrafficsim.core.vis.context.state;

import com.jogamp.opengl.GL;
import microtrafficsim.core.vis.opengl.utils.Color;


/**
 * OpenGL clear-color state.
 *
 * @author Maximilian Luz
 */
public class ClearColor {

    private Color value;

    /**
     * Constructs a new {@code ClearColor} state.
     */
    public ClearColor() {
        value = new Color(0.0f, 0.0f, 0.0f, 0.0f);
    }

    /**
     * Set the clear-color.
     *
     * @param gl    the {@code GL}-Object of the OpenGL context.
     * @param value the new clear-color.
     */
    public void set(GL gl, Color value) {
        set(gl, value.r, value.g, value.b, value.a);
    }

    /**
     * Set the clear-color.
     *
     * @param gl the {@code GL}-Object of the OpenGL context.
     * @param r  the red component of the clear-color
     * @param g  the green component of the clear-color
     * @param b  the blue component of the clear-color
     * @param a  the alpha component of the clear-color
     */
    public void set(GL gl, float r, float g, float b, float a) {
        if (value.r == r && value.g == g && value.b == b && value.a == a) return;

        gl.glClearColor(r, g, b, a);
        this.value.set(r, g, b, a);
    }

    /**
     * Returns the current clear-color.
     *
     * @return the current clear-color.
     */
    public Color get() {
        return new Color(value);
    }
}
