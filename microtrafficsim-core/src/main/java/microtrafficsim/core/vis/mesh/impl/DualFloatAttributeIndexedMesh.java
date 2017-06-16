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
 * Index-based mesh containing a single (multi-component) single-precision floating-point vertex attribute.
 *
 * @author Maximilian Luz
 */
public class DualFloatAttributeIndexedMesh implements Mesh {
    private State state;

    private int usage;
    private int mode;
    private VertexAttribute attrib1;
    private VertexAttribute attrib2;
    private DataType datatype1;
    private DataType datatype2;

    private FloatBuffer vertices;
    private IntBuffer   indices;

    private BufferStorage     vbo;
    private BufferStorage     ibo;
    private VertexArrayObject vao;    // from OpenGL 3.1 upwards a VAO is always required for drawing

    private VertexAttributePointer ptrAttrib1;
    private VertexAttributePointer ptrAttrib2;

    private List<Bucket> buckets;

    private HashSet<LifeTimeObserver<Mesh>> ltObservers;


    /**
     * Creates a new mesh with the given properties.
     *
     * @param usage     the OpenGL usage of the mesh.
     * @param mode      the OpenGL draw-mode of the mesh.
     * @param attrib1   the first attribute stored in the vertex-buffer of this mesh, must be a float compatible attribute.
     * @param attrib2   the second attribute stored in the vertex-buffer of this mesh, must be a float compatible attribute.
     * @param datatype1 the OpenGL data-type of the first attribute to be drawn, must be a single-precision floating-point type
     * @param datatype2 the OpenGL data-type of the second attribute to be drawn, must be a single-precision floating-point type
     *                  compatible with {@code attribute}.
     * @param vertices  the vertex buffer.
     * @param indices   the index buffer.
     */
    public DualFloatAttributeIndexedMesh(int usage, int mode, VertexAttribute attrib1, VertexAttribute attrib2,
                                         DataType datatype1, DataType datatype2, FloatBuffer vertices, IntBuffer indices)
    {
        this.state = State.UNINITIALIZED;

        this.usage = usage;
        this.mode  = mode;
        this.attrib1 = attrib1;
        this.attrib2 = attrib2;
        this.datatype1 = datatype1;
        this.datatype2 = datatype2;

        this.vertices = vertices;
        this.indices  = indices;

        this.vbo = null;
        this.ibo = null;

        this.ltObservers = new HashSet<>();
    }

    /**
     * Creates a new mesh with the given properties, using {@code VertexAttributes.POSITION3} as attribute and
     * {@code DataTypes.FLOAT_3} as data-type.
     *
     * @param usage     the OpenGL usage of the mesh.
     * @param mode      the OpenGL draw-mode of the mesh.
     * @param vertices  the vertex buffer.
     * @param indices   the index buffer.
     * @return the new mesh.
     */
    public static DualFloatAttributeIndexedMesh newPos3LineMesh(int usage, int mode, FloatBuffer vertices, IntBuffer indices) {
        return new DualFloatAttributeIndexedMesh(
                usage, mode,
                VertexAttributes.POSITION3,
                VertexAttributes.LINE,
                DataTypes.FLOAT_3,
                DataTypes.FLOAT_3,
                vertices, indices);
    }


    /**
     * Sets the vertex buffer of this mesh.
     *
     * @param vertices the new vertex buffer of this mesh.
     */
    public void setVertexBuffer(FloatBuffer vertices) {
        this.vertices = vertices;
    }

    /**
     * Sets the index buffer of this mesh.
     *
     * @param indices the new index buffer of this mesh.
     */
    public void setIndexBuffer(IntBuffer indices) {
        this.indices = indices;
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
            ibo.dispose(gl);
            vao.dispose(gl);
        }

        vbo = BufferStorage.create(gl, GL2GL3.GL_ARRAY_BUFFER);
        ibo = BufferStorage.create(gl, GL2GL3.GL_ELEMENT_ARRAY_BUFFER);
        vao = VertexArrayObject.create(gl);

        final int len = (datatype1.size + datatype2.size) * 4;
        final int offs1 = datatype1.size * 4;
        ptrAttrib1 = VertexAttributePointer.create(attrib1, datatype1, vbo, len, 0);
        ptrAttrib2 = VertexAttributePointer.create(attrib2, datatype2, vbo, len, offs1);

        state = State.INITIALIZED;
        return true;
    }

    @Override
    public boolean dispose(RenderContext context) {
        boolean disposable = state != State.DISPOSED && state != State.UNINITIALIZED;

        if (disposable) {
            GL2GL3 gl = context.getDrawable().getGL().getGL2GL3();

            vbo.dispose(gl);
            ibo.dispose(gl);

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

        // vertices
        gl.glBindBuffer(vbo.target, vbo.handle);
        gl.glBufferData(vbo.target, vertices.capacity() * 4L, vertices, usage);
        gl.glBindBuffer(vbo.target, 0);

        // indices
        gl.glBindBuffer(ibo.target, ibo.handle);
        gl.glBufferData(ibo.target, indices.capacity() * 4L, indices, usage);
        gl.glBindBuffer(ibo.target, 0);

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
        ibo.bind(gl);

        ptrAttrib1.set(gl);
        ptrAttrib1.enable(gl);

        ptrAttrib2.set(gl);
        ptrAttrib2.enable(gl);

        gl.glDrawElements(mode, indices.capacity(), GL2GL3.GL_UNSIGNED_INT, 0);

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
        gl.glDrawElements(mode, indices.capacity(), GL2GL3.GL_UNSIGNED_INT, 0);
        vao.unbind(gl);
    }

    @Override
    public VertexArrayObject createVAO(RenderContext context, ShaderProgram program) {
        GL2GL3 gl = context.getDrawable().getGL().getGL2GL3();

        VertexArrayObject vao = VertexArrayObject.create(gl);

        vao.bind(gl);
        vbo.bind(gl);
        ibo.bind(gl);

        ptrAttrib1.set(gl);
        ptrAttrib1.enable(gl);

        ptrAttrib2.set(gl);
        ptrAttrib2.enable(gl);

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
     * {@code MeshBucket} implementation for the {@code SingleFloatAttributeIndexedMesh}.
     */
    public class Bucket implements MeshBucket {
        private final float zIndex;
        private final int   offset;
        private final int   count;

        /**
         * Constructs a new bucket with the given properties.
         *
         * @param zIndex the z-index of the bucket.
         * @param offset the offset (in floats) of the data in the buffer.
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
            ibo.bind(gl);

            ptrAttrib1.set(gl);
            ptrAttrib1.enable(gl);

            ptrAttrib2.set(gl);
            ptrAttrib2.enable(gl);

            gl.glDrawElements(mode, count, GL2GL3.GL_UNSIGNED_INT, offset * 4);

            vao.unbind(gl);
        }

        @Override
        public void display(RenderContext context, VertexArrayObject vao) {
            GL2GL3 gl = context.getDrawable().getGL().getGL2GL3();

            vao.bind(gl);
            gl.glDrawElements(mode, count, GL2GL3.GL_UNSIGNED_INT, offset * 4);
            vao.unbind(gl);
        }
    }
}
