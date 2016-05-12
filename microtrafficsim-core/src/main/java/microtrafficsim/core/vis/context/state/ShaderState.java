package microtrafficsim.core.vis.context.state;

import com.jogamp.opengl.GL2ES2;
import microtrafficsim.core.vis.opengl.shader.ShaderProgram;


public class ShaderState {
    private ShaderProgram bound;

    public ShaderState() {
        bound = null;
    }


    public void setCurrentProgram(ShaderProgram program) {
        this.bound = program;
    }

    public ShaderProgram getCurrentProgram() {
        return bound;
    }

    public void unbind(GL2ES2 gl) {
        if (bound != null) bound.unbind(gl);
    }
}
