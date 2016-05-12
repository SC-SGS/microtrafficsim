package microtrafficsim.core.vis.map.tiles;

import com.jogamp.opengl.GL3;
import microtrafficsim.core.map.Bounds;
import microtrafficsim.core.map.tiles.TileId;
import microtrafficsim.core.map.tiles.TilingScheme;
import microtrafficsim.core.vis.context.RenderContext;
import microtrafficsim.core.vis.map.projections.Projection;
import microtrafficsim.core.vis.map.tiles.layers.TileLayer;
import microtrafficsim.core.vis.map.tiles.layers.TileLayerProvider;
import microtrafficsim.core.vis.opengl.BufferStorage;
import microtrafficsim.core.vis.opengl.DataTypes;
import microtrafficsim.core.vis.opengl.shader.Shader;
import microtrafficsim.core.vis.opengl.shader.ShaderProgram;
import microtrafficsim.core.vis.opengl.shader.attributes.VertexArrayObject;
import microtrafficsim.core.vis.opengl.shader.attributes.VertexAttributePointer;
import microtrafficsim.core.vis.opengl.shader.attributes.VertexAttributes;
import microtrafficsim.core.vis.opengl.shader.uniforms.UniformMat4f;
import microtrafficsim.math.Mat4f;
import microtrafficsim.math.Rect2d;
import microtrafficsim.utils.resources.PackagedResource;
import microtrafficsim.utils.resources.Resource;

import java.nio.FloatBuffer;
import java.util.HashSet;


public class PreRenderedTileProvider implements TileProvider {
    // TODO: sync load tile and pre-render, handle init/dispose etc.
    // TODO: implement basic caching / resource re-using

    private static final Resource TILE_COPY_SHADER_VS = new PackagedResource(PreRenderedTileProvider.class, "/shaders/tile.vs");
    private static final Resource TILE_COPY_SHADER_FS = new PackagedResource(PreRenderedTileProvider.class, "/shaders/tile.fs");

    private TileLayerProvider provider;
    private HashSet<TileChangeListener> tileListener;

    private ShaderProgram tilecopy;
    private UniformMat4f tiletransform;
    private Quad quad;


    public PreRenderedTileProvider() {
        this(null);
    }

    public PreRenderedTileProvider(TileLayerProvider provider) {
        this.provider = provider;
        this.tileListener = new HashSet<>();
    }


    @Override
    public Bounds getBounds() {
        return provider != null ? provider.getBounds() : null;
    }

    @Override
    public Rect2d getProjectedBounds() {
        return provider != null ? provider.getProjectedBounds() : null;
    }

    @Override
    public Projection getProjection() {
        return provider != null ? provider.getProjection() : null;
    }

    @Override
    public TilingScheme getTilingScheme() {
        return provider != null ? provider.getTilingScheme() : null;
    }


    @Override
    public void initialize(RenderContext context) {
        tiletransform = (UniformMat4f) context.getUniformManager().getGlobalUniform("u_tile");

        GL3 gl = context.getDrawable().getGL().getGL3();

        Shader vs = Shader.create(gl, GL3.GL_VERTEX_SHADER, "tilecopy.vs")
                .loadFromResource(TILE_COPY_SHADER_VS)
                .compile(gl);

        Shader fs = Shader.create(gl, GL3.GL_FRAGMENT_SHADER, "tilecopy.fs")
                .loadFromResource(TILE_COPY_SHADER_FS)
                .compile(gl);

        tilecopy = ShaderProgram.create(gl, context, "tilecopy")
            .attach(gl, vs, fs)
            .link(gl)
            .detach(gl, vs, fs);

        vs.dispose(gl);
        fs.dispose(gl);

        // TODO

        // TEMPORARY
        quad = new Quad();
        quad.initialize(gl);
    }

    @Override
    public void dispose(RenderContext context) {
        GL3 gl = context.getDrawable().getGL().getGL3();

        tilecopy.dispose(gl);

        // TODO

        // TEMPORARY
        quad.dispose(gl);
        quad = null;
    }


    @Override
    public Tile require(RenderContext context, TileId tile) {
        // TODO
        // NOTE: make exception-safe (interrupts)

        // TEMPORARY
        boolean tmp = false;
        for (String name : provider.getAvailableLayers()) {
            TileLayer layer = provider.require(context, name, tile);
            tmp |= (layer != null);
            provider.release(context, layer);
        }

        if (tmp)
            return new TileDummy(tile);
        else
            return null;
    }

    @Override
    public void release(RenderContext context, Tile tile) {
        // TODO
    }


    @Override
    public boolean addTileChangeListener(TileChangeListener listener) {
        return tileListener.add(listener);
    }

    @Override
    public boolean removeTileChangeListener(TileChangeListener listener) {
        return tileListener.remove(listener);
    }

    @Override
    public boolean hasTileChangeListener(TileChangeListener listener) {
        return tileListener.contains(listener);
    }


    // TEMPORARY
    private class TileDummy implements Tile {

        private TileId id;
        private Mat4f transform;

        public TileDummy(TileId id) {
            this.id = id;
            this.transform = Mat4f.identity().scale(128, 128, 1);
        }


        @Override
        public TileId getId() {
            return id;
        }

        @Override
        public void display(RenderContext context) {
            GL3 gl = context.getDrawable().getGL().getGL3();

            tilecopy.bind(gl);
            tiletransform.set(transform);
            quad.draw(gl);
        }

        @Override
        public void setTransformation(Mat4f m) {
            this.transform = m;
        }

        @Override
        public Mat4f getTransformation() {
            return transform;
        }
    }

    private static class Quad {

        float[] vertices = {
                -1.f, -1.f, 0.f,
                 1.f, -1.f, 0.f,
                -1.f,  1.f, 0.f,
                 1.f,  1.f, 0.f
        };

        private VertexAttributePointer ptrPosition;
        private VertexArrayObject vao;
        private BufferStorage vbo;


        void initialize(GL3 gl) {
            FloatBuffer buffer = FloatBuffer.wrap(vertices);

            vao = VertexArrayObject.create(gl);
            vbo = BufferStorage.create(gl, GL3.GL_ARRAY_BUFFER);
            ptrPosition = VertexAttributePointer.create(VertexAttributes.POSITION3, DataTypes.FLOAT_3, vbo, 12, 0);

            // create VAO
            vao.bind(gl);
            vbo.bind(gl);
            ptrPosition.enable(gl);
            ptrPosition.set(gl);
            vao.unbind(gl);

            // load buffer
            vbo.bind(gl);
            gl.glBufferData(vbo.target, buffer.capacity() * 4, buffer, GL3.GL_STATIC_DRAW);
            vbo.unbind(gl);
        }

        void dispose(GL3 gl) {
            vao.dispose(gl);
            vbo.dispose(gl);
        }

        void draw(GL3 gl) {
            vao.bind(gl);
            gl.glDrawArrays(GL3.GL_TRIANGLE_STRIP, 0, 4);
            vao.unbind(gl);
        }
    }
}
