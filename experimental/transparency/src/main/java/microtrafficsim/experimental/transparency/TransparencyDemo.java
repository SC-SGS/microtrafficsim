package microtrafficsim.experimental.transparency;

import com.jogamp.newt.event.KeyEvent;
import com.jogamp.newt.event.KeyListener;
import com.jogamp.newt.event.WindowAdapter;
import com.jogamp.newt.event.WindowEvent;
import com.jogamp.newt.opengl.GLWindow;
import com.jogamp.opengl.GL3;
import com.jogamp.opengl.GLCapabilities;
import com.jogamp.opengl.GLProfile;
import com.jogamp.opengl.util.FPSAnimator;
import microtrafficsim.core.vis.Renderer;
import microtrafficsim.core.vis.context.RenderContext;
import microtrafficsim.core.vis.opengl.BufferStorage;
import microtrafficsim.core.vis.opengl.DataTypes;
import microtrafficsim.core.vis.opengl.shader.Shader;
import microtrafficsim.core.vis.opengl.shader.ShaderCompileError;
import microtrafficsim.core.vis.opengl.shader.ShaderLinkError;
import microtrafficsim.core.vis.opengl.shader.ShaderProgram;
import microtrafficsim.core.vis.opengl.shader.attributes.VertexArrayObject;
import microtrafficsim.core.vis.opengl.shader.attributes.VertexAttributePointer;
import microtrafficsim.core.vis.opengl.shader.attributes.VertexAttributes;
import microtrafficsim.core.vis.opengl.shader.uniforms.Uniform1i;
import microtrafficsim.core.vis.opengl.shader.uniforms.UniformMat4f;
import microtrafficsim.core.vis.opengl.shader.uniforms.UniformSampler2D;
import microtrafficsim.core.vis.opengl.shader.uniforms.UniformVec2f;
import microtrafficsim.core.vis.opengl.utils.Color;
import microtrafficsim.core.vis.opengl.utils.DebugUtils;
import microtrafficsim.math.Mat4f;
import microtrafficsim.math.Vec3f;
import microtrafficsim.utils.resources.PackagedResource;
import microtrafficsim.utils.resources.Resource;

import java.io.PrintStream;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;


class TransparencyDemo implements Renderer {

	public static final int WINDOW_WIDTH = 1600;
	public static final int WINDOW_HEIGHT = 900;

	public static final float ZNEAR = 0.1f;
	public static final float ZFAR = 100.f;

	public static final Color CLEAR_COLOR = new Color(0.75f, 0.75f, 0.75f, 1.0f);

	public static final Resource DEFAULT_VERT_SHADER = new PackagedResource(TransparencyDemo.class, "default.vs");
	public static final Resource DEFAULT_FRAG_SHADER = new PackagedResource(TransparencyDemo.class, "default.fs");
	public static final Resource OIT_ACCUM_VERT_SHADER = new PackagedResource(TransparencyDemo.class, "oit_accum.vs");
	public static final Resource OIT_ACCUM_FRAG_SHADER = new PackagedResource(TransparencyDemo.class, "oit_accum.fs");
	public static final Resource OIT_BLEND_VERT_SHADER = new PackagedResource(TransparencyDemo.class, "oit_blend.vs");
	public static final Resource OIT_BLEND_FRAG_SHADER = new PackagedResource(TransparencyDemo.class, "oit_blend.fs");

	public static final int PRIMITIVE_RESTART_INDEX = 0xFFFFFFFF;

	public static final float[] GEOM_VERTICES = {
			-1.0f,  1.0f, -0.5f,
			-1.0f, -1.0f, -0.5f,
			 1.0f,  1.0f, -0.5f,
			 1.0f, -1.0f, -0.5f,

			-1.0f,  1.0f,  0.0f,
			-1.0f, -1.0f,  0.0f,
			 1.0f,  1.0f,  0.0f,
			 1.0f, -1.0f,  0.0f,

			-1.0f,  1.0f,  0.5f,
			-1.0f, -1.0f,  0.5f,
			 1.0f,  1.0f,  0.5f,
			 1.0f, -1.0f,  0.5f,
	};

	public static final float[] GEOM_COLORS = {
			1.0f, 0.0f, 0.0f, 0.5f,
			1.0f, 0.0f, 0.0f, 0.5f,
			1.0f, 0.0f, 0.0f, 0.5f,
			1.0f, 0.0f, 0.0f, 0.5f,

			1.0f, 1.0f, 0.0f, 0.25f,
			1.0f, 1.0f, 0.0f, 0.25f,
			1.0f, 1.0f, 0.0f, 0.25f,
			1.0f, 1.0f, 0.0f, 0.25f,

			0.0f, 0.0f, 1.0f, 0.5f,
			0.0f, 0.0f, 1.0f, 0.5f,
			0.0f, 0.0f, 1.0f, 0.5f,
			0.0f, 0.0f, 1.0f, 0.5f
	};

	public static final int[] GEOM_INDICES_FWD = {
			0, 1,  2,  3, PRIMITIVE_RESTART_INDEX,
			4, 5,  6,  7, PRIMITIVE_RESTART_INDEX,
			8, 9, 10, 11
	};

	public static final int[] GEOM_INDICES_BWD = {
			8, 9, 10, 11, PRIMITIVE_RESTART_INDEX,
			4, 5,  6,  7, PRIMITIVE_RESTART_INDEX,
			0, 1,  2,  3
	};


	public static final int TEXUNIT_FB_ACCUMULATION = 0;
	public static final int TEXUNIT_FB_REVEALAGE = 1;


	public enum Mode {
		BLEND_NONE(0, "No Blending"),
		BLEND_ALPHA(0, "Default Alpha-Blending"),
		BLEND_WBOIT(0, "Weighted, Blended Order-Independent Transparency (WBOIT)"),
		BUFFER_WBOIT_ACCUM(1, "WBOIT: Accumulation-Buffer"),
		BUFFER_WBOIT_REVEALAGE(2, "WBOIT: Revealage-Buffer");

		public final int shaderId;
		public final String name;
		Mode(int shaderId, String name) {
			this.shaderId = shaderId;
			this.name = name;
		}
	}


	private GLWindow window;
	private RenderContext context;
	private Mode mode;
	private KeyListener keylistener;

	// shaders
	private ShaderProgram defaultShader;
	private ShaderProgram accumShader;
	private ShaderProgram blendShader;

	private UniformVec2f uZPlane;

	private UniformMat4f uMatrixViewProjection;
	private UniformMat4f uMatrixModel;

	private Uniform1i uMode;
	private UniformSampler2D uTexAccumulation;
	private UniformSampler2D uTexRevealage;

	// geometry
	private VertexArrayObject empty;

	private VertexArrayObject geomFwdVAO;
	private VertexArrayObject geomBwdVAO;
	private BufferStorage geomVBO;
	private BufferStorage geomFwdIBO;
	private BufferStorage geomBwdIBO;

	// framebuffers and textures
	int framebuffer;
	int fbTexAccum;
	int fbTexReveal;

	// matrices
	Mat4f viewprojection;
	Mat4f modelFwd;
	Mat4f modelBwd;


	public TransparencyDemo(GLWindow window, RenderContext context) {
		this.window = window;
		this.context = context;
		this.mode = Mode.BLEND_ALPHA;
		this.keylistener = new KeyListenerImpl();

		System.out.println("Current Mode: " + mode.name);
	}


	@Override
	public void init(RenderContext context) {
		DebugUtils.setDebugGL(context.getDrawable());
		GL3 gl = context.getDrawable().getGL().getGL3();

		context.PrimitiveRestart.enable(gl);
		context.PrimitiveRestart.setIndex(gl, PRIMITIVE_RESTART_INDEX);

		context.ClearColor.set(gl, CLEAR_COLOR);

		initShaders(gl);
		initGeometry(gl);
		initFrameBuffers(gl);

		setMatrices();
	}

	private void initShaders(GL3 gl) {
		// set global uniforms
		uMatrixModel = (UniformMat4f) context.getUniformManager().putGlobalUniform("u_model", DataTypes.FLOAT_MAT4);
		uMatrixViewProjection = (UniformMat4f) context.getUniformManager().putGlobalUniform("u_viewprojection", DataTypes.FLOAT_MAT4);

		// set default attributes
		context.getVertexAttribManager().putDefaultAttributeBinding("a_position", VertexAttributes.POSITION3);
		context.getVertexAttribManager().putDefaultAttributeBinding("a_color", VertexAttributes.COLOR);

		// default program
		{
			Shader vs = Shader.create(gl, GL3.GL_VERTEX_SHADER, "default.vs")
					.loadFromResource(DEFAULT_VERT_SHADER)
					.compile(gl);

			Shader fs = Shader.create(gl, GL3.GL_FRAGMENT_SHADER, "default.fs")
					.loadFromResource(DEFAULT_FRAG_SHADER)
					.compile(gl);

			defaultShader = ShaderProgram.create(gl, context, "default")
					.attach(gl, vs, fs)
					.link(gl)
					.detach(gl, vs, fs);

			vs.dispose(gl);
			fs.dispose(gl);
		}

		// accumulation program
		{
			Shader vs = Shader.create(gl, GL3.GL_VERTEX_SHADER, "oit_accum.vs")
					.loadFromResource(OIT_ACCUM_VERT_SHADER)
					.compile(gl);

			Shader fs = Shader.create(gl, GL3.GL_FRAGMENT_SHADER, "oit_accum.fs")
					.loadFromResource(OIT_ACCUM_FRAG_SHADER)
					.compile(gl);

			accumShader = ShaderProgram.create(gl, context, "oit_accum");
			accumShader.attach(gl, vs, fs);
			gl.glBindFragDataLocation(accumShader.getHandle(), 0, "out_accumulation");
			gl.glBindFragDataLocation(accumShader.getHandle(), 1, "out_revealage");
			accumShader.link(gl);
			accumShader.detach(gl, vs, fs);

			uZPlane = (UniformVec2f) accumShader.getUniform("u_zplane");
			uZPlane.set(ZNEAR, ZFAR);

			vs.dispose(gl);
			fs.dispose(gl);
		}

		// blending program
		{
			Shader vs = Shader.create(gl, GL3.GL_VERTEX_SHADER, "oit_blend.vs")
					.loadFromResource(OIT_BLEND_VERT_SHADER)
					.compile(gl);

			Shader fs = Shader.create(gl, GL3.GL_FRAGMENT_SHADER, "oit_blend.fs")
					.loadFromResource(OIT_BLEND_FRAG_SHADER)
					.compile(gl);

			blendShader = ShaderProgram.create(gl, context, "oit_blend")
					.attach(gl, vs, fs)
					.link(gl)
					.detach(gl, vs, fs);

			uMode = (Uniform1i) blendShader.getUniform("u_mode");
			uTexAccumulation = (UniformSampler2D) blendShader.getUniform("u_accumulation");
			uTexRevealage = (UniformSampler2D) blendShader.getUniform("u_revealage");

			uTexAccumulation.set(TEXUNIT_FB_ACCUMULATION);
			uTexRevealage.set(TEXUNIT_FB_REVEALAGE);

			vs.dispose(gl);
			fs.dispose(gl);
		}
	}

	private void initGeometry(GL3 gl) {
		// empty VAO for blending
		empty = VertexArrayObject.create(gl);
		empty.bind(gl);
		empty.unbind(gl);

		// generate and load vertex buffer data
		FloatBuffer vbodata = FloatBuffer.allocate(GEOM_VERTICES.length * (3 + 4));
		for (int i = 0; i < GEOM_VERTICES.length / 3; i++) {
			vbodata.put(GEOM_VERTICES[i * 3    ]);
			vbodata.put(GEOM_VERTICES[i * 3 + 1]);
			vbodata.put(GEOM_VERTICES[i * 3 + 2]);

			vbodata.put(GEOM_COLORS[i * 4    ]);
			vbodata.put(GEOM_COLORS[i * 4 + 1]);
			vbodata.put(GEOM_COLORS[i * 4 + 2]);
			vbodata.put(GEOM_COLORS[i * 4 + 3]);
		}
		vbodata.rewind();

		geomVBO = BufferStorage.create(gl, GL3.GL_ARRAY_BUFFER);
		geomVBO.bind(gl);
		gl.glBufferData(GL3.GL_ARRAY_BUFFER, vbodata.capacity() * 4, vbodata, GL3.GL_STATIC_DRAW);
		geomVBO.unbind(gl);


		// init geometry vertex attribute pointers
		VertexAttributePointer ptrPosition = VertexAttributePointer.create(VertexAttributes.POSITION3, DataTypes.FLOAT_3, geomVBO, 28, 0);
		VertexAttributePointer ptrColor = VertexAttributePointer.create(VertexAttributes.COLOR, DataTypes.FLOAT_4, geomVBO, 28, 12);

		// set up forward geometry
		{
			// generate and load index buffer data
			IntBuffer ibodata = IntBuffer.allocate(GEOM_INDICES_FWD.length);
			for (int i : GEOM_INDICES_FWD) {
				ibodata.put(i);
			}
			ibodata.rewind();

			geomFwdIBO = BufferStorage.create(gl, GL3.GL_ELEMENT_ARRAY_BUFFER);
			geomFwdIBO.bind(gl);
			gl.glBufferData(GL3.GL_ELEMENT_ARRAY_BUFFER, ibodata.capacity() * 4, ibodata, GL3.GL_STATIC_DRAW);
			geomFwdIBO.unbind(gl);

			// init geometry VAO
			geomFwdVAO = VertexArrayObject.create(gl);
			geomFwdVAO.bind(gl);
			geomVBO.bind(gl);
			geomFwdIBO.bind(gl);

			ptrPosition.set(gl);
			ptrPosition.enable(gl);

			ptrColor.set(gl);
			ptrColor.enable(gl);

			geomFwdVAO.unbind(gl);
		}

		// set up backward geometry
		{
			// generate and load index buffer data
			IntBuffer ibodata= IntBuffer.allocate(GEOM_INDICES_BWD.length);
			for (int i : GEOM_INDICES_BWD) {
				ibodata.put(i);
			}
			ibodata.rewind();

			geomBwdIBO = BufferStorage.create(gl, GL3.GL_ELEMENT_ARRAY_BUFFER);
			geomBwdIBO.bind(gl);
			gl.glBufferData(GL3.GL_ELEMENT_ARRAY_BUFFER, ibodata.capacity() * 4, ibodata, GL3.GL_STATIC_DRAW);
			geomBwdIBO.unbind(gl);

			// init geometry VAO
			geomBwdVAO = VertexArrayObject.create(gl);
			geomBwdVAO.bind(gl);
			geomVBO.bind(gl);
			geomBwdIBO.bind(gl);

			ptrPosition.set(gl);
			ptrPosition.enable(gl);

			ptrColor.set(gl);
			ptrColor.enable(gl);

			geomBwdVAO.unbind(gl);
		}
	}

	private void initFrameBuffers(GL3 gl) {
		final int width = context.getDrawable().getSurfaceWidth();
		final int height = context.getDrawable().getSurfaceHeight();

		int[] obj = { -1, -1, -1 };
		gl.glGenFramebuffers(1, obj, 0);
		framebuffer = obj[0];

		gl.glGenTextures(2, obj, 1);
		fbTexAccum = obj[1];
		fbTexReveal = obj[2];

		gl.glBindFramebuffer(GL3.GL_FRAMEBUFFER, framebuffer);

		gl.glBindTexture(GL3.GL_TEXTURE_2D, fbTexAccum);
		gl.glTexImage2D(GL3.GL_TEXTURE_2D, 0, GL3.GL_RGBA16F, width, height, 0, GL3.GL_RGBA, GL3.GL_UNSIGNED_SHORT, null);
		gl.glTexParameteri(GL3.GL_TEXTURE_2D, GL3.GL_TEXTURE_MAG_FILTER, GL3.GL_NEAREST);
		gl.glTexParameteri(GL3.GL_TEXTURE_2D, GL3.GL_TEXTURE_MIN_FILTER, GL3.GL_NEAREST);
		gl.glFramebufferTexture(GL3.GL_FRAMEBUFFER, GL3.GL_COLOR_ATTACHMENT0, fbTexAccum, 0);

		gl.glBindTexture(GL3.GL_TEXTURE_2D, fbTexReveal);
		gl.glTexImage2D(GL3.GL_TEXTURE_2D, 0, GL3.GL_R8, width, height, 0, GL3.GL_RED, GL3.GL_UNSIGNED_BYTE, null);
		gl.glTexParameteri(GL3.GL_TEXTURE_2D, GL3.GL_TEXTURE_MAG_FILTER, GL3.GL_NEAREST);
		gl.glTexParameteri(GL3.GL_TEXTURE_2D, GL3.GL_TEXTURE_MIN_FILTER, GL3.GL_NEAREST);
		gl.glFramebufferTexture(GL3.GL_FRAMEBUFFER, GL3.GL_COLOR_ATTACHMENT1, fbTexReveal, 0);
		gl.glBindTexture(GL3.GL_TEXTURE_2D, 0);

		gl.glBindFramebuffer(GL3.GL_FRAMEBUFFER, 0);
	}

	private void setMatrices() {
		Vec3f position = new Vec3f(0, 0, 10);
		Vec3f center = new Vec3f(0, 0, 0);
		Vec3f up = new Vec3f(0, 1, 0);

		Mat4f projection = Mat4f.perspecive(90.f, WINDOW_WIDTH / (float) WINDOW_HEIGHT, ZNEAR, ZFAR);
		Mat4f view = Mat4f.lookAt(position, center, up);

		viewprojection = Mat4f.mul(projection, view);
		modelFwd = Mat4f.identity()
				.rotate(0, 1, 0, 10)
				.translate(10, 0, 0)
				.scale(5, 5, 5);

		modelBwd = Mat4f.identity()
				.rotate(0, 1, 0, -10)
				.translate(-10, 0, 0)
				.scale(5, 5, 5);

		uMatrixViewProjection.set(viewprojection);
	}


	@Override
	public void dispose(RenderContext context) {
		GL3 gl = context.getDrawable().getGL().getGL3();

		gl.glDeleteFramebuffers(1, new int[] { framebuffer }, 0);
		gl.glDeleteTextures(2, new int[] { fbTexAccum, fbTexReveal }, 0);

		geomFwdVAO.dispose(gl);
		geomFwdIBO.dispose(gl);
		geomBwdVAO.dispose(gl);
		geomBwdIBO.dispose(gl);
		geomVBO.dispose(gl);

		empty.dispose(gl);

		accumShader.dispose(gl);
		blendShader.dispose(gl);
	}

	@Override
	public void display(RenderContext context) {
		if (mode == Mode.BLEND_NONE || mode == Mode.BLEND_ALPHA)
			drawDefault(context);
		else
			drawOIT(context);
	}

	private void drawDefault(RenderContext context) {
		GL3 gl = context.getDrawable().getGL().getGL3();

		if (mode == Mode.BLEND_NONE) {
			context.BlendMode.disable(gl);
		} else {
			context.BlendMode.enable(gl);
			context.BlendMode.setFactors(gl, GL3.GL_SRC_ALPHA, GL3.GL_ONE_MINUS_SRC_ALPHA);
		}

		gl.glClear(GL3.GL_COLOR_BUFFER_BIT | GL3.GL_DEPTH_BUFFER_BIT);

		defaultShader.bind(gl);
		drawGeometry(gl);
		defaultShader.unbind(gl);
	}

	private void drawOIT(RenderContext context) {
		GL3 gl = context.getDrawable().getGL().getGL3();

		gl.glClear(GL3.GL_COLOR_BUFFER_BIT | GL3.GL_DEPTH_BUFFER_BIT);

		uMode.set(mode.shaderId);
		context.BlendMode.enable(gl);

		// geometry-pass
		{
			gl.glBindFramebuffer(GL3.GL_FRAMEBUFFER, framebuffer);
			gl.glDrawBuffers(2, new int[] {GL3.GL_COLOR_ATTACHMENT0, GL3.GL_COLOR_ATTACHMENT1}, 0);

			gl.glClearBufferfv(GL3.GL_COLOR, 0, new float[]{0, 0, 0, 0}, 0);
			gl.glClearBufferfv(GL3.GL_COLOR, 1, new float[]{1}, 0);

			// Note: when using depth-buffer disable write to depth
			context.BlendMode.invalidate();
			gl.glBlendFunci(0, GL3.GL_ONE, GL3.GL_ONE);
			gl.glBlendFunci(1, GL3.GL_ZERO, GL3.GL_ONE_MINUS_SRC_COLOR);

			accumShader.bind(gl);
			drawGeometry(gl);
			accumShader.unbind(gl);

			gl.glBindFramebuffer(GL3.GL_FRAMEBUFFER, 0);
		}

		// blend-pass
		{
			context.BlendMode.setFactors(gl, GL3.GL_ONE_MINUS_SRC_ALPHA, GL3.GL_SRC_ALPHA);

			gl.glActiveTexture(GL3.GL_TEXTURE0 + TEXUNIT_FB_ACCUMULATION);
			gl.glBindTexture(GL3.GL_TEXTURE_2D, fbTexAccum);

			gl.glActiveTexture(GL3.GL_TEXTURE0 + TEXUNIT_FB_REVEALAGE);
			gl.glBindTexture(GL3.GL_TEXTURE_2D, fbTexReveal);

			empty.bind(gl);
			blendShader.bind(gl);
			gl.glDrawArrays(GL3.GL_TRIANGLES, 0, 3);
			blendShader.unbind(gl);
			empty.unbind(gl);

			gl.glActiveTexture(GL3.GL_TEXTURE1);
			gl.glBindTexture(GL3.GL_TEXTURE_2D, 0);

			gl.glActiveTexture(GL3.GL_TEXTURE0);
			gl.glBindTexture(GL3.GL_TEXTURE_2D, 0);
		}
	}

	private void drawGeometry(GL3 gl) {
		geomFwdVAO.bind(gl);
		uMatrixModel.set(modelFwd);
		gl.glDrawElements(GL3.GL_TRIANGLE_STRIP, GEOM_INDICES_FWD.length, GL3.GL_UNSIGNED_INT, 0);

		geomBwdVAO.bind(gl);
		uMatrixModel.set(modelBwd);
		gl.glDrawElements(GL3.GL_TRIANGLE_STRIP, GEOM_INDICES_BWD.length, GL3.GL_UNSIGNED_INT, 0);
		geomBwdVAO.unbind(gl);
	}

	@Override
	public void reshape(RenderContext context, int x, int y, int width, int height) {
		GL3 gl = context.getDrawable().getGL().getGL3();

		gl.glBindTexture(GL3.GL_TEXTURE_2D, fbTexAccum);
		gl.glTexImage2D(GL3.GL_TEXTURE_2D, 0, GL3.GL_RGBA16F, width, height, 0, GL3.GL_RGBA, GL3.GL_UNSIGNED_SHORT, null);

		gl.glBindTexture(GL3.GL_TEXTURE_2D, fbTexReveal);
		gl.glTexImage2D(GL3.GL_TEXTURE_2D, 0, GL3.GL_R8, width, height, 0, GL3.GL_RED, GL3.GL_UNSIGNED_BYTE, null);
		gl.glBindTexture(GL3.GL_TEXTURE_2D, 0);
	}


	public KeyListener getKeyListener() {
		return keylistener;
	}


	private class KeyListenerImpl implements KeyListener {
		@Override public void keyPressed(KeyEvent e) {}

		@Override
		public void keyReleased(KeyEvent e) {
			Mode prev = mode;

			switch (e.getKeyCode()) {
			case KeyEvent.VK_F1:
				mode = Mode.BLEND_NONE;
				break;

			case KeyEvent.VK_F2:
				mode = Mode.BLEND_ALPHA;
				break;

			case KeyEvent.VK_F3:
				mode = Mode.BLEND_WBOIT;
				break;

			case KeyEvent.VK_F5:
				mode = Mode.BUFFER_WBOIT_ACCUM;
				break;

			case KeyEvent.VK_F6:
				mode = Mode.BUFFER_WBOIT_REVEALAGE;
				break;

			case KeyEvent.VK_ESCAPE:
				window.getAnimator().stop();
				window.disposeGLEventListener(context, true);
				window.destroy();
				break;
			}

			if (prev != mode)
				System.out.println("Current Mode: " + mode.name);
		}
	}

	public static class DebugExceptionHandler implements RenderContext.UncaughtExceptionHandler {

		@Override
		public void uncaughtException(RenderContext context, Throwable exception) {
			if (exception instanceof ShaderCompileError)
				exceptionPrintf(System.err, (ShaderCompileError) exception);
			else if (exception instanceof ShaderLinkError)
				exceptionPrintf(System.err, (ShaderLinkError) exception);
			else
				exception.printStackTrace();

			// XXX: clean exit strategy?
			Runtime.getRuntime().halt(1);
		}

		private void exceptionPrintf(PrintStream out, ShaderCompileError error) {
			out.println(error.toString());
			out.println("-- LOG -------------------------------------------------------------------------");
			out.println(error.getShaderInfoLog());
			out.println("-- STACK TRACE -----------------------------------------------------------------");
			error.printStackTrace(out);
		}

		private void exceptionPrintf(PrintStream out, ShaderLinkError error) {
			out.println(error.toString());
			out.println("-- LOG -------------------------------------------------------------------------");
			out.println(error.getProgramInfoLog());
			out.println("-- STACK TRACE -----------------------------------------------------------------");
			error.printStackTrace(out);
		}
	}


	public static void main(String[] args) {
		GLProfile profile = GLProfile.get(GLProfile.GL3);
		GLCapabilities caps = new GLCapabilities(profile);

		GLWindow window = GLWindow.create(caps);

		RenderContext context = new RenderContext();
		TransparencyDemo demo = new TransparencyDemo(window, context);
		context.setRenderer(demo);

		window.setTitle("MicroTrafficSim - Transparency Demo");
		window.addGLEventListener(context);
		window.setSize(WINDOW_WIDTH, WINDOW_HEIGHT);

		FPSAnimator animator = new FPSAnimator(window, 60, true);
		context.setAnimator(animator);
		context.setUncaughtExceptionHandler(new DebugExceptionHandler());

		window.addKeyListener(demo.getKeyListener());

		window.addWindowListener(new WindowAdapter() {
			@Override
			public void windowDestroyNotify(WindowEvent e) {
				animator.stop();
				System.exit(0);
			}
		});

		window.setVisible(true);
		animator.start();
	}
}
