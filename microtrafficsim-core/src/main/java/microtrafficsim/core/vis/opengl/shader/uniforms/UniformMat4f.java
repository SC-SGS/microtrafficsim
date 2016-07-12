package microtrafficsim.core.vis.opengl.shader.uniforms;

import com.jogamp.opengl.GL2ES2;

import microtrafficsim.core.vis.opengl.DataType;
import microtrafficsim.core.vis.opengl.DataTypes;
import microtrafficsim.core.vis.opengl.shader.Uniform;
import microtrafficsim.core.vis.opengl.shader.UniformFactory;
import microtrafficsim.math.Mat4f;


public class UniformMat4f extends Uniform<Mat4f> {
	
	public static final UniformFactory FACTORY = (name, type) -> {
		if (DataTypes.FLOAT_MAT4.equals(type))
			return new UniformMat4f(name);
		else
			return null;
	};
	
	
	private Mat4f value;

	public UniformMat4f(String name) {
		super(name);
		this.value = Mat4f.identity();
	}
	
	
	@Override
	public void set(Mat4f value) {
		this.value.set(value);
		notifyValueChange();
	}
	
	@Override
	public Mat4f get() {
		return value;
	}
	

	@Override
	public void update(GL2ES2 gl, int location) {
		gl.glUniformMatrix4fv(location, 1, true, value.getRaw(), 0);
	}

	@Override
	public DataType getType() {
		return DataTypes.FLOAT_MAT4;
	}
	
	@Override
	public Class<Mat4f> getClientType() {
		return Mat4f.class;
	}
}
