package microtrafficsim.experimental.lines;

import microtrafficsim.core.vis.opengl.shader.uniforms.Uniform1f;
import microtrafficsim.core.vis.opengl.shader.uniforms.Uniform1i;
import microtrafficsim.core.vis.opengl.shader.uniforms.UniformMat4f;
import microtrafficsim.core.vis.opengl.shader.uniforms.UniformVec4f;


class Uniforms {
    final UniformMat4f uModelMatrix;
    final UniformVec4f uColor;
    final Uniform1f uLineWidth;
    final Uniform1i uCapType;
    final Uniform1i uJoinType;

    Uniforms(UniformMat4f uModelMatrix,
             UniformVec4f uColor,
             Uniform1f    uLineWidth,
             Uniform1i    uCapType,
             Uniform1i    uJoinType)
    {
        this.uModelMatrix = uModelMatrix;
        this.uColor       = uColor;
        this.uLineWidth   = uLineWidth;
        this.uCapType     = uCapType;
        this.uJoinType    = uJoinType;
    }
}
