package microtrafficsim.core.vis.mesh.impl;

import com.jogamp.opengl.GL2GL3;
import microtrafficsim.core.vis.context.RenderContext;
import microtrafficsim.core.vis.mesh.Mesh;
import microtrafficsim.core.vis.mesh.MeshBucket;
import microtrafficsim.core.vis.opengl.BufferStorage;
import microtrafficsim.core.vis.opengl.DataType;
import microtrafficsim.core.vis.opengl.DataTypes;
import microtrafficsim.core.vis.opengl.shader.ShaderProgram;
import microtrafficsim.core.vis.opengl.shader.attributes.VertexArrayObject;
import microtrafficsim.core.vis.opengl.shader.attributes.VertexAttribute;
import microtrafficsim.core.vis.opengl.shader.attributes.VertexAttributePointer;
import microtrafficsim.core.vis.opengl.shader.attributes.VertexAttributes;
import microtrafficsim.core.vis.opengl.utils.LifeTimeObserver;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


// TODO: generalize?

/**
 * Mesh containing a single (multi-component) single-precision floating-point vertex attribute (not index-based).
 *
 * @author Maximilian Luz
 */
public class SingleFloatAttributeMesh implements Mesh {
    private State state;

    private int usage;
    private int mode;
    private VertexAttribute attribute;
    private DataType datatype;

    private FloatBuffer vertices;
    private int nVertices;

    private BufferStorage     vbo;
    private VertexArrayObject vao;    // from OpenGL 3.1 upwards a VAO is always required for drawing

    private VertexAttributePointer ptrPosition;

    private List<Bucket> buckets;

    private HashSet<LifeTimeObserver<Mesh>> ltObservers;


    /**
     * Creates a new mesh with the given properties.
     *
     * @param usage     the OpenGL usage of the mesh.
     * @param mode      the OpenGL draw-mode of the mesh.
     * @param attribute the attribute stored in the vertex-buffer of this mesh, must be a float compatible attribute.
     * @param datatype  the OpenGL data-type of the data to be drawn, must be a single-precision floating-point type
     *                  compatible with {@code attribute}.
     * @param vertices  the vertex buffer.
     */
    public SingleFloatAttributeMesh(int usage, int mode, VertexAttribute attribute, DataType datatype, FloatBuffer vertices) {
        this.state = State.UNINITIALIZED;

        this.usage = usage;
        this.mode  = mode;
        this.attribute = attribute;
        this.datatype = datatype;

        this.vertices = vertices;
        this.vbo = null;

        this.ltObservers = new HashSet<>();
    }


    /**
     * Creates a new mesh with the given properties, using {@code VertexAttributes.POSITION2} as attribute and
     * {@code DataTypes.FLOAT_2} as data-type.
     *
     * @param usage     the OpenGL usage of the mesh.
     * @param mode      the OpenGL draw-mode of the mesh.
     * @param vertices  the vertex buffer.
     * @return the new mesh.
     */
    public static SingleFloatAttributeMesh newPos2Mesh(int usage, int mode, FloatBuffer vertices) {
        return new SingleFloatAttributeMesh(usage, mode, VertexAttributes.POSITION2, DataTypes.FLOAT_2, vertices);
    }

    /**
     * Creates a new mesh with the given properties, using {@code VertexAttributes.POSITION3} as attribute and
     * {@code DataTypes.FLOAT_3} as data-type.
     *
     * @param usage     the OpenGL usage of the mesh.
     * @param mode      the OpenGL draw-mode of the mesh.
     * @param vertices  the vertex buffer.
     * @return the new mesh.
     */
    public static SingleFloatAttributeMesh newPos3Mesh(int usage, int mode, FloatBuffer vertices) {
        return new SingleFloatAttributeMesh(usage, mode, VertexAttributes.POSITION3, DataTypes.FLOAT_3, vertices);
    }


    /**
     * Sets the vertex buffer of this mesh.
     *
     * @param vertices the new vertex buffer of this mesh.
     */
    public void setVertexBuffer(FloatBuffer vertices) {
        this.vertices = vertices;
    }

    @Override
    public State getState() {
        return state;
    }

    @Override
    public boolean initialize(RenderContext context, boolean force) {
        if (state == State.DISPOSED) return false;

        GL2GL3 gl = context.getDrawable().getGL().getGL2GL3();
        if (state == State.INITIALIZED || state == State.LOADED) {
            if (!force) return false;

            // dispose old
            vbo.dispose(gl);
            vao.dispose(gl);
        }

        vbo = BufferStorage.create(gl, GL2GL3.GL_ARRAY_BUFFER);
        vao = VertexArrayObject.create(gl);

        ptrPosition = VertexAttributePointer.create(attribute, datatype, vbo);

        state = State.INITIALIZED;
        return true;
    }

    @Override
    public boolean dispose(RenderContext context) {
        boolean disposable = state != State.DISPOSED && state != State.UNINITIALIZED;

        if (disposable) {
            vbo.dispose(context.getDrawable().getGL().getGL2GL3());
            state = State.DISPOSED;
        }

        for (LifeTimeObserver<Mesh> lto : ltObservers)
            lto.disposed(this);

        return disposable;
    }

    @Override
    public boolean load(RenderContext context, boolean force) {
        if (state == State.DISPOSED || state == State.UNINITIALIZED) return false;
        if (state == State.LOADED && !force) return false;

        GL2GL3 gl = context.getDrawable().getGL().getGL2GL3();

        gl.glBindBuffer(vbo.target, vbo.handle);
        gl.glBufferData(vbo.target, vertices.capacity() * 4L, vertices, usage);
        gl.glBindBuffer(vbo.target, 0);

        nVertices = vertices.capacity() / datatype.size;

        state = State.LOADED;
        return true;
    }

    @Override
    public void display(RenderContext context, ShaderProgram shader) {
        display(context, shader, mode);
    }

    @Override
    public void display(RenderContext context, ShaderProgram shader, int mode) {
        GL2GL3 gl = context.getDrawable().getGL().getGL2GL3();

        vao.bind(gl);
        vbo.bind(gl);

        ptrPosition.set(gl);
        ptrPosition.enable(gl);

        gl.glDrawArrays(mode, 0, nVertices);

        vao.unbind(gl);
    }

    @Override
    public void display(RenderContext context, VertexArrayObject vao) {
        display(context, vao, mode);
    }

    @Override
    public void display(RenderContext context, VertexArrayObject vao, int mode) {
        GL2GL3 gl = context.getDrawable().getGL().getGL2GL3();

        vao.bind(gl);
        gl.glDrawArrays(mode, 0, nVertices);
        vao.unbind(gl);
    }

    @Override
    public VertexArrayObject createVAO(RenderContext context, ShaderProgram program) {
        GL2GL3 gl = context.getDrawable().getGL().getGL2GL3();

        VertexArrayObject vao = VertexArrayObject.create(gl);

        vao.bind(gl);
        vbo.bind(gl);

        ptrPosition.set(gl);
        ptrPosition.enable(gl);

        vao.unbind(gl);

        return vao;
    }

    @Override
    public List<MeshBucket> getBuckets() {
        return Collections.unmodifiableList(buckets);
    }

    public void setBuckets(List<Bucket> buckets) {
        this.buckets = buckets;
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


    /**
     * {@code MeshBucket} implementation for the {@code SingleFloatAttributeMesh}.
     */
    public class Bucket implements MeshBucket {
        private final float zIndex;
        private final int   offset;
        private final int   count;

        /**
         * Constructs a new bucket with the given properties.
         *
         * @param zIndex the z-index of the bucket.
         * @param offset the offset (in vertices) of the data in the buffer.
         * @param count  the vertex-count of the data in the buffer.
         */
        public Bucket(float zIndex, int offset, int count) {
            this.zIndex = zIndex;
            this.offset = offset;
            this.count  = count;
        }

        @Override
        public float getZIndex() {
            return zIndex;
        }

        @Override
        public void display(RenderContext context, ShaderProgram shader) {
            GL2GL3 gl = context.getDrawable().getGL().getGL2GL3();

            vao.bind(gl);
            vbo.bind(gl);

            ptrPosition.set(gl);
            ptrPosition.enable(gl);

            gl.glDrawArrays(mode, offset, count);

            vao.unbind(gl);
        }

        @Override
        public void display(RenderContext context, VertexArrayObject vao) {
            GL2GL3 gl = context.getDrawable().getGL().getGL2GL3();

            vao.bind(gl);
            gl.glDrawArrays(mode, offset, count);
            vao.unbind(gl);
        }
    }
}
