package microtrafficsim.core.vis.context.state;

import com.jogamp.opengl.GL2ES2;
import microtrafficsim.core.vis.opengl.shader.ShaderProgram;


public class ShaderState {
    private ShaderProgram bound;

    public ShaderState() {
        bound = null;
    }


    public void bind(GL2ES2 gl, ShaderProgram program) {
        if (program == bound) return;

        gl.glUseProgram(program.getHandle());
        bound = program;
    }

    public void bind(GL2ES2 gl, ShaderProgram program, boolean force) {
        if (!force && program == bound) return;

        gl.glUseProgram(program.getHandle());
        bound = program;
    }

    public void unbind(GL2ES2 gl) {
        gl.glUseProgram(0);
        bound = null;
    }


    public ShaderProgram getCurrentProgram() {
        return bound;
    }
}
