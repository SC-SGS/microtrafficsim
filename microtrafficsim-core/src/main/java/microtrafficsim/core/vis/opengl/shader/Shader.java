package microtrafficsim.core.vis.opengl.shader;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.jogamp.opengl.GL2ES2;
import com.jogamp.opengl.util.glsl.ShaderUtil;

import microtrafficsim.core.vis.exceptions.ResourceError;
import microtrafficsim.core.vis.opengl.utils.LifeTimeObserver;
import microtrafficsim.utils.Streams;
import microtrafficsim.utils.resources.Resource;


public class Shader {
	
	public static Shader create(GL2ES2 gl, int type, String name) {
		int handle = gl.glCreateShader(type);
		return new Shader(type, handle, name);
	}
	
	
	protected int type;
	protected int handle;
	
	protected String name;
	protected String[] source;
	
	protected ArrayList<ShaderProgram> programs;
	protected HashSet<LifeTimeObserver<Shader>> ltObservers;
	
	protected Shader(int type, int handle, String name) {
		this.type = type;
		this.handle = handle;
		this.name = name;
		this.source = null;
		this.programs = new ArrayList<>();
		this.ltObservers = new HashSet<>();
	}
	
	public void dispose(GL2ES2 gl) {
		gl.glDeleteShader(handle);
		handle = -1;
		
		for (LifeTimeObserver<Shader> lto : ltObservers)
			lto.disposed(this);
	}
	
	
	public String getName() {
		return name;
	}
	
	public int getType() {
		return type;
	}
	
	public int getHandle() {
		return handle;
	}
	
	
	public Shader setSource(String source) {
		this.source = new String[1];
		return this;
	}
	
	public Shader setSource(String[] source) {
		this.source = source;
		return this;
	}
	
	public Shader loadFromResource(Resource resource) {
		try (InputStream in = resource.asStream()) {
			source = Streams.toStringArrayEOL(in);
		} catch (IOException | NullPointerException e) {
			throw new ResourceError(resource.toString(), e);
		}
		
		return this;
	}
	
	public String[] getSource() {
		return source;
	}
	
	
	public Shader compile(GL2ES2 gl) {
		gl.glShaderSource(handle, source.length, source, null);
		gl.glCompileShader(handle);
		
		// check status
		int[] status = { 0 };
		gl.glGetShaderiv(handle, GL2ES2.GL_COMPILE_STATUS, status, 0);
		if (status[0] == GL2ES2.GL_FALSE)
			throw new ShaderCompileError(name, source, ShaderUtil.getShaderInfoLog(gl, handle));
		
		return this;
	}
	
	
	public void addReferencedProgram(ShaderProgram program) {
		programs.add(program);
	}
	
	public void removeReferencedProgram(GL2ES2 gl, ShaderProgram program) {
		programs.remove(program);
	}
	
	public List<ShaderProgram> getReferencedPrograms() {
		return Collections.unmodifiableList(programs);
	}


	public void addLifeTimeObserver(LifeTimeObserver<Shader> lto) {
		ltObservers.add(lto);
	}
	
	public void removeLifeTimeObserver(LifeTimeObserver<Shader> lto) {
		ltObservers.remove(lto);
	}
	
	public Set<LifeTimeObserver<Shader>> getLifeTimeObservers() {
		return Collections.unmodifiableSet(ltObservers);
	}
}
