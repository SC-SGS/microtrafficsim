package microtrafficsim.core.vis.opengl.shader.attributes;

import com.jogamp.opengl.GL2GL3;

import microtrafficsim.core.vis.opengl.BufferStorage;
import microtrafficsim.core.vis.opengl.DataType;


public abstract class VertexAttributePointer {
	public final VertexAttribute attribute;
	public final DataType type;
	public final BufferStorage buffer;
	public final int stride;
	public final long offset;
	
	
	public static VertexAttributePointer create(VertexAttribute attribute, DataType type,
			BufferStorage buffer) {
		return create(attribute, type, buffer, 0, 0);
	}
	
	public static VertexAttributePointer create(VertexAttribute attribute, DataType type,
			BufferStorage buffer, int stride, long offset) {
		
		if (attribute.type.isSinglePrecisionFloat())
			return new VertexAttributeFPointer(attribute, type, buffer, stride, offset);
		else if (attribute.type.isDoublePrecisionFloat())
			return new VertexAttributeLPointer(attribute, type, buffer, stride, offset);
		else if (attribute.type.isSignedInteger() || attribute.type.isUnsignedInteger())
			return new VertexAttributeIPointer(attribute, type, buffer, stride, offset);
		else	// should not happen
			return null;
	}
	
	
	public VertexAttributePointer(VertexAttribute attribute, DataType type,
			BufferStorage buffer, int stride, long offset) {
		
		this.attribute = attribute;
		this.type = type;
		this.buffer = buffer;
		this.offset = offset;
		this.stride = stride;
	}
	
	public abstract void set(GL2GL3 gl);
	
	public void enable(GL2GL3 gl) {
		gl.glEnableVertexAttribArray(attribute.index);
	}
	
	
	public static class VertexAttributeFPointer extends VertexAttributePointer {

		public VertexAttributeFPointer(VertexAttribute attribute, DataType type,
				BufferStorage buffer, int stride, long offset) {
			super(attribute, type, buffer, stride, offset);
		}

		@Override
		public void set(GL2GL3 gl) {
			gl.glVertexAttribPointer(attribute.index, type.size, type.typeId,
					attribute.normalize, stride, offset);
		}
	}
	
	public static class VertexAttributeIPointer extends VertexAttributePointer {

		public VertexAttributeIPointer(VertexAttribute attribute, DataType type,
				BufferStorage buffer, int stride, long offset) {
			super(attribute, type, buffer, stride, offset);
		}

		@Override
		public void set(GL2GL3 gl) {
			gl.glVertexAttribIPointer(attribute.index, type.size, type.typeId,
					stride, offset);
		}
	}
	
	public static class VertexAttributeLPointer extends VertexAttributePointer {

		public VertexAttributeLPointer(VertexAttribute attribute, DataType type,
				BufferStorage buffer, int stride, long offset) {
			super(attribute, type, buffer, stride, offset);
		}

		@Override
		public void set(GL2GL3 gl) {
			gl.glVertexAttribLPointer(attribute.index, type.size, type.typeId,
					stride, offset);
		}
	}
}
