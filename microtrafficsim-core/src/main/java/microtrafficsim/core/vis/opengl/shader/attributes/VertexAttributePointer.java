package microtrafficsim.core.vis.opengl.shader.attributes;

import com.jogamp.opengl.GL2GL3;

import microtrafficsim.core.vis.opengl.BufferStorage;
import microtrafficsim.core.vis.opengl.DataType;


/**
 * Wrapper for the OpenGL vertex attribute pointer functions ({@code glEnableVertexAttribArray} as well as
 * {@code glEnableVertexAttribPointer}, {@code glEnableVertexAttribIPointer} and {@code glEnableVertexAttribLPointer})
 *
 * @author Maximilian Luz
 */
public abstract class VertexAttributePointer {
    public final VertexAttribute attribute;
    public final DataType type;
    public final BufferStorage buffer;
    public final int           stride;
    public final long          offset;


    /**
     * Creates a new {@code VertexAttributePointer} for the given {@code VertexAttribute}, {@code DataType} and
     * {@code BufferStorage}.
     *
     * @param attribute the vertex attribute of this pointer.
     * @param type      the type of data to which the pointer points.
     * @param buffer    the buffer containing the data to which the pointer points.
     * @return the created {@code VertexAttributePointer}.
     */
    public static VertexAttributePointer create(VertexAttribute attribute, DataType type, BufferStorage buffer) {
        return create(attribute, type, buffer, 0, 0);
    }

    /**
     * Creates a new {@code VertexAttributePointer} for the given {@code VertexAttribute}, {@code DataType},
     * {@code BufferStorage}, stride and offset.
     *
     * @param attribute the vertex attribute of this pointer.
     * @param type      the type of data to which the pointer points.
     * @param buffer    the buffer containing the data to which the pointer points.
     * @param stride    the stride of the buffer.
     * @param offset    the offset in the buffer.
     * @return the created {@code VertexAttributePointer}.
     */
    public static VertexAttributePointer create(VertexAttribute attribute, DataType type, BufferStorage buffer,
                                                int stride, long offset) {
        if (attribute.type.isSinglePrecisionFloat())
            return new VertexAttributeFPointer(attribute, type, buffer, stride, offset);
        else if (attribute.type.isDoublePrecisionFloat())
            return new VertexAttributeLPointer(attribute, type, buffer, stride, offset);
        else if (attribute.type.isSignedInteger() || attribute.type.isUnsignedInteger())
            return new VertexAttributeIPointer(attribute, type, buffer, stride, offset);
        else    // should not happen
            return null;
    }


    /**
     * Constructs a new {@code VertexAttributePointer} with the given properties.
     *
     * @param attribute the vertex attribute of this pointer.
     * @param type      the type of data to which the pointer points.
     * @param buffer    the buffer containing the data to which the pointer points.
     * @param stride    the stride of the buffer.
     * @param offset    the offset in the buffer.
     */
    public VertexAttributePointer(VertexAttribute attribute, DataType type, BufferStorage buffer, int stride,
                                  long offset) {
        this.attribute = attribute;
        this.type      = type;
        this.buffer    = buffer;
        this.offset    = offset;
        this.stride    = stride;
    }

    /**
     * Sets this pointer on the OpenGL context.
     *
     * @param gl the {@code GL2GL3}-Object of the OpenGL context.
     */
    public abstract void set(GL2GL3 gl);

    /**
     * Enables this pointer on the OpenGL context.
     *
     * @param gl the {@code GL2GL3}-Object of the OpenGL context.
     */
    public void enable(GL2GL3 gl) {
        gl.glEnableVertexAttribArray(attribute.index);
    }


    /**
     * Implementation for {@code VertexAttributePointer}s pointing to single-precision floating point data.
     */
    public static class VertexAttributeFPointer extends VertexAttributePointer {

        public VertexAttributeFPointer(VertexAttribute attribute, DataType type, BufferStorage buffer, int stride,
                                       long offset) {
            super(attribute, type, buffer, stride, offset);
        }

        @Override
        public void set(GL2GL3 gl) {
            gl.glVertexAttribPointer(attribute.index, type.size, type.typeId, attribute.normalize, stride, offset);
        }
    }

    /**
     * Implementation for {@code VertexAttributePointer}s pointing to (various) integer data.
     */
    public static class VertexAttributeIPointer extends VertexAttributePointer {

        public VertexAttributeIPointer(VertexAttribute attribute, DataType type, BufferStorage buffer, int stride,
                                       long offset) {
            super(attribute, type, buffer, stride, offset);
        }

        @Override
        public void set(GL2GL3 gl) {
            gl.glVertexAttribIPointer(attribute.index, type.size, type.typeId, stride, offset);
        }
    }

    /**
     * Implementation for {@code VertexAttributePointer}s pointing to double-precision floating point data.
     */
    public static class VertexAttributeLPointer extends VertexAttributePointer {

        public VertexAttributeLPointer(VertexAttribute attribute, DataType type, BufferStorage buffer, int stride,
                                       long offset) {
            super(attribute, type, buffer, stride, offset);
        }

        @Override
        public void set(GL2GL3 gl) {
            gl.glVertexAttribLPointer(attribute.index, type.size, type.typeId, stride, offset);
        }
    }
}
