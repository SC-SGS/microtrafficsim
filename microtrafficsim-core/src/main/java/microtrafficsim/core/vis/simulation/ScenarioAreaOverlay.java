package microtrafficsim.core.vis.simulation;

import com.jogamp.newt.event.MouseEvent;
import com.jogamp.opengl.GL2ES3;
import com.jogamp.opengl.GL3;
import microtrafficsim.core.map.Coordinate;
import microtrafficsim.core.map.area.ISimplePolygon;
import microtrafficsim.core.map.area.RectangleArea;
import microtrafficsim.core.map.area.SimplePolygon;
import microtrafficsim.core.vis.Overlay;
import microtrafficsim.core.vis.context.RenderContext;
import microtrafficsim.core.vis.map.projections.Projection;
import microtrafficsim.core.vis.mesh.impl.Pos3IndexedMesh;
import microtrafficsim.core.vis.mesh.utils.Polygons;
import microtrafficsim.core.vis.opengl.shader.Shader;
import microtrafficsim.core.vis.opengl.shader.ShaderProgram;
import microtrafficsim.core.vis.opengl.shader.attributes.VertexArrayObject;
import microtrafficsim.core.vis.opengl.shader.uniforms.UniformVec4f;
import microtrafficsim.core.vis.opengl.utils.Color;
import microtrafficsim.core.vis.view.OrthographicView;
import microtrafficsim.math.Rect2d;
import microtrafficsim.math.Vec2d;
import microtrafficsim.math.Vec2i;
import microtrafficsim.utils.resources.PackagedResource;
import microtrafficsim.utils.resources.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.HashSet;


public class ScenarioAreaOverlay implements Overlay {
    private static Logger logger = LoggerFactory.getLogger(ScenarioAreaOverlay.class);

    private static final Resource SHADER_SRC_VS = new PackagedResource(ScenarioAreaOverlay.class, "/shaders/basic.vs");
    private static final Resource SHADER_SRC_FS = new PackagedResource(ScenarioAreaOverlay.class, "/shaders/basic.fs");

    private static final Color COLOR_START  = Color.fromRGBA(0x00900030);
    private static final Color COLOR_TARGET = Color.fromRGBA(0x90000030);

    private boolean enabled = true;
    private Projection projection;

    private HashSet<ISimplePolygon> start = new HashSet<>();
    private HashSet<ISimplePolygon> target = new HashSet<>();

    private Pos3IndexedMesh startMesh = null;
    private Pos3IndexedMesh targetMesh = null;

    private VertexArrayObject startMeshVao = null;
    private VertexArrayObject targetMeshVao = null;

    private boolean reloadStartMesh = true;
    private boolean reloadTargetMesh = true;

    private ShaderProgram shader;
    private UniformVec4f uColor;

    private OrthographicView view;

    private MouseListener mouseListener = new MouseListenerImpl();


    public ScenarioAreaOverlay(Projection projection) {
        this.projection = projection;
        tmpInit();
    }

    // TMP initialization for tokyo-tiny, TODO: remove
    private void tmpInit() {
        // target.add(new RectangleArea(35.614000, 139.72120, 35.63920, 139.72508));
        // target.add(new RectangleArea(35.614000, 139.72120, 35.61652, 139.75990));
        // target.add(new RectangleArea(35.614000, 139.75603, 35.63920, 139.75990));
        // target.add(new RectangleArea(35.636677, 139.72120, 35.63920, 139.75990));

        start.add(new RectangleArea(35.6140, 139.72120, 35.63920, 139.75990));

        target.add(new SimplePolygon(new Coordinate[]{
                new Coordinate(35.61400, 139.72120),
                new Coordinate(35.63920, 139.72120),
                new Coordinate(35.61652, 139.72508),
                new Coordinate(35.61652, 139.75990),
                new Coordinate(35.61400, 139.75990)
        }));
    }


    private void rebuildMesh(Pos3IndexedMesh mesh, HashSet<ISimplePolygon> from) {
        ArrayList<Vec2d[]> vertices = new ArrayList<>(from.size());
        ArrayList<int[]> indices = new ArrayList<>(from.size());

        for (ISimplePolygon polygon : from) {
            Vec2d[] contour = project(polygon.getCoordinates());
            int[] ix = Polygons.triangulate(contour);

            if (ix == null) {
                logger.error("could not triangulate polygon");
                continue;
            }

            vertices.add(contour);
            indices.add(ix);
        }

        int nVertices = 0;
        int nIndices = 0;

        for (int k = 0; k < vertices.size(); k++) {
            nVertices += vertices.get(k).length;
            nIndices += indices.get(k).length;
        }

        FloatBuffer vb = FloatBuffer.allocate(nVertices * 3);
        IntBuffer ib = IntBuffer.allocate(nIndices);

        int offset = 0;
        for (int k = 0; k < vertices.size(); k++) {
            Vec2d[] vx = vertices.get(k);
            int[] ix = indices.get(k);

            for (Vec2d v : vx) {
                vb.put((float) v.x);
                vb.put((float) v.y);
                vb.put(0.f);
            }

            for (int i : ix)
                ib.put(offset + i);

            offset += vx.length;
        }

        vb.rewind();
        ib.rewind();

        mesh.setVertexBuffer(vb);       // XXX: concurrency
        mesh.setIndexBuffer(ib);        // XXX: concurrency
    }

    private Vec2d[] project(Coordinate[] c) {
        Vec2d[] r = new Vec2d[c.length];

        for (int i = 0; i < c.length; i++)
            r[i] = projection.project(c[i]);

        return r;
    }


    public void setView(OrthographicView view) {
        this.view = view;
    }


    @Override
    public void init(RenderContext context) {
        GL2ES3 gl = context.getDrawable().getGL().getGL2ES3();

        // create shader
        Shader vs = Shader.create(gl, GL3.GL_VERTEX_SHADER, "basic.vs")
                .loadFromResource(SHADER_SRC_VS)
                .compile(gl);

        Shader fs = Shader.create(gl, GL3.GL_FRAGMENT_SHADER, "basic.fs")
                .loadFromResource(SHADER_SRC_FS)
                .compile(gl);

        shader = ShaderProgram.create(gl, context, "basic")
                .attach(gl, vs, fs)
                .link(gl)
                .detach(gl, vs, fs);

        uColor = (UniformVec4f) shader.getUniform("u_color");

        // create meshes
        startMesh = new Pos3IndexedMesh(GL3.GL_STATIC_DRAW, GL3.GL_TRIANGLES, null, null);
        targetMesh = new Pos3IndexedMesh(GL3.GL_STATIC_DRAW, GL3.GL_TRIANGLES, null, null);

        startMesh.initialize(context);
        targetMesh.initialize(context);

        startMeshVao = startMesh.createVAO(context, shader);
        targetMeshVao = targetMesh.createVAO(context, shader);

        // build meshes from specified polygons
        rebuildMesh(startMesh, start);
        reloadStartMesh = true;

        rebuildMesh(targetMesh, target);
        reloadTargetMesh = true;
    }

    @Override
    public void dispose(RenderContext context) {
        GL2ES3 gl = context.getDrawable().getGL().getGL2ES3();

        startMeshVao.dispose(gl);
        targetMeshVao.dispose(gl);

        startMesh.dispose(context);
        targetMesh.dispose(context);

        shader.dispose(gl);
    }

    @Override
    public void resize(RenderContext context) {}

    @Override
    public void display(RenderContext context, MapBuffer map) {
        if (reloadStartMesh) {
            startMesh.load(context, true);
            reloadStartMesh = false;
        }
        if (reloadTargetMesh) {
            targetMesh.load(context, true);
            reloadTargetMesh = false;
        }

        GL3 gl = context.getDrawable().getGL().getGL3();

        gl.glDepthMask(false);
        context.DepthTest.disable(gl);

        context.BlendMode.enable(gl);
        context.BlendMode.setEquation(gl, GL3.GL_FUNC_ADD, GL3.GL_FUNC_ADD);
        context.BlendMode.setFactors(gl, GL3.GL_SRC_ALPHA, GL3.GL_ONE_MINUS_SRC_ALPHA, GL3.GL_ONE, GL3.GL_ONE_MINUS_SRC_ALPHA);

        shader.bind(gl);

        uColor.set(COLOR_START);
        startMesh.display(context, startMeshVao);

        uColor.set(COLOR_TARGET);
        targetMesh.display(context, targetMeshVao);

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


    @Override
    public MouseListener getMouseListener() {
        return mouseListener;
    }


    private class MouseListenerImpl implements MouseListener {

        @Override
        public boolean mouseClicked(MouseEvent e) {
            System.out.println(unporject(e.getX(), e.getY()).toString());
            return false;
        }

        private Coordinate unporject(int x, int y) {
            Vec2i viewport = view.getSize();
            Rect2d bounds = view.getViewportBounds();

            return projection.unproject(new Vec2d(
                    (x / (double) viewport.x) * (bounds.xmax - bounds.xmin) + bounds.xmin,
                    (y / (double) viewport.y) * (bounds.ymax - bounds.ymin) + bounds.ymin
            ));
        }
    }
}
