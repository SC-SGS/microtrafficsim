package microtrafficsim.core.vis.opengl.shader;

import com.jogamp.opengl.GL2ES2;

import microtrafficsim.utils.resources.Resource;


public class ManagedShader extends Shader {
	
	private int refcount;
	
	public static ManagedShader create(GL2ES2 gl, int type, String name) {
		return new ManagedShader(Shader.create(gl, type, name));
	}
	
	
	public ManagedShader(Shader from) {
		super(from.type, from.handle, from.name);
		
		this.source = from.source;
		this.programs = from.programs;
		
		this.ltObservers = from.ltObservers;
		
		this.refcount = 1;
	}
	
	public ManagedShader require() {
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
	public ManagedShader compile(GL2ES2 gl) {
		return (ManagedShader) super.compile(gl);
	}
	
	
	@Override
	public void addReferencedProgram(ShaderProgram program) {
		if (programs.add(program))
			this.require();
	}
	
	@Override
	public void removeReferencedProgram(GL2ES2 gl, ShaderProgram program) {
		if (programs.remove(program))
			this.dispose(gl);
	}
}
