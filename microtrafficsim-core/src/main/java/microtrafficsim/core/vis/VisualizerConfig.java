package microtrafficsim.core.vis;

import com.jogamp.opengl.GLCapabilities;
import com.jogamp.opengl.GLProfile;


public class VisualizerConfig {
	
	public GLProfile glprofile;
	public GLCapabilities glcapabilities;
	public int fps;
	
	public VisualizerConfig(GLProfile glprofile, GLCapabilities glcapabilities, int fps) {
		this.glprofile = glprofile;
		this.glcapabilities = glcapabilities;
		this.fps = fps;
	}
}
