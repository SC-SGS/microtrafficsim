package microtrafficsim.core.vis.opengl.shader.uniforms;

import com.jogamp.opengl.GL2ES2;

import microtrafficsim.core.vis.opengl.DataType;
import microtrafficsim.core.vis.opengl.DataTypes;
import microtrafficsim.core.vis.opengl.shader.Uniform;
import microtrafficsim.core.vis.opengl.shader.UniformFactory;


public class Uniform1f extends Uniform<Float> {
	
	public static final UniformFactory FACTORY = (name, type) -> {
		if (DataTypes.FLOAT.equals(type))
			return new Uniform1f(name);
		else
			return null;
	};
	
	
	private float value;

	public Uniform1f(String name) {
		super(name);
		this.value = 0.f;
	}
	
	
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
