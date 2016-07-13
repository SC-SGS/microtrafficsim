package microtrafficsim.core.vis.context.state;

import com.jogamp.opengl.GL;
import microtrafficsim.math.Rect2i;


public class ViewportState {
    private Rect2i viewport;

    public ViewportState() {
        this.viewport = new Rect2i(0, 0, 0, 0);
    }

    public void set(GL gl, Rect2i viewport) {
        if (this.viewport.equals(viewport)) return;

        gl.glViewport(viewport.xmin, viewport.ymin, viewport.xmax - viewport.xmin, viewport.ymax - viewport.ymin);
        this.viewport.set(viewport);
    }

    public void set(GL gl, Rect2i viewport, boolean force) {
        if (this.viewport.equals(viewport) && !force) return;

        gl.glViewport(viewport.xmin, viewport.ymin, viewport.xmax - viewport.xmin, viewport.ymax - viewport.ymin);
        this.viewport.set(viewport);
    }

    public void set(GL gl, int x, int y, int width, int height) {
        if ((viewport.xmin == x) && (viewport.ymin == y) && (viewport.xmax == x + width)
                && (viewport.ymax == y + height))
            return;

        gl.glViewport(x, y, width, height);
        this.viewport.set(x, y, x + width, y + height);
    }

    public void set(GL gl, int x, int y, int width, int height, boolean force) {
        if ((viewport.xmin == x) && (viewport.ymin == y) && (viewport.xmax == x + width)
                && (viewport.ymax == y + height)
                && !force)
            return;

        gl.glViewport(x, y, width, height);
        this.viewport.set(x, y, x + width, y + height);
    }

    public void update(GL gl) {
        gl.glViewport(viewport.xmin, viewport.ymin, viewport.xmax - viewport.xmin, viewport.ymax - viewport.ymin);
    }

    public void setInternal(Rect2i viewport) {
        this.viewport.set(viewport);
    }

    public void setInternal(int x, int y, int width, int height) {
        this.viewport.set(x, y, x + width, y + height);
    }

    public Rect2i get() {
        return new Rect2i(viewport);
    }
}
