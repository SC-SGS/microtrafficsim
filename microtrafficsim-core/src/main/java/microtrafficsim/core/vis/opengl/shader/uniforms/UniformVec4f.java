package microtrafficsim.core.vis.opengl.shader.uniforms;

import com.jogamp.opengl.GL2ES2;
import microtrafficsim.core.vis.opengl.DataType;
import microtrafficsim.core.vis.opengl.DataTypes;
import microtrafficsim.core.vis.opengl.shader.Uniform;
import microtrafficsim.core.vis.opengl.shader.UniformFactory;
import microtrafficsim.core.vis.opengl.utils.Color;
import microtrafficsim.math.Vec4f;


/**
 * Four component single-precision floating point vector uniform variable.
 *
 * @author Maximilian Luz
 */
public class UniformVec4f extends Uniform<Vec4f> {

    /**
     * Factory to create a four component single-precision floating point uniform variable with the given name.
     * The factory will return {@code null} if the provided type is not a four component vector of single precision
     * floating points.
     */
    public static final UniformFactory FACTORY = (name, type) -> {
        if (DataTypes.FLOAT_VEC4.equals(type))
            return new UniformVec4f(name);
        else
            return null;
    };


    private Vec4f value;

    /**
     * Constructs a new four component single-precision floating point vector uniform variable.
     *
     * @param name the name of the uniform variable.
     */
    public UniformVec4f(String name) {
        super(name);
        this.value = new Vec4f(0.f, 0.f, 0.f, 0.f);
    }


    @Override
    public void set(Vec4f value) {
        this.value.set(value);
        notifyValueChange();
    }

    /**
     * Sets the value of this {@code Uniform}. The actual OpenGL/GLSL assignment may (for efficiency) be delayed until
     * an owning shader is bound, it will be executed at once if any such shader is currently bound.
     *
     * @param value the new value of this {@code Uniform}:
     */
    public void set(Color value) {
        this.value.set(value.r, value.g, value.b, value.a);
        notifyValueChange();
    }

    /**
     * Sets the value of this {@code Uniform}. The actual OpenGL/GLSL assignment may (for efficiency) be delayed until
     * an owning shader is bound, it will be executed at once if any such shader is currently bound.
     *
     * @param x the x-component of the new value.
     * @param y the y-component of the new value.
     * @param z the z-component of the new value.
     * @param w the w-component of the new value.
     */
    public void set(float x, float y, float z, float w) {
        value.set(x, y, z, w);
        notifyValueChange();
    }

    @Override
    public Vec4f get() {
        return value;
    }


    @Override
    public void update(GL2ES2 gl, int location) {
        gl.glUniform4f(location, value.x, value.y, value.z, value.w);
    }

    @Override
    public DataType getType() {
        return DataTypes.FLOAT_VEC4;
    }

    @Override
    public Class<Vec4f> getClientType() {
        return Vec4f.class;
    }
}
