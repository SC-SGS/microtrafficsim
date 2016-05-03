package microtrafficsim.core.vis.opengl.shader;


public class ShaderLinkError extends Error {

	private static final long serialVersionUID = -7727205142213678052L;
	
	private String name;
	private String log;
	
	
	public ShaderLinkError(String name, String log) {
		super("Error linking shader-program '" + name + "'");
		this.name = name;
		this.log = log;
	}

	public String getProgramName() {
		return name;
	}
	
	public String getProgramInfoLog() {
		return log;
	}
}
