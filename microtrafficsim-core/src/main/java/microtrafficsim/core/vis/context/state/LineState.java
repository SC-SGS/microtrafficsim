package microtrafficsim.core.vis.context.state;

import com.jogamp.opengl.GL2GL3;
import com.jogamp.opengl.GL3;


/**
 * OpenGL line state.
 *
 * @author Maximilian Luz
 */
public class LineState {
    private float width = 1.0f;
    private boolean lineSmoothEnabled = false;
    private int lineSmoothHint = -1;


    public void setLineWidth(GL2GL3 gl, float width) {
        if (this.width == width) return;

        gl.glLineWidth(width);
        this.width = width;
    }

    public float getLineWidth() {
        return width;
    }


    public void setLineSmoothEnabled(GL2GL3 gl, boolean enabled) {
        if (this.lineSmoothEnabled == enabled) return;

        if (enabled)
            gl.glEnable(GL2GL3.GL_LINE_SMOOTH);
        else
            gl.glDisable(GL2GL3.GL_LINE_SMOOTH);

        this.lineSmoothEnabled = enabled;
    }

    public boolean isLineSmoothEnabled() {
        return lineSmoothEnabled;
    }


    public void setLineSmoothHint(GL2GL3 gl, int hint) {
        if (this.lineSmoothHint == hint) return;

        gl.glHint(GL3.GL_LINE_SMOOTH_HINT, hint);
        this.lineSmoothHint = hint;
    }
}
