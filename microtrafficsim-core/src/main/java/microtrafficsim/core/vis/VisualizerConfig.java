package microtrafficsim.core.vis;

import com.jogamp.opengl.GLCapabilities;
import com.jogamp.opengl.GLProfile;


/**
 * Configuration for the Visualizer.
 *
 * @author Maximilian Luz
 */
public class VisualizerConfig {

    /**
     * The {@code GLProfile} to be used for the {@code Visualizer}.
     */
    public GLProfile glprofile;

    /**
     * The {@code GLCapabilities} describing the desired capabilities for the OpenGL visualizer.
     */
    public GLCapabilities glcapabilities;

    /**
     * The frames-per-second (FPS) limit to be applied. Set to zero for unlimited.
     */
    public int fps;


    /**
     * Construct a new configuration based on the given properties.
     *
     * @param glprofile      the profile to be used for the OpenGL {@code Visualizer}.
     * @param glcapabilities the desired capabilities for the OpenGL {@code Visualizer}.
     * @param fps            the desired fps limit, zero for no unlimited fps.
     */
    public VisualizerConfig(GLProfile glprofile, GLCapabilities glcapabilities, int fps) {
        this.glprofile      = glprofile;
        this.glcapabilities = glcapabilities;
        this.fps            = fps;
    }
}
