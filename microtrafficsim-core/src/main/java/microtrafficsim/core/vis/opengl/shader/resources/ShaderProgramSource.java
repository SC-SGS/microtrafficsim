package microtrafficsim.core.vis.opengl.shader.resources;

import java.util.HashSet;
import java.util.Set;

import microtrafficsim.utils.resources.Resource;


/**
 * Source of a {@code ShaderProgram}, described by name and set of {@code ShaderSource}s.
 *
 * @author Maximilian Luz
 */
public class ShaderProgramSource {
    private String            name;
    private Set<ShaderSource> sources;

    /**
     * Construct a new {@code ShaderProgramSource} with the given name.
     *
     * @param name the name of the shader program.
     */
    public ShaderProgramSource(String name) {
        this.name    = name;
        this.sources = new HashSet<>();
    }


    /**
     * Returns the name of the shader program.
     *
     * @return the name of the shader program.
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name of the shader program.
     *
     * @param name the name of the shader program.
     */
    public void setName(String name) {
        this.name = name;
    }


    /**
     * Adds a {@code ShaderSource} to this program-source.
     *
     * @param s the {@code ShaderSource} to add.
     * @return {@code true} if the underlying set of {@code ShaderSource}s changed.
     */
    public boolean addSource(ShaderSource s) {
        return sources.add(s);
    }

    /**
     * Adds the given {@code Resource} as source to this program-source.
     *
     * @param type   the type of the shader the provided resource is for.
     * @param source the resource containing the shader code.
     * @return {@code true} if the underlying set of {@code ShaderSource}s changed.
     */
    public boolean addSource(int type, Resource source) {
        return addSource(new ShaderSource(type, source));
    }


    /**
     * Removes a {@code ShaderSource} from this program-source.
     *
     * @param s the {@code ShaderSource} to remove.
     * @return {@code true} if the underlying set of {@code ShaderSource}s changed.
     */
    public boolean removeSource(ShaderSource s) {
        return sources.remove(s);
    }

    /**
     * Remove the given {@code Resource} as source from this program-source.
     *
     * @param type   the type of the shader the provided resource is for.
     * @param source the resource containing the shader code that should be removed.
     * @return {@code true} if the underlying set of {@code ShaderSource}s changed.
     */
    public boolean removeSource(int type, Resource source) {
        return removeSource(new ShaderSource(type, source));
    }


    /**
     * Checks if this program source contains the given {@code ShaderSource}.
     *
     * @param s the {@code ShaderSource} to check for.
     * @return {@code true} if this program-source contains the given {@code ShaderSource}.
     */
    public boolean hasSource(ShaderSource s) {
        return sources.contains(s);
    }

    /**
     * Checks if this program source contains the given {@code Resource} in combination with the given type.
     *
     * @param type   the type to ckeck for.
     * @param source the resource to check for.
     * @return {@code true} if this program-source contains the described {@code ShaderSource}.
     */
    public boolean hasSource(int type, Resource source) {
        return hasSource(new ShaderSource(type, source));
    }


    /**
     * Returns the set of all {@code ShaderSource}s.
     *
     * @return the set of all {@code ShaderSource}s.
     */
    public Set<ShaderSource> getSources() {
        return sources;
    }
}
