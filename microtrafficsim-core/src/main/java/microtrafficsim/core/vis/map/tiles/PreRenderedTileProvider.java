package microtrafficsim.core.vis.map.tiles;

import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL3;
import microtrafficsim.core.map.Bounds;
import microtrafficsim.core.map.style.StyleSheet;
import microtrafficsim.core.map.tiles.TileId;
import microtrafficsim.core.map.tiles.TilingScheme;
import microtrafficsim.core.vis.context.RenderContext;
import microtrafficsim.core.vis.context.tasks.RenderTask;
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
import microtrafficsim.core.vis.opengl.utils.Color;
import microtrafficsim.math.*;
import microtrafficsim.utils.resources.PackagedResource;
import microtrafficsim.utils.resources.Resource;

import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;


public class PreRenderedTileProvider implements TileProvider {

    // TODO: reload only layer on layer-change
    // TODO: refactor layer status control (enabled/disabled), add observers.
    //          redraw vs. reload on status change --> reload/unload only specific layer

    private static final Rect2d TILE_TARGET = new Rect2d(-1.0, -1.0, 1.0, 1.0);

    private static final Resource TILE_COPY_SHADER_VS = new PackagedResource(PreRenderedTileProvider.class, "/shaders/tiles/tilecopy.vs");
    private static final Resource TILE_COPY_SHADER_FS = new PackagedResource(PreRenderedTileProvider.class, "/shaders/tiles/tilecopy.fs");

    private static final int TEX_UNIT_TILE_COLOR = 0;
    private static final int TEX_UNIT_TILE_DEPTH = 1;

    private static final BucketComparator CMP_BUCKET = new BucketComparator();

    private TileLayerProvider provider;
    private HashSet<TileChangeListener> tileListener;
    private TileLayerProvider.LayerChangeListener layerListener;

    private Color bgcolor = Color.fromRGBA(0x00000000);

    private ShaderProgram tilecopy;
    private UniformMat4f uTileCopyTransform;

    private UniformMat4f uProjection;
    private UniformMat4f uView;
    private Uniform1f uViewScale;
    private UniformVec4f uViewport;

    private TileBufferPool pool;
    private TileQuad quad;

    public PreRenderedTileProvider(TileLayerProvider provider) {
        this(provider, getDefaultTileBufferPoolSizeFrom(new Vec2i(2560, 1440),
                provider.getTilingScheme().getTileSize(), 0));
    }

    public PreRenderedTileProvider(TileLayerProvider provider, int targetBufferPoolSize) {
        this.layerListener = new LayerChangeListenerImpl();

        this.provider = provider;
        this.tileListener = new HashSet<>();
        this.provider.addLayerChangeListener(layerListener);
        this.pool = new TileBufferPool(provider.getTilingScheme().getTileSize() , targetBufferPoolSize);
    }

    private static int getDefaultTileBufferPoolSizeFrom(Vec2i display, Vec2i tile, int additional) {
        // maximum number of tiles present on the screen + additional
        return 4 * (display.x / tile.x + 1) * (display.y / tile.y + 1) + additional;
    }


    @Override
    public Bounds getBounds() {
        return provider.getBounds();
    }

    @Override
    public Rect2d getProjectedBounds() {
        return provider.getProjectedBounds();
    }

    @Override
    public Projection getProjection() {
        return provider.getProjection();
    }

    @Override
    public TilingScheme getTilingScheme() {
        return provider.getTilingScheme();
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
        uProjection = (UniformMat4f) context.getUniformManager().getGlobalUniform("u_projection");
        uView = (UniformMat4f) context.getUniformManager().getGlobalUniform("u_view");
        uViewScale = (Uniform1f) context.getUniformManager().getGlobalUniform("u_viewscale");
        uViewport = (UniformVec4f) context.getUniformManager().getGlobalUniform("u_viewport");

        UniformSampler2D uColorSampler = (UniformSampler2D) tilecopy.getUniform("u_color_sampler");
        UniformSampler2D uDepthSampler = (UniformSampler2D) tilecopy.getUniform("u_depth_sampler");
        uColorSampler.set(TEX_UNIT_TILE_COLOR);
        uDepthSampler.set(TEX_UNIT_TILE_DEPTH);

        quad = new TileQuad();
        quad.initialize(gl);
    }

    @Override
    public void dispose(RenderContext context) {
        pool.dispose(context);

        GL3 gl = context.getDrawable().getGL().getGL3();
        tilecopy.dispose(gl);   tilecopy = null;
        quad.dispose(gl);       quad = null;
    }


    @Override
    public void beforeRendering(RenderContext context) {
        GL gl = context.getDrawable().getGL();

        context.BlendMode.enable(gl);
        context.BlendMode.setEquation(gl, GL3.GL_FUNC_ADD);
        context.BlendMode.setFactors(gl, GL3.GL_ONE, GL3.GL_ONE_MINUS_SRC_ALPHA);
    }

    @Override
    public void afterRendering(RenderContext context) {}

    @Override
    public Tile require(RenderContext context, TileId id) throws CancellationException, ExecutionException {
        ArrayList<TileLayerBucket> buckets = new ArrayList<>();
        PreRenderedTile tile;
        Future<Void> task;

        try {
            ArrayList<TileLayer> layers = new ArrayList<>();

            for (String name : provider.getAvailableLayers(id)) {
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
             *  tile.initialize(c) will clean up on interrupts etc. It either returns
             *  using a CancellationException or exit successfully.
             */
            tile = new PreRenderedTile(id, buckets);
            task = context.addTask(c -> {
                tile.initialize(c);
                return null;
            });

        // make sure we clean up on interrupts
        } catch (InterruptedException e){
            HashSet<TileLayer> layers = new HashSet<>();
            for (TileLayerBucket bucket : buckets)
                layers.add(bucket.layer);

            for (TileLayer layer : layers)
                provider.release(context, layer);

            throw new CancellationException();      // cancel the task
        }

        try {                                       // try to wait for the task
            task.get();
        } catch (InterruptedException iex) {        // on interrupt: try cancel, cancel this task, async cleanup
            task.cancel(true);
            context.addTask(new TileCleanupTask(task, tile));
            throw new CancellationException();
        }

        return tile;
    }

    @Override
    public void release(RenderContext context, Tile tile) {
        if (tile instanceof PreRenderedTile)
            ((PreRenderedTile) tile).dispose(context);
    }


    @Override
    public void apply(StyleSheet style) {
        bgcolor = style.getTileBackgroundColor();
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
        private TileBuffer buffer;

        PreRenderedTile(TileId id, ArrayList<TileLayerBucket> buckets) {
            this.id = id;
            this.buckets = buckets;
            this.transform = Mat4f.identity();
            this.buffer = null;
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

            gl.glActiveTexture(GL3.GL_TEXTURE0 + TEX_UNIT_TILE_COLOR);
            gl.glBindTexture(GL3.GL_TEXTURE_2D, buffer.color);

            gl.glActiveTexture(GL3.GL_TEXTURE0 + TEX_UNIT_TILE_DEPTH);
            gl.glBindTexture(GL3.GL_TEXTURE_2D, buffer.depth);

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
            Mat4f oldUView = new Mat4f(uView.get());
            Mat4f oldUProjection = new Mat4f(uProjection.get());
            Vec4f oldUViewport = new Vec4f(uViewport.get());
            float oldUViewScale = uViewScale.get();
            Rect2i oldViewport = context.Viewport.get();

            HashSet<TileLayer> layers = new HashSet<>();
            for (TileLayerBucket bucket : buckets)
                if (bucket.layer.getLayer().isEnabled())
                    layers.add(bucket.layer);

            try {
                // initialize layers
                for (TileLayer layer : layers) {
                    if (Thread.interrupted()) throw new CancellationException();
                    layer.initialize(context);
                }

                // create render buffer
                buffer = pool.require(context);

                // render tile to FBO
                gl.glBindFramebuffer(GL3.GL_FRAMEBUFFER, buffer.fbo);
                gl.glClearBufferfv(GL3.GL_COLOR, 0, new float[]{bgcolor.r, bgcolor.g, bgcolor.b, bgcolor.a}, 0);
                gl.glClearBufferfv(GL3.GL_DEPTH, 0, new float[]{ 0.f }, 0);

                context.Viewport.set(gl, 0, 0, pool.width, pool.height);

                context.BlendMode.enable(gl);
                context.BlendMode.setEquation(gl, GL3.GL_FUNC_ADD);
                context.BlendMode.setFactors(gl, GL3.GL_SRC_ALPHA, GL3.GL_ONE_MINUS_SRC_ALPHA, GL3.GL_ONE, GL3.GL_ONE_MINUS_SRC_ALPHA);

                context.DepthTest.enable(gl);
                context.DepthTest.setFunction(gl, GL3.GL_GEQUAL);

                uProjection.set(Mat4f.identity());
                uViewScale.set((float) Math.pow(2.0, id.z));
                uViewport.set(pool.width, pool.height, 1.0f / pool.width, 1.0f / pool.height);

                for (TileLayerBucket bucket : buckets) {
                    if (Thread.interrupted()) throw new CancellationException();

                    if (bucket.layer.getLayer().isEnabled()) {
                        uView.set(bucket.layer.getTransform());
                        bucket.display(context);
                    }
                }

            } catch (CancellationException e) {
                dispose(context);
                throw e;            // cancel the executing task
            } finally {
                gl.glBindFramebuffer(GL3.GL_FRAMEBUFFER, 0);

                // reset old view parameter
                context.Viewport.set(gl, oldViewport);
                uView.set(oldUView);
                uProjection.set(oldUProjection);
                uViewport.set(oldUViewport);
                uViewScale.set(oldUViewScale);
            }
        }

        public void dispose(RenderContext context) {
            pool.release(context, buffer);
            buffer = null;

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

    private class TileCleanupTask implements RenderTask<Void> {

        private final Future<Void> task;
        private final Tile tile;

        TileCleanupTask(Future<Void> task, Tile tile) {
            this.task = task;
            this.tile = tile;
        }

        @Override
        public Void execute(RenderContext context) throws Exception {
            if (!task.isDone()) {               // if task is still running, try again
                context.addTask(this, true);
                return null;
            }

            if (!task.isCancelled())            // if task was successful, release the tile
                release(context, tile);

            return null;
        }
    }

    private static class TileBuffer {
        private TileBuffer() {}

        int fbo = -1;
        int color = -1;
        int depth = -1;

        static TileBuffer create(RenderContext context, int width, int height) {
            TileBuffer buffer = new TileBuffer();

            GL3 gl = context.getDrawable().getGL().getGL3();
            int[] obj = {-1, -1, -1};

            // create textures
            gl.glGenTextures(2, obj, 0);
            buffer.color = obj[0];
            buffer.depth = obj[1];

            gl.glBindTexture(GL3.GL_TEXTURE_2D, buffer.color);
            gl.glTexParameteri(GL3.GL_TEXTURE_2D, GL3.GL_TEXTURE_WRAP_S, GL3.GL_CLAMP_TO_EDGE);
            gl.glTexParameteri(GL3.GL_TEXTURE_2D, GL3.GL_TEXTURE_WRAP_T, GL3.GL_CLAMP_TO_EDGE);
            gl.glTexParameteri(GL3.GL_TEXTURE_2D, GL3.GL_TEXTURE_MIN_FILTER, GL3.GL_LINEAR);
            gl.glTexParameteri(GL3.GL_TEXTURE_2D, GL3.GL_TEXTURE_MAG_FILTER, GL3.GL_LINEAR);
            gl.glTexImage2D(GL3.GL_TEXTURE_2D, 0, GL3.GL_RGBA8, width, height, 0, GL3.GL_RGBA, GL3.GL_BYTE, null);
            gl.glBindTexture(GL3.GL_TEXTURE_2D, 0);

            gl.glBindTexture(GL3.GL_TEXTURE_2D, buffer.depth);
            gl.glTexParameteri(GL3.GL_TEXTURE_2D, GL3.GL_TEXTURE_WRAP_S, GL3.GL_CLAMP_TO_EDGE);
            gl.glTexParameteri(GL3.GL_TEXTURE_2D, GL3.GL_TEXTURE_WRAP_T, GL3.GL_CLAMP_TO_EDGE);
            gl.glTexParameteri(GL3.GL_TEXTURE_2D, GL3.GL_TEXTURE_MIN_FILTER, GL3.GL_LINEAR);
            gl.glTexParameteri(GL3.GL_TEXTURE_2D, GL3.GL_TEXTURE_MAG_FILTER, GL3.GL_LINEAR);
            gl.glTexParameteri(GL3.GL_TEXTURE_2D, GL3.GL_TEXTURE_COMPARE_MODE, GL3.GL_NONE);
            gl.glTexImage2D(GL3.GL_TEXTURE_2D, 0, GL3.GL_DEPTH_COMPONENT16, width, height, 0, GL3.GL_DEPTH_COMPONENT, GL3.GL_BYTE, null);
            gl.glBindTexture(GL3.GL_TEXTURE_2D, 0);

            // create framebuffer
            gl.glGenFramebuffers(1, obj, 2);
            buffer.fbo = obj[2];

            gl.glBindFramebuffer(GL3.GL_FRAMEBUFFER, buffer.fbo);
            gl.glFramebufferTexture2D(GL3.GL_FRAMEBUFFER, GL3.GL_COLOR_ATTACHMENT0, GL3.GL_TEXTURE_2D, buffer.color, 0);
            gl.glFramebufferTexture2D(GL3.GL_FRAMEBUFFER, GL3.GL_DEPTH_ATTACHMENT, GL3.GL_TEXTURE_2D, buffer.depth, 0);
            int status = gl.glCheckFramebufferStatus(GL3.GL_FRAMEBUFFER);
            gl.glBindFramebuffer(GL3.GL_FRAMEBUFFER, 0);

            if (status != GL3.GL_FRAMEBUFFER_COMPLETE)
                throw new RuntimeException("Failed to create framebuffer object (status: 0x"
                        + Integer.toHexString(status) + ")");

            return buffer;
        }

        void dispose(RenderContext context) {
            if (fbo == -1) return;

            GL3 gl = context.getDrawable().getGL().getGL3();
            int[] obj = { fbo, color, depth};

            // detach textures fron fbo
            gl.glBindFramebuffer(GL3.GL_FRAMEBUFFER, fbo);
            gl.glFramebufferTexture2D(GL3.GL_FRAMEBUFFER, GL3.GL_COLOR_ATTACHMENT0, GL3.GL_TEXTURE_2D, 0, 0);
            gl.glFramebufferTexture2D(GL3.GL_FRAMEBUFFER, GL3.GL_DEPTH_ATTACHMENT, GL3.GL_TEXTURE_2D, 0, 0);
            gl.glBindFramebuffer(GL3.GL_FRAMEBUFFER, 0);

            // delete fbo and textrues
            gl.glDeleteFramebuffers(1, obj, 0);
            gl.glDeleteTextures(2, obj, 1);

            fbo = -1;
            color = -1;
            depth = -1;
        }
    }

    private static class TileBufferPool {
        final int width;
        final int height;
        final int targetPoolSize;
        private int actualPoolSize;

        private LinkedList<TileBuffer> unused = new LinkedList<>();

        TileBufferPool(Vec2i size, int targetPoolSize) {
            this.width = size.x;
            this.height = size.y;
            this.targetPoolSize = targetPoolSize;
        }

        TileBuffer require(RenderContext context) {
            TileBuffer buffer;

            // If an unused buffer is available, take it. Else create a new one.
            if (!unused.isEmpty()) {
                buffer = unused.poll();
            } else {
                buffer = TileBuffer.create(context, width, height);
                actualPoolSize++;
            }

            return buffer;
        }

        void release(RenderContext context, TileBuffer buffer) {
            if (actualPoolSize < targetPoolSize && unused != null) {
                unused.push(buffer);
            } else {
                buffer.dispose(context);
                actualPoolSize--;
            }
        }

        void dispose(RenderContext context) {
            for (TileBuffer buffer : unused)
                buffer.dispose(context);

            unused = null;
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

    private class LayerChangeListenerImpl implements TileLayerProvider.LayerChangeListener {

        @Override
        public void layersChanged() {
            for (TileChangeListener l : tileListener)
                l.tilesChanged();
        }

        @Override
        public void layersChanged(TileId tile) {
            for (TileChangeListener l : tileListener)
                l.tileChanged(tile);
        }

        @Override
        public void layerChanged(String name) {
            // TODO: only reload layer

            for (TileChangeListener l : tileListener)
                l.tilesChanged();
        }

        @Override
        public void layerChanged(String name, TileId tile) {
            // TODO: only reload layer

            for (TileChangeListener l : tileListener)
                l.tileChanged(tile);
        }
    }

}
