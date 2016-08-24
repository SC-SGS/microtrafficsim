package microtrafficsim.core.vis.view;

import microtrafficsim.math.Mat4f;
import microtrafficsim.math.Vec2i;
import microtrafficsim.math.Vec3d;


/**
 * Basic view (camera).
 *
 * @author Maximilian Luz
 */
public interface View {

    /**
     * Return the size of the on-screen viewport (in pixels).
     *
     * @return the viewport size.
     */
    Vec2i getSize();

    /**
     * Set the viewport size.
     *
     * @param size the new viewport size.
     */
    void resize(Vec2i size);

    /**
     * Set the viewport size.
     *
     * @param x the new viewport width.
     * @param y the new viewport height.
     */
    void resize(int x, int y);

    /**
     * Returns the 3D-position of this view in the world-space.
     *
     * @return the position of this view.
     */
    Vec3d getPosition();

    /**
     * Sets the 3D-position of this view in the world-space.
     *
     * @param position the new position of this view.
     */
    void setPosition(Vec3d position);

    /**
     * Sets the 3D-position of this view in the world-space.
     *
     * @param x the new x-axis position of this view.
     * @param y the new y-axis position of this view.
     * @param z the new z-axis position of this view.
     */
    void setPosition(double x, double y, double z);

    /**
     * Return the view-projection matrix of this view.
     *
     * @return the view-projection matrix.
     */
    Mat4f getViewProjection();

    /**
     * Return the view matrix of this view.
     *
     * @return the view matrix.
     */
    Mat4f getViewMatrix();

    /**
     * Return the projection matrix of this view.
     *
     * @return the projection matrix.
     */
    Mat4f getProjectionMatrix();
}
