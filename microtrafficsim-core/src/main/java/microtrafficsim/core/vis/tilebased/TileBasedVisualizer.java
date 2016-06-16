package microtrafficsim.core.vis.tilebased;

import com.jogamp.opengl.GL3;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLCapabilities;
import com.jogamp.opengl.GLProfile;
import microtrafficsim.core.map.style.StyleSheet;
import microtrafficsim.core.map.tiles.TileId;
import microtrafficsim.core.vis.Overlay;
import microtrafficsim.core.vis.UnsupportedFeatureException;
import microtrafficsim.core.vis.Visualizer;
import microtrafficsim.core.vis.VisualizerConfig;
import microtrafficsim.core.vis.context.RenderContext;
import microtrafficsim.core.vis.context.UniformManager;
import microtrafficsim.core.vis.context.VertexAttributeManager;
import microtrafficsim.core.vis.map.tiles.TileManager;
import microtrafficsim.core.vis.map.tiles.TileProvider;
import microtrafficsim.core.vis.opengl.DataTypes;
import microtrafficsim.core.vis.opengl.shader.Shader;
import microtrafficsim.core.vis.opengl.shader.ShaderProgram;
import microtrafficsim.core.vis.opengl.shader.attributes.VertexArrayObject;
import microtrafficsim.core.vis.opengl.shader.attributes.VertexAttributes;
import microtrafficsim.core.vis.opengl.shader.uniforms.UniformMat4f;
import microtrafficsim.core.vis.opengl.shader.uniforms.UniformSampler2D;
import microtrafficsim.core.vis.opengl.shader.uniforms.UniformVec4f;
import microtrafficsim.core.vis.opengl.utils.Color;
import microtrafficsim.core.vis.opengl.utils.DebugUtils;
import microtrafficsim.core.vis.view.OrthographicView;
import microtrafficsim.core.vis.view.View;
import microtrafficsim.math.Mat4f;
import microtrafficsim.math.Rect2d;
import microtrafficsim.math.Vec2d;
import microtrafficsim.utils.resources.PackagedResource;
import microtrafficsim.utils.resources.Resource;

import java.util.ArrayList;
import java.util.Collection;
import java.util.TreeMap;
import java.util.concurrent.ExecutorService;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static microtrafficsim.build.BuildSetup.DEBUG_VISUALIZATION;


public class TileBasedVisualizer implements Visualizer {

    private static final int DEFAULT_FPS = 60;
    private static final String GL_PROFILE = GLProfile.GL3;

    private static final Resource FBO_COLOR_DEPTH_COPY_VS = new PackagedResource(TileBasedVisualizer.class, "/shaders/fbo_color_depth_copy.vs");
    private static final Resource FBO_COLOR_DEPTH_COPY_FS = new PackagedResource(TileBasedVisualizer.class, "/shaders/fbo_color_depth_copy.fs");

    private static final int FBO_COLOR_TEXUNIT = 0;
    private static final int FBO_DEPTH_TEXUNIT = 1;

    private RenderContext context;
    private OrthographicView view;

    private TileProvider provider;
    private TileManager manager;
    private TreeMap<Integer, Overlay> overlays = new TreeMap<>();

    // back buffer for the map
    private Overlay.MapBuffer backbuffer = null;
    private VertexArrayObject empty;
    private ShaderProgram fboCopyShader;

    // global uniforms
    private UniformMat4f uView;
    private UniformMat4f uProjection;
    private UniformVec4f uViewport;

    private Color bgcolor = Color.fromRGBA(0x000000FF);


    public TileBasedVisualizer(
            RenderContext context,
            OrthographicView view,
            TileProvider provider,
            ExecutorService worker)
    {
        this.context = context;
        this.view = view;
        this.provider = provider;

        this.manager = new TileManager(provider, worker);
        provider.addTileChangeListener(new TileChangeListenerImpl());

        initShaderBindings();
    }

    private void initShaderBindings() {
        UniformManager uniforms = context.getUniformManager();
        VertexAttributeManager attributes = context.getVertexAttribManager();

        // initialize global uniform variables
        uView = (UniformMat4f) uniforms.putGlobalUniform("u_view", DataTypes.FLOAT_MAT4);
        uProjection = (UniformMat4f) uniforms.putGlobalUniform("u_projection", DataTypes.FLOAT_MAT4);
        uniforms.putGlobalUniform("u_viewscale", DataTypes.FLOAT);
        uViewport = (UniformVec4f) uniforms.putGlobalUniform("u_viewport", DataTypes.FLOAT_VEC4);

        // initialize default attribute bindings
        attributes.putDefaultAttributeBinding("a_position", VertexAttributes.POSITION3);
        attributes.putDefaultAttributeBinding("a_position2", VertexAttributes.POSITION2);

        attributes.putDefaultAttributeBinding("a_normal", VertexAttributes.NORMAL3);
        attributes.putDefaultAttributeBinding("a_normal2", VertexAttributes.NORMAL2);

        attributes.putDefaultAttributeBinding("a_texcoord3", VertexAttributes.TEXCOORD3);
        attributes.putDefaultAttributeBinding("a_texcoord", VertexAttributes.TEXCOORD2);

        attributes.putDefaultAttributeBinding("a_color", VertexAttributes.COLOR);
    }


    @Override
    public VisualizerConfig getDefaultConfig() throws UnsupportedFeatureException {
        GLProfile profile = GLProfile.get(GL_PROFILE);
        GLCapabilities caps = new GLCapabilities(profile);

        caps.setDoubleBuffered(true);
        caps.setDepthBits(16);

        // check if required features are available
        ArrayList<String> missing = checkGLFeatureSet(profile);
        if (!missing.isEmpty())
            throw new UnsupportedFeatureException(missing);

        return new VisualizerConfig(profile, caps, DEFAULT_FPS);
    }

    private ArrayList<String> checkGLFeatureSet(GLProfile profile) {
        ArrayList<String> missing = new ArrayList<>();

        if (!profile.isGL3()) {
            missing.add("GL_VERSION_3_X");
            return missing;
        }

		/*
		 * the above only checks for the major version, to check for the minor
		 * version we have to parse the availability String for GL3. Note that
		 * this is a check for the default device.
		 */
        String available = GLProfile.glAvailabilityToString();
        Pattern gl31 = Pattern.compile(".*GL3 true \\[(\\d)\\.(\\d).*");
        Matcher matcher = gl31.matcher(available);

        if (matcher.matches()) {
            int major = Integer.parseInt(matcher.group(1));
            int minor = Integer.parseInt(matcher.group(2));

            if (major < 3 || (major == 3 && minor < 2)) {
                missing.add("GL_VERSION_3_2");
                return missing;
            }
        } else {
            missing.add("GL_VERSION_3_2");
            return missing;
        }

        return missing;
    }


    @Override
    public void init(RenderContext context) {
        if (DEBUG_VISUALIZATION) {
            DebugUtils.setDebugGL(context.getDrawable());
            context.getDrawable().getGL().setSwapInterval(0);
        }

        // initialize OpenGL
        GL3 gl = context.getDrawable().getGL().getGL3();

        context.ClearColor.set(gl, bgcolor);
        context.ClearDepth.set(gl, 1.f);

        context.PrimitiveRestart.enable(gl);
        context.PrimitiveRestart.setIndex(gl, 0xFFFFFFFF);

        gl.glEnable(GL3.GL_CULL_FACE);

        // initialize the framebuffer
        initFrameBuffer(context);

        // initialize the subsystems
        resetView();

        manager.initialize(context);
        for (Overlay overlay: overlays.values())
            overlay.init(context);
    }

    @Override
    public void dispose(RenderContext context) {
        // dispose the framebuffer
        disposeFrameBuffer(context);

        // dispose the subsystems
        manager.dispose(context);
        for (Overlay overlay: overlays.values())
            overlay.dispose(context);
    }

    private void initFrameBuffer(RenderContext context) {
        GLAutoDrawable drawable = context.getDrawable();

        GL3 gl = drawable.getGL().getGL3();
        final int width = drawable.getSurfaceWidth();
        final int height = drawable.getSurfaceHeight();
        int[] obj = { -1, -1, -1 };

        // create empty VAO for buffer-copying
        empty = VertexArrayObject.create(gl);
        empty.bind(gl);
        empty.unbind(gl);

        // create shader for buffer-copying
        Shader vs = Shader.create(gl, GL3.GL_VERTEX_SHADER, "fbo_color_depth_copy.vs")
                .loadFromResource(FBO_COLOR_DEPTH_COPY_VS)
                .compile(gl);

        Shader fs = Shader.create(gl, GL3.GL_FRAGMENT_SHADER, "fbo_color_depth_copy.fs")
                .loadFromResource(FBO_COLOR_DEPTH_COPY_FS)
                .compile(gl);

        fboCopyShader = ShaderProgram.create(gl, context, "fbo_color_depth_copy")
                .attach(gl, vs, fs)
                .link(gl)
                .detach(gl, vs, fs);

        UniformSampler2D uFboColorSampler = (UniformSampler2D) fboCopyShader.getUniform("u_color_sampler");
        UniformSampler2D uFboDepthSampler = (UniformSampler2D) fboCopyShader.getUniform("u_depth_sampler");
        uFboColorSampler.set(FBO_COLOR_TEXUNIT);
        uFboDepthSampler.set(FBO_DEPTH_TEXUNIT);

        // create backbuffer
        gl.glGenTextures(2, obj, 0);
        gl.glGenFramebuffers(1, obj, 2);
        backbuffer = new Overlay.MapBuffer(obj[2], obj[0], obj[1]);

        gl.glBindTexture(GL3.GL_TEXTURE_2D, backbuffer.color);
        gl.glTexParameteri(GL3.GL_TEXTURE_2D, GL3.GL_TEXTURE_WRAP_S, GL3.GL_CLAMP_TO_EDGE);
        gl.glTexParameteri(GL3.GL_TEXTURE_2D, GL3.GL_TEXTURE_WRAP_T, GL3.GL_CLAMP_TO_EDGE);
        gl.glTexParameteri(GL3.GL_TEXTURE_2D, GL3.GL_TEXTURE_MIN_FILTER, GL3.GL_NEAREST);
        gl.glTexParameteri(GL3.GL_TEXTURE_2D, GL3.GL_TEXTURE_MAG_FILTER, GL3.GL_NEAREST);
        gl.glTexImage2D(GL3.GL_TEXTURE_2D, 0, GL3.GL_RGBA8, width, height, 0, GL3.GL_RGBA, GL3.GL_BYTE, null);
        gl.glBindTexture(GL3.GL_TEXTURE_2D, 0);

        gl.glBindTexture(GL3.GL_TEXTURE_2D, backbuffer.depth);
        gl.glTexParameteri(GL3.GL_TEXTURE_2D, GL3.GL_TEXTURE_WRAP_S, GL3.GL_CLAMP_TO_EDGE);
        gl.glTexParameteri(GL3.GL_TEXTURE_2D, GL3.GL_TEXTURE_WRAP_T, GL3.GL_CLAMP_TO_EDGE);
        gl.glTexParameteri(GL3.GL_TEXTURE_2D, GL3.GL_TEXTURE_MIN_FILTER, GL3.GL_NEAREST);
        gl.glTexParameteri(GL3.GL_TEXTURE_2D, GL3.GL_TEXTURE_MAG_FILTER, GL3.GL_NEAREST);
        gl.glTexParameteri(GL3.GL_TEXTURE_2D, GL3.GL_TEXTURE_COMPARE_MODE, GL3.GL_NONE);
        gl.glTexImage2D(GL3.GL_TEXTURE_2D, 0, GL3.GL_DEPTH_COMPONENT16, width, height, 0, GL3.GL_DEPTH_COMPONENT, GL3.GL_BYTE, null);
        gl.glBindTexture(GL3.GL_TEXTURE_2D, 0);

        // create framebuffer
        gl.glBindFramebuffer(GL3.GL_FRAMEBUFFER, backbuffer.fbo);
        gl.glFramebufferTexture2D(GL3.GL_FRAMEBUFFER, GL3.GL_COLOR_ATTACHMENT0, GL3.GL_TEXTURE_2D, backbuffer.color, 0);
        gl.glFramebufferTexture2D(GL3.GL_FRAMEBUFFER, GL3.GL_DEPTH_ATTACHMENT, GL3.GL_TEXTURE_2D, backbuffer.depth, 0);
        int status = gl.glCheckFramebufferStatus(GL3.GL_FRAMEBUFFER);
        gl.glBindFramebuffer(GL3.GL_FRAMEBUFFER, 0);

        if (status != GL3.GL_FRAMEBUFFER_COMPLETE)
            throw new RuntimeException("Failed to create framebuffer object (status: 0x"
                    + Integer.toHexString(status) + ")");
    }

    private void resizeFrameBuffer(RenderContext context, int width, int height) {
        if (backbuffer == null) return;
        GL3 gl = context.getDrawable().getGL().getGL3();

        gl.glBindTexture(GL3.GL_TEXTURE_2D, backbuffer.color);
        gl.glTexImage2D(GL3.GL_TEXTURE_2D, 0, GL3.GL_RGBA8, width, height, 0, GL3.GL_RGBA, GL3.GL_BYTE, null);
        gl.glBindTexture(GL3.GL_TEXTURE_2D, 0);

        gl.glBindTexture(GL3.GL_TEXTURE_2D, backbuffer.depth);
        gl.glTexImage2D(GL3.GL_TEXTURE_2D, 0, GL3.GL_DEPTH_COMPONENT16, width, height, 0, GL3.GL_DEPTH_COMPONENT, GL3.GL_BYTE, null);
        gl.glBindTexture(GL3.GL_TEXTURE_2D, 0);
    }

    private void disposeFrameBuffer(RenderContext context) {
        if (backbuffer == null) return;

        GL3 gl = context.getDrawable().getGL().getGL3();
        int[] obj= { backbuffer.fbo, backbuffer.color, backbuffer.depth };

        // detach textures
        gl.glBindFramebuffer(GL3.GL_FRAMEBUFFER, backbuffer.fbo);
        gl.glFramebufferTexture2D(GL3.GL_FRAMEBUFFER, GL3.GL_COLOR_ATTACHMENT0, GL3.GL_TEXTURE_2D, 0, 0);
        gl.glFramebufferTexture2D(GL3.GL_FRAMEBUFFER, GL3.GL_DEPTH_ATTACHMENT, GL3.GL_TEXTURE_2D, 0, 0);
        gl.glBindFramebuffer(GL3.GL_FRAMEBUFFER, 0);

        // delete objects
        gl.glDeleteFramebuffers(1, obj, 0);
        gl.glDeleteTextures(2, obj, 1);

        backbuffer = null;

        // delete vao, shader
        empty.dispose(gl);
        fboCopyShader.dispose(gl);
    }

    @Override
    public void display(RenderContext context) throws Exception {
        GL3 gl = context.getDrawable().getGL().getGL3();
        int width = context.getDrawable().getSurfaceWidth();
        int height = context.getDrawable().getSurfaceHeight();

        uViewport.set(width, height, 1.f / width, 1.f / height);

        context.DepthTest.enable(gl);
        context.DepthTest.setFunction(gl, GL3.GL_ALWAYS);

        // draw map to framebuffer
        uView.set(Mat4f.identity());
        uProjection.set(Mat4f.identity());

        gl.glBindFramebuffer(GL3.GL_FRAMEBUFFER, backbuffer.fbo);

        gl.glClearBufferfv(GL3.GL_COLOR, 0, new float[]{bgcolor.r, bgcolor.g, bgcolor.b, bgcolor.a}, 0);
        gl.glClearBufferfv(GL3.GL_DEPTH, 0, new float[]{ 0.0f }, 0);

        manager.update(context, view);
        manager.display(context, view);

        gl.glBindFramebuffer(GL3.GL_FRAMEBUFFER, 0);

        // draw back-buffer to window-buffer
        context.BlendMode.enable(gl);
        context.BlendMode.setEquation(gl, GL3.GL_FUNC_ADD);
        context.BlendMode.setFactors(gl, GL3.GL_ONE, GL3.GL_ONE_MINUS_SRC_ALPHA);

        empty.bind(gl);
        fboCopyShader.bind(gl);

        gl.glActiveTexture(GL3.GL_TEXTURE0 + FBO_COLOR_TEXUNIT);
        gl.glBindTexture(GL3.GL_TEXTURE_2D, backbuffer.color);

        gl.glActiveTexture(GL3.GL_TEXTURE0 + FBO_DEPTH_TEXUNIT);
        gl.glBindTexture(GL3.GL_TEXTURE_2D, backbuffer.depth);

        gl.glDrawArrays(GL3.GL_TRIANGLES, 0, 3);

        fboCopyShader.unbind(gl);
        empty.unbind(gl);

        context.DepthTest.setFunction(gl, GL3.GL_GEQUAL);

        // draw overlays
        uView.set(view.getViewMatrix());
        uProjection.set(view.getProjectionMatrix());

        for (Overlay overlay : overlays.values())
            overlay.display(context, backbuffer);
    }

    @Override
    public void reshape(RenderContext context, int x, int y, int width, int height) {
        view.resize(width, height);
        resizeFrameBuffer(context, width, height);

        for (Overlay overlay : overlays.values())
            overlay.resize(context);
    }


    @Override
    public RenderContext getContext() {
        return context;
    }


    @Override
    public View getView() {
        return view;
    }

    @Override
    public void resetView() {
        Rect2d bounds = manager.getProjectedBounds();
        if (bounds == null) return;

        // center view
        Vec2d center = Vec2d.add(bounds.min(), bounds.max()).mul(0.5f);
        view.setPosition(center);

        // set zoom based on window width
        Vec2d dbounds = Vec2d.sub(bounds.max(), bounds.min());
        Vec2d dscreen = new Vec2d(view.getSize());

        double sx = (dscreen.x * 0.90f) / dbounds.x;
        double sy = (dscreen.y * 0.90f) / dbounds.y;

        view.setScale(Math.min(sx, sy));
    }


    @Override
    public Overlay putOverlay(int index, Overlay overlay) {
        overlay.setView(view);
        return overlays.put(index, overlay);
    }

    @Override
    public Overlay removeOverlay(int index) {
        return overlays.remove(index);
    }

    @Override
    public Overlay getOverlay(int index) {
        return overlays.get(index);
    }

    @Override
    public Collection<Overlay> getAllOverlays() {
        return overlays.values();
    }


    public void apply(StyleSheet style) {
        bgcolor = style.getBackgroundColor();
        provider.apply(style);
    }


    private class TileChangeListenerImpl implements TileProvider.TileChangeListener {
        @Override
        public void tilesChanged() {
            resetView();
        }

        @Override public void tileChanged(TileId tile) {}
    }
}
