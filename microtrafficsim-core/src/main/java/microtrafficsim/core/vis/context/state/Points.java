package microtrafficsim.core.vis.context.state;

import com.jogamp.opengl.GL2GL3;


public class Points {
	
	private float pointsize;
	private int pointSpriteCoordOrigin;
	
	public Points() {
		pointSpriteCoordOrigin = GL2GL3.GL_UPPER_LEFT;
	}
	
	
	public void setPointSize(GL2GL3 gl, float size) {
		if (this.pointsize == size) return;
		
		gl.glPointSize(size);
		this.pointsize = size;
	}
	
	public float getPointSize() {
		return pointsize;
	}
	
	
	public void setPointSpriteCoordOrigin(GL2GL3 gl, int origin) {
		if (pointSpriteCoordOrigin == origin)
			return;
		
		gl.glPointParameteri(GL2GL3.GL_POINT_SPRITE_COORD_ORIGIN, origin);
		this.pointSpriteCoordOrigin = origin;
	}
	
	public int getPointSpriteCoordOrigin() {
		return pointSpriteCoordOrigin;
	}
}
