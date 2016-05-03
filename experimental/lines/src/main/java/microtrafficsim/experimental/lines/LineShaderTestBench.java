package microtrafficsim.experimental.lines;

import com.jogamp.newt.event.KeyEvent;
import com.jogamp.newt.event.WindowAdapter;
import com.jogamp.newt.event.WindowEvent;
import com.jogamp.newt.opengl.GLWindow;
import com.jogamp.opengl.GL3;
import com.jogamp.opengl.GLCapabilities;
import com.jogamp.opengl.GLProfile;
import com.jogamp.opengl.util.FPSAnimator;
import microtrafficsim.core.vis.Renderer;
import microtrafficsim.core.vis.context.RenderContext;
import microtrafficsim.core.vis.input.OrthoInputController;
import microtrafficsim.core.vis.opengl.DataTypes;
import microtrafficsim.core.vis.opengl.shader.*;
import microtrafficsim.core.vis.opengl.shader.attributes.VertexAttributes;
import microtrafficsim.core.vis.opengl.shader.uniforms.*;
import microtrafficsim.core.vis.opengl.utils.Color;
import microtrafficsim.core.vis.opengl.utils.DebugUtils;
import microtrafficsim.core.vis.opengl.utils.FramebufferUtils;
import microtrafficsim.core.vis.view.OrthographicView;
import microtrafficsim.math.Vec3f;
import microtrafficsim.utils.resources.FileResource;
import microtrafficsim.utils.resources.PackagedResource;
import microtrafficsim.utils.resources.Resource;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;


class LineShaderTestBench implements Renderer {

	private static final int WINDOW_WIDTH = 1600;
	private static final int WINDOW_HEIGHT = 900;

	private static final float ZNEAR = 0.1f;
	private static final float ZFAR = 100.f;

	private static final float ZOOM_MIN = -5;
	private static final float ZOOM_MAX =  10;
	private static final float ZOOM_MULTIPLIER = 0.1f;

	private static final Color BG_COLOR = Color.fromRGB(0xFFFFFF);
	private static final Color FG_COLOR = Color.fromRGB(0x8FC270);

	private static final int CAP_TYPE_BUTT =   0;
	private static final int CAP_TYPE_SQUARE = 1;
	private static final int CAP_TYPE_ROUND =  2;

	private static final int JOIN_TYPE_NONE =  0;
	private static final int JOIN_TYPE_MITER = 1;
	private static final int JOIN_TYPE_BEVEL = 2;
	private static final int JOIN_TYPE_ROUND = 3;

	private Resource linesVertShader;
	private Resource linesGeomShader;
	private Resource linesFragShader;

	private GLWindow window;
	private RenderContext context;
	private OrthographicView view;

	// shader
	private ShaderProgram shader;
	private UniformMat4f uMatrixViewProjection;
	private UniformVec4f uViewPort;
	private Uniform1f uViewScale;
	private Uniform1f uLineBlur;

	private Uniforms uniforms;

	// geometry
	private Model model;
	private ModelInstance[] instances;

	private int angle1;
	private int angle2;
	private int lineblur;


	private LineShaderTestBench(String shaderPath, GLWindow window, RenderContext context) {
		if (shaderPath == null) {
			linesVertShader = new PackagedResource(LineShaderTestBench.class, "lines.vs");
			linesFragShader = new PackagedResource(LineShaderTestBench.class, "lines.fs");
			linesGeomShader = new PackagedResource(LineShaderTestBench.class, "lines.gs");
		} else {
			linesVertShader = new FileResource(new File(shaderPath + "/lines.vs"));
			linesFragShader = new FileResource(new File(shaderPath + "/lines.fs"));
			linesGeomShader = new FileResource(new File(shaderPath + "/lines.gs"));
		}

		this.window = window;
		this.context = context;

		this.view = new OrthographicView(WINDOW_WIDTH, WINDOW_HEIGHT, ZNEAR, ZFAR, ZOOM_MIN, ZOOM_MAX);
		this.view.setPosition(0, 0);
		OrthoInputController controller = new OrthoInputController(view, ZOOM_MULTIPLIER);
		setKeyCommands(controller);

		this.window.addKeyListener(controller);
		this.window.addMouseListener(controller);

		this.uMatrixViewProjection = null;

		this.model = null;
		this.instances = new ModelInstance[0];

		this.angle1 = 0;
		this.angle2 = 180;
		this.lineblur = 5;
	}

	private void setKeyCommands(OrthoInputController controller) {
		/* line angle */
		controller.addKeyCommand(
				KeyEvent.EVENT_KEY_PRESSED,
				KeyEvent.VK_UP,
				e -> {
					int a = (e.getModifiers() & KeyEvent.CTRL_MASK) != 0 ? 1 : 5;
					angle1 = (angle1 + a) % 360;
					if (model != null) model.setAngle1(angle1);
				});

		controller.addKeyCommand(
				KeyEvent.EVENT_KEY_PRESSED,
				KeyEvent.VK_DOWN,
				e -> {
					int a = (e.getModifiers() & KeyEvent.CTRL_MASK) != 0 ? 1 : 5;
					angle1 = (angle1 - a) % 360;
					if (model != null) model.setAngle1(angle1);
				});

		controller.addKeyCommand(
				KeyEvent.EVENT_KEY_PRESSED,
				KeyEvent.VK_LEFT,
				e -> {
					int a = (e.getModifiers() & KeyEvent.CTRL_MASK) != 0 ? 1 : 5;
					angle2 = (angle2 + a) % 360;
					if (model != null) model.setAngle2(angle2);
				});

		controller.addKeyCommand(
				KeyEvent.EVENT_KEY_PRESSED,
				KeyEvent.VK_RIGHT,
				e -> {
					int a = (e.getModifiers() & KeyEvent.CTRL_MASK) != 0 ? 1 : 5;
					angle2 = (angle2 - a) % 360;
					if (model != null) model.setAngle2(angle2);
				});

		controller.addKeyCommand(
				KeyEvent.EVENT_KEY_RELEASED,
				KeyEvent.VK_ESCAPE,
				e -> {
					window.getAnimator().stop();
					window.disposeGLEventListener(context, true);
					window.destroy();
				});

		/* line blur */
		controller.addKeyCommand(
				KeyEvent.EVENT_KEY_PRESSED,
				KeyEvent.VK_PAGE_UP,
				e -> {
					lineblur = lineblur + 1;
					context.addTask(c -> uLineBlur.set(lineblur));
				});

		controller.addKeyCommand(
				KeyEvent.EVENT_KEY_PRESSED,
				KeyEvent.VK_PAGE_DOWN,
				e -> {
					lineblur = Math.max(lineblur - 1, 0);
					context.addTask(c -> uLineBlur.set(lineblur));
				});

		/* screenshot */
		controller.addKeyCommand(
				KeyEvent.EVENT_KEY_RELEASED,
				KeyEvent.VK_F12,
				e -> asyncScreenshot(context));

		/* exit */
		controller.addKeyCommand(
				KeyEvent.EVENT_KEY_RELEASED,
				KeyEvent.VK_F5,
				e -> context.addTask(c -> reloadShaders(c.getDrawable().getGL().getGL3())));
	}


	@Override
	public void init(RenderContext context) {
		DebugUtils.setDebugGL(context.getDrawable());
		GL3 gl = context.getDrawable().getGL().getGL3();

		initShaders(gl);
		initGeometry(gl);

		// set properties
		context.BlendMode.enable(gl);
		context.BlendMode.setEquation(gl, GL3.GL_FUNC_ADD);
		context.BlendMode.setFactors(gl, GL3.GL_SRC_ALPHA, GL3.GL_ONE_MINUS_SRC_ALPHA);

		context.ClearColor.set(gl, BG_COLOR);

 		gl.glEnable(GL3.GL_CULL_FACE);
	}

	private void initShaders(GL3 gl) {
		// set global uniforms
		uMatrixViewProjection = (UniformMat4f) context.getUniformManager().putGlobalUniform("u_viewprojection", DataTypes.FLOAT_MAT4);
		uViewPort = (UniformVec4f) context.getUniformManager().putGlobalUniform("u_viewport", DataTypes.FLOAT_VEC4);
		uViewScale = (Uniform1f) context.getUniformManager().putGlobalUniform("u_viewscale", DataTypes.FLOAT);
		uLineBlur = (Uniform1f) context.getUniformManager().putGlobalUniform("u_lineblur", DataTypes.FLOAT);

		uniforms = new Uniforms(
				(UniformMat4f) context.getUniformManager().putGlobalUniform("u_model", DataTypes.FLOAT_MAT4),
				(UniformVec4f) context.getUniformManager().putGlobalUniform("u_color", DataTypes.FLOAT_VEC4),
				(Uniform1f) context.getUniformManager().putGlobalUniform("u_linewidth", DataTypes.FLOAT),
				(Uniform1i) context.getUniformManager().putGlobalUniform("u_cap_type", DataTypes.INT),
				(Uniform1i) context.getUniformManager().putGlobalUniform("u_join_type", DataTypes.INT)
		);

		// set default attributes
		context.getVertexAttribManager().putDefaultAttributeBinding("a_position", VertexAttributes.POSITION3);

		// load shader
        reloadShaders(gl);
	}

	private void reloadShaders(GL3 gl) {
		boolean error = false;

		try {
			Shader vs = Shader.create(gl, GL3.GL_VERTEX_SHADER, "lines.vs")
					.loadFromResource(linesVertShader)
					.compile(gl);

			Shader gs = Shader.create(gl, GL3.GL_GEOMETRY_SHADER, "lines.gs")
					.loadFromResource(linesGeomShader)
					.compile(gl);

			Shader fs = Shader.create(gl, GL3.GL_FRAGMENT_SHADER, "adjacency.fs")
					.loadFromResource(linesFragShader)
					.compile(gl);

			this.shader = ShaderProgram.create(gl, context, "lines")
					.attach(gl, vs, gs, fs)
					.link(gl)
					.detach(gl, vs, gs, fs);

		} catch (Throwable t) {
			error = true;

			System.out.println("reloading shaders: error");
			exceptionPrintf(System.err, t);
			System.err.println();
		}

		if (!error)
			System.out.println("reloading shaders: success");
	}

	private void initGeometry(GL3 gl) {
		model = new Model();
		model.initialize(gl);

		instances = new ModelInstance[] {
				new ModelInstance(model, new Vec3f(-420, -100.f, 0), 100.f, FG_COLOR, GL3.GL_FILL, 40.f, CAP_TYPE_BUTT,   JOIN_TYPE_NONE, true),
				new ModelInstance(model, new Vec3f(-420,  100.f, 0), 100.f, FG_COLOR, GL3.GL_LINE, 40.f, CAP_TYPE_BUTT,   JOIN_TYPE_NONE, false),
				new ModelInstance(model, new Vec3f(-140, -100.f, 0), 100.f, FG_COLOR, GL3.GL_FILL, 40.f, CAP_TYPE_SQUARE, JOIN_TYPE_MITER, true),
				new ModelInstance(model, new Vec3f(-140,  100.f, 0), 100.f, FG_COLOR, GL3.GL_LINE, 40.f, CAP_TYPE_SQUARE, JOIN_TYPE_MITER, false),
				new ModelInstance(model, new Vec3f( 140, -100.f, 0), 100.f, FG_COLOR, GL3.GL_FILL, 40.f, CAP_TYPE_ROUND,  JOIN_TYPE_BEVEL, true),
				new ModelInstance(model, new Vec3f( 140,  100.f, 0), 100.f, FG_COLOR, GL3.GL_LINE, 40.f, CAP_TYPE_ROUND,  JOIN_TYPE_BEVEL, false),
				new ModelInstance(model, new Vec3f( 420, -100.f, 0), 100.f, FG_COLOR, GL3.GL_FILL, 40.f, CAP_TYPE_ROUND,  JOIN_TYPE_ROUND, true),
				new ModelInstance(model, new Vec3f( 420,  100.f, 0), 100.f, FG_COLOR, GL3.GL_LINE, 40.f, CAP_TYPE_ROUND,  JOIN_TYPE_ROUND, false)
		};
	}


	@Override
	public void dispose(RenderContext context) {
		GL3 gl = context.getDrawable().getGL().getGL3();

		model.dispose(gl);
		shader.dispose(gl);
	}

	@Override
	public void display(RenderContext context) {
		uMatrixViewProjection.set(view.getViewProjection());
		uViewPort.set(view.getSize().x, view.getSize().y, 1.f / view.getSize().x, 1.f/view.getSize().y);
		uViewScale.set((float) view.getScale());

		GL3 gl = context.getDrawable().getGL().getGL3();
		gl.glClear(GL3.GL_COLOR_BUFFER_BIT);

		shader.bind(gl);
		for (ModelInstance instance : instances)
			instance.display(context, uniforms);
		shader.unbind(gl);
	}

	@Override
	public void reshape(RenderContext context, int x, int y, int width, int height) {
		view.resize(width, height);
	}


	private static void asyncScreenshot(RenderContext context) {
		new Thread(() -> {
			JFileChooser chooser = new JFileChooser();
			chooser.setFileFilter(new FileFilter() {

				@Override
				public String getDescription() {
					return ".png";
				}

				@Override
				public boolean accept(File f) {
					if (f.isDirectory()) return true;

					String extension = null;

					String s = f.getName();
					int i = s.lastIndexOf('.');

					if (i > 0 &&  i < s.length() - 1)
						extension = s.substring(i+1).toLowerCase();

					if (extension == null) return false;

					switch (extension) {
						case "png":		return true;
						default:		return false;
					}
				}
			});

			int action = chooser.showSaveDialog(null);

			if (action == JFileChooser.APPROVE_OPTION) {
				File file = chooser.getSelectedFile();
				if (file.exists()) file.delete();

				try {
					file.createNewFile();
				} catch (IOException e) {
					return;
				}

				context.addTask(c -> {
					try {
						FramebufferUtils.writeFramebuffer(c.getDrawable(), "png", file);
					} catch (IOException e) {
						/* ignore if we can't write to the file and clean up */
						if (file.exists())
							file.delete();
					}
				});
			}
		}).start();
	}

	private class DebugExceptionHandler implements RenderContext.UncaughtExceptionHandler {

		@Override
		public void uncaughtException(RenderContext context, Throwable exception) {
			exceptionPrintf(System.err, exception);

			// exit
			window.getAnimator().stop();
			window.disposeGLEventListener(context, true);
			window.destroy();
		}
	}

	private static void exceptionPrintf(PrintStream out, Throwable t) {
		if (t instanceof ShaderCompileError)
			exceptionPrintf(out, (ShaderCompileError) t);
		else if (t instanceof ShaderLinkError)
			exceptionPrintf(out, (ShaderLinkError) t);
		else
			t.printStackTrace();
	}

	private static void exceptionPrintf(PrintStream out, ShaderCompileError error) {
		out.println(error.toString());
		out.println("-- LOG -------------------------------------------------------------------------");
		out.println(error.getShaderInfoLog());
		out.println("-- STACK TRACE -----------------------------------------------------------------");
		error.printStackTrace(out);
	}

	private static void exceptionPrintf(PrintStream out, ShaderLinkError error) {
		out.println(error.toString());
		out.println("-- LOG -------------------------------------------------------------------------");
		out.println(error.getProgramInfoLog());
		out.println("-- STACK TRACE -----------------------------------------------------------------");
		error.printStackTrace(out);
	}


	public static void main(String[] args) {
        String shaderPath;

        if (args.length == 1) {
            switch(args[0]) {
                case "-h":
                case "--help":
                    printUsage();
                    return;

                default:
                    shaderPath = args[0];
            }
        } else {
            shaderPath = null;
        }

		GLProfile profile = GLProfile.get(GLProfile.GL3);
		GLCapabilities caps = new GLCapabilities(profile);

		GLWindow window = GLWindow.create(caps);

		RenderContext context = new RenderContext();
		LineShaderTestBench demo = new LineShaderTestBench(shaderPath, window, context);
		context.setRenderer(demo);

		window.setTitle("MicroTrafficSim - Line Shader Test Bench");
		window.addGLEventListener(context);
		window.setSize(WINDOW_WIDTH, WINDOW_HEIGHT);

		FPSAnimator animator = new FPSAnimator(window, 60, true);
		context.setAnimator(animator);
		context.setUncaughtExceptionHandler(demo.new DebugExceptionHandler());

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

    private static void printUsage() {
        System.out.println("MicroTrafficSim - Line Shader Test Bench");
        System.out.println("");
        System.out.println("Usage:");
        System.out.println("  lines                Run this test bench with the default shader files");
        System.out.println("  lines <path>         Run this example with the specified shader files");
        System.out.println("  lines --help | -h    Show this help message.");
        System.out.println("");
        System.out.println("Notes:");
        System.out.println("  Using the default shader files may disable the ability to reload them.");
        System.out.println("");
    }
}
