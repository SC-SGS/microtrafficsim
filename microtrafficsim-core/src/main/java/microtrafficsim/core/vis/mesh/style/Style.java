package microtrafficsim.core.vis.mesh.style;

import microtrafficsim.core.vis.opengl.shader.resources.ShaderProgramSource;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;


// TODO: check style uniform suppliers (currently unchecked cast)
// TODO: safer way to implement properties?

/**
 * Style for {@code Mesh}es, described by a shader, uniform value bindings and generic properties.
 *
 * @author Maximilian Luz
 */
public class Style {
    public ShaderProgramSource      shader;
    public Map<String, Supplier<?>> uniforms;
    public Map<String, Object>      properties;

    /**
     * Creates a new {@code Style} with the shader program provided by the given {@code ShaderProgramSource}.
     *
     * @param shader the source of the shader-program to use for this style.
     */
    public Style(ShaderProgramSource shader) {
        this.shader     = shader;
        this.uniforms   = new HashMap<>();
        this.properties = new HashMap<>();
    }

    /**
     * Returns the shader program source used in this style.
     *
     * @return the shader program source used in this style.
     */
    public ShaderProgramSource getShader() {
        return shader;
    }

    /**
     * Sets the shader program source used in this style.
     *
     * @param shader the shader program source used in this style.
     */
    public void setShader(ShaderProgramSource shader) {
        this.shader = shader;
    }

    /**
     * Sets the {@code Supplier} for the {@code Uniform} with the given name.
     *
     * @param name     the name of the uniform for which the supplier should be set.
     * @param supplier the supplier to use for the given uniform.
     * @return the supplier previously used for the uniform associated with the given name.
     */
    public Supplier<?> setUniformSupplier(String name, Supplier<?> supplier) {
        return uniforms.put(name, supplier);
    }

    /**
     * Returns the {@code Supplier} used for the {@code Uniform} associated with the given name.
     *
     * @param name the name of the {@code Uniform} for which the {@code Supplier} should be returned.
     * @return the {@code Supplier} used for the {@code Uniform} associated with the given name.
     */
    public Supplier<?> getUniformSupplier(String name) {
        return uniforms.get(name);
    }

    /**
     * Checks if the {@code Uniform} associated with the given name has a {@code Supplier}.
     *
     * @param name the name of the {@code Uniform} for which the check should be performed.
     * @return {@code true} if the {@code Uniform} associated with the given name has a {@code Supplier}.
     */
    public boolean hasUniformSupplier(String name) {
        return uniforms.containsKey(name);
    }

    /**
     * Returns all uniform suppliers used in this style.
     *
     * @return all uniform suppliers used in this style, associated with the uniform name.
     */
    public Map<String, Supplier<?>> getUniformSuppliers() {
        return Collections.unmodifiableMap(uniforms);
    }


    /**
     * Set the property associated with the given name.
     *
     * @param name  the name of the property to set.
     * @param value the value of the property.
     * @param <T>   the type of the property.
     * @return the value previously associated with the property.
     */
    public <T> Object setProperty(String name, T value) {
        return properties.put(name, value);
    }

    /**
     * Returns the property associated with the given name if it exists or the provided default value if it does not.
     * Warning: The
     *
     * @param name  the name of the property to get.
     * @param def   the default value to be returned if the property has not been set.
     * @param <T>   the type of the property.
     * @return the value associated with the property or the provided default value if the property has not been set.
     */
    @SuppressWarnings("unchecked")
    public <T> T getProperty(String name, T def) {
        T val = (T) properties.get(name);
        return val != null ? val : def;
    }


    /**
     * Returns the value of the property associated with the given name.
     *
     * @param name the name of the property.
     * @return the value of the property associated with the given name.
     */
    public Object getProperty(String name) {
        return properties.get(name);
    }

    /**
     * Checks if the property associated with the given name is set.
     *
     * @param name the name of the property.
     * @return {@code true} if the property associated with the given name is set.
     */
    public boolean hasProperty(String name) {
        return properties.containsKey(name);
    }

    /**
     * Returns all properties associated by their name.
     *
     * @return all properties associated by their name.
     */
    public Map<String, Object> getProperties() {
        return Collections.unmodifiableMap(properties);
    }
}
