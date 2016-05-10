package microtrafficsim.core.vis.opengl.shader.uniforms;

import com.jogamp.opengl.GL2ES2;

import microtrafficsim.core.vis.opengl.DataType;
import microtrafficsim.core.vis.opengl.DataTypes;
import microtrafficsim.core.vis.opengl.shader.Uniform;
import microtrafficsim.core.vis.opengl.shader.UniformFactory;
import microtrafficsim.math.Vec2i;


public class UniformVec2i extends Uniform<Vec2i> {
	
	public final static UniformFactory FACTORY = (name, type) -> {
		if (DataTypes.INT_VEC2.equals(type))
			return new UniformVec2i(name);
		else
			return null;
	};
	
	
	private Vec2i value;

	public UniformVec2i(String name) {
		super(name);
		this.value = new Vec2i(0, 0);
	}
	
	
	@Override
	public void set(Vec2i value) {
		this.value.set(value);
		notifyValueChange();
	}
	
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
