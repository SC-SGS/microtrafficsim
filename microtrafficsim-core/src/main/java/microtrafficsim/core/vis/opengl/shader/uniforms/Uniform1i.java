package microtrafficsim.core.vis.opengl.shader.uniforms;

import com.jogamp.opengl.GL2ES2;
import microtrafficsim.core.vis.opengl.DataType;
import microtrafficsim.core.vis.opengl.DataTypes;
import microtrafficsim.core.vis.opengl.shader.Uniform;
import microtrafficsim.core.vis.opengl.shader.UniformFactory;


/**
 * Signed integer uniform variable.
 *
 * @author Maximilian Luz
 */
public class Uniform1i extends Uniform<Integer> {

    /**
     * Factory to create a signed integer uniform variable with the given name.
     * The factory will return {@code null} if the provided type is not a signed integer type.
     */
    public static final UniformFactory FACTORY = (name, type) -> {
        if (DataTypes.INT.equals(type))
            return new Uniform1i(name);
        else
            return null;
    };


    private int value;

    /**
     * Constructs a new signed integer uniform variable.
     *
     * @param name the name of the uniform variable.
     */
    public Uniform1i(String name) {
        super(name);
        this.value = 0;
    }


    /**
     * Sets the value of this {@code Uniform}. The actual OpenGL/GLSL assignment may (for efficiency) be delayed until
     * an owning shader is bound, it will be executed at once if any such shader is currently bound.
     *
     * @param value the new value of this {@code Uniform}:
     */
    public void set(int value) {
        this.value = value;
        notifyValueChange();
    }

    @Override
    public void set(Integer value) {
        this.value = value;
        notifyValueChange();
    }

    @Override
    public Integer get() {
        return value;
    }


    @Override
    public void update(GL2ES2 gl, int location) {
        gl.glUniform1i(location, value);
    }

    @Override
    public DataType getType() {
        return DataTypes.INT;
    }

    @Override
    public Class<Integer> getClientType() {
        return Integer.class;
    }
}
