package microtrafficsim.core.vis.map.tiles;

import com.jogamp.opengl.GL3;
import microtrafficsim.core.map.Bounds;
import microtrafficsim.core.map.tiles.TileId;
import microtrafficsim.core.map.tiles.TilingScheme;
import microtrafficsim.core.vis.context.RenderContext;
import microtrafficsim.core.vis.map.projections.Projection;
import microtrafficsim.core.vis.map.tiles.layers.TileLayer;
import microtrafficsim.core.vis.map.tiles.layers.TileLayerBucket;
import microtrafficsim.core.vis.map.tiles.layers.TileLayerProvider;
import microtrafficsim.core.vis.opengl.BufferStorage;
import microtrafficsim.core.vis.opengl.DataTypes;
import microtrafficsim.core.vis.opengl.shader.Shader;
import microtrafficsim.core.vis.opengl.shader.ShaderProgram;
import microtrafficsim.core.vis.opengl.shader.attributes.VertexArrayObject;
import microtrafficsim.core.vis.opengl.shader.attributes.VertexAttributePointer;
import microtrafficsim.core.vis.opengl.shader.attributes.VertexAttributes;
import microtrafficsim.core.vis.opengl.shader.uniforms.Uniform1f;
import microtrafficsim.core.vis.opengl.shader.uniforms.UniformMat4f;
import microtrafficsim.core.vis.opengl.shader.uniforms.UniformSampler2D;
import microtrafficsim.core.vis.opengl.shader.uniforms.UniformVec4f;
import microtrafficsim.math.Mat4f;
import microtrafficsim.math.Rect2d;
import microtrafficsim.math.Vec4f;
import microtrafficsim.utils.resources.PackagedResource;
import microtrafficsim.utils.resources.Resource;

import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;


public class PreRenderedTileProvider implements TileProvider {

    private static int count = 0;
    // TODO: implement basic caching, re-use textures and FBOs
    // TODO: depth attachment for FBOs?

    private static final Rect2d TILE_TARGET = new Rect2d(-1.0, -1.0, 1.0, 1.0);

    private static final Resource TILE_COPY_SHADER_VS = new PackagedResource(PreRenderedTileProvider.class, "/shaders/tiles/tilecopy.vs");
    private static final Resource TILE_COPY_SHADER_FS = new PackagedResource(PreRenderedTileProvider.class, "/shaders/tiles/tilecopy.fs");

    private static final int TEX_UNIT_TILE = 0;

    private static final BucketComparator CMP_BUCKET = new BucketComparator();

    private TileLayerProvider provider;
    private HashSet<TileChangeListener> tileListener;

    private ShaderProgram tilecopy;
    private UniformMat4f uTileCopyTransform;
    private UniformSampler2D uTileSampler;

    private UniformMat4f uProjection;
    private UniformMat4f uView;
    private Uniform1f uViewScale;
    private UniformVec4f uViewport;

    private TileQuad quad;

    // -- TEMPORARY -----------------------------------------------------------
    private ShaderProgram tilepjt;
    private static final Resource TILE_PJT_SHADER_VS = new PackagedResource(PreRenderedTileProvider.class, "/shaders/tile_projection_test.vs");
    private static final Resource TILE_PJT_SHADER_FS = new PackagedResource(PreRenderedTileProvider.class, "/shaders/tile_projection_test.fs");
    // ------------------------------------------------------------------------


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
        uProjection = (UniformMat4f) context.getUniformManager().getGlobalUniform("u_projection");
        uView = (UniformMat4f) context.getUniformManager().getGlobalUniform("u_view");
        uViewScale = (Uniform1f) context.getUniformManager().getGlobalUniform("u_viewscale");
        uViewport = (UniformVec4f) context.getUniformManager().getGlobalUniform("u_viewport");

        uTileSampler.set(TEX_UNIT_TILE);

        quad = new TileQuad();
        quad.initialize(gl);


        // -- TEMPORARY -------------------------------------------------------
        vs = Shader.create(gl, GL3.GL_VERTEX_SHADER, "tilepjt.vs")
                .loadFromResource(TILE_PJT_SHADER_VS)
                .compile(gl);

        fs = Shader.create(gl, GL3.GL_FRAGMENT_SHADER, "tilepjt.fs")
                .loadFromResource(TILE_PJT_SHADER_FS)
                .compile(gl);

        tilepjt = ShaderProgram.create(gl, context, "tilepjt")
                .attach(gl, vs, fs)
                .link(gl)
                .detach(gl, vs, fs);

        vs.dispose(gl);
        fs.dispose(gl);
        // --------------------------------------------------------------------
    }

    @Override
    public void dispose(RenderContext context) {
        GL3 gl = context.getDrawable().getGL().getGL3();

        tilecopy.dispose(gl);   tilecopy = null;
        quad.dispose(gl);       quad = null;

        // -- TEMPORARY -------------------------------------------------------
        tilepjt.dispose(gl);    tilepjt = null;
        // --------------------------------------------------------------------
    }


    @Override
    public Tile require(RenderContext context, TileId id) throws InterruptedException, ExecutionException {
        ArrayList<TileLayerBucket> buckets = new ArrayList<>();

        try {
            ArrayList<TileLayer> layers = new ArrayList<>();

            for (String name : provider.getAvailableLayers()) {
                if (Thread.interrupted())
                    throw new InterruptedException();

                TileLayer layer = provider.require(context, name, id, TILE_TARGET);
                if (layer != null) {
                    layers.add(layer);
                    buckets.addAll(layer.getBuckets());
                }
            }

            if (buckets.size() == 0) {
                for (TileLayer layer : layers)
                    layer.dispose(context);
                return null;
            }

            buckets.sort(CMP_BUCKET);

            /* Note:
             *  If tile.initialize(c) is the last method that could throw an exception,
             *  so there is no need to dispose() the tile itself.
             */
            PreRenderedTile tile = new PreRenderedTile(id, buckets);
            Future<Void> task = context.addTask(c -> {
                tile.initialize(c);
                return null;
            });

            boolean interrupted = false;
            while (true) {
                try {
                    task.get();
                    break;
                } catch (InterruptedException iex) {    // try cancel task on first interrupt, ignore others
                    if (!interrupted) {
                        task.cancel(true);              // if cancel is successful, task.get() will throw a
                        interrupted = true;             // CancellationException, otherwise the task will complete
                    }
                }
            }

            return tile;

        } catch (Exception e) {         // make sure we clean up on interrupts and exceptions
            HashSet<TileLayer> layers = new HashSet<>();
            for (TileLayerBucket bucket : buckets)
                layers.add(bucket.layer);

            for (TileLayer layer : layers)
                provider.release(context, layer);

            if (e instanceof CancellationException)
                throw new InterruptedException();
            else
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
        private ArrayList<TileLayerBucket> buckets;

        private Mat4f transform;

        private int texture;
        private int fbo;


        public PreRenderedTile(TileId id, ArrayList<TileLayerBucket> buckets) {
            this.id = id;
            this.buckets = buckets;
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


        public void initialize(RenderContext context) throws CancellationException {
            GL3 gl = context.getDrawable().getGL().getGL3();

            // save old view parameter
            Mat4f oldView = new Mat4f(uView.get());
            Mat4f oldProjection = new Mat4f(uProjection.get());
            Vec4f oldViewport = new Vec4f(uViewport.get());
            float oldViewScale = uViewScale.get();

            HashSet<TileLayer> layers = new HashSet<>();
            for (TileLayerBucket bucket : buckets)
                if (bucket.layer.getLayer().isEnabled())
                    layers.add(bucket.layer);

            try {
                // -- initialize layers --
                for (TileLayer layer : layers) {
                    if (Thread.interrupted())
                        throw new InterruptedException();

                    layer.initialize(context);
                }

                // -- create render buffers --
                int[] obj = {-1, -1};

                // create texture
                gl.glGenTextures(1, obj, 0);
                texture = obj[0];

                int width = 512;                                            // TODO: get width
                int height = 512;                                           // TODO: get height

                gl.glBindTexture(GL3.GL_TEXTURE_2D, texture);
                gl.glTexImage2D(GL3.GL_TEXTURE_2D, 0, GL3.GL_RGBA8, width, height, 0, GL3.GL_RGBA, GL3.GL_BYTE, null);
                gl.glTexParameteri(GL3.GL_TEXTURE_2D, GL3.GL_TEXTURE_WRAP_S, GL3.GL_CLAMP_TO_EDGE);
                gl.glTexParameteri(GL3.GL_TEXTURE_2D, GL3.GL_TEXTURE_WRAP_T, GL3.GL_CLAMP_TO_EDGE);
                gl.glTexParameteri(GL3.GL_TEXTURE_2D, GL3.GL_TEXTURE_MIN_FILTER, GL3.GL_LINEAR);
                gl.glTexParameteri(GL3.GL_TEXTURE_2D, GL3.GL_TEXTURE_MAG_FILTER, GL3.GL_LINEAR);
                gl.glBindTexture(GL3.GL_TEXTURE_2D, 0);

                // create FBO
                gl.glGenFramebuffers(1, obj, 1);
                fbo = obj[1];

                count++;
                System.err.println(count);

                gl.glBindFramebuffer(GL3.GL_FRAMEBUFFER, fbo);
                gl.glFramebufferTexture2D(GL3.GL_FRAMEBUFFER, GL3.GL_COLOR_ATTACHMENT0, GL3.GL_TEXTURE_2D, texture, 0);
                int status = gl.glCheckFramebufferStatus(GL3.GL_FRAMEBUFFER);
                gl.glBindFramebuffer(GL3.GL_FRAMEBUFFER, 0);

                if (status != GL3.GL_FRAMEBUFFER_COMPLETE)
                    throw new RuntimeException("Failed to create Framebuffer Object (status: 0x" + Integer.toHexString(status));

                // -- render tile --

                // render tile to FBO
                float[] clear = {0.f, 0.f, 0.f, 0.f};
                gl.glBindFramebuffer(GL3.GL_FRAMEBUFFER, fbo);
                gl.glClearBufferfv(GL3.GL_COLOR, 0, clear, 0);
                gl.glViewport(0, 0, width, height);                         // TODO: context state

                context.BlendMode.enable(gl);
                context.BlendMode.setEquation(gl, GL3.GL_FUNC_ADD);
                context.BlendMode.setFactors(gl, GL3.GL_SRC_ALPHA, GL3.GL_ONE_MINUS_SRC_ALPHA, GL3.GL_ONE, GL3.GL_ONE_MINUS_SRC_ALPHA);

                uView.set(Mat4f.identity());
                uProjection.set(Mat4f.identity().scale(1.f, 1.f, 0.1f));    // allow layer values for -10 to 10
                uViewScale.set((float) Math.pow(2.0, id.z));
                uViewport.set(width, height, 1.0f / width, 1.0f / height);

                for (TileLayerBucket bucket : buckets) {
                    if (Thread.interrupted())
                        throw new InterruptedException();

                    if (bucket.layer.getLayer().isEnabled())
                        bucket.display(context);
                }

            } catch (InterruptedException e) {
                dispose(context);
                throw new CancellationException();  // cancel the executing task
            } finally {
                gl.glBindFramebuffer(GL3.GL_FRAMEBUFFER, 0);

                // reset old view parameter
                uView.set(oldView);
                uProjection.set(oldProjection);
                uViewport.set(oldViewport);
                uViewScale.set(oldViewScale);
            }
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

                count--;
                System.err.println(count);
            }
            if (texture != -1) {
                gl.glDeleteTextures(1, obj, 1);
                texture = -1;
            }

            HashSet<TileLayer> layers = new HashSet<>();
            for (TileLayerBucket bucket : buckets)
                layers.add(bucket.layer);

            for (TileLayer layer : layers) {
                layer.dispose(context);
                provider.release(context, layer);
            }

            buckets.clear();
        }
    }

    private static class TileQuad {

        float[] vertices = {
                -1.0f, -1.0f, 0.f,    0.f, 0.f,
                 1.0f, -1.0f, 0.f,    1.f, 0.f,
                -1.0f,  1.0f, 0.f,    0.f, 1.f,
                 1.0f,  1.0f, 0.f,    1.f, 1.f
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

    private static class BucketComparator implements Comparator<TileLayerBucket> {

        @Override
        public int compare(TileLayerBucket a, TileLayerBucket b) {
            if (a.zIndex > b.zIndex)
                return 1;
            else if (a.zIndex < b.zIndex)
                return -1;

            if (a.layer.getLayer().getIndex() > b.layer.getLayer().getIndex())
                return 1;
            else if (a.layer.getLayer().getIndex() < b.layer.getLayer().getIndex())
                return -1;
            else
                return 0;
        }
    }
}
