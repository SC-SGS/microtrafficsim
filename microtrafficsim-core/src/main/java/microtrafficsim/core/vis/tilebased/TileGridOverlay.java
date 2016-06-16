package microtrafficsim.core.vis.tilebased;


import com.jogamp.opengl.GL3;
import microtrafficsim.core.map.tiles.TileRect;
import microtrafficsim.core.map.tiles.TilingScheme;
import microtrafficsim.core.vis.Overlay;
import microtrafficsim.core.vis.context.RenderContext;
import microtrafficsim.core.vis.opengl.BufferStorage;
import microtrafficsim.core.vis.opengl.DataTypes;
import microtrafficsim.core.vis.opengl.shader.Shader;
import microtrafficsim.core.vis.opengl.shader.ShaderProgram;
import microtrafficsim.core.vis.opengl.shader.attributes.VertexArrayObject;
import microtrafficsim.core.vis.opengl.shader.attributes.VertexAttributePointer;
import microtrafficsim.core.vis.opengl.shader.attributes.VertexAttributes;
import microtrafficsim.core.vis.opengl.shader.uniforms.UniformVec4f;
import microtrafficsim.core.vis.opengl.utils.Color;
import microtrafficsim.core.vis.view.OrthographicView;
import microtrafficsim.math.Rect2d;
import microtrafficsim.math.Vec2d;
import microtrafficsim.utils.resources.PackagedResource;
import microtrafficsim.utils.resources.Resource;

import java.nio.FloatBuffer;


public class TileGridOverlay implements Overlay {
    private static final Resource VERTEX_SHADER = new PackagedResource(TileGridOverlay.class, "/shaders/basic.vs");
    private static final Resource FRAGMENT_SHADER = new PackagedResource(TileGridOverlay.class, "/shaders/basic.fs");
    private static final Color COLOR = Color.fromRGB(0xFF0000);

    private OrthographicView view;

    private boolean enabled;
    private TilingScheme scheme;

    private ShaderProgram shader;
    private UniformVec4f uColor;

    private BufferStorage vbo;
    private VertexArrayObject vao;


    public TileGridOverlay(TilingScheme scheme) {
        this.enabled = true;
        this.scheme = scheme;
        this.shader = null;
    }

    public void setView(OrthographicView view) {
        this.view = view;
    }


    @Override
    public void init(RenderContext context) {
        GL3 gl = context.getDrawable().getGL().getGL3();

        /* load shaders */
        Shader vs = Shader.create(gl, GL3.GL_VERTEX_SHADER, "basic.vs")
                .loadFromResource(VERTEX_SHADER)
                .compile(gl);

        Shader fs = Shader.create(gl, GL3.GL_FRAGMENT_SHADER, "basic.fs")
                .loadFromResource(FRAGMENT_SHADER)
                .compile(gl);

        shader = ShaderProgram.create(gl, context, "basic")
                .attach(gl, vs, fs)
                .link(gl)
                .detach(gl);

        uColor = (UniformVec4f) shader.getUniform("u_color");
        uColor.set(COLOR);

        /* create vbo */
        vbo = BufferStorage.create(gl, GL3.GL_ARRAY_BUFFER);

        /* create vao */
        VertexAttributePointer ptrPosition
                = VertexAttributePointer.create(VertexAttributes.POSITION3, DataTypes.FLOAT_3, vbo, 0, 0);

        vao = VertexArrayObject.create(gl);
        vao.bind(gl);
        vbo.bind(gl);
        ptrPosition.enable(gl);
        ptrPosition.set(gl);
        vao.unbind(gl);
    }

    @Override
    public void dispose(RenderContext context) {
        GL3 gl = context.getDrawable().getGL().getGL3();

        vao.dispose(gl);
        vbo.dispose(gl);
        shader.dispose(gl);
    }

    @Override
    public void resize(RenderContext context) {}

    @Override
    public void display(RenderContext context, MapBuffer buffer) {
        GL3 gl = context.getDrawable().getGL().getGL3();
        context.DepthTest.disable(gl);
        gl.glDepthMask(false);

        /* update tile grid */
        double zoom = ((OrthographicView) view).getZoomLevel();
        Rect2d bounds = ((OrthographicView) view).getViewportBounds();

        TileRect tiles = scheme.getTiles(bounds, zoom);       // bounds inclusive

        int tx = tiles.xmax - tiles.xmin + 2;
        int ty = tiles.ymax - tiles.ymin + 2;

        int nVertices = 2 * (tx + ty);

        vbo.bind(gl);
        gl.glBufferData(vbo.target, nVertices * 3 * 4L, null, GL3.GL_DYNAMIC_DRAW);
        FloatBuffer vertices = gl.glMapBufferRange(vbo.target, 0, nVertices * 3 * 4L,
                GL3.GL_MAP_WRITE_BIT | GL3.GL_MAP_INVALIDATE_BUFFER_BIT)
                .asFloatBuffer();

        for (int x = tiles.xmin; x <= tiles.xmax + 1; x++) {
            Vec2d a = scheme.getPosition(x, tiles.ymin, tiles.zoom);
            Vec2d b = scheme.getPosition(x, tiles.ymax + 1, tiles.zoom);

            vertices.put((float) a.x);
            vertices.put((float) a.y);
            vertices.put(0);
            vertices.put((float) b.x);
            vertices.put((float) b.y);
            vertices.put(0);
        }

        for (int y = tiles.ymin; y <= tiles.ymax + 1; y++) {
            Vec2d a = scheme.getPosition(tiles.xmin, y, tiles.zoom);
            Vec2d b = scheme.getPosition(tiles.xmax + 1, y, tiles.zoom);

            vertices.put((float) a.x);
            vertices.put((float) a.y);
            vertices.put(0);
            vertices.put((float) b.x);
            vertices.put((float) b.y);
            vertices.put(0);
        }

        gl.glUnmapBuffer(vbo.target);
        vbo.unbind(gl);

        /* draw */
        shader.bind(gl);
        vao.bind(gl);
        gl.glDrawArrays(GL3.GL_LINES, 0, nVertices);
        vao.unbind(gl);
        shader.unbind(gl);

        gl.glDepthMask(true);
    }


    @Override
    public void enable() {
        this.enabled = true;
    }

    @Override
    public void disable() {
        this.enabled = false;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }
}
