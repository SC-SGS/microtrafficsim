package microtrafficsim.core.vis.opengl.shader.uniforms;

import com.jogamp.opengl.GL2ES2;
import microtrafficsim.core.vis.opengl.DataType;
import microtrafficsim.core.vis.opengl.DataTypes;
import microtrafficsim.core.vis.opengl.shader.Uniform;
import microtrafficsim.core.vis.opengl.shader.UniformFactory;


/**
 * Single-precision floating point uniform variable.
 *
 * @author Maximilian Luz
 */
public class Uniform1f extends Uniform<Float> {

    /**
     * Factory to create a single-precision floating point uniform variable with the given name.
     * The factory will return {@code null} if the provided type is not a single precision floating point type.
     */
    public static final UniformFactory FACTORY = (name, type) -> {
        if (DataTypes.FLOAT.equals(type))
            return new Uniform1f(name);
        else
            return null;
    };


    private float value;

    /**
     * Constructs a new single-precision floating point uniform variable.
     *
     * @param name the name of the uniform variable.
     */
    public Uniform1f(String name) {
        super(name);
        this.value = 0.f;
    }


    /**
     * Sets the value of this {@code Uniform}. The actual OpenGL/GLSL assignment may (for efficiency) be delayed until
     * an owning shader is bound, it will be executed at once if any such shader is currently bound.
     *
     * @param value the new value of this {@code Uniform}:
     */
    public void set(float value) {
        this.value = value;
        notifyValueChange();
    }

    @Override
    public void set(Float value) {
        this.value = value;
        notifyValueChange();
    }

    @Override
    public Float get() {
        return value;
    }


    @Override
    public void update(GL2ES2 gl, int location) {
        gl.glUniform1f(location, value);
    }

    @Override
    public DataType getType() {
        return DataTypes.FLOAT;
    }

    @Override
    public Class<Float> getClientType() {
        return Float.class;
    }
}
