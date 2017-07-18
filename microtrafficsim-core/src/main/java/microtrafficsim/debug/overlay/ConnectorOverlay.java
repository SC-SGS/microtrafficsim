package microtrafficsim.debug.overlay;

import com.jogamp.opengl.GL3;
import microtrafficsim.core.logic.nodes.Node;
import microtrafficsim.core.logic.streetgraph.Graph;
import microtrafficsim.core.logic.streets.DirectedEdge;
import microtrafficsim.core.map.Coordinate;
import microtrafficsim.core.map.MapProperties;
import microtrafficsim.core.simulation.configs.SimulationConfig;
import microtrafficsim.core.vis.Overlay;
import microtrafficsim.core.vis.context.RenderContext;
import microtrafficsim.core.vis.map.projections.Projection;
import microtrafficsim.core.vis.mesh.Mesh;
import microtrafficsim.core.vis.mesh.impl.SingleFloatAttributeIndexedMesh;
import microtrafficsim.core.vis.opengl.shader.ShaderProgram;
import microtrafficsim.core.vis.opengl.shader.attributes.VertexArrayObject;
import microtrafficsim.core.vis.opengl.shader.resources.ShaderProgramSource;
import microtrafficsim.core.vis.opengl.shader.resources.ShaderSource;
import microtrafficsim.core.vis.opengl.shader.uniforms.UniformVec4f;
import microtrafficsim.core.vis.opengl.utils.Color;
import microtrafficsim.core.vis.utils.LaneOffset;
import microtrafficsim.core.vis.view.OrthographicView;
import microtrafficsim.math.Vec2d;
import microtrafficsim.utils.resources.PackagedResource;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;


public class ConnectorOverlay implements Overlay {

    private static final ShaderProgramSource SHADER_PROG_SRC = new ShaderProgramSource(
            "/shaders/basic",
            new ShaderSource(GL3.GL_VERTEX_SHADER, new PackagedResource(ConnectorOverlay.class, "/shaders/basic.vs")),
            new ShaderSource(GL3.GL_FRAGMENT_SHADER, new PackagedResource(ConnectorOverlay.class, "/shaders/basic.fs"))
    );

    private static final Color COLOR = Color.fromRGBA(0xFF000040);

    private static final double ARROW_LEN = 2.5;

    private Projection projection;
    private SimulationConfig config;

    private RenderContext context = null;
    private OrthographicView view = null;
    private boolean enabled = true;

    private ShaderProgram shader = null;
    private UniformVec4f uColor = null;

    private boolean drivingOnTheRight = true;
    private Mesh mesh = null;
    private VertexArrayObject vao = null;


    public ConnectorOverlay(Projection projection, SimulationConfig config) {
        this.projection = projection;
        this.config = config;
    }


    @Override
    public void setView(OrthographicView view) {
        this.view = view;
    }

    public void setProjection(Projection projection) {
        this.projection = projection;
    }


    @Override
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }


    @Override
    public void initialize(RenderContext context) throws Exception {
        this.context = context;

        this.shader = context.getShaderManager().load(SHADER_PROG_SRC);
        this.uColor = (UniformVec4f) shader.getUniform("u_color");
    }

    @Override
    public void dispose(RenderContext context) throws Exception {
        GL3 gl = context.getDrawable().getGL().getGL3();

        if (this.shader != null)
            this.shader.dispose(gl);

        if (this.vao != null)
            this.vao.dispose(gl);

        if (this.mesh != null)
            this.mesh.dispose(context);
    }

    @Override
    public void resize(RenderContext context) throws Exception {}

    @Override
    public void display(RenderContext context, MapBuffer map) throws Exception {
        if (mesh == null || !isEnabled()) return;

        GL3 gl = context.getDrawable().getGL().getGL3();

        context.DepthTest.setMask(gl, false);
        context.DepthTest.disable(gl);

        // enable blending
        context.BlendMode.enable(gl);
        context.BlendMode.setEquation(gl, GL3.GL_FUNC_ADD);
        context.BlendMode.setFactors(gl, GL3.GL_SRC_ALPHA, GL3.GL_ONE_MINUS_SRC_ALPHA, GL3.GL_ONE,
                GL3.GL_ONE_MINUS_SRC_ALPHA);


        uColor.set(COLOR);

        try {
            context.Lines.setLineWidth(gl, 2.f);
        } catch (Exception e) { /* ignore */ };

        try {
            context.Lines.setLineSmoothEnabled(gl, true);
        } catch (Exception e) { /* ignore */ };

        shader.bind(gl);
        mesh.display(context, vao);
        shader.unbind(gl);
    }


    public void update(Graph graph, MapProperties properties) {
        this.drivingOnTheRight = properties.drivingOnTheRight;
        Mesh mesh = generateConnectorMesh(graph);

        context.addTask(c -> {
            update(c, mesh);
            return null;
        });
    }

    private void update(RenderContext ctx, Mesh mesh) {
        if (this.mesh != null) this.mesh.dispose(ctx);

        mesh.initialize(ctx);
        mesh.load(ctx);

        this.mesh = mesh;
        this.vao = mesh.createVAO(ctx, this.shader);
    }


    private Mesh generateConnectorMesh(Graph graph) {
        int restart = context.PrimitiveRestart.getIndex();
        double lanewidth = config.visualization.style.getNormalizedStreetLaneWidth(19);

        ArrayList<Vec2d> vertices = new ArrayList<>();
        ArrayList<Integer> indices = new ArrayList<>();

        for (Node node : graph.getNodes()) {
            addConnectors(vertices, indices, restart, node, lanewidth);
        }

        FloatBuffer vb = FloatBuffer.allocate(vertices.size() * 2);
        for (Vec2d v : vertices) {
            vb.put((float) v.x);
            vb.put((float) v.y);
        }
        vb.rewind();

        IntBuffer ib = IntBuffer.allocate(indices.size());
        for (int i : indices) {
            ib.put(i);
        }
        ib.rewind();

        int usage = GL3.GL_STATIC_DRAW;
        int mode = GL3.GL_LINE_STRIP;
        return SingleFloatAttributeIndexedMesh.newPos2Mesh(usage, mode, vb, ib);
    }

    private void addConnectors(ArrayList<Vec2d> vertices, ArrayList<Integer> indices, int restart,
                               Node node, double lanewidth)
    {
        for (Map.Entry<DirectedEdge.Lane, TreeMap<DirectedEdge, DirectedEdge.Lane>> connector : node.getConnectors().entrySet()) {
            DirectedEdge.Lane from = connector.getKey();

            for (DirectedEdge.Lane to : connector.getValue().values()) {
                addConnector(vertices, indices, restart,
                        from.getEdge(), from.getIndex(),
                        to.getEdge(), to.getIndex(), lanewidth);
            }
        }
    }

    private void addConnector(ArrayList<Vec2d> vertices, ArrayList<Integer> indices, int restart,
                              DirectedEdge fromEdge, int fromLane, DirectedEdge toEdge,
                              int toLane, double lanewidth) {
        if (fromEdge.getEntity() == toEdge.getEntity()) {
            addUTurnConnector(vertices, indices, restart, fromEdge, fromLane, toEdge, toLane, lanewidth);
        } else {
            addStandardConnector(vertices, indices, restart, fromEdge, fromLane, toEdge, toLane, lanewidth);
        }
    }

    private void addStandardConnector(ArrayList<Vec2d> vertices, ArrayList<Integer> indices, int restart,
                                      DirectedEdge fromEdge, int fromLane, DirectedEdge toEdge,
                                      int toLane, double lanewidth)
    {
        // TODO: cyclic SNAFU?

        Vec2d pos;
        Vec2d dirFrom;
        Vec2d dirTo;

        if (fromEdge.getEntity().getForwardEdge() == fromEdge) {
            Coordinate[] coordinates = fromEdge.getEntity().getGeometry().coordinates;
            pos = projection.project(coordinates[coordinates.length - 1]);
            dirFrom = projection.project(coordinates[coordinates.length - 2]).sub(pos).normalize();
        } else {
            Coordinate[] coordinates = fromEdge.getEntity().getGeometry().coordinates;
            pos = projection.project(coordinates[0]);
            dirFrom = projection.project(coordinates[1]).sub(pos).normalize();
        }

        if (toEdge.getEntity().getForwardEdge() == toEdge) {
            Coordinate[] coordinates = toEdge.getEntity().getGeometry().coordinates;
            Vec2d p = projection.project(coordinates[0]);
            dirTo = projection.project(coordinates[1]).sub(p).normalize();
        } else {
            Coordinate[] coordinates = toEdge.getEntity().getGeometry().coordinates;
            Vec2d p = projection.project(coordinates[coordinates.length - 1]);
            dirTo = projection.project(coordinates[coordinates.length - 2]).sub(p).normalize();
        }

        Vec2d dirFrom90 = new Vec2d(-dirFrom.y, dirFrom.x);
        Vec2d dirTo90   = new Vec2d(-dirTo.y,   dirTo.x);

        double laneOffsetFrom = LaneOffset.getLaneOffset(lanewidth, fromEdge, fromLane, drivingOnTheRight);
        double laneOffsetTo   = LaneOffset.getLaneOffset(lanewidth, toEdge,   toLane,   drivingOnTheRight) * -1.0;

        Vec2d posFrom = Vec2d.mul(dirFrom, lanewidth * ARROW_LEN).add(pos).add(Vec2d.mul(dirFrom90, laneOffsetFrom));
        Vec2d posTo   = Vec2d.mul(dirTo,   lanewidth * ARROW_LEN).add(pos).add(Vec2d.mul(dirTo90,   laneOffsetTo));

        Vec2d posArrowHead = Vec2d.mul(dirTo, lanewidth * -0.75).add(posTo);
        Vec2d posArrowHeadLeft  = Vec2d.mul(dirTo90, lanewidth * -0.33).add(posArrowHead);
        Vec2d posArrowHeadRight = Vec2d.mul(dirTo90, lanewidth * +0.33).add(posArrowHead);

        double sf = Vec2d.dot(dirFrom, dirFrom90);
        sf = sf != 0.0 ? 1.0 / sf : Double.MAX_VALUE;
        sf = Math.min(sf * laneOffsetFrom, lanewidth * ARROW_LEN);

        double st = Vec2d.dot(dirTo, dirTo90);
        st = st != 0.0 ? 1.0 / st : Double.MAX_VALUE;
        st = Math.min(st * laneOffsetTo, lanewidth * ARROW_LEN);

        Vec2d posVia = intersect(posFrom, Vec2d.add(posFrom, dirFrom), posTo, Vec2d.add(posTo, dirTo));
        if (posVia == null || Vec2d.sub(pos, posVia).len() > lanewidth * 2)
            posVia = Vec2d.mul(dirFrom90, laneOffsetFrom).add(pos);

        int idx = vertices.size();
        vertices.add(posFrom);
        vertices.add(posVia);
        vertices.add(posTo);
        vertices.add(posArrowHeadLeft);
        vertices.add(posArrowHeadRight);

        indices.add(idx);           // from
        indices.add(idx + 1);       // via
        indices.add(idx + 2);       // to
        indices.add(idx + 3);       // arrow-head left
        indices.add(idx + 4);       // arrow-head right
        indices.add(idx + 2);       // to
        indices.add(restart);
    }

    private void addUTurnConnector(ArrayList<Vec2d> vertices, ArrayList<Integer> indices, int restart,
                                   DirectedEdge fromEdge, int fromLane, DirectedEdge toEdge,
                                   int toLane, double lanewidth)
    {
        Vec2d pos;
        Vec2d dir;

        if (fromEdge.getEntity().getForwardEdge() == fromEdge) {
            Coordinate[] coordinates = fromEdge.getEntity().getGeometry().coordinates;
            pos = projection.project(coordinates[coordinates.length - 1]);
            dir = projection.project(coordinates[coordinates.length - 2]).sub(pos).normalize();
        } else {
            Coordinate[] coordinates = fromEdge.getEntity().getGeometry().coordinates;
            pos = projection.project(coordinates[0]);
            dir = projection.project(coordinates[1]).sub(pos).normalize();
        }

        Vec2d dir90 = new Vec2d(-dir.y, dir.x);

        double laneOffsetFrom = LaneOffset.getLaneOffset(lanewidth, fromEdge, fromLane, drivingOnTheRight);
        double laneOffsetTo   = LaneOffset.getLaneOffset(lanewidth, toEdge,   toLane,   drivingOnTheRight) * -1.0;

        Vec2d posFromX = Vec2d.mul(dir90, laneOffsetFrom).add(pos);
        Vec2d posToX   = Vec2d.mul(dir90, laneOffsetTo).add(pos);

        Vec2d posFrom = Vec2d.mul(dir, lanewidth * ARROW_LEN).add(posFromX);
        Vec2d posTo   = Vec2d.mul(dir, lanewidth * ARROW_LEN).add(posToX);

        Vec2d posArrowHead = Vec2d.mul(dir, lanewidth * -0.75).add(posTo);
        Vec2d posArrowHeadLeft  = Vec2d.mul(dir90, lanewidth * -0.33).add(posArrowHead);
        Vec2d posArrowHeadRight = Vec2d.mul(dir90, lanewidth * +0.33).add(posArrowHead);

        int idx = vertices.size();
        vertices.add(posFrom);
        vertices.add(posFromX);
        vertices.add(posToX);
        vertices.add(posTo);
        vertices.add(posArrowHeadLeft);
        vertices.add(posArrowHeadRight);

        indices.add(idx);           // from
        indices.add(idx + 1);       // from-x
        indices.add(idx + 2);       // to-x
        indices.add(idx + 3);       // to
        indices.add(idx + 4);       // arrow-head left
        indices.add(idx + 5);       // arrow-head right
        indices.add(idx + 3);       // to
        indices.add(restart);
    }

    private static Vec2d intersect(Vec2d aa, Vec2d ab, Vec2d ba, Vec2d bb) {
        double adx = aa.x - ab.x;
        double ady = aa.y - ab.y;
        double bdx = ba.x - bb.x;
        double bdy = ba.y - bb.y;

        double d = adx * bdy - ady * bdx;
        if (Math.abs(d) == 0.0)
            return null;

        // calculate intersection point
        double det1 = aa.x * ab.y - ab.x * aa.y;
        double det2 = ba.x * bb.y - bb.x * ba.y;

        return new Vec2d((det1 * bdx - det2 * adx) / d, (det1 * bdy - det2 * ady) / d);
    }
}
