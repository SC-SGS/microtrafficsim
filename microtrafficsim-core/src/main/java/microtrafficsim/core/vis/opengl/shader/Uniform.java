package microtrafficsim.core.vis.opengl.shader;

import java.util.HashSet;
import java.util.Set;

import com.jogamp.opengl.GL2ES2;

import microtrafficsim.core.vis.opengl.DataType;


public abstract class Uniform<T> {
	private String name;
	private Set<ShaderProgram> owners;
	
	public Uniform(String name) {
		this.name = name;
		this.owners = new HashSet<>();
	}
	
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}

	
	public void addOwner(ShaderProgram program) {
		owners.add(program);
	}
	
	public void removeOwner(ShaderProgram program) {
		owners.remove(program);
	}
	
	public boolean hasOwner() {
		return !owners.isEmpty();
	}
	
	
	public void notifyValueChange() {
		for (ShaderProgram owner : owners)
			owner.uniformValueChanged(this);
	}
	
	
	public abstract void update(GL2ES2 gl, int location);
	public abstract DataType getType();
	public abstract Class<T> getClientType();
	
	public abstract void set(T value);
	public abstract T get();
}
