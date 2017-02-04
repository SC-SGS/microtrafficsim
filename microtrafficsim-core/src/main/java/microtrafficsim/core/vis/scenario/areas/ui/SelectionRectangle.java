package microtrafficsim.core.vis.scenario.areas.ui;

import com.jogamp.opengl.GL3;
import microtrafficsim.core.vis.context.RenderContext;
import microtrafficsim.core.vis.mesh.impl.SingleFloatAttributeMesh;
import microtrafficsim.core.vis.opengl.shader.ShaderCompileException;
import microtrafficsim.core.vis.opengl.shader.ShaderLinkException;
import microtrafficsim.core.vis.opengl.shader.ShaderProgram;
import microtrafficsim.core.vis.opengl.shader.attributes.VertexArrayObject;
import microtrafficsim.core.vis.opengl.shader.resources.ShaderProgramSource;
import microtrafficsim.core.vis.opengl.shader.resources.ShaderSource;
import microtrafficsim.core.vis.opengl.shader.uniforms.UniformVec4f;
import microtrafficsim.core.vis.opengl.utils.Color;
import microtrafficsim.math.Rect2d;
import microtrafficsim.math.Vec2d;
import microtrafficsim.utils.resources.PackagedResource;

import java.io.IOException;
import java.nio.FloatBuffer;


public class SelectionRectangle {

    private static final ShaderProgramSource SHADER = new ShaderProgramSource(
            "/shaders/basic",
            new ShaderSource(GL3.GL_VERTEX_SHADER, new PackagedResource(SelectionRectangle.class,
                    "/shaders/basic.vs")),
            new ShaderSource(GL3.GL_FRAGMENT_SHADER, new PackagedResource(SelectionRectangle.class,
                    "/shaders/basic.fs"))
    );

    private static final Color COLOR = Color.fromRGB(0xDD0000);


    private boolean enabled = false;
    private Vec2d start = new Vec2d(0.0, 0.0);
    private Vec2d stop = new Vec2d(0.0, 0.0);
    private FloatBuffer vertices = FloatBuffer.allocate(8);

    private ShaderProgram shader = null;
    private UniformVec4f uColor = null;
    private SingleFloatAttributeMesh mesh = null;
    private VertexArrayObject vao = null;


    public SelectionRectangle() {
        mesh = SingleFloatAttributeMesh.newPos2Mesh(GL3.GL_DYNAMIC_DRAW, GL3.GL_LINE_LOOP, loadVertexBuffer());
    }


    public void initialize(RenderContext context) throws ShaderLinkException, IOException, ShaderCompileException {
        shader = context.getShaderManager().load(SHADER);
        uColor = (UniformVec4f) shader.getUniform("u_color");

        mesh.initialize(context);
        mesh.load(context);

        vao = mesh.createVAO(context, shader);
    }

    public void dispose(RenderContext context) {
        GL3 gl = context.getDrawable().getGL().getGL3();

        vao.dispose(gl);
        shader.dispose(gl);
        mesh.dispose(context);
    }

    public void display(RenderContext context) {
        if (!enabled) return;
        GL3 gl = context.getDrawable().getGL().getGL3();

        mesh.setVertexBuffer(loadVertexBuffer());
        mesh.load(context, true);

        context.Lines.setLineWidth(gl, 1.0f);
        context.Lines.setLineSmoothEnabled(gl, false);

        shader.bind(gl);
        uColor.set(COLOR);
        mesh.display(context, vao);
    }


    public void begin(Vec2d v) {
        start.set(v);
        stop.set(v);
        enabled = true;
    }

    public void update(Vec2d v) {
        stop.set(v);
    }

    public Rect2d end(Vec2d v) {
        stop.set(v);
        enabled = false;

        return getRect();
    }

    public boolean isEnabled() {
        return enabled;
    }

    public boolean isSelectionValid() {
        return enabled && !start.equals(stop);
    }

    public Rect2d getRect() {
        return new Rect2d(
                Math.min(start.x, stop.x),
                Math.min(start.y, stop.y),
                Math.max(start.x, stop.x),
                Math.max(start.y, stop.y)
        );
    }


    private FloatBuffer loadVertexBuffer() {
        vertices.clear();

        vertices.put((float) start.x);
        vertices.put((float) start.y);

        vertices.put((float) start.x);
        vertices.put((float) stop.y);

        vertices.put((float) stop.x);
        vertices.put((float) stop.y);

        vertices.put((float) stop.x);
        vertices.put((float) start.y);

        vertices.rewind();
        return vertices;
    }
}
