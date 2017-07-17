package microtrafficsim.core.vis.context.state;

import com.jogamp.opengl.GL;


/**
 * OpenGL depth-test state.
 *
 * @author Maximilian Luz
 */
public class DepthTest {

    private boolean mask;
    private boolean enabled;
    private int     function;

    /**
     * Constructs a new {@code DepthTest} state.
     */
    public DepthTest() {
        this.mask = true;
        this.enabled  = false;
        this.function = -1;
    }

    public void setMask(GL gl, boolean enabled) {
        if (this.mask == enabled) return;

        gl.glDepthMask(enabled);
        this.mask = enabled;
    }

    public boolean getMask() {
        return mask;
    }

    /**
     * Enables the depth-test.
     *
     * @param gl the {@code GL}-Object of the OpenGL context.
     */
    public void enable(GL gl) {
        if (enabled) return;

        gl.glEnable(GL.GL_DEPTH_TEST);
        enabled = true;
    }

    /**
     * Disables the depth-test.
     *
     * @param gl the {@code GL}-Object of the OpenGL context.
     */
    public void disable(GL gl) {
        if (!enabled) return;

        gl.glDisable(GL.GL_DEPTH_TEST);
        enabled = false;
    }

    /**
     * Checks if the depth-test is enabled.
     *
     * @return {@code true} if the depth-test is enabled, {@code false} otherwise.
     */
    public boolean isEnabled() {
        return enabled;
    }


    /**
     * Sets the depth-test function. This call may not set the depth-test-function if the current function equals the
     * new function.
     *
     * @param gl        the {@code GL}-Object of the OpenGL context.
     * @param depthfunc the new depth-test function.
     */
    public void setFunction(GL gl, int depthfunc) {
        setFunction(gl, depthfunc, false);
    }

    /**
     * Sets the depth-test function. This call will only set the depth-test-function if the current function does not
     * equal the new function or the {@code force} flag has been set.
     *
     * @param gl        the {@code GL}-Object of the OpenGL context.
     * @param depthfunc the new depth-test function.
     * @param force     if set to {@code true}, this function will always call the respective OpenGL function.
     */
    public void setFunction(GL gl, int depthfunc, boolean force) {
        if (!force && this.function != depthfunc) return;

        gl.glDepthFunc(depthfunc);
        this.function = depthfunc;
    }

    /**
     * Returns the depth-test function currently in use.
     *
     * @return the depth-test function currently in use.
     */
    public int getFunction() {
        return function;
    }
}
