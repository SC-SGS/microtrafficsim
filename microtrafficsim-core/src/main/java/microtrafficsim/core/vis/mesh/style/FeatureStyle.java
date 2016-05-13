package microtrafficsim.core.vis.mesh.style;

import java.util.ArrayList;
import java.util.List;

import com.jogamp.opengl.GL2ES2;

import microtrafficsim.core.vis.context.RenderContext;
import microtrafficsim.core.vis.context.ShaderManager;
import microtrafficsim.core.vis.opengl.shader.ManagedShader;
import microtrafficsim.core.vis.opengl.shader.ManagedShaderProgram;
import microtrafficsim.core.vis.opengl.shader.Shader;
import microtrafficsim.core.vis.opengl.shader.ShaderProgram;
import microtrafficsim.core.vis.opengl.shader.resources.ShaderProgramSource;
import microtrafficsim.core.vis.opengl.shader.resources.ShaderSource;


public class FeatureStyle {
	
	private Style style;
	
	private ManagedShaderProgram program;
	private List<UniformValueBinding<?>> uniforms;
	
	public FeatureStyle(Style style) {
		this.style = style;
		this.program = null;
		this.uniforms = null;
	}
	
	
	public void initialize(RenderContext context) {
		ShaderManager manager = context.getShaderManager();
		ShaderProgramSource psrc = style.getShader();
		
		// get program, create if necessary
		program = manager.getProgram(psrc.getName());
		if (program == null) {
			GL2ES2 gl = context.getDrawable().getGL().getGL2ES2();
			
			program = ManagedShaderProgram.create(gl, context, psrc.getName());
			
			ArrayList<ManagedShader> shaders = new ArrayList<>();
			
			for (ShaderSource ssrc : psrc.getSources()) {
				String name = ssrc.resource.getUniqueName();
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
	
	public void dispose(RenderContext context) {
		GL2ES2 gl = context.getDrawable().getGL().getGL2ES2();

		if (program != null)
			program.dispose(gl);
		
		program = null;
		uniforms = null;
	}
	
	public void bind(RenderContext context) {
		GL2ES2 gl = context.getDrawable().getGL().getGL2ES2();
		
		program.bind(gl);
		uniforms.forEach(UniformValueBinding::update);
	}
	
	public void unbind(RenderContext context) {
		GL2ES2 gl = context.getDrawable().getGL().getGL2ES2();
		program.unbind(gl);
	}


	public ShaderProgram getShaderProgram() {
		return program;
	}
}
