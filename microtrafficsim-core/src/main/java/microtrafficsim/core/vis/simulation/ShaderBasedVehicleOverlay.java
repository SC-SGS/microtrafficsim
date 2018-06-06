package microtrafficsim.core.vis.simulation;

import com.jogamp.opengl.GL3;
import microtrafficsim.core.entities.vehicle.LogicVehicleEntity;
import microtrafficsim.core.logic.streets.DirectedEdge;
import microtrafficsim.core.map.Coordinate;
import microtrafficsim.core.map.MapProperties;
import microtrafficsim.core.map.style.VehicleStyleSheet;
import microtrafficsim.core.simulation.builder.impl.VisVehicleFactory;
import microtrafficsim.core.simulation.configs.SimulationConfig;
import microtrafficsim.core.simulation.core.Simulation;
import microtrafficsim.core.vis.context.RenderContext;
import microtrafficsim.core.vis.map.projections.Projection;
import microtrafficsim.core.vis.opengl.BufferStorage;
import microtrafficsim.core.vis.opengl.DataTypes;
import microtrafficsim.core.vis.opengl.shader.ShaderCompileException;
import microtrafficsim.core.vis.opengl.shader.ShaderLinkException;
import microtrafficsim.core.vis.opengl.shader.ShaderProgram;
import microtrafficsim.core.vis.opengl.shader.attributes.VertexAttributePointer;
import microtrafficsim.core.vis.opengl.shader.attributes.VertexAttributes;
import microtrafficsim.core.vis.opengl.shader.resources.ShaderProgramSource;
import microtrafficsim.core.vis.opengl.shader.resources.ShaderSource;
import microtrafficsim.core.vis.opengl.shader.uniforms.Uniform1f;
import microtrafficsim.core.vis.opengl.shader.uniforms.UniformMat4f;
import microtrafficsim.core.vis.opengl.shader.uniforms.UniformVec2f;
import microtrafficsim.core.vis.utils.LaneOffset;
import microtrafficsim.core.vis.view.OrthographicView;
import microtrafficsim.math.*;
import microtrafficsim.utils.resources.PackagedResource;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collection;


// TODO: shader based anti-aliasing in fragment-shader?

/**
 * Overlay to display simulated vehicles using the geometry-shader.
 *
 * @author Maximilian Luz
 */
public class ShaderBasedVehicleOverlay implements VehicleOverlay {

    private static final Vec2f VEHICLE_SIZE               = new Vec2f(15.f, 15.f);
    private static final int   VIEWPORT_CULLING_EXPANSION = 20;

    private static final ShaderProgramSource SHADER_PROG_SRC = new ShaderProgramSource(
            "/shaders/overlay/vehicle/shaderbased/vehicle_overlay",
            new ShaderSource(GL3.GL_VERTEX_SHADER, new PackagedResource(ShaderBasedVehicleOverlay.class,
                    "/shaders/overlay/vehicle/shaderbased/vehicle_overlay.vs")),
            new ShaderSource(GL3.GL_GEOMETRY_SHADER, new PackagedResource(ShaderBasedVehicleOverlay.class,
                    "/shaders/overlay/vehicle/shaderbased/vehicle_overlay.gs")),
            new ShaderSource(GL3.GL_FRAGMENT_SHADER, new PackagedResource(ShaderBasedVehicleOverlay.class,
                    "/shaders/overlay/vehicle/shaderbased/vehicle_overlay.fs"))
    );

    private final VisVehicleFactory vehicleFactory;

    private Simulation       simulation;
    private Projection       projection;
    private OrthographicView view;

    private int           vao;
    private BufferStorage vbo;

    private VertexAttributePointer ptrPosition;
    private VertexAttributePointer ptrNormal;
    private VertexAttributePointer ptrColor;

    private ShaderProgram prog;
    private UniformVec2f  uVehicleSize;
    private Uniform1f     uVehicleScale;

    private MapProperties map;

    private boolean enabled;

    private UniformMat4f uView;
    private UniformMat4f uProjection;


    /**
     * Creates a {@code ShaderBasedVehicleOverlay} with the given projection and default vehicle color.
     *
     * @param projection   the {@code Projection} used for the visualization.
     * @param vehicleStyle the default color used for the vehicles.
     */
    public ShaderBasedVehicleOverlay(Projection projection, VehicleStyleSheet vehicleStyle) {
        this.simulation = null;
        this.projection = projection;

        this.vehicleFactory = () -> new Vehicle(vehicleStyle.getDefaultVehicleColor());

        this.vao = -1;
        this.vbo = null;

        this.ptrPosition = null;
        this.ptrNormal   = null;
        this.ptrColor    = null;

        this.prog          = null;
        this.uVehicleSize  = null;
        this.uVehicleScale = null;

        this.enabled = true;
    }

    @Override
    public void setView(OrthographicView view) {
        this.view = view;
    }

    @Override
    public void setMapProperties(MapProperties properties) {
        this.map = properties;
    }


    @Override
    public void initialize(RenderContext context) throws IOException, ShaderCompileException, ShaderLinkException {
        GL3 gl = context.getDrawable().getGL().getGL3();

        prog = context.getShaderManager().load(SHADER_PROG_SRC);

        uVehicleSize  = (UniformVec2f) prog.getUniform("u_vehicle_size");
        uVehicleScale = (Uniform1f) prog.getUniform("u_vehicle_scale");

        uView = (UniformMat4f) context.getUniformManager().getGlobalUniform("u_view");
        uProjection = (UniformMat4f) context.getUniformManager().getGlobalUniform("u_projection");

        uVehicleScale.set(1.0f);

        // create vbo, vao
        int[] obj = {-1, -1};

        gl.glGenVertexArrays(1, obj, 0);
        gl.glGenBuffers(1, obj, 1);

        vao = obj[0];

        vbo         = new BufferStorage(GL3.GL_ARRAY_BUFFER, obj[1]);
        ptrPosition = VertexAttributePointer.create(VertexAttributes.POSITION2, DataTypes.FLOAT_2, vbo, 20, 0);
        ptrNormal   = VertexAttributePointer.create(VertexAttributes.NORMAL2, DataTypes.FLOAT_2, vbo, 20, 8);
        ptrColor    = VertexAttributePointer.create(VertexAttributes.COLOR, DataTypes.UNSIGNED_BYTE_4, vbo, 20, 16);

        // set up vertex array
        gl.glBindVertexArray(vao);
        gl.glBindBuffer(vbo.target, vbo.handle);
        gl.glEnableVertexAttribArray(ptrPosition.attribute.index);
        ptrPosition.set(gl);
        gl.glEnableVertexAttribArray(ptrNormal.attribute.index);
        ptrNormal.set(gl);
        gl.glEnableVertexAttribArray(ptrColor.attribute.index);
        ptrColor.set(gl);
        gl.glBindVertexArray(0);

        // allocate initial buffer
        gl.glBindBuffer(vbo.target, vbo.handle);
        gl.glBufferData(vbo.target, 1000 * 5 * 4L, null, GL3.GL_DYNAMIC_DRAW);
        gl.glBindBuffer(vbo.target, 0);
    }

    @Override
    public void dispose(RenderContext context) {
        GL3 gl = context.getDrawable().getGL().getGL3();

        // delete buffer
        int[] obj = {vao, vbo.handle};

        gl.glDeleteVertexArrays(1, obj, 0);
        gl.glDeleteBuffers(1, obj, 1);

        vao = -1;
        vbo = null;

        // delete shader
        prog.dispose(gl);
    }

    @Override
    public void resize(RenderContext context) {}

    @Override
    public void display(RenderContext context, MapBuffer map) {
        if (!enabled || simulation == null) return;
        if (simulation.getScenario() == null) return;
        GL3 gl = context.getDrawable().getGL().getGL3();

        SimulationConfig config = simulation.getScenario().getConfig();
        int zoom = (int) Math.ceil(view.getZoomLevel());
        double lanewidth = config.visualization.style.getNormalizedStreetLaneWidth(zoom);
        double scalenorm = config.visualization.style.getScaleNorm();
        boolean drivingOnTheRight = this.map.drivingOnTheRight;

        // NOTE: assumes z-axis top-down orthographic projection
        uVehicleSize.set(getVehicleSize(view.getZoomLevel(), view.getMaxZoomLevel()));

        // disable depth test
        context.DepthTest.setMask(gl, false);
        context.DepthTest.disable(gl);

        // enable blending
        context.BlendMode.enable(gl);
        context.BlendMode.setEquation(gl, GL3.GL_FUNC_ADD);
        context.BlendMode.setFactors(gl, GL3.GL_SRC_ALPHA, GL3.GL_ONE_MINUS_SRC_ALPHA, GL3.GL_ONE,
                GL3.GL_ONE_MINUS_SRC_ALPHA);

        // calculate bounding rectangle, assumes top-down (z-axis) orthographic view
        double invscale = 1.f / view.getScale();
        Vec3d  viewpos  = view.getPosition();
        Vec2i  viewport = view.getSize();

        double vx = invscale * (viewport.x + VIEWPORT_CULLING_EXPANSION) / 2;
        double vy = invscale * (viewport.y + VIEWPORT_CULLING_EXPANSION) / 2;

        double left   = viewpos.x - vx;
        double right  = viewpos.x + vx;
        double bottom = viewpos.y - vy;
        double top    = viewpos.y + vy;

        Rect2d viewrect = view.getViewportBounds();
        Rect2d ndcrect = new Rect2d(-1.0, -1.0, 1.0, 1.0);

        // update vehicle list
        Collection<? extends LogicVehicleEntity>
                vehicles = new ArrayList<>(simulation.getScenario().getVehicleContainer().getSpawnedVehicles());
        int                                      len      = vehicles.size();
        if (len == 0) return;

        // orphan last buffer and load it to a new one
        gl.glBindBuffer(vbo.target, vbo.handle);
        gl.glBufferData(vbo.target, len * 5 * 4L, null, GL3.GL_DYNAMIC_DRAW);

        ByteBuffer buffer = gl.glMapBufferRange(vbo.target, 0, len * 5 * 4L,
                                                GL3.GL_MAP_WRITE_BIT | GL3.GL_MAP_INVALIDATE_BUFFER_BIT);

        // write positions
        int vehicleCount = 0;
        for (LogicVehicleEntity logic : vehicles) {
            Vehicle v = (Vehicle) logic.getEntity().getVisualization();

            Coordinate cpos = v.getPosition();
            Vec2d      pos  = projection.project(cpos);

            Coordinate ctarget = v.getTarget();
            Vec2d      dir     = projection.project(ctarget).sub(pos).normalize();

            // adjust position to lane
            DirectedEdge edge = v.getCurrentEdge();
            if (edge == null) continue;

            double laneOffset = LaneOffset.getLaneOffset(lanewidth, edge, v.getIndexOfCurrentLane(), drivingOnTheRight);

            pos.x += dir.y * laneOffset;
            pos.y -= dir.x * laneOffset;

            // continue if out of bounds
            if (pos.x < left || pos.x > right || pos.y < bottom || pos.y > top) continue;

            pos = Rect2d.project(viewrect, ndcrect, pos);

            buffer.putFloat((float) pos.x);
            buffer.putFloat((float) pos.y);
            buffer.putFloat((float) dir.x);
            buffer.putFloat((float) dir.y);
            buffer.putInt(v.getBaseColor().toIntABGR());
            vehicleCount++;
        }

        gl.glUnmapBuffer(vbo.target);
        gl.glBindBuffer(vbo.target, 0);

        // draw
        Mat4f viewBefore = new Mat4f(uView.get());
        Mat4f projBefore = new Mat4f(uProjection.get());

        uView.set(Mat4f.identity());
        uProjection.set(Mat4f.identity());

        prog.bind(gl);
        gl.glBindVertexArray(vao);
        gl.glDrawArrays(GL3.GL_POINTS, 0, vehicleCount);
        gl.glBindVertexArray(0);
        prog.unbind(gl);

        uView.set(viewBefore);
        uProjection.set(projBefore);
    }


    @Override
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }


    /**
     * Returns the simulation displayed in with this overlay.
     *
     * @return the simulation displayed in this overlay.
     */
    public Simulation getSimulation() {
        return simulation;
    }

    @Override
    public void setSimulation(Simulation simulation) {
        this.simulation = simulation;
    }

    @Override
    public VisVehicleFactory getVehicleFactory() {
        return vehicleFactory;
    }


    private Vec2f getVehicleSize(double zoom, double zoomMax) {
        final double slowdecaylevel = 15.0;

        final double z = Math.max(zoom, slowdecaylevel);
        final double s1 = Math.pow(1.25, -(zoomMax - z));
        final double s2 = zoom > slowdecaylevel ? 1.0 : Math.pow(1.05, -(slowdecaylevel - zoom));

        return new Vec2f((float) ((VEHICLE_SIZE.x * s1) * s2), (float) ((VEHICLE_SIZE.y * s1) * s2));
    }
}
