package microtrafficsim.core.vis.opengl.shader.attributes;

import microtrafficsim.core.vis.opengl.DataType;


public class VertexAttribute {
    public final DataType type;
    public final int      index;
    public final boolean normalize;

    public VertexAttribute(DataType type, int index) {
        this(type, index, false);
    }

    public VertexAttribute(DataType type, int index, boolean normalize) {
        this.type      = type;
        this.index     = index;
        this.normalize = normalize;
    }
}
