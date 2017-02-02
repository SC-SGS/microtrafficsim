package microtrafficsim.core.vis.mesh;

import microtrafficsim.core.vis.context.RenderContext;
import microtrafficsim.core.vis.opengl.shader.ShaderProgram;
import microtrafficsim.core.vis.opengl.shader.attributes.VertexArrayObject;
import microtrafficsim.core.vis.opengl.utils.LifeTimeObserver;

import java.util.List;
import java.util.Set;


/**
 * Renderable geometry.
 *
 * @author Maximilian Luz
 */
public interface Mesh {

    /**
     * The states of the mesh.
     */
    enum State { UNINITIALIZED, INITIALIZED, LOADED, DISPOSED }

    /**
     * Returns the state in which this mesh is.
     *
     * @return the state in which this mesh is.
     */
    State getState();

    /**
     * Initializes this mesh.
     *
     * @param context the context on which the mesh should be initialized.
     * @param force   set to {@code true} if the mesh should be re-initialized if it has already been initialized.
     * @return {@code true} if this call has actually initialized the mesh, {@code false} if the mesh has already
     * been initialized and {@code force} is {@code false}.
     */
    boolean initialize(RenderContext context, boolean force);

    /**
     * Disposes this mesh.
     *
     * @param context the context on which the mesh has been initialized.
     * @return {@code true} if this call has actually disposed the mesh, {@code false} if the mesh has already been
     * disposed.
     */
    boolean dispose(RenderContext context);

    /**
     * Loads this mesh from CPU memory to GPU memory.
     *
     * @param context the {@code RenderContext} representing the OpenGL context.
     * @param force   set to {@code true} if the mesh should be re-loaded if it has already been loaded.
     * @return {@code true} if this call has actually loaded the mesh, {@code false} if the mesh has already
     * been loaded and {@code force} is {@code false}.
     */
    boolean load(RenderContext context, boolean force);

    /**
     * Renders this mesh on the given context with the given {@code ShaderProgram}.
     *
     * @param context the context on which the mesh should be drawn.
     * @param shader  the shader to use for rendering.
     */
    void display(RenderContext context, ShaderProgram shader);

    /**
     * Renders this mesh on the given context with the given {@code ShaderProgram}.
     *
     * @param context the context on which the mesh should be drawn.
     * @param shader  the shader to use for rendering.
     * @param mode    the primitive mode to use for rendering.
     */
    void display(RenderContext context, ShaderProgram shader, int mode);

    /**
     * Renders this mesh on the given context with the given {@code VertexArrayObject}.
     *
     * @param context the context on which the mesh should be drawn.
     * @param vao     the vertex array object to render with.
     */
    void display(RenderContext context, VertexArrayObject vao);

    /**
     * Renders this mesh on the given context with the given {@code VertexArrayObject}.
     *
     * @param context the context on which the mesh should be drawn.
     * @param vao     the vertex array object to render with.
     * @param mode    the primitive mode to use for rendering.
     */
    void display(RenderContext context, VertexArrayObject vao, int mode);

    /**
     * Initializes this mesh. This call may not re-initialize the mesh if it already has been initialized.
     * This call is equivalent to {@link Mesh#initialize(RenderContext, boolean) Mesh.initialize(context, false)}.
     *
     * @param context the context on which the mesh should be initialized.
     * @return {@code true} if this call has actually initialized the mesh, {@code false} if the mesh has already been
     * initialized.
     * @see Mesh#initialize(RenderContext, boolean)
     */
    default boolean initialize(RenderContext context) {
        return initialize(context, false);
    }

    /**
     * Loads this mesh from CPU memeory to GPU memory. This call may not re-load the mesh if it already has been loaded.
     * This call is equivalent to {@link Mesh#load(RenderContext, boolean) Mesh.load(context, false)}.
     *
     * @param context the context on which the mesh has been initialized.
     * @return {@code true} if this call has actually loaded the mesh, {@code false} if the mesh has already been
     * loaded.
     * @see Mesh#load(RenderContext, boolean)
     */
    default boolean load(RenderContext context) {
        return load(context, false);
    }

    /**
     * Creates a vertex array object to be used for more efficient rendering with the given {@code ShaderProgram}.
     *
     * @param context the context on which the mesh has been initialized.
     * @param program the program for which the vertex array object should be created.
     * @return the vertex array object created.
     */
    VertexArrayObject createVAO(RenderContext context, ShaderProgram program);

    /**
     * Returns the {@code MeshBucket}s of this mesh.
     *
     * @return the (unordered) list of {@code MeshBucket}s of this mesh.
     */
    List<? extends MeshBucket> getBuckets();

    /**
     * Adds an observer that is being notified when the life-time of the wrapped OpenGL mesh ends, i.e.
     * a {@link Mesh#dispose(RenderContext)} call has been made.
     *
     * @param lto the {@code LifeTimeObserver} to add.
     * @return {@code true} if the underlying set of observers has changed by this call.
     */
    boolean addLifeTimeObserver(LifeTimeObserver<Mesh> lto);

    /**
     * Removes the specified {@code LifeTimeObserver} from the set of observers for this mesh.
     *
     * @param lto the {@code LifeTimeObserver} to remove.
     * @return {@code true} if the underlying set of observers has changed by this call.
     */
    boolean removeLifeTimeObserver(LifeTimeObserver<Mesh> lto);

    /**
     * Returns all {@code LifeTimeObservers} observing the life time of this mesh.
     *
     * @return all {@code LifeTimeObservers} observing the life time of this mesh.
     */
    Set<LifeTimeObserver<Mesh>> getLifeTimeObservers();
}
