package microtrafficsim.core.vis.opengl.shader.uniforms;

import com.jogamp.opengl.GL2ES2;

import microtrafficsim.core.vis.opengl.DataType;
import microtrafficsim.core.vis.opengl.DataTypes;
import microtrafficsim.core.vis.opengl.shader.Uniform;
import microtrafficsim.core.vis.opengl.shader.UniformFactory;
import microtrafficsim.core.vis.opengl.utils.Color;
import microtrafficsim.math.Vec4f;


public class UniformVec4f extends Uniform<Vec4f> {
	
	public static final UniformFactory FACTORY = (name, type) -> {
		if (DataTypes.FLOAT_VEC4.equals(type))
			return new UniformVec4f(name);
		else
			return null;
	};
	
	
	private Vec4f value;

	public UniformVec4f(String name) {
		super(name);
		this.value = new Vec4f(0.f, 0.f, 0.f, 0.f);
	}
	
	
	@Override
	public void set(Vec4f value) {
		this.value.set(value);
		notifyValueChange();
	}
	
	public void set(Color value) {
		this.value.set(value.r, value.g, value.b, value.a);
		notifyValueChange();
	}
	
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
