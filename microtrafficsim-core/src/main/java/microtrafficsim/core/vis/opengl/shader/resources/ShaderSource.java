package microtrafficsim.core.vis.opengl.shader.resources;

import microtrafficsim.utils.hashing.FNVHashBuilder;
import microtrafficsim.utils.resources.Resource;


public class ShaderSource {
	public final int type;
	public final Resource resource;
	
	public ShaderSource(int type, Resource resource) {
		this.type = type;
        this.resource = resource;
	}
	
	
	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof ShaderSource))
			return false;
		
		ShaderSource other = (ShaderSource) obj;
		
		return this.type == other.type
				&& this.resource.equals(other.resource);
	}
	
	@Override
	public int hashCode() {
		return new FNVHashBuilder()
				.add(type)
				.add(resource)
				.getHash();
	}
}
