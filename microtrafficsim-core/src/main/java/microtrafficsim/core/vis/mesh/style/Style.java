package microtrafficsim.core.vis.mesh.style;

import microtrafficsim.core.vis.opengl.shader.resources.ShaderProgramSource;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;


public class Style {
    public ShaderProgramSource shader;
    public Map<String, Supplier<?>> uniforms;
    public Map<String, Object>      properties;

    public Style() {
        this(null);
    }

    public Style(ShaderProgramSource shader) {
        this.shader     = shader;
        this.uniforms   = new HashMap<>();
        this.properties = new HashMap<>();
    }

    public ShaderProgramSource getShader() {
        return shader;
    }

    public void setShader(ShaderProgramSource shader) {
        this.shader = shader;
    }

    public Supplier<?> setUniformSupplier(String name, Supplier<?> supplier) {
        return uniforms.put(name, supplier);
    }

    public Supplier<?> getUniformSupplier(String name) {
        return uniforms.get(name);
    }

    public boolean hasUniformSupplier(String name) {
        return uniforms.containsKey(name);
    }

    public Map<String, Supplier<?>> getUniformSuppliers() {
        return Collections.unmodifiableMap(uniforms);
    }


    public <T> Object setProperty(String name, T value) {
        return properties.put(name, value);
    }

    @SuppressWarnings("unchecked")
    public <T> T getProperty(String name, T def) {
        T val = (T) properties.get(name);
        return val != null ? val : def;
    }

    public Object getProperty(String name) {
        return properties.get(name);
    }

    public boolean hasProperty(String name) {
        return properties.containsKey(name);
    }

    public Map<String, Object> getProperties() {
        return Collections.unmodifiableMap(properties);
    }
}
