package microtrafficsim.core.vis.mesh;

import microtrafficsim.core.vis.context.RenderContext;
import microtrafficsim.core.vis.opengl.shader.ShaderProgram;
import microtrafficsim.core.vis.opengl.shader.attributes.VertexArrayObject;
import microtrafficsim.core.vis.opengl.utils.LifeTimeObserver;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


/**
 * Reference counted {@code Mesh} wrapper. Newly created references to an instance of the
 * {@code ManagedShader} must call {@link ManagedMesh#require()} to make sure the
 * reference count is updated correctly.
 *
 * @author Maximilian Luz
 */
public class ManagedMesh implements Mesh {

    private Mesh                            mesh;
    private int                             refcount;
    private HashSet<LifeTimeObserver<Mesh>> ltObservers;


    /**
     * Constructs a new {@code ManagedMesh} based on the given mesh.
     *
     * @param mesh the {@code Mesh} to wrap in this resource-counted wrapper.
     */
    public ManagedMesh(Mesh mesh) {
        this.mesh        = mesh;
        this.refcount    = 1;
        this.ltObservers = new HashSet<>();
    }


    /**
     * Returns the reference count of this mesh.
     *
     * @return the reference count of this mesh.
     */
    public int getReferenceCount() {
        return refcount;
    }

    @Override
    public State getState() {
        return mesh.getState();
    }


    /**
     * Increments the reference count of this {@code ManagedMesh}. This method must be called
     * for newly created and used references to make sure the reference count is updated correctly.
     *
     * @return this {@code ManagedShader}.
     */
    public synchronized ManagedMesh require() {
        if (refcount > 0) {
            refcount++;
            return this;
        } else {
            return null;
        }
    }


    @Override
    public boolean initialize(RenderContext context) {
        return mesh.initialize(context);
    }

    @Override
    public boolean initialize(RenderContext context, boolean force) {
        return mesh.initialize(context, force);
    }

    @Override
    public boolean load(RenderContext context) {
        return mesh.load(context);
    }

    @Override
    public boolean load(RenderContext context, boolean force) {
        return mesh.load(context, force);
    }

    @Override
    public void display(RenderContext context, ShaderProgram shader) {
        mesh.display(context, shader);
    }

    @Override
    public void display(RenderContext context, VertexArrayObject vao) {
        mesh.display(context, vao);
    }

    /**
     * Safely disposes this {@code ManagedMesh}. The wrapped OpenGL mesh will only be destroyed
     * once no more (non-disposed) references exist.
     *
     * @param context the {@code RenderContext} on which the mesh has been created.
     */
    @Override
    public synchronized boolean dispose(RenderContext context) {
        if (refcount == 1) {
            refcount       = 0;
            boolean status = mesh.dispose(context);
            for (LifeTimeObserver<Mesh> lto : ltObservers)
                lto.disposed(this);
            return status;
        } else {
            refcount--;
            return true;
        }
    }

    /**
     * Safely disposes this {@code ManagedMesh}. The wrapped OpenGL mesh will only be destroyed if
     * either no more (non-disposed) references exist or the {@code force} flag has been set.
     *
     * @param context the {@code RenderContext} on which the mesh has been created.
     * @param force   if {@code true}, forces the disposal of the wrapped OpenGL shader.
     */
    public synchronized boolean dispose(RenderContext context, boolean force) {
        if (force) {
            boolean status = mesh.dispose(context);
            for (LifeTimeObserver<Mesh> lto : ltObservers)
                lto.disposed(this);
            return status;
        } else {
            return this.dispose(context);
        }
    }


    @Override
    public VertexArrayObject createVAO(RenderContext context, ShaderProgram program) {
        return mesh.createVAO(context, program);
    }


    @Override
    public List<? extends MeshBucket> getBuckets() {
        return mesh.getBuckets();
    }


    @Override
    public boolean addLifeTimeObserver(LifeTimeObserver<Mesh> lto) {
        return ltObservers.add(lto);
    }

    @Override
    public boolean removeLifeTimeObserver(LifeTimeObserver<Mesh> lto) {
        return ltObservers.remove(lto);
    }

    @Override
    public Set<LifeTimeObserver<Mesh>> getLifeTimeObservers() {
        return Collections.unmodifiableSet(ltObservers);
    }
}
