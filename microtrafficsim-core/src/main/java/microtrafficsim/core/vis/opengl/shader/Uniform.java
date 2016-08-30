package microtrafficsim.core.vis.opengl.shader;

import java.util.HashSet;
import java.util.Set;

import com.jogamp.opengl.GL2ES2;

import microtrafficsim.core.vis.opengl.DataType;


/**
 * Wrapper for OpenGL/GLSL uniform variables.
 *
 * @param <T> the type of the uniform-variable.
 */
public abstract class Uniform<T> {
    private String             name;
    private Set<ShaderProgram> owners;

    /**
     * Creates a new uniform-variable with the given name.
     *
     * @param name the name of the created uniform variable.
     */
    public Uniform(String name) {
        this.name   = name;
        this.owners = new HashSet<>();
    }


    /**
     * Returns the name of this {@code Uniform}.
     *
     * @return the name of this {@code Uniform}.
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name of this {@code Uniform}.
     *
     * @param name the new name of this {@code Uniform}.
     */
    public void setName(String name) {
        this.name = name;
    }


    /**
     * Adds the specified {@code ShaderProgram} as an owner to this {@code Uniform} variable.
     * Owners will be notified when the value of the uniform changes.
     *
     * @param program the owner to be added.
     */
    public void addOwner(ShaderProgram program) {
        owners.add(program);
    }

    /**
     * Removes the specified {@code ShaderProgram} from the set of owners of this {@code Uniform}.
     *
     * @param program the owner to be removed.
     */
    public void removeOwner(ShaderProgram program) {
        owners.remove(program);
    }

    /**
     * Checks if this {@code Uniform} has any owner.
     *
     * @return {@code true} if this uniform has any owner.
     */
    public boolean hasOwner() {
        return !owners.isEmpty();
    }


    /**
     * Notifies a value-change to the owners of this {@code Uniform}.
     */
    public void notifyValueChange() {
        for (ShaderProgram owner : owners)
            owner.uniformValueChanged(this);
    }


    /**
     * Updates the underlying OpenGL/GLSL uniform variable. This call requires the specific shader-program to be bound.
     *
     * @param gl       the GL-object of the OpenGL context.
     * @param location the OpenGL location of this uniform.
     */
    public abstract void update(GL2ES2 gl, int location);

    /**
     * Returns the actual OpenGL {@code DataType} of this {@code Uniform}.
     *
     * @return the type of this {@code Uniform}.
     */
    public abstract DataType getType();

    /**
     * Returns the Java client-type of this {@code Uniform}, i.e. the type to which the OpenGL uniform type gets mapped.
     *
     * @return the (Java) client-type of this {@code Uniform}.
     */
    public abstract Class<T> getClientType();

    /**
     * Sets the value of this {@code Uniform}. The actual OpenGL/GLSL assignment may (for efficiency) be delayed until
     * an owning shader is bound, it will be executed at once if any such shader is currently bound.
     *
     * @param value the new value of this {@code Uniform}:
     */
    public abstract void set(T value);

    /**
     * Returns the current value of this {@code Uniform}.
     *
     * @return the current value of this {@code Uniform}.
     */
    public abstract T get();
}
