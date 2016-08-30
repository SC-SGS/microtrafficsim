package microtrafficsim.core.vis.opengl.shader;


/**
 * Exception indicating that a failure occurred during the link-operation of a {@code ShaderProgram}.
 *
 * @author Maximilian Luz
 */
public class ShaderLinkException extends Exception {
    private static final long serialVersionUID = -7727205142213678052L;

    private String name;
    private String log;


    /**
     * Constructs a new {@code ShaderLinkException} with the given program name and error-log.
     *
     * @param name the name of the {@code ShaderProgram} that failed to link.
     * @param log  the error-log describing the failure.
     */
    public ShaderLinkException(String name, String log) {
        super("Error linking shader-program '" + name + "'");
        this.name = name;
        this.log  = log;
    }

    /**
     * Returns the name of the shader program that failed to link.
     *
     * @return the name of the shader program that failed to link.
     */
    public String getProgramName() {
        return name;
    }

    /**
     * Returns the error-log describing the failure.
     *
     * @return the error-log describing the failure.
     */
    public String getProgramInfoLog() {
        return log;
    }
}
