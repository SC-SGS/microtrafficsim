package microtrafficsim.core.vis.errors;


public class ResourceError extends Error {

	private static final long serialVersionUID = -1431668284337159832L;
	
	private String resource;
	
	
	public ResourceError(String resource) {
		super("Error accessing resource '" + resource + "'");
		this.resource = resource;
	}
	
	public ResourceError(String resource, Throwable cause) {
		super("Error accessing resource '" + resource + "'", cause);
		this.resource = resource;
	}
	
	public String getResource() {
		return resource;
	}
}
