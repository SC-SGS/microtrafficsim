package microtrafficsim.core.vis.context;

import microtrafficsim.core.vis.opengl.DataType;
import microtrafficsim.core.vis.opengl.DataTypes;
import microtrafficsim.core.vis.opengl.shader.Uniform;
import microtrafficsim.core.vis.opengl.shader.UniformFactory;
import microtrafficsim.core.vis.opengl.shader.uniforms.*;

import java.util.HashMap;


public class UniformManager {

    private HashMap<String, Uniform<?>>       globals;
    private HashMap<DataType, UniformFactory> factories;
    // TODO: support uniform arrays


    public UniformManager() {
        this(true);
    }

    public UniformManager(boolean init) {
        this.globals   = new HashMap<>();
        this.factories = new HashMap<>();

        if (init) defaultInitFactories();
    }


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

    public Uniform<?> getGlobalUniform(String name) {
        return globals.get(name);
    }

    public Uniform<?> removeGlobalUniform(String name) {
        return globals.remove(name);
    }


    public UniformFactory putFactory(DataType type, UniformFactory factory) {
        return factories.put(type, factory);
    }

    public UniformFactory getFactory(DataType type) {
        return factories.get(type);
    }


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
