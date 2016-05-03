package microtrafficsim.core.vis.opengl.shader.attributes;

import microtrafficsim.core.vis.opengl.DataTypes;


public class VertexAttributes {
	private VertexAttributes() {}

	public static final VertexAttribute POSITION2 = new VertexAttribute(DataTypes.FLOAT_VEC2, 0);
	public static final VertexAttribute POSITION3 = new VertexAttribute(DataTypes.FLOAT_VEC3, 0);
	
	public static final VertexAttribute NORMAL2 = new VertexAttribute(DataTypes.FLOAT_VEC2, 1);
	public static final VertexAttribute NORMAL3 = new VertexAttribute(DataTypes.FLOAT_VEC3, 1);
	
	public static final VertexAttribute COLOR = new VertexAttribute(DataTypes.FLOAT_VEC4, 2, true);
}
