package microtrafficsim.core.vis.mesh;

import microtrafficsim.core.vis.context.RenderContext;
import microtrafficsim.core.vis.opengl.shader.ShaderProgram;
import microtrafficsim.core.vis.opengl.shader.attributes.VertexArrayObject;


/**
 * Bucket to group parts of a {@code Mesh} by their z-position.
 *
 * @author Maximilian Luz
 */
public interface MeshBucket {

    /**
     * Returns the z-index of this bucket.
     *
     * @return the z-index of this bucket.
     */
    float getZIndex();

    /**
     * Renders this bucket on the given context with the given shader.
     *
     * @param context the context on which to render this bucket.
     * @param shader  the shader to render with.
     */
    void display(RenderContext context, ShaderProgram shader);

    /**
     * Renders this bucket on the given context with the given vertex array.
     *
     * @param context the context on which to render this bucket.
     * @param vao     the vertex array object to render with.
     */
    void display(RenderContext context, VertexArrayObject vao);
}
