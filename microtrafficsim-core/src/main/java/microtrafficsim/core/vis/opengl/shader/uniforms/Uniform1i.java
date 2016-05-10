package microtrafficsim.core.vis.opengl.shader.uniforms;

import com.jogamp.opengl.GL2ES2;
import microtrafficsim.core.vis.opengl.DataType;
import microtrafficsim.core.vis.opengl.DataTypes;
import microtrafficsim.core.vis.opengl.shader.Uniform;
import microtrafficsim.core.vis.opengl.shader.UniformFactory;


public class Uniform1i extends Uniform<Integer> {

	public final static UniformFactory FACTORY = (name, type) -> {
		if (DataTypes.INT.equals(type))
			return new Uniform1i(name);
		else
			return null;
	};


	private int value;

	public Uniform1i(String name) {
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
		return DataTypes.INT;
	}
	
	@Override
	public Class<Integer> getClientType() {
		return Integer.class;
	}
}
