package microtrafficsim.core.vis.opengl.shader.uniforms;

import com.jogamp.opengl.GL2ES2;
import microtrafficsim.core.vis.opengl.DataType;
import microtrafficsim.core.vis.opengl.DataTypes;
import microtrafficsim.core.vis.opengl.shader.Uniform;
import microtrafficsim.core.vis.opengl.shader.UniformFactory;
import microtrafficsim.math.Vec2f;


/**
 * Two component single-precision floating point vector uniform variable.
 *
 * @author Maximilian Luz
 */
public class UniformVec2f extends Uniform<Vec2f> {

    /**
     * Factory to create a two component single-precision floating point uniform variable with the given name.
     * The factory will return {@code null} if the provided type is not a two component vector of single precision
     * floating points.
     */
    public static final UniformFactory FACTORY = (name, type) -> {
        if (DataTypes.FLOAT_VEC2.equals(type))
            return new UniformVec2f(name);
        else
            return null;
    };


    private Vec2f value;

    /**
     * Constructs a new two component single-precision floating point vector uniform variable.
     *
     * @param name the name of the uniform variable.
     */
    public UniformVec2f(String name) {
        super(name);
        this.value = new Vec2f(0.f, 0.f);
    }


    @Override
    public void set(Vec2f value) {
        this.value.set(value);
        notifyValueChange();
    }

    /**
     * Sets the value of this {@code Uniform}. The actual OpenGL/GLSL assignment may (for efficiency) be delayed until
     * an owning shader is bound, it will be executed at once if any such shader is currently bound.
     *
     * @param x the x-component of the new value.
     * @param y the y-component of the new value.
     */
    public void set(float x, float y) {
        value.set(x, y);
        notifyValueChange();
    }

    @Override
    public Vec2f get() {
        return value;
    }


    @Override
    public void update(GL2ES2 gl, int location) {
        gl.glUniform2f(location, value.x, value.y);
    }

    @Override
    public DataType getType() {
        return DataTypes.FLOAT_VEC2;
    }

    @Override
    public Class<Vec2f> getClientType() {
        return Vec2f.class;
    }
}
