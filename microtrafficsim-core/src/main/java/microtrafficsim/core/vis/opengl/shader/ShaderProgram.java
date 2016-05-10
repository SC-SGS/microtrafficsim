package microtrafficsim.core.vis.opengl.shader;

import static microtrafficsim.build.BuildSetup.DEBUG_VISUALIZATION;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jogamp.opengl.GL2ES2;
import com.jogamp.opengl.util.glsl.ShaderUtil;

import microtrafficsim.core.vis.context.RenderContext;
import microtrafficsim.core.vis.opengl.DataType;
import microtrafficsim.core.vis.opengl.shader.attributes.VertexAttribute;
import microtrafficsim.core.vis.opengl.utils.LifeTimeObserver;


public class ShaderProgram {
	protected final static Logger logger = LoggerFactory.getLogger(ShaderProgram.class);
	
	
	public static ShaderProgram create(GL2ES2 gl, RenderContext context, String name) {
		return new ShaderProgram(context, gl.glCreateProgram(), name);
	}
	
	
	protected final RenderContext context;
	
	protected int handle;
	protected final String name;
	
	protected GL2ES2 bound;
	protected boolean dirty;
	
	protected HashMap<String, VertexAttribute> attribBindings;
	
	protected ArrayList<Shader> shaders;
	protected HashMap<String, UniformBinding> uniforms;
	protected HashMap<String, VertexAttribute> attributes;
	
	protected HashSet<LifeTimeObserver<ShaderProgram>> ltObservers;
	
	protected ShaderProgram(RenderContext context, int handle, String name) {
		this.context = context;
		this.handle = handle;
		this.name = name;
		
		this.bound = null;
		this.dirty = true;
		
		this.attribBindings = new HashMap<>();
		
		this.shaders = new ArrayList<>();
		this.uniforms = new HashMap<>();
		this.attributes = new HashMap<>();
		
		this.ltObservers = new HashSet<>();
	}
	
	public void dispose(GL2ES2 gl) {
		for (UniformBinding binding : uniforms.values())
			binding.var.removeOwner(this);
		
		for (Shader s : shaders) {
			gl.glDetachShader(handle, s.getHandle());
			s.removeReferencedProgram(gl, this);
		}
		
		attribBindings.clear();
		
		shaders.clear();
		uniforms.clear();
		attributes.clear();
		
		gl.glDeleteProgram(handle);
		handle = -1;
		
		for (LifeTimeObserver<ShaderProgram> lto : ltObservers)
			lto.disposed(this);
	}
	
	
	public String getName() {
		return name;
	}
	
	public int getHandle() {
		return handle;
	}
	
	
	public ShaderProgram attach(GL2ES2 gl, Shader... shaders) {
		for (Shader s : shaders) {
			gl.glAttachShader(handle, s.getHandle());
			this.shaders.add(s);
			s.addReferencedProgram(this);
		}
		
		dirty = true;
		return this;
	}
	
	public ShaderProgram detach(GL2ES2 gl, Shader... shaders) {
		for (Shader s : shaders) {
			gl.glDetachShader(handle, s.getHandle());
			this.shaders.remove(s);
			s.removeReferencedProgram(gl, this);
		}
		
		return this;
	}
	
	public List<Shader> getAttachedShaders() {
		return Collections.unmodifiableList(shaders);
	}
	
	
	public ShaderProgram link(GL2ES2 gl) {
		// bind attributes
		HashMap<String, VertexAttribute> attribBindings = new HashMap<>(this.attribBindings);
		attribBindings.putAll(context.getVertexAttribManager().getDefaultAttributeBindings());
		for (Map.Entry<String, VertexAttribute> e : attribBindings.entrySet())
			gl.glBindAttribLocation(handle, e.getValue().index, e.getKey());
		
		// link program
		gl.glLinkProgram(handle);
		
		// check link status
		int[] status = { 0 };
		gl.glGetProgramiv(handle, GL2ES2.GL_LINK_STATUS, status, 0);
		if (status[0] == GL2ES2.GL_FALSE)
			throw new ShaderLinkError(name, ShaderUtil.getProgramInfoLog(gl, handle));
		
		// reload active attributes and uniforms
		reloadActiveAttributes(gl);
		reloadActiveUniforms(gl);
		
		dirty = false;
		return this;
	}
	
	
	public VertexAttribute putAttributeBinding(String name, VertexAttribute attribute) {
		if (attributes.containsKey(name))
			dirty = true;
		
		return attribBindings.put(name, attribute);
	}
	
	public VertexAttribute removeAttributeBinding(String name) {
		if (attributes.containsKey(name))
			dirty = true;
		
		return attribBindings.remove(name);
	}
	
	public VertexAttribute getAttributeBinding(String name) {
		return attribBindings.get(name);
	}
	
	public Map<String, VertexAttribute> getAttributeBindings() {
		return Collections.unmodifiableMap(attribBindings);
	}
	
	
	public VertexAttribute getActiveAttribute(String name) {
		return attributes.get(name);
	}
	
	public Map<String, VertexAttribute> getActiveAttributes() {
		return Collections.unmodifiableMap(attributes);
	}
	
	public boolean isAttributeActive(String name) {
		return attributes.containsKey(name);
	}
	
	
	public Uniform<?> getUniform(String name) {
		UniformBinding binding = uniforms.get(name);
		return binding != null ? uniforms.get(name).var : null;
	}
	
	public HashMap<String, Uniform<?>> getActiveUniforms() {
		HashMap<String, Uniform<?>> active = new HashMap<>();
		
		for (UniformBinding b : uniforms.values()) {
			active.put(b.var.getName(), b.var);
		}
		
		return active;
	}
	
	
	private void reloadActiveAttributes(GL2ES2 gl) {
		attributes.clear();
		
		// get number of active attributes
		int[] total = { -1 };
		gl.glGetProgramiv(handle, GL2ES2.GL_ACTIVE_ATTRIBUTES, total, 0);
		
		// iterate over uniforms
		for (int i = 0; i < total[0]; i++) {
			byte[] namebuffer = new byte[256];
			
			int[] len = { -1 };
			int[] size = { -1 };
			int[] typeid = { -1 };
			
			gl.glGetActiveAttrib(handle, i, namebuffer.length, len, 0, size, 0, typeid, 0, namebuffer, 0);
			String name = new String(namebuffer, 0, len[0]);
			
			int index = gl.glGetAttribLocation(handle, name);
			
			DataType type = new DataType(typeid[0], size[0]);
			VertexAttribute attrib = attribBindings.get(name);
			
			if (attrib == null)
				attrib = context.getVertexAttribManager().getDefaultAttributeBinding(name);
			
			if (attrib == null) {
				attrib = new VertexAttribute(type, index);
			} else if (DEBUG_VISUALIZATION) {
				if (!type.equals(attrib.type)) {
					logger.error("On ShaderProgram '" + this.name + "', Vertex-Attribute '" + name + "':"
							+ "\n\tType specified by bindings differs from type specified in shader:"
							+ " expected '" + attrib.type.getTypeString()
							+ "', got '" + type.getTypeString() + "'.");
				}
			}
			
			attributes.put(name, attrib);
		}
	}
	
	private void reloadActiveUniforms(GL2ES2 gl) {
		HashMap<String, UniformBinding> uniforms = new HashMap<>();
		
		// get number of active uniforms
		int[] total = { -1 };
		gl.glGetProgramiv(handle, GL2ES2.GL_ACTIVE_UNIFORMS, total, 0);
		
		// iterate over uniforms
		for (int i = 0; i < total[0]; i++) {
			byte[] namebuffer = new byte[256];
			
			int[] len = { -1 };
			int[] size = { -1 };
			int[] type = { -1 };
			
			// get uniform type and name
			gl.glGetActiveUniform(handle, i, namebuffer.length, len, 0, size, 0, type, 0, namebuffer, 0);
			String name = new String(namebuffer, 0, len[0]);
			
			// get uniform location
			int location = gl.glGetUniformLocation(handle, name);
			
			Uniform<?> uniform;
			DataType utype = new DataType(type[0], size[0]);
			
			// check if uniform has changed
			UniformBinding binding = this.uniforms.get(name);
			if (binding != null && utype.equals(binding.var.getType())) {
				uniform = binding.var;
			} else {
				uniform = context.getUniformManager().create(name, utype);
				if (uniform != null) uniform.addOwner(this);
			}

			/*
			 * The null-check is necessary here because some Vendors (nVidia) apparently implement const arrays
			 * as uniform arrays (see http://stackoverflow.com/questions/21859433/glsl-const-array-pros).
			 * This means there could be non-user specified uniforms of unknown/unsupported type.
			 */
			if (uniform != null)
				uniforms.put(name, new UniformBinding(location, uniform));
		}
		
		// if uniform has changed, remove this program as owner
		for (UniformBinding binding : this.uniforms.values())
			if (!uniforms.containsKey(binding.var.getName()))
				binding.var.removeOwner(this);
		
		this.uniforms = uniforms;
	}
	
	
	public void bind(GL2ES2 gl) {
		context.ShaderState.bind(gl, this);
        bound = gl;

		// update all dirty uniforms
		for (UniformBinding binding : uniforms.values()) {
			if (binding.dirty) {
				binding.var.update(gl, binding.location);
				binding.dirty = false;
			}
		}
	}
	
	public void unbind(GL2ES2 gl) {
		bound = null;
		context.ShaderState.unbind(gl);
	}
	
	
	public boolean isDirty() {
		return dirty;
	}
	

	public void uniformValueChanged(Uniform<?> uniform) {
		UniformBinding binding = uniforms.get(uniform.getName());
		
		// if program is bound, update directly
		if (bound != null)
			binding.var.update(bound, binding.location);
		else
			binding.dirty = true;
	}
	
	
	public void addLifeTimeObserver(LifeTimeObserver<ShaderProgram> lto) {
		ltObservers.add(lto);
	}
	
	public void removeLifeTimeObserver(LifeTimeObserver<ShaderProgram> lto) {
		ltObservers.remove(lto);
	}
	
	public Set<LifeTimeObserver<ShaderProgram>> getLifeTimeObservers() {
		return Collections.unmodifiableSet(ltObservers);
	}
	
	
	protected static class UniformBinding {
		public final int location;
		public final Uniform<?> var;
		public boolean dirty;
		
		public UniformBinding(int location, Uniform<?> var) {
			this.location = location;
			this.var = var;
			this.dirty = false;
		}
	}
}
