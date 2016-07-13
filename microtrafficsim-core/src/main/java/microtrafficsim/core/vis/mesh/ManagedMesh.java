package microtrafficsim.core.vis.mesh;

import microtrafficsim.core.vis.context.RenderContext;
import microtrafficsim.core.vis.opengl.shader.ShaderProgram;
import microtrafficsim.core.vis.opengl.shader.attributes.VertexArrayObject;
import microtrafficsim.core.vis.opengl.utils.LifeTimeObserver;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


public class ManagedMesh implements Mesh {

    private Mesh                            mesh;
    private int                             refcount;
    private HashSet<LifeTimeObserver<Mesh>> ltObservers;


    public ManagedMesh(Mesh mesh) {
        this.mesh        = mesh;
        this.refcount    = 1;
        this.ltObservers = new HashSet<>();
    }


    public int getReferenceCount() {
        return refcount;
    }

    @Override
    public State getState() {
        return mesh.getState();
    }


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
    public void addLifeTimeObserver(LifeTimeObserver<Mesh> lto) {
        ltObservers.add(lto);
    }

    @Override
    public void removeLifeTimeObserver(LifeTimeObserver<Mesh> lto) {
        ltObservers.remove(lto);
    }

    @Override
    public Set<LifeTimeObserver<Mesh>> getLifeTimeObservers() {
        return Collections.unmodifiableSet(ltObservers);
    }
}
