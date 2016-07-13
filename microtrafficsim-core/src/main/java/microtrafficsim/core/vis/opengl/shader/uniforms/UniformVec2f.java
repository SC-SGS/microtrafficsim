package microtrafficsim.core.vis.opengl.shader.uniforms;

import com.jogamp.opengl.GL2ES2;
import microtrafficsim.core.vis.opengl.DataType;
import microtrafficsim.core.vis.opengl.DataTypes;
import microtrafficsim.core.vis.opengl.shader.Uniform;
import microtrafficsim.core.vis.opengl.shader.UniformFactory;
import microtrafficsim.math.Vec2f;


public class UniformVec2f extends Uniform<Vec2f> {

    public static final UniformFactory FACTORY = (name, type) -> {
        if (DataTypes.FLOAT_VEC2.equals(type))
            return new UniformVec2f(name);
        else
            return null;
    };


    private Vec2f value;

    public UniformVec2f(String name) {
        super(name);
        this.value = new Vec2f(0.f, 0.f);
    }


    @Override
    public void set(Vec2f value) {
        this.value.set(value);
        notifyValueChange();
    }

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
