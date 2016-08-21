package microtrafficsim.core.vis.opengl.shader;

import microtrafficsim.core.vis.opengl.DataType;


/**
 * Factory-interface for {@code Uniform}s.
 *
 * @author Maximilian Luz
 */
public interface UniformFactory {

    /**
     * Creates a new {@code Uniform} of the the given type associated with the given name.
     *
     * @param name the name of the uniform.
     * @param type the type of the uniform.
     * @return the created {@code Uniform}.
     */
    Uniform<?> create(String name, DataType type);
}
