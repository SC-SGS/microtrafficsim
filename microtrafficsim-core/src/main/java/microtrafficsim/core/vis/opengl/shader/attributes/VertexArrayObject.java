package microtrafficsim.core.vis.opengl.shader.attributes;

import com.jogamp.opengl.GL2ES3;


/**
 * Wrapper for OpenGL vertex array objects.
 *
 * @author Maximilian Luz
 */
public class VertexArrayObject {

    /**
     * Creates a new OpenGL vertex array object and wraps it in a {@code VertexArrayObject}.
     *
     * @param gl the {@code GL2ES3}-Object of the OpenGL context.
     * @return the wrapped vertex array object.
     */
    public static VertexArrayObject create(GL2ES3 gl) {
        int[] obj = {-1};
        gl.glGenVertexArrays(1, obj, 0);

        return new VertexArrayObject(obj[0]);
    }


    private int handle;

    /**
     * Constructs a new {@code VertexArrayObject} from the given OpenGL handle.
     *
     * @param handle the OpenGL vertex array object handle.
     */
    public VertexArrayObject(int handle) {
        this.handle = handle;
    }

    /**
     * Returns the OpenGL handle of the wrapped vertex array object.
     *
     * @return the OpenGL handle of the wrapped vertex array object.
     */
    public int getHandle() {
        return handle;
    }


    /**
     * Binds this vertex array object.
     *
     * @param gl the {@code GL2ES3}-Object of the OpenGL context.
     */
    public void bind(GL2ES3 gl) {
        gl.glBindVertexArray(handle);
    }

    /**
     * Unbinds the currently bound vertex array object.
     *
     * @param gl the {@code GL2ES3}-Object of the OpenGL context.
     */
    public void unbind(GL2ES3 gl) {
        gl.glBindVertexArray(0);
    }


    /**
     * Disposes the wrapped OpenGL vertex array object.
     *
     * @param gl the {@code GL2ES3}-Object of the OpenGL context.
     */
    public void dispose(GL2ES3 gl) {
        if (this.handle == -1) return;

        int[] obj = {handle};
        gl.glDeleteVertexArrays(1, obj, 0);
        this.handle = -1;
    }
}
