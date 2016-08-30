package microtrafficsim.core.vis.opengl.shader.uniforms;

import com.jogamp.opengl.GL2ES2;
import microtrafficsim.core.vis.opengl.DataType;
import microtrafficsim.core.vis.opengl.DataTypes;
import microtrafficsim.core.vis.opengl.shader.Uniform;
import microtrafficsim.core.vis.opengl.shader.UniformFactory;
import microtrafficsim.math.Vec2i;


/**
 * Two component signed integer vector uniform variable.
 *
 * @author Maximilian Luz
 */
public class UniformVec2i extends Uniform<Vec2i> {

    /**
     * Factory to create a two component signed integer uniform variable with the given name.
     * The factory will return {@code null} if the provided type is not a two component vector of signed integers.
     */
    public static final UniformFactory FACTORY = (name, type) -> {
        if (DataTypes.INT_VEC2.equals(type))
            return new UniformVec2i(name);
        else
            return null;
    };


    private Vec2i value;

    /**
     * Constructs a new two component signed integer vector uniform variable.
     *
     * @param name the name of the uniform variable.
     */
    public UniformVec2i(String name) {
        super(name);
        this.value = new Vec2i(0, 0);
    }


    @Override
    public void set(Vec2i value) {
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
    public void set(int x, int y) {
        value.set(x, y);
        notifyValueChange();
    }

    @Override
    public Vec2i get() {
        return value;
    }


    @Override
    public void update(GL2ES2 gl, int location) {
        gl.glUniform2i(location, value.x, value.y);
    }

    @Override
    public DataType getType() {
        return DataTypes.INT_VEC2;
    }

    @Override
    public Class<Vec2i> getClientType() {
        return Vec2i.class;
    }
}
