package microtrafficsim.core.vis.opengl.shader;

import java.util.Arrays;


/**
 * Error indicating that a failure occurred during the compile-operation of a {@code Shader}.
 *
 * @author Maximilian Luz
 */
public class ShaderCompileError extends Error {
    private static final long serialVersionUID = -5983903247471139023L;

    private String name;
    private String src;
    private String log;


    /**
     * Constructs a new {@code ShaderCompileError} with the given program name, source and error-log.
     *
     * @param name the name of the {@code Shader} that failed to compile.
     * @param src  the source of the {@code Shader} that failed to compile, as array of strings including new-line
     *             characters.
     * @param log  the error-log describing the failure.
     */
    public ShaderCompileError(String name, String[] src, String log) {
        this(name, buildSource(src), log);
    }

    /**
     * Constructs a new {@code ShaderCompileError} with the given program name, source and error-log.
     *
     * @param name the name of the {@code Shader} that failed to compile.
     * @param src  the source of the {@code Shader} that failed to compile.
     * @param log  the error-log describing the failure.
     */
    public ShaderCompileError(String name, String src, String log) {
        super("Error compiling Shader '" + name + "'");
        this.name = name;
        this.log  = log;
        this.src  = src;
    }

    /**
     * Build the source-string from the given array of strings by concatenating these.
     *
     * @param src the array of strings to concatenate.
     * @return the concatenated string.
     */
    private static String buildSource(String[] src) {
        StringBuilder builder = new StringBuilder();
        Arrays.stream(src).forEach(builder::append);
        return builder.toString();
    }

    /**
     * Returns the name of the shader that failed to compile.
     *
     * @return the name of the shader that failed to compile.
     */
    public String getShaderName() {
        return name;
    }

    /**
     * Returns the source of the shader that failed to compile.
     *
     * @return the source of the shader that failed to compile.
     */
    public String getShaderSource() {
        return src;
    }

    /**
     * Returns the error-log describing the failure.
     *
     * @return the error-log describing the failure.
     */
    public String getShaderInfoLog() {
        return log;
    }
}
