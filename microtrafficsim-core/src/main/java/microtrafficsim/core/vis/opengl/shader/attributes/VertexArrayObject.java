package microtrafficsim.core.vis.opengl.shader.attributes;

import com.jogamp.opengl.GL2ES3;


public class VertexArrayObject {

    public static VertexArrayObject create(GL2ES3 gl) {
        int[] obj = {-1};
        gl.glGenVertexArrays(1, obj, 0);

        return new VertexArrayObject(obj[0]);
    }


    private int handle;

    public VertexArrayObject(int handle) {
        this.handle = handle;
    }

    public int getHandle() {
        return handle;
    }


    public void bind(GL2ES3 gl) {
        gl.glBindVertexArray(handle);
    }

    public void unbind(GL2ES3 gl) {
        gl.glBindVertexArray(0);
    }


    public void dispose(GL2ES3 gl) {
        if (this.handle == -1) return;

        int[] obj = {handle};
        gl.glDeleteVertexArrays(1, obj, 0);
        this.handle = -1;
    }
}
