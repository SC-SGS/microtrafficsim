package microtrafficsim.core.vis.context;

import microtrafficsim.core.vis.opengl.shader.attributes.VertexAttribute;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;


public class VertexAttributeManager {

    private HashMap<String, VertexAttribute> defaultAttribBindings;

    public VertexAttributeManager() {
        this.defaultAttribBindings = new HashMap<>();
    }


    public Map<String, VertexAttribute> getDefaultAttributeBindings() {
        return Collections.unmodifiableMap(defaultAttribBindings);
    }

    public VertexAttribute putDefaultAttributeBinding(String name, VertexAttribute attribute) {
        return defaultAttribBindings.put(name, attribute);
    }

    public VertexAttribute removeDefaultAttributeBinding(String name) {
        return defaultAttribBindings.remove(name);
    }

    public VertexAttribute getDefaultAttributeBinding(String name) {
        return defaultAttribBindings.get(name);
    }

    public boolean hasDefaultAttributeBinding(String name) {
        return defaultAttribBindings.containsKey(name);
    }
}
