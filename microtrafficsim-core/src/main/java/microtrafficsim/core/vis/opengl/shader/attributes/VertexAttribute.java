package microtrafficsim.core.vis.opengl.shader.attributes;

import microtrafficsim.core.vis.opengl.DataType;


/**
 * Wrapper for OpenGL vertex attributes.
 *
 * @author Maximilian Luz
 */
public class VertexAttribute {
    public final DataType type;
    public final int      index;
    public final boolean normalize;

    /**
     * Constructs a new {@code VertexAttribute} with the given type and index.
     *
     * @param type  the type of the data that can be accessed via the attribute.
     * @param index the OpenGL/GLSL index of the attribute.
     */
    public VertexAttribute(DataType type, int index) {
        this(type, index, false);
    }

    /**
     * Constructs a new {@code VertexAttribute} with the given type and index.
     *
     * @param type      the type of the OpenGL/GLSL data that can be accessed via the attribute.
     * @param index     the OpenGL/GLSL index of the attribute.
     * @param normalize set to {@code true} if integral data should be normalized to the interval -1, 1 when converted
     *                  to floating point data.
     */
    public VertexAttribute(DataType type, int index, boolean normalize) {
        this.type      = type;
        this.index     = index;
        this.normalize = normalize;
    }
}
