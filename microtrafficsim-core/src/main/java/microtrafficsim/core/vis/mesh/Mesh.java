package microtrafficsim.core.vis.mesh;

import microtrafficsim.core.vis.context.RenderContext;
import microtrafficsim.core.vis.opengl.shader.ShaderProgram;
import microtrafficsim.core.vis.opengl.shader.attributes.VertexArrayObject;
import microtrafficsim.core.vis.opengl.utils.LifeTimeObserver;

import java.util.List;
import java.util.Set;


public interface Mesh {
    enum State { UNINITIALIZED, INITIALIZED, LOADED, DISPOSED }

    State getState();

    boolean initialize(RenderContext context, boolean force);
    boolean dispose(RenderContext context);
    boolean load(RenderContext context, boolean force);
    void display(RenderContext context, ShaderProgram shader);
    void display(RenderContext context, VertexArrayObject vao);

    default boolean initialize(RenderContext context) {
        return initialize(context, false);
    }

    default boolean load(RenderContext context) {
        return load(context, false);
    }

    VertexArrayObject createVAO(RenderContext context, ShaderProgram program);

    List<? extends MeshBucket> getBuckets();

    void addLifeTimeObserver(LifeTimeObserver<Mesh> lto);
    void removeLifeTimeObserver(LifeTimeObserver<Mesh> lto);
    Set<LifeTimeObserver<Mesh>> getLifeTimeObservers();
}
