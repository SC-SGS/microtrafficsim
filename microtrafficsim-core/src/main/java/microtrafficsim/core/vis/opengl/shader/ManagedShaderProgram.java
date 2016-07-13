package microtrafficsim.core.vis.opengl.shader;

import com.jogamp.opengl.GL2ES2;
import microtrafficsim.core.vis.context.RenderContext;


public class ManagedShaderProgram extends ShaderProgram {

    private int refcount;


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

    public static ManagedShaderProgram create(GL2ES2 gl, RenderContext context, String name) {
        return new ManagedShaderProgram(ShaderProgram.create(gl, context, name));
    }

    public ManagedShaderProgram require() {
        if (handle < 0) return null;

        refcount++;
        return this;
    }

    @Override
    public void dispose(GL2ES2 gl) {
        if (refcount == 1) {
            super.dispose(gl);
            refcount = 0;
        } else {
            refcount--;
        }
    }

    public void dispose(GL2ES2 gl, boolean force) {
        if (force)
            super.dispose(gl);
        else
            this.dispose(gl);
    }

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
    public ManagedShaderProgram link(GL2ES2 gl) {
        return (ManagedShaderProgram) super.link(gl);
    }
}
