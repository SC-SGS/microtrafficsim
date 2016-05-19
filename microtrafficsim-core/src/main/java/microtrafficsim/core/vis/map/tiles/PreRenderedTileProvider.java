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
import microtrafficsim.core.vis.opengl.shader.uniforms.UniformSampler2D;
import microtrafficsim.math.Mat4f;
import microtrafficsim.math.Rect2d;
import microtrafficsim.utils.resources.PackagedResource;
import microtrafficsim.utils.resources.Resource;

import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;


public class PreRenderedTileProvider implements TileProvider {
    // TODO: implement basic caching, re-use textures and FBOs
    // TODO: depth attachment for FBOs?

    private static final Resource TILE_COPY_SHADER_VS = new PackagedResource(PreRenderedTileProvider.class, "/shaders/tiles/tilecopy.vs");
    private static final Resource TILE_COPY_SHADER_FS = new PackagedResource(PreRenderedTileProvider.class, "/shaders/tiles/tilecopy.fs");

    private static final int TEX_UNIT_TILE = 0;

    private TileLayerProvider provider;
    private HashSet<TileChangeListener> tileListener;

    private ShaderProgram tilecopy;
    private UniformMat4f uTileCopyTransform;
    private UniformSampler2D uTileSampler;

    private TileQuad quad;


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

        uTileCopyTransform = (UniformMat4f) tilecopy.getUniform("u_tilecopy");
        uTileSampler = (UniformSampler2D) tilecopy.getUniform("u_tile_sampler");

        uTileSampler.set(TEX_UNIT_TILE);

        quad = new TileQuad();
        quad.initialize(gl);
    }

    @Override
    public void dispose(RenderContext context) {
        GL3 gl = context.getDrawable().getGL().getGL3();

        tilecopy.dispose(gl);   tilecopy = null;
        quad.dispose(gl);       quad = null;
    }


    @Override
    public Tile require(RenderContext context, TileId id) throws InterruptedException, ExecutionException {
        ArrayList<TileLayer> layers = new ArrayList<>();

        try {
            for (String name : provider.getAvailableLayers()) {
                if (Thread.interrupted())
                    throw new InterruptedException();

                TileLayer layer = provider.require(context, name, id);
                if (layer != null)
                    layers.add(layer);
            }

            if (layers.size() == 0)
                return null;

            /* Note:
             *  If tile.initialize(c) is the last method that could throw an exception,
             *  so there is no need to dispose() the tile itself.
             */
            PreRenderedTile tile = new PreRenderedTile(id, layers);
            Future<Void> task = context.addTask(c -> {
                tile.initialize(c);
                return null;
            });
            task.get();

            return tile;

        } catch (Exception e) {         // make sure we clean up on interrupts and exceptions
            for (TileLayer layer : layers)
                provider.release(context, layer);

            throw e;
        }
    }

    @Override
    public void release(RenderContext context, Tile tile) {
        if (tile instanceof PreRenderedTile)
            ((PreRenderedTile) tile).dispose(context);
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


    private class PreRenderedTile implements Tile {

        private TileId id;
        private ArrayList<TileLayer> layers;

        private Mat4f transform;

        private int texture;
        private int fbo;


        public PreRenderedTile(TileId id, ArrayList<TileLayer> layers) {
            this.id = id;
            this.layers = layers;
            this.transform = Mat4f.identity();
            this.texture = -1;
            this.fbo = -1;
        }


        @Override
        public TileId getId() {
            return id;
        }

        @Override
        public void display(RenderContext context) {
            GL3 gl = context.getDrawable().getGL().getGL3();

            tilecopy.bind(gl);
            uTileCopyTransform.set(transform);

            gl.glActiveTexture(GL3.GL_TEXTURE0 + TEX_UNIT_TILE);
            gl.glBindTexture(GL3.GL_TEXTURE_2D, texture);

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


        public void initialize(RenderContext context) {
            GL3 gl = context.getDrawable().getGL().getGL3();

            int[] obj = { -1, -1 };

            // create texture
            gl.glGenTextures(1, obj, 0);
            texture = obj[0];

            int width = 512;                                        // TODO: get width
            int height = 512;                                       // TODO: get height

            gl.glBindTexture(GL3.GL_TEXTURE_2D, texture);
            gl.glTexImage2D(GL3.GL_TEXTURE_2D, 0, GL3.GL_RGBA8, width, height, 0, GL3.GL_RGBA, GL3.GL_BYTE, null);
            gl.glTexParameteri(GL3.GL_TEXTURE_2D, GL3.GL_TEXTURE_WRAP_S, GL3.GL_CLAMP_TO_EDGE);
            gl.glTexParameteri(GL3.GL_TEXTURE_2D, GL3.GL_TEXTURE_WRAP_R, GL3.GL_CLAMP_TO_EDGE);
            gl.glTexParameteri(GL3.GL_TEXTURE_2D, GL3.GL_TEXTURE_MIN_FILTER, GL3.GL_LINEAR);
            gl.glTexParameteri(GL3.GL_TEXTURE_2D, GL3.GL_TEXTURE_MAG_FILTER, GL3.GL_LINEAR);
            gl.glBindTexture(GL3.GL_TEXTURE_2D, 0);

            // create FBO
            gl.glGenFramebuffers(1, obj, 1);
            fbo = obj[1];

            gl.glBindFramebuffer(GL3.GL_FRAMEBUFFER, fbo);
            gl.glFramebufferTexture2D(GL3.GL_FRAMEBUFFER, GL3.GL_COLOR_ATTACHMENT0, GL3.GL_TEXTURE_2D, texture, 0);
            int status = gl.glCheckFramebufferStatus(GL3.GL_FRAMEBUFFER);
            gl.glBindFramebuffer(GL3.GL_FRAMEBUFFER, 0);

            if (status != GL3.GL_FRAMEBUFFER_COMPLETE)
                throw new RuntimeException("Failed to create Framebuffer Object (status: 0x" + Integer.toHexString(status));


            // render tile to FBO
            float[] clear = { 0.f, 0.f, 0.f, .75f };                 // TODO: set alpha to 0
            gl.glBindFramebuffer(GL3.GL_FRAMEBUFFER, fbo);
            gl.glClearBufferfv(GL3.GL_COLOR, 0, clear, 0);

            // TODO: test setup
            // TODO: pre-render

            gl.glBindFramebuffer(GL3.GL_FRAMEBUFFER, 0);
        }

        public void dispose(RenderContext context) {
            GL3 gl = context.getDrawable().getGL().getGL3();

            int[] obj = { fbo, texture };
            if (fbo != -1) {
                gl.glBindFramebuffer(GL3.GL_FRAMEBUFFER, fbo);
                gl.glFramebufferTexture2D(GL3.GL_FRAMEBUFFER, GL3.GL_COLOR_ATTACHMENT0, GL3.GL_TEXTURE_2D, 0, 0);
                gl.glBindFramebuffer(GL3.GL_FRAMEBUFFER, 0);

                gl.glDeleteFramebuffers(1, obj, 0);
                fbo = -1;
            }
            if (texture != -1) {
                gl.glDeleteTextures(1, obj, 1);
                texture = -1;
            }

            for (TileLayer layer : layers) {
                layer.dispose(context);
                provider.release(context, layer);
            }
        }
    }

    private static class TileQuad {

        float[] vertices = {
                -1.f, -1.f, 0.f,    0.f, 1.f,
                 1.f, -1.f, 0.f,    1.f, 1.f,
                -1.f,  1.f, 0.f,    0.f, 0.f,
                 1.f,  1.f, 0.f,    1.f, 0.f
        };

        private VertexAttributePointer ptrPosition;
        private VertexAttributePointer ptrTexcoord;
        private VertexArrayObject vao;
        private BufferStorage vbo;


        void initialize(GL3 gl) {
            vao = VertexArrayObject.create(gl);
            vbo = BufferStorage.create(gl, GL3.GL_ARRAY_BUFFER);
            ptrPosition = VertexAttributePointer.create(VertexAttributes.POSITION3, DataTypes.FLOAT_3, vbo, 20,  0);
            ptrTexcoord = VertexAttributePointer.create(VertexAttributes.TEXCOORD2, DataTypes.FLOAT_2, vbo, 20, 12);

            // create VAO
            vao.bind(gl);
            vbo.bind(gl);
            ptrPosition.enable(gl);
            ptrPosition.set(gl);
            ptrTexcoord.enable(gl);
            ptrTexcoord.set(gl);
            vao.unbind(gl);

            // load buffer
            FloatBuffer buffer = FloatBuffer.wrap(vertices);

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
