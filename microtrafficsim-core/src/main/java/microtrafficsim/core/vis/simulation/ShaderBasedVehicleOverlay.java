package microtrafficsim.core.vis.simulation;

import com.jogamp.opengl.GL3;
import microtrafficsim.core.frameworks.vehicle.IVisualizationVehicle;
import microtrafficsim.core.frameworks.vehicle.LogicVehicleEntity;
import microtrafficsim.core.map.Coordinate;
import microtrafficsim.core.simulation.Simulation;
import microtrafficsim.core.vis.Overlay;
import microtrafficsim.core.vis.context.RenderContext;
import microtrafficsim.core.vis.map.projections.Projection;
import microtrafficsim.core.vis.opengl.BufferStorage;
import microtrafficsim.core.vis.opengl.DataTypes;
import microtrafficsim.core.vis.opengl.shader.Shader;
import microtrafficsim.core.vis.opengl.shader.ShaderProgram;
import microtrafficsim.core.vis.opengl.shader.attributes.VertexAttributePointer;
import microtrafficsim.core.vis.opengl.shader.attributes.VertexAttributes;
import microtrafficsim.core.vis.opengl.shader.uniforms.Uniform1f;
import microtrafficsim.core.vis.opengl.shader.uniforms.UniformVec2f;
import microtrafficsim.core.vis.opengl.utils.Color;
import microtrafficsim.core.vis.view.OrthographicView;
import microtrafficsim.math.Vec2d;
import microtrafficsim.math.Vec2f;
import microtrafficsim.math.Vec2i;
import microtrafficsim.math.Vec3d;
import microtrafficsim.utils.resources.PackagedResource;
import microtrafficsim.utils.resources.Resource;

import java.nio.ByteBuffer;
import java.util.Collection;
import java.util.function.Supplier;


// TODO: shader based anti-aliasing in fragment-shader

public class ShaderBasedVehicleOverlay implements Overlay {
	
	private final static Color DEFAULT_FG_COLOR = Color.fromRGB(0xCC4C1A);
	
	private final static Vec2f VEHICLE_SIZE = new Vec2f(7.5f, 7.5f);
	private final static float VEHICLE_SCALE_NORM = 1.f / (1 << 18);
	private final static int VIEWPORT_CULLING_EXPANSION = 20;
	
	private final static Resource SHADER_VERT = new PackagedResource(
			ShaderBasedVehicleOverlay.class,
			"/shaders/overlay/vehicle/shaderbased/vehicle_overlay.vs"
	);
	
	private final static Resource SHADER_GEOM = new PackagedResource(
			ShaderBasedVehicleOverlay.class,
			"/shaders/overlay/vehicle/shaderbased/vehicle_overlay.gs"
	);
	
	private final static Resource SHADER_FRAG = new PackagedResource(
			ShaderBasedVehicleOverlay.class,
			"/shaders/overlay/vehicle/shaderbased/vehicle_overlay.fs"
	);
	
	
	private Simulation simulation;
	private Projection projection;

	private OrthographicView view;

	private final Supplier<IVisualizationVehicle> vehicleFactory;

	private int vao;
	private BufferStorage vbo;
	
	private VertexAttributePointer ptrPosition;
	private VertexAttributePointer ptrNormal;
	private VertexAttributePointer ptrColor;
	
	private ShaderProgram prog;
	private UniformVec2f uVehicleSize;
	private Uniform1f uVehicleScale;
	
	private boolean enabled;

	
	public ShaderBasedVehicleOverlay(Projection projection) {
		this(projection, DEFAULT_FG_COLOR);
	}
	
	public ShaderBasedVehicleOverlay(Projection projection, Color defaultVehicleColor) {
		this.simulation = null;
		this.projection = projection;

		this.vehicleFactory = () -> new Vehicle(defaultVehicleColor);

		this.vao = -1;
		this.vbo = null;
		
		this.ptrPosition = null;
		this.ptrNormal = null;
		this.ptrColor = null;
		
		this.prog = null;
		this.uVehicleSize = null;
		this.uVehicleScale = null;

		this.enabled = true;
	}

	public void setView(OrthographicView view) {
		this.view = view;
	}

	
	@Override
	public void init(RenderContext context) {
		GL3 gl = context.getDrawable().getGL().getGL3();
		
		// load shader
		Shader vs = Shader.create(gl, GL3.GL_VERTEX_SHADER, "shaderbased.vehicle_overlay.vs")
				.loadFromResource(SHADER_VERT)
				.compile(gl);
		
		Shader gs = Shader.create(gl, GL3.GL_GEOMETRY_SHADER, "shaderbased.vehicle_overlay.gs")
				.loadFromResource(SHADER_GEOM)
				.compile(gl);
		
		Shader fs = Shader.create(gl, GL3.GL_FRAGMENT_SHADER, "shaderbased.vehicle_overlay.fs")
				.loadFromResource(SHADER_FRAG)
				.compile(gl);
		
		prog = ShaderProgram.create(gl, context, "shaderbased.vehicle_overlay")
				.attach(gl, vs, gs, fs)
				.link(gl)
				.detach(gl, vs, gs, fs);
		
		vs.dispose(gl);
		gs.dispose(gl);
		fs.dispose(gl);
		
		uVehicleSize = (UniformVec2f) prog.getUniform("u_vehicle_size");
		uVehicleScale = (Uniform1f) prog.getUniform("u_vehicle_scale");
		
		uVehicleSize.set(VEHICLE_SIZE);
		
		// create vbo, vao
		int[] obj = { -1, -1 };
		
		gl.glGenVertexArrays(1, obj, 0);
		gl.glGenBuffers(1, obj, 1);
		
		vao = obj[0];
		
		vbo = new BufferStorage(GL3.GL_ARRAY_BUFFER, obj[1]);
		ptrPosition = VertexAttributePointer.create(VertexAttributes.POSITION2, DataTypes.FLOAT_2, vbo, 20, 0);
		ptrNormal = VertexAttributePointer.create(VertexAttributes.NORMAL2, DataTypes.FLOAT_2, vbo, 20, 8);
		ptrColor = VertexAttributePointer.create(VertexAttributes.COLOR, DataTypes.UNSIGNED_BYTE_4, vbo, 20, 16);
			
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
		int[] obj = { vao, vbo.handle };
		
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
		GL3 gl = context.getDrawable().getGL().getGL3();
		
		// NOTE: assumes z-axis top-down orthographic projection
		uVehicleScale.set((float) Math.pow(2, VEHICLE_SCALE_NORM * ((OrthographicView) view).getScale()));
		
		// disable depth test
		context.DepthTest.disable(gl);
		
		// calculate bounding rectangle, assumes top-down (z-axis) orthographic view
		double invscale = 1.f / ((OrthographicView) view).getScale();
		Vec3d viewpos = view.getPosition();
		Vec2i viewport = view.getSize();
		
		double vx = invscale * (viewport.x + VIEWPORT_CULLING_EXPANSION) / 2;
		double vy = invscale * (viewport.y + VIEWPORT_CULLING_EXPANSION) / 2;

		double left = viewpos.x - vx;
		double right = viewpos.x + vx;
		double bottom = viewpos.y - vy;
		double top = viewpos.y + vy;
		
		// update vehicle list
		Collection<? extends LogicVehicleEntity> vehicles = simulation.getSpawnedVehicles();
		int len = vehicles.size();
		if (len == 0) return;
		
		// orphan last buffer and load it to a new one
		gl.glBindBuffer(vbo.target, vbo.handle);
		gl.glBufferData(vbo.target, len * 5 * 4L, null, GL3.GL_DYNAMIC_DRAW);
		
		ByteBuffer buffer = gl.glMapBufferRange(vbo.target, 0, len * 5 * 4L,
				GL3.GL_MAP_WRITE_BIT | GL3.GL_MAP_INVALIDATE_BUFFER_BIT);
		
		// write positions
		int vehicleCount = 0;
		for (LogicVehicleEntity logic : vehicles) {
			IVisualizationVehicle v = logic.getEntity().getVisualization();
			
			Coordinate cpos = v.getPosition();
			Vec2d pos = projection.project(cpos);
			
			// continue if out of bounds
			if (pos.x < left || pos.x > right || pos.y < bottom || pos.y > top)
				continue;
			
			Coordinate ctarget = v.getTarget();
			Vec2d dir = projection.project(ctarget).sub(pos).normalize();
			
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
		prog.bind(gl);
		gl.glBindVertexArray(vao);
		gl.glDrawArrays(GL3.GL_POINTS, 0, vehicleCount);
		gl.glBindVertexArray(0);
		prog.unbind(gl);
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
	
	
	public void setSimulation(Simulation simulation) {
		this.simulation = simulation;
	}
	
	public Simulation getSimulation() {
		return simulation;
	}
	
	
	public Supplier<IVisualizationVehicle> getVehicleFactory() {
		return vehicleFactory;
	}
}
