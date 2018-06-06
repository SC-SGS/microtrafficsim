package microtrafficsim.core.vis.tilebased;

import com.jogamp.opengl.GL3;
import microtrafficsim.core.map.tiles.TileRect;
import microtrafficsim.core.map.tiles.TilingScheme;
import microtrafficsim.core.vis.Overlay;
import microtrafficsim.core.vis.context.RenderContext;
import microtrafficsim.core.vis.opengl.BufferStorage;
import microtrafficsim.core.vis.opengl.DataTypes;
import microtrafficsim.core.vis.opengl.shader.ShaderCompileException;
import microtrafficsim.core.vis.opengl.shader.ShaderLinkException;
import microtrafficsim.core.vis.opengl.shader.ShaderProgram;
import microtrafficsim.core.vis.opengl.shader.attributes.VertexArrayObject;
import microtrafficsim.core.vis.opengl.shader.attributes.VertexAttributePointer;
import microtrafficsim.core.vis.opengl.shader.attributes.VertexAttributes;
import microtrafficsim.core.vis.opengl.shader.resources.ShaderProgramSource;
import microtrafficsim.core.vis.opengl.shader.resources.ShaderSource;
import microtrafficsim.core.vis.opengl.shader.uniforms.UniformVec4f;
import microtrafficsim.core.vis.opengl.utils.Color;
import microtrafficsim.core.vis.view.OrthographicView;
import microtrafficsim.math.Rect2d;
import microtrafficsim.math.Vec2d;
import microtrafficsim.utils.resources.PackagedResource;

import java.io.IOException;
import java.nio.FloatBuffer;


// TODO: adapt for floating-point issues - project to NDC using double-precision before sending the vertices to the GPU

/**
 * Overlay displaying the tile-grid implied by a tiling-scheme.
 *
 * @author Maximilian Luz
 */
public class TileGridOverlay implements Overlay {

    private static final ShaderProgramSource SHADER_PROG_SRC = new ShaderProgramSource(
            "/shaders/basic",
            new ShaderSource(GL3.GL_VERTEX_SHADER, new PackagedResource(TileGridOverlay.class, "/shaders/basic.vs")),
            new ShaderSource(GL3.GL_FRAGMENT_SHADER, new PackagedResource(TileGridOverlay.class, "/shaders/basic.fs"))
    );

    private static final Color COLOR = Color.fromRGB(0xFF0000);


    private OrthographicView view;

    private boolean      enabled;
    private TilingScheme scheme;

    private ShaderProgram shader;
    private UniformVec4f uColor;

    private BufferStorage     vbo;
    private VertexArrayObject vao;


    /** * Constructs a new {@code TileGridOverlay} for the given {@code TilingScheme}.
     *
     * @param scheme the {@code TilingScheme} for which this overlay should be created.
     */
    public TileGridOverlay(TilingScheme scheme) {
        this.enabled = true;
        this.scheme  = scheme;
        this.shader  = null;
    }

    public void setView(OrthographicView view) {
        this.view = view;
    }


    @Override
    public void initialize(RenderContext context) throws IOException, ShaderCompileException, ShaderLinkException {
        GL3 gl = context.getDrawable().getGL().getGL3();

        /* load shaders */
        shader = context.getShaderManager().load(SHADER_PROG_SRC);
        uColor = (UniformVec4f) shader.getUniform("u_color");

        /* create vbo */
        vbo = BufferStorage.create(gl, GL3.GL_ARRAY_BUFFER);

        /* create vao */
        VertexAttributePointer ptrPosition = VertexAttributePointer.
                create(VertexAttributes.POSITION3, DataTypes.FLOAT_2, vbo, 0, 0);

        assert ptrPosition != null;

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

        context.DepthTest.setMask(gl, false);
        context.DepthTest.disable(gl);

        /* update tile grid */
        double zoom   = view.getZoomLevel();
        Rect2d bounds = view.getViewportBounds();

        TileRect tiles = scheme.getTiles(bounds, zoom);    // bounds inclusive

        int tx = tiles.xmax - tiles.xmin + 2;
        int ty = tiles.ymax - tiles.ymin + 2;

        int nVertices = 2 * (tx + ty);

        vbo.bind(gl);
        gl.glBufferData(vbo.target, nVertices * 2 * 4L, null, GL3.GL_DYNAMIC_DRAW);
        FloatBuffer vertices = gl.glMapBufferRange(vbo.target, 0, nVertices * 2 * 4L,
                                                   GL3.GL_MAP_WRITE_BIT | GL3.GL_MAP_INVALIDATE_BUFFER_BIT)
                                       .asFloatBuffer();

        for (int x = tiles.xmin; x <= tiles.xmax + 1; x++) {
            Vec2d a = scheme.getPosition(x, tiles.ymin, tiles.zoom);
            Vec2d b = scheme.getPosition(x, tiles.ymax + 1, tiles.zoom);

            vertices.put((float) a.x);
            vertices.put((float) a.y);
            vertices.put((float) b.x);
            vertices.put((float) b.y);
        }

        for (int y = tiles.ymin; y <= tiles.ymax + 1; y++) {
            Vec2d a = scheme.getPosition(tiles.xmin, y, tiles.zoom);
            Vec2d b = scheme.getPosition(tiles.xmax + 1, y, tiles.zoom);

            vertices.put((float) a.x);
            vertices.put((float) a.y);
            vertices.put((float) b.x);
            vertices.put((float) b.y);
        }

        gl.glUnmapBuffer(vbo.target);
        vbo.unbind(gl);

        uColor.set(COLOR);

        context.Lines.setLineWidth(gl, 1.f);
        context.Lines.setLineSmoothEnabled(gl, false);

        /* draw */
        shader.bind(gl);
        vao.bind(gl);
        gl.glDrawArrays(GL3.GL_LINES, 0, nVertices);
        vao.unbind(gl);
        shader.unbind(gl);
    }


    @Override
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }
}
