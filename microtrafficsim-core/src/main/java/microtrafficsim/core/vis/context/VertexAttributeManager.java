package microtrafficsim.core.vis.context;

import microtrafficsim.core.vis.opengl.shader.attributes.VertexAttribute;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;


/**
 * Manager to manage vertex default attributes.
 *
 * @author Maximilian Luz
 */
public class VertexAttributeManager {

    private HashMap<String, VertexAttribute> defaultAttribBindings;

    /**
     * Creates a new, empty {@code VertexAttributeManager}.
     */
    public VertexAttributeManager() {
        this.defaultAttribBindings = new HashMap<>();
    }


    /**
     * Returns the default attribute bindings, associated by their OpenGL/GLSL shader-code name.
     *
     * @return the default attribute bindings.
     */
    public Map<String, VertexAttribute> getDefaultAttributeBindings() {
        return Collections.unmodifiableMap(defaultAttribBindings);
    }

    /**
     * Associates the given {@code VertexAttribute} with the given name.
     *
     * @param name      the OpenGL/GLSL shader-code name to associate the attribute with.
     * @param attribute the vertex-attribute to associate with the given name.
     * @return the {@code VertexAttribute} previously associated with the given name.
     */
    public VertexAttribute putDefaultAttributeBinding(String name, VertexAttribute attribute) {
        return defaultAttribBindings.put(name, attribute);
    }

    /**
     * Removes the {@code VertexAttribute} associated with the given name
     *
     * @param name the OpenGL/GLSL shader-code name to remove the attribute-binding for.
     * @return the {@code VertexAttribute} previously associated with the given name.
     */
    public VertexAttribute removeDefaultAttributeBinding(String name) {
        return defaultAttribBindings.remove(name);
    }

    /**
     * Returns the {@code VertexAttribute} associated with the given name.
     *
     * @param name the OpenGL/GLSL shader-code name to get the attribute-binding for.
     * @return the {@code VertexAttribute} associated with the given name.
     */
    public VertexAttribute getDefaultAttributeBinding(String name) {
        return defaultAttribBindings.get(name);
    }

    /**
     * Checks if a {@code VertexAttribute} is associated with the given name.
     *
     * @param name the OpenGL/GLSL shader-code name to check for an attribute-binding.
     * @return {@code true} if there is a {@code VertexAttribute} associated with the given name.
     */
    public boolean hasDefaultAttributeBinding(String name) {
        return defaultAttribBindings.containsKey(name);
    }
}
