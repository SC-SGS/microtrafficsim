package microtrafficsim.core.vis.opengl.shader.uniforms;

import com.jogamp.opengl.GL2ES2;

import microtrafficsim.core.vis.opengl.DataType;
import microtrafficsim.core.vis.opengl.DataTypes;
import microtrafficsim.core.vis.opengl.shader.Uniform;
import microtrafficsim.core.vis.opengl.shader.UniformFactory;


public class UniformSampler2D extends Uniform<Integer> {
	
	public final static UniformFactory FACTORY = (name, type) -> {
		if (DataTypes.SAMPLER_2D.equals(type))
			return new UniformSampler2D(name);
		else
			return null;
	};
	
	
	private int value;

	public UniformSampler2D(String name) {
		super(name);
		this.value = 0;
	}
	
	
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
		return DataTypes.SAMPLER_2D;
	}
	
	@Override
	public Class<Integer> getClientType() {
		return Integer.class;
	}
}
