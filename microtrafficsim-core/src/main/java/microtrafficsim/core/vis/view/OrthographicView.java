package microtrafficsim.core.vis.view;

import microtrafficsim.math.*;


/**
 * Z-axis orthographic view.
 *
 * @author Maximilian Luz
 */
public class OrthographicView implements View {

    private int width;
    private int height;

    private float zNear;
    private float zFar;

    private double zoom;
    private double zoomMax;
    private double zoomMin;

    private Vec3d position;

    private Mat4f projection;
    private Mat4f view;
    private Mat4f viewprojection;


    /**
     * Constructs a new orthographic view with the given parameters.
     *
     * @param width   the width (in pixels) of the viewport.
     * @param height  the height (in pixels) of the viewport.
     * @param zNear   the z-axis value of the near-plane.
     * @param zFar    the z-axis value of the far-plane.
     * @param zoomMin the minimum zoom limit.
     * @param zoomMax the maximum zoom limit.
     */
    public OrthographicView(int width, int height, float zNear, float zFar, double zoomMin, double zoomMax) {
        this.width  = width;
        this.height = height;
        this.zNear  = zNear;
        this.zFar   = zFar;

        this.projection     = Mat4f.identity();
        this.view           = Mat4f.identity();
        this.viewprojection = Mat4f.identity();

        this.position = new Vec3d(0.0, 0.0, 10.0);

        this.zoom    = 0.0f;
        this.zoomMin = zoomMin;
        this.zoomMax = zoomMax;

        updateProjection();
        updateView();
    }


    @Override
    public Vec2i getSize() {
        return new Vec2i(width, height);
    }

    @Override
    public void resize(Vec2i size) {
        this.width  = size.x;
        this.height = size.y;
        updateProjection();
    }

    @Override
    public void resize(int width, int height) {
        this.width  = width;
        this.height = height;
        updateProjection();
    }


    @Override
    public Vec3d getPosition() {
        return position;
    }

    @Override
    public void setPosition(Vec3d position) {
        this.position.set(position);
        updateView();
    }

    /**
     * Set the 3D-position using the given 2D vector. The z-axis component will not be modified.
     *
     * @param position the new position.
     */
    public void setPosition(Vec2d position) {
        this.position.x = position.x;
        this.position.y = position.y;
        updateView();
    }

    /**
     * Set the 3D-position using the given 2D vector. The z-axis component will not be modified.
     *
     * @param x the x-axis value of the new position.
     * @param y the y-axis value of the new position.
     */
    public void setPosition(double x, double y) {
        this.position.x = x;
        this.position.y = y;
        updateView();
    }

    @Override
    public void setPosition(double x, double y, double z) {
        this.position.x = x;
        this.position.y = y;
        this.position.z = z;
        updateView();
    }

    /**
     * Returns the current zoom-level.
     *
     * @return the current zoom-level.
     */
    public double getZoomLevel() {
        return this.zoom;
    }

    public double getMaxZoomLevel() {
        return this.zoomMax;
    }

    /**
     * Set the zoom-level. This will be capped by the minimum- and maximum-limit.
     *
     * @param zoom the new zoom-level.
     */
    public void setZoomLevel(double zoom) {
        this.zoom = zoom;

        if (this.zoom > zoomMax)
            this.zoom = zoomMax;
        else if (this.zoom < zoomMin)
            this.zoom = zoomMin;

        updateProjection();
    }

    /**
     * Return the scale-factor described by the zoom-level as {@code scale = pow(2, zoom)}.
     *
     * @return the scale of this view.
     */
    public double getScale() {
        return Math.pow(2.0, zoom);
    }

    /**
     * Set the scale of this view, this also implicitly sets the zoom-level.
     *
     * @param scale the new scale of this view.
     */
    public void setScale(double scale) {
        setZoomLevel(Math.log(scale) / Math.log(2));
    }

    /**
     * Returns the viewport-bounds translated to world-coordinates.
     *
     * @return the viewport-bounds expressed in world-coordinates.
     */
    public Rect2d getViewportBounds() {
        double scale = getScale();

        return new Rect2d(position.x - width / (scale * 2.0),
                          position.y - height / (scale * 2.0),
                          position.x + width / (scale * 2.0),
                          position.y + height / (scale * 2.0));
    }


    @Override
    public Mat4f getViewProjection() {
        return viewprojection;
    }

    @Override
    public Mat4f getViewMatrix() {
        return view;
    }

    @Override
    public Mat4f getProjectionMatrix() {
        return projection;
    }


    /**
     * Centers this view on the specified target, displaying said target with the specified scale factor.
     *
     * @param target the target-area to center the view on.
     * @param scale  the scale at which the target-area should be displayed, relative to this area. A scale of one
     *               means that the area is displayed in its entirety plus additional margin if the aspect ratio of the
     *               area does not match the screen. On higher values, only the center of the area will be displayed.
     */
    public void show(Rect2d target, float scale) {
        double sx = (width * scale) / (target.xmax - target.xmin);
        double sy = (height * scale) / (target.ymax - target.ymin);

        setPosition((target.xmin + target.xmax) / 2.f, (target.ymin + target.ymax) / 2.f);
        setScale(Math.min(sx, sy));
    }


    /**
     * Updates the projection and view-projection matrices.
     */
    private void updateProjection() {
        /*
         * Note: no need to zero-check the size here, JOGL crashes before the reshape
         * call if height/width is zero. Set the Window minimum-size instead.
         */

        float left   = -width / 2.0f;
        float right  = width / 2.0f;
        float top    = height / 2.0f;
        float bottom = -height / 2.0f;

        float scale = (float) getScale();
        projection.makeOrtho(left, right, top, bottom, zNear, zFar).scale(scale, scale, 1);
        viewprojection.set(projection).mul(view);
    }

    /**
     * Updates the view and view-projection matrices.
     */
    private void updateView() {
        view.makeLookInDirection(new Vec3f(position), new Vec3f(0, 0, -1), new Vec3f(0, 1, 0));
        viewprojection.set(projection).mul(view);
    }
}
