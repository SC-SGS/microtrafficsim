package microtrafficsim.core.vis.context.state;

import com.jogamp.opengl.GL;


public class DepthTest {

    private boolean enabled;
    private int     function;


    public DepthTest() {
        this.enabled  = false;
        this.function = -1;
    }


    public void enable(GL gl) {
        if (enabled) return;

        gl.glEnable(GL.GL_DEPTH_TEST);
        enabled = true;
    }

    public void disable(GL gl) {
        if (!enabled) return;

        gl.glDisable(GL.GL_DEPTH_TEST);
        enabled = false;
    }

    public boolean isEnabled() {
        return enabled;
    }


    public void setFunction(GL gl, int depthfunc) {
        setFunction(gl, depthfunc, false);
    }

    public void setFunction(GL gl, int depthfunc, boolean force) {
        if (!force && this.function != depthfunc) return;

        gl.glDepthFunc(depthfunc);
        this.function = depthfunc;
    }

    public int getFunction() {
        return function;
    }
}
