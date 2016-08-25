package microtrafficsim.core.vis.mesh.style;

import com.jogamp.opengl.GL2ES2;
import microtrafficsim.core.vis.context.RenderContext;
import microtrafficsim.core.vis.context.ShaderManager;
import microtrafficsim.core.vis.opengl.shader.*;
import microtrafficsim.core.vis.opengl.shader.resources.ShaderProgramSource;
import microtrafficsim.core.vis.opengl.shader.resources.ShaderSource;

import java.util.ArrayList;
import java.util.List;


/**
 * Style for map features.
 *
 * @author Maximilian Luz
 */
public class FeatureStyle {

    private Style style;

    private ManagedShaderProgram         program;
    private List<UniformValueBinding<?>> uniforms;

    /**
     * Creates a new {@code FeatureStyle} based on the given style.
     *
     * @param style the style this {@code FeatureStyle} is based on.
     */
    public FeatureStyle(Style style) {
        this.style    = style;
        this.program  = null;
        this.uniforms = null;
    }


    /**
     * Initializes this style.
     *
     * @param context the context to initialize this style on.
     * @throws ShaderLinkException if the shader-program of this style fails to link.
     */
    public void initialize(RenderContext context) throws ShaderLinkException {
        ShaderManager       manager = context.getShaderManager();
        ShaderProgramSource psrc    = style.getShader();

        // get program, create if necessary
        program = manager.getProgram(psrc.getName());
        if (program == null) {
            GL2ES2 gl = context.getDrawable().getGL().getGL2ES2();

            program = ManagedShaderProgram.create(context, psrc.getName());

            ArrayList<ManagedShader> shaders = new ArrayList<>();

            for (ShaderSource ssrc : psrc.getSources()) {
                String        name   = ssrc.resource.getUniqueName();
                ManagedShader shader = manager.getShader(name);

                if (shader == null) {
                    shader = ManagedShader.create(gl, ssrc.type, name)
                            .loadFromResource(ssrc.resource)
                            .compile(gl);
                }

                shaders.add(shader);
                manager.putShader(name, shader);
            }

            for (Shader s : shaders) {
                program.attach(gl, s);

                if (s instanceof ManagedShader)
                    s.dispose(gl);
            }

            program.link(gl);

            /* NOTE:
             * 	Shaders are not detached to keep them in memory and avoid re-compilation.
             */

            manager.putProgram(psrc.getName(), program);
        }

        uniforms = UniformValueBinding.create(style, program.getActiveUniforms());
    }

    /**
     * Disposes this style.
     *
     * @param context the context on which the style has been initialized.
     */
    public void dispose(RenderContext context) {
        GL2ES2 gl = context.getDrawable().getGL().getGL2ES2();

        if (program != null) program.dispose(gl);

        program  = null;
        uniforms = null;
    }

    /**
     * Binds this style.
     *
     * @param context the context on which the style has been initialized.
     */
    public void bind(RenderContext context) {
        GL2ES2 gl = context.getDrawable().getGL().getGL2ES2();

        program.bind(gl);
        uniforms.forEach(UniformValueBinding::update);
    }

    /**
     * Unbinds this style.
     *
     * @param context the context on which the style has been initialized.
     */
    public void unbind(RenderContext context) {
        GL2ES2 gl = context.getDrawable().getGL().getGL2ES2();
        program.unbind(gl);
    }


    /**
     * Returns the shader-program used in this style.
     *
     * @return the shader-program used in this style.
     */
    public ShaderProgram getShaderProgram() {
        return program;
    }
}
