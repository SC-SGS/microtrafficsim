package microtrafficsim.core.vis.context;

import microtrafficsim.core.vis.opengl.DataType;
import microtrafficsim.core.vis.opengl.DataTypes;
import microtrafficsim.core.vis.opengl.shader.Uniform;
import microtrafficsim.core.vis.opengl.shader.UniformFactory;
import microtrafficsim.core.vis.opengl.shader.uniforms.*;

import java.util.HashMap;


/**
 * Manager for (global) uniform variables.
 *
 * @author Maximilian Luz
 */
public class UniformManager {

    private HashMap<String, Uniform<?>>       globals;
    private HashMap<DataType, UniformFactory> factories;
    // TODO: support uniform arrays


    /**
     * Constructs a new {@code UniformManager} and default-initializes it.
     */
    public UniformManager() {
        this(true);
    }

    /**
     * Constructs a new {@code UniformManager}.
     *
     * @param init default-initialize the manager if set to {@code true}.
     */
    public UniformManager(boolean init) {
        this.globals   = new HashMap<>();
        this.factories = new HashMap<>();

        if (init) defaultInitFactories();
    }


    /**
     * Creates a uniform variable with the given name and type.
     *
     * @param name the name of the uniform.
     * @param type the type of the uniform
     * @return either if a global uniform is associated with the given name, said global uniform or a newly created
     * uniform.
     */
    public Uniform<?> create(String name, DataType type) {
        Uniform<?> uniform = globals.get(name);
        if (uniform != null) {
            if (uniform.getType().equals(type))
                return uniform;
            else
                return null;
        }

        UniformFactory factory = factories.get(type);
        if (factory != null) return factory.create(name, type);

        return null;
    }


    /**
     * Adds a global uniform of the given type associated with the given name.
     *
     * @param name the name of the global uniform.
     * @param type the type of the global uniform.
     * @return the global uniform associated of the given type associated with the given name if it already exists or
     * has been created by this call, or {@code null} if a global uniform associated with the given name exists but the
     * type differs.
     */
    public Uniform<?> putGlobalUniform(String name, DataType type) {
        Uniform<?> old = globals.get(name);

        if (old != null) {
            if (!old.getType().equals(type))
                return null;
            else
                return old;
        }

        Uniform<?> uniform = create(name, type);
        if (uniform != null) globals.put(name, uniform);

        return uniform;
    }

    /**
     * Returns the global uniform associated with the given name.
     *
     * @param name the name of the uniform to return.
     * @return the global uniform associated with the given name or {@code null} if no such uniform exists.
     */
    public Uniform<?> getGlobalUniform(String name) {
        return globals.get(name);
    }

    /**
     * Removes the global uniform associated with the given name.
     *
     * @param name the name of the uniform to remove.
     * @return the global uniform previously associated with the given name or {@code null} if no such uniform existed.
     */
    public Uniform<?> removeGlobalUniform(String name) {
        return globals.remove(name);
    }


    /**
     * Associate the given uniform factory with the given data-type.
     *
     * @param type    the type to assocaite the factory with.
     * @param factory the factory to be associated with the given type.
     * @return the factory previously associated with the given type or {@code null} if no such uniform exists.
     */
    public UniformFactory putFactory(DataType type, UniformFactory factory) {
        return factories.put(type, factory);
    }

    /**
     * Remove the uniform-factory for the given data-type.
     *
     * @param type the type to remove the factory for.
     * @return the factory previously associated with the given type or {@code null} if no such uniform exists.
     */
    public UniformFactory removeFactory(DataType type) {
        return factories.remove(type);
    }


    /**
     * Returns the uniform-factory associated with the given type
     *
     * @param type the type to get the factory for.
     * @return the factory associated with the given type or {@code null} if no such uniform exists.
     */
    public UniformFactory getFactory(DataType type) {
        return factories.get(type);
    }


    /**
     * Default-initializes the uniform factories.
     */
    private void defaultInitFactories() {
        factories.put(DataTypes.FLOAT, Uniform1f.FACTORY);
        factories.put(DataTypes.FLOAT_VEC2, UniformVec2f.FACTORY);
        factories.put(DataTypes.FLOAT_VEC4, UniformVec4f.FACTORY);
        factories.put(DataTypes.FLOAT_MAT4, UniformMat4f.FACTORY);

        factories.put(DataTypes.INT, Uniform1i.FACTORY);
        factories.put(DataTypes.INT_VEC2, UniformVec2i.FACTORY);

        factories.put(DataTypes.SAMPLER_2D, UniformSampler2D.FACTORY);
    }
}
