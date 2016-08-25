package microtrafficsim.core.vis.opengl.shader;

import com.jogamp.opengl.GL2ES2;
import microtrafficsim.core.vis.context.RenderContext;


/**
 * Reference counted {@code ShaderProgram} wrapper. Newly created references to an instance of the
 * {@code ManagedShaderProgram} must call {@link ManagedShaderProgram#require()} to make sure the
 * reference count is updated correctly.
 *
 * @author Maximilian Luz
 */
public class ManagedShaderProgram extends ShaderProgram {

    private int refcount;


    /**
     * Create a new OpenGL shader program and wraps it in a {@code ManagedShaderProgram}.
     *
     * @param context the {@code RenderContext} on which the program should be created.
     * @param name    the (unique) name of the program that can be used to identify it.
     * @return the created and wrapped shader program.
     */
    public static ManagedShaderProgram create(RenderContext context, String name) {
        return new ManagedShaderProgram(ShaderProgram.create(context, name));
    }

    /**
     * Construct a new reference counted wrapper for the given {@code ShaderProgram}.
     *
     * @param from the {@code ShaderProgram} to wrap.
     */
    public ManagedShaderProgram(ShaderProgram from) {
        super(from.context, from.handle, from.name);

        this.bound = from.bound;
        this.dirty = from.dirty;

        this.attribBindings = from.attribBindings;

        this.shaders = from.shaders;
        for (Shader s : this.shaders)
            if (s instanceof ManagedShader)
                ((ManagedShader) s).require();

        this.uniforms   = from.uniforms;
        this.attributes = from.attributes;

        this.ltObservers = from.ltObservers;

        refcount = 1;
    }

    /**
     * Increments the reference count of this {@code ManagedShaderProgram}. This method must be called
     * for newly created and used references to make sure the reference count is updated correctly.
     *
     * @return this {@code ManagedShaderProgram}.
     */
    public ManagedShaderProgram require() {
        if (handle < 0) return null;

        refcount++;
        return this;
    }

    /**
     * Safely disposes this {@code ManagedShaderProgram}. The wrapped OpenGL shader program will only be destroyed
     * once no more (non-disposed) references exist.
     *
     * @param gl the {@code GL2ES2}-Object of the OpenGL context on which the program has been created.
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
     * Safely disposes this {@code ManagedShaderProgram}. The wrapped OpenGL shader program will only be destroyed if
     * either no more (non-disposed) references exist or the {@code force} flag has been set.
     *
     * @param gl    the {@code GL2ES2}-Object of the OpenGL context on which the program has been created.
     * @param force if {@code true}, forces the disposal of the wrapped OpenGL shader program.
     */
    public void dispose(GL2ES2 gl, boolean force) {
        if (force)
            super.dispose(gl);
        else
            this.dispose(gl);
    }

    /**
     * Returns the reference count of this shader program.
     *
     * @return the reference count of this shader program.
     */
    public int getReferenceCount() {
        return refcount;
    }


    @Override
    public ManagedShaderProgram attach(GL2ES2 gl, Shader... shaders) {
        return (ManagedShaderProgram) super.attach(gl, shaders);
    }

    @Override
    public ManagedShaderProgram detach(GL2ES2 gl, Shader... shaders) {
        return (ManagedShaderProgram) super.detach(gl, shaders);
    }


    @Override
    public ManagedShaderProgram link(GL2ES2 gl) throws ShaderLinkException {
        return (ManagedShaderProgram) super.link(gl);
    }
}
