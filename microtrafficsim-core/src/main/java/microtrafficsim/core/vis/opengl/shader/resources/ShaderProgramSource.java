package microtrafficsim.core.vis.opengl.shader.resources;

import java.util.HashSet;
import java.util.Set;

import microtrafficsim.utils.resources.Resource;


public class ShaderProgramSource {
	private String name;
	private Set<ShaderSource> sources;
	
	public ShaderProgramSource(String name) {
		this.name = name;
		this.sources = new HashSet<>();
	}
	
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	
	public boolean addSource(ShaderSource s) {
		return sources.add(s);
	}
	
	public boolean addSource(int type, Resource source) {
        System.out.println("name" + source.getUniqueName());
        return addSource(new ShaderSource(type, source));
	}
	
	
	public boolean removeSource(ShaderSource s) {
		return sources.remove(s);
	}
	
	public boolean removeSource(int type, Resource source) {
		return removeSource(new ShaderSource(type, source));
	}
	
	
	public boolean hasSource(ShaderSource s) {
		return sources.contains(s);
	}
	
	public boolean hasSource(int type, Resource source) {
		return hasSource(new ShaderSource(type, source));
	}
	
	
	public Set<ShaderSource> getSources() {
		return sources;
	}
}
