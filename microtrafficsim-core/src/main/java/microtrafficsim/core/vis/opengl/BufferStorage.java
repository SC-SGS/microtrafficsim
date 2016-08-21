package microtrafficsim.core.vis.opengl;

import com.jogamp.opengl.GL;


/**
 * Wrapper for an OpenGL buffer storage.
 *
 * @author Maximilian Luz
 */
public class BufferStorage {

    /**
     * The target-type of the buffer.
     */
    public final int target;

    /**
     * The handle of the buffer.
     */
    public final int handle;

    /**
     * Creates a new {@code OpenGL} buffer object and returns it wrapped in a {@code BufferStorage}.
     *
     * @param gl     the {@code GL}-Object of the OpenGL context.
     * @param target the target of the created buffer.
     * @return the newly created OpenGL buffer wrapped as {@code BufferStorage}.
     */
    public static BufferStorage create(GL gl, int target) {
        int[] obj = {-1};
        gl.glGenBuffers(1, obj, 0);

        return new BufferStorage(target, obj[0]);
    }

    /**
     * Creates a new buffer-wrapper using the given target and handle (does not create an actual OpenGL buffer).
     *
     * @param target the target of this buffer.
     * @param handle the handle of this buffer.
     */
    public BufferStorage(int target, int handle) {
        this.target = target;
        this.handle = handle;
    }

    /**
     * Disposes this buffer.
     *
     * @param gl the {@code GL}-Object of the OpenGL context.
     */
    public void dispose(GL gl) {
        int[] obj = {handle};
        gl.glDeleteBuffers(1, obj, 0);
    }


    /**
     * Binds the buffer associated with this wrapper.
     *
     * @param gl the {@code GL}-Object of the OpenGL context.
     */
    public void bind(GL gl) {
        gl.glBindBuffer(target, handle);
    }

    /**
     * Unbinds the buffer associated with this wrapper.
     *
     * @param gl the {@code GL}-Object of the OpenGL context.
     */
    public void unbind(GL gl) {
        gl.glBindBuffer(target, 0);
    }
}
