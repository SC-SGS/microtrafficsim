package microtrafficsim.core.vis.mesh;


import microtrafficsim.core.vis.context.RenderContext;
import microtrafficsim.core.vis.opengl.shader.ShaderProgram;
import microtrafficsim.core.vis.opengl.shader.attributes.VertexArrayObject;
import microtrafficsim.math.Vec3f;


public interface MeshBucket {
    float getZIndex();

    void display(RenderContext context, ShaderProgram shader);
    void display(RenderContext context, VertexArrayObject vao);
}
