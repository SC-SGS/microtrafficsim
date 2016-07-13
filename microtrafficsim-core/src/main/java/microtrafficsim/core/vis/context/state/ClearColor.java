package microtrafficsim.core.vis.context.state;

import com.jogamp.opengl.GL;
import microtrafficsim.core.vis.opengl.utils.Color;


public class ClearColor {

    private Color value;

    public ClearColor() {
        value = new Color(0.0f, 0.0f, 0.0f, 0.0f);
    }

    public void set(GL gl, Color value) {
        set(gl, value.r, value.g, value.b, value.a);
    }

    public void set(GL gl, float r, float g, float b, float a) {
        if (value.r == r && value.g == g && value.b == b && value.a == a) return;

        gl.glClearColor(r, g, b, a);
        this.value.set(r, g, b, a);
    }

    public Color get() {
        return new Color(value);
    }
}
