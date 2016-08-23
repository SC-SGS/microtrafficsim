package microtrafficsim.core.vis.context.state;

import com.jogamp.opengl.GL2ES2;
import microtrafficsim.core.vis.opengl.shader.ShaderProgram;


/**
 * OpenGL shader state.
 *
 * @author Maximilian Luz
 */
public class ShaderState {
    private ShaderProgram bound;

    /**
     * Constructs a new {@code ShaderState}.
     */
    public ShaderState() {
        bound = null;
    }

    /**
     * Returns the currently bound shader program.
     *
     * @return the currently bound shader program.
     */
    public ShaderProgram getCurrentProgram() {
        return bound;
    }

    /**
     * Sets the currently bound shader-program, does not actually bind the shader-program.
     *
     * @param program the program to set the state to.
     */
    public void setCurrentProgram(ShaderProgram program) {
        this.bound = program;
    }

    /**
     * Unbinds any currently bound shader.
     *
     * @param gl the {@code GL2ES2}-Object of the OpenGL context.
     */
    public void unbind(GL2ES2 gl) {
        if (bound != null) bound.unbind(gl);
    }
}
