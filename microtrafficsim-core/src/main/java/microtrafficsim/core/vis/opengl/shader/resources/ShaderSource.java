package microtrafficsim.core.vis.opengl.shader.resources;

import microtrafficsim.utils.hashing.FNVHashBuilder;
import microtrafficsim.utils.resources.Resource;


/**
 * Shader source, generic resource with OpenGL type-id of the shader.
 *
 * @author Maximilian Luz
 */
public class ShaderSource {
    public final int type;
    public final Resource resource;


    /**
     * Construct a new {@code ShaderSource} from the given type and resource.
     *
     * @param type     the type of the shader.
     * @param resource the source-code of the shader as resource.
     */
    public ShaderSource(int type, Resource resource) {
        this.type     = type;
        this.resource = resource;
    }


    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof ShaderSource)) return false;

        ShaderSource other = (ShaderSource) obj;
        return this.type == other.type && this.resource.equals(other.resource);
    }

    @Override
    public int hashCode() {
        return new FNVHashBuilder()
                .add(type)
                .add(resource)
                .getHash();
    }
}
