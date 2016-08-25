package microtrafficsim.core.vis.opengl.shader;

import com.jogamp.opengl.GL2ES2;
import microtrafficsim.utils.resources.Resource;


/**
 * Reference counted {@code Shader} wrapper. Newly created references to an instance of the
 * {@code ManagedShader} must call {@link ManagedShader#require()} to make sure the
 * reference count is updated correctly.
 *
 * @author Maximilian Luz
 */
public class ManagedShader extends Shader {

    private int refcount;

    /**
     * Create a new OpenGL shader and wraps it in a {@code ManagedShader}.
     *
     * @param gl   the {@code GL2ES2}-Object of the OpenGL context on which the shader should be created.
     * @param type the OpenGL type-id of the shader type.
     * @param name the (unique) name of the program that can be used to identify it.
     * @return the created and wrapped shader.
     */
    public static ManagedShader create(GL2ES2 gl, int type, String name) {
        return new ManagedShader(Shader.create(gl, type, name));
    }

    /**
     * Construct a new reference counted wrapper for the given {@code Shader}.
     *
     * @param from the {@code Shader} to wrap.
     */
    public ManagedShader(Shader from) {
        super(from.type, from.handle, from.name);

        this.source   = from.source;
        this.programs = from.programs;

        this.ltObservers = from.ltObservers;

        this.refcount = 1;
    }

    /**
     * Increments the reference count of this {@code ManagedShader}. This method must be called
     * for newly created and used references to make sure the reference count is updated correctly.
     *
     * @return this {@code ManagedShader}.
     */
    public ManagedShader require() {
        if (handle < 0) return null;

        refcount++;
        return this;
    }

    /**
     * Safely disposes this {@code ManagedShader}. The wrapped OpenGL shader will only be destroyed
     * once no more (non-disposed) references exist.
     *
     * @param gl the {@code GL2ES2}-Object of the OpenGL context on which the shader has been created.
     */
    @Override
    public void dispose(GL2ES2 gl) {
        if (refcount == 1) {
            super.dispose(gl);
            refcount = 0;
        } else {
            refcount--;
        }
    }

    /**
     * Safely disposes this {@code ManagedShader}. The wrapped OpenGL shader will only be destroyed if
     * either no more (non-disposed) references exist or the {@code force} flag has been set.
     *
     * @param gl    the {@code GL2ES2}-Object of the OpenGL context on which the shader has been created.
     * @param force if {@code true}, forces the disposal of the wrapped OpenGL shader.
     */
    public void dispose(GL2ES2 gl, boolean force) {
        if (force)
            super.dispose(gl);
        else
            this.dispose(gl);
    }

    /**
     * Returns the reference count of this shader.
     *
     * @return the reference count of this shader.
     */
    public int getReferenceCount() {
        return refcount;
    }


    @Override
    public ManagedShader setSource(String source) {
        return (ManagedShader) super.setSource(source);
    }

    @Override
    public ManagedShader setSource(String[] source) {
        return (ManagedShader) super.setSource(source);
    }

    @Override
    public ManagedShader loadFromResource(Resource resource) {
        return (ManagedShader) super.loadFromResource(resource);
    }

    @Override
    public ManagedShader compile(GL2ES2 gl) throws ShaderCompileException {
        return (ManagedShader) super.compile(gl);
    }


    @Override
    public void addReferencedProgram(ShaderProgram program) {
        if (programs.add(program)) this.require();
    }

    @Override
    public void removeReferencedProgram(GL2ES2 gl, ShaderProgram program) {
        if (programs.remove(program)) this.dispose(gl);
    }
}
