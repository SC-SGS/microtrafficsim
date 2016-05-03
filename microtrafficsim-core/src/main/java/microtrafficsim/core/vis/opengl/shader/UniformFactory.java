package microtrafficsim.core.vis.opengl.shader;

import microtrafficsim.core.vis.opengl.DataType;


public interface UniformFactory {
	Uniform<?> create(String name, DataType type);
}
