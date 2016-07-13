package microtrafficsim.core.vis.opengl.shader;

import java.util.Arrays;


public class ShaderCompileError extends Error {
    private static final long serialVersionUID = -5983903247471139023L;

    private String name;
    private String src;
    private String log;


    public ShaderCompileError(String name, String[] src, String log) {
        this(name, buildSource(src), log);
    }

    public ShaderCompileError(String name, String src, String log) {
        super("Error compiling Shader '" + name + "'");
        this.name = name;
        this.log  = log;
        this.src  = src;
    }

    private static String buildSource(String[] src) {
        StringBuilder builder = new StringBuilder();
        Arrays.stream(src).forEach(builder::append);
        return builder.toString();
    }

    public String getShaderName() {
        return name;
    }

    public String getShaderSource() {
        return src;
    }

    public String getShaderInfoLog() {
        return log;
    }
}
