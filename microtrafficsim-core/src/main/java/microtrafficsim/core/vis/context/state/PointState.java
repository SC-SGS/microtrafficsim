package microtrafficsim.core.vis.context.state;

import com.jogamp.opengl.GL2GL3;


/**
 * OpenGL point state.
 *
 * @author Maximilian Luz
 */
public class PointState {

    private float pointsize;
    private int   pointSpriteCoordOrigin;

    /**
     * Constructs a new {@code PointState}.
     */
    public PointState() {
        pointSpriteCoordOrigin = GL2GL3.GL_UPPER_LEFT;
    }


    /**
     * Set the OpenGL point size.
     *
     * @param gl   the {@code GL2GL3}-Object of the OpenGL context.
     * @param size the new point-size.
     */
    public void setPointSize(GL2GL3 gl, float size) {
        if (this.pointsize == size) return;

        gl.glPointSize(size);
        this.pointsize = size;
    }

    /**
     * Returns the point-size currently used.
     *
     * @return the point-size currently used.
     */
    public float getPointSize() {
        return pointsize;
    }


    /**
     * Sets the point-sprite coordinate origin.
     *
     * @param gl     the {@code GL2GL3}-Object of the OpenGL context.
     * @param origin the origin of the point-sprite coordinate system.
     */
    public void setPointSpriteCoordOrigin(GL2GL3 gl, int origin) {
        if (pointSpriteCoordOrigin == origin) return;

        gl.glPointParameteri(GL2GL3.GL_POINT_SPRITE_COORD_ORIGIN, origin);
        this.pointSpriteCoordOrigin = origin;
    }

    /**
     * Returns the point-sprite coordinate origin that is currently used.
     *
     * @return the point-sprite coordinate origin that is currently used.
     */
    public int getPointSpriteCoordOrigin() {
        return pointSpriteCoordOrigin;
    }
}
