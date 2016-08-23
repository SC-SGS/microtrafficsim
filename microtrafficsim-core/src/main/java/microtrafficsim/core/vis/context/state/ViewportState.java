package microtrafficsim.core.vis.context.state;

import com.jogamp.opengl.GL;
import microtrafficsim.math.Rect2i;


/**
 * OpenGL viewport state.
 *
 * @author Maximilian Luz
 */
public class ViewportState {
    private Rect2i viewport;

    /**
     * Creates a new {@code ViewportState}.
     */
    public ViewportState() {
        this.viewport = new Rect2i(0, 0, 0, 0);
    }

    /**
     * Sets the OpenGL viewport. This function will only call the actual OpenGL function if the value has changed.
     *
     * @param gl       the {@code GL}-Object of the OpenGL context.
     * @param viewport the new viewport.
     */
    public void set(GL gl, Rect2i viewport) {
        if (this.viewport.equals(viewport)) return;

        gl.glViewport(viewport.xmin, viewport.ymin, viewport.xmax - viewport.xmin, viewport.ymax - viewport.ymin);
        this.viewport.set(viewport);
    }

    /**
     * Sets the OpenGL viewport. This function will only call the actual OpenGL function if the value has changed or
     * {@code force} is {@code true}.
     *
     * @param gl       the {@code GL}-Object of the OpenGL context.
     * @param viewport the new viewport.
     * @param force    set to {@code true} to force an actual OpenGL call.
     */
    public void set(GL gl, Rect2i viewport, boolean force) {
        if (this.viewport.equals(viewport) && !force) return;

        gl.glViewport(viewport.xmin, viewport.ymin, viewport.xmax - viewport.xmin, viewport.ymax - viewport.ymin);
        this.viewport.set(viewport);
    }

    /**
     * Sets the OpenGL viewport. This function will only call the actual OpenGL function if the value has changed.
     *
     * @param gl     the {@code GL}-Object of the OpenGL context.
     * @param x      the (new) minimum x-axis value of the viewport.
     * @param y      the (new) minimum y-axis value of the viewport.
     * @param width  the (new) width of the viewport.
     * @param height the (new) height of the viewport.
     */
    public void set(GL gl, int x, int y, int width, int height) {
        if ((viewport.xmin == x) && (viewport.ymin == y) && (viewport.xmax == x + width)
                && (viewport.ymax == y + height))
            return;

        gl.glViewport(x, y, width, height);
        this.viewport.set(x, y, x + width, y + height);
    }

    /**
     * Sets the OpenGL viewport. This function will only call the actual OpenGL function if the value has changed or
     * {@code force} is {@code true}.
     *
     * @param gl     the {@code GL}-Object of the OpenGL context.
     * @param x      the (new) minimum x-axis value of the viewport.
     * @param y      the (new) minimum y-axis value of the viewport.
     * @param width  the (new) width of the viewport.
     * @param height the (new) height of the viewport.
     * @param force  set to {@code true} to force an actual OpenGL call.
     */
    public void set(GL gl, int x, int y, int width, int height, boolean force) {
        if ((viewport.xmin == x) && (viewport.ymin == y) && (viewport.xmax == x + width)
                && (viewport.ymax == y + height)
                && !force)
            return;

        gl.glViewport(x, y, width, height);
        this.viewport.set(x, y, x + width, y + height);
    }

    /**
     * Force-update the OpenGL viewport.
     *
     * @param gl the {@code GL}-Object of the OpenGL context.
     */
    public void update(GL gl) {
        gl.glViewport(viewport.xmin, viewport.ymin, viewport.xmax - viewport.xmin, viewport.ymax - viewport.ymin);
    }

    /**
     * Sets the internal state of the viewport, does not set the actual OpenGL viewport.
     *
     * @param viewport the new viewport.
     */
    public void setInternal(Rect2i viewport) {
        this.viewport.set(viewport);
    }

    /**
     * Sets the internal state of the viewport, does not set the actual OpenGL viewport.
     *
     * @param x      the (new) minimum x-axis value of the viewport.
     * @param y      the (new) minimum y-axis value of the viewport.
     * @param width  the (new) width of the viewport.
     * @param height the (new) height of the viewport.
     */
    public void setInternal(int x, int y, int width, int height) {
        this.viewport.set(x, y, x + width, y + height);
    }

    /**
     * Returns the current viewport.
     *
     * @return the current viewport.
     */
    public Rect2i get() {
        return new Rect2i(viewport);
    }
}
