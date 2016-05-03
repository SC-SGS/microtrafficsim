package microtrafficsim.experimental.lines;

import microtrafficsim.core.vis.opengl.shader.uniforms.Uniform1f;
import microtrafficsim.core.vis.opengl.shader.uniforms.Uniform1i;
import microtrafficsim.core.vis.opengl.shader.uniforms.UniformMat4f;
import microtrafficsim.core.vis.opengl.shader.uniforms.UniformVec4f;


public class Uniforms {
    public final UniformMat4f uModelMatrix;
    public final UniformVec4f uColor;
    public final Uniform1f uLineWidth;
    public final Uniform1i uCapType;
    public final Uniform1i uJoinType;

    public Uniforms(
            UniformMat4f uModelMatrix,
            UniformVec4f uColor,
            Uniform1f uLineWidth,
            Uniform1i uCapType,
            Uniform1i uJoinType)
    {
        this.uModelMatrix = uModelMatrix;
        this.uColor = uColor;
        this.uLineWidth = uLineWidth;
        this.uCapType = uCapType;
        this.uJoinType = uJoinType;
    }
}
