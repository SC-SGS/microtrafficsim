package microtrafficsim.core.vis.scenario.areas.ui;

import com.jogamp.opengl.GL3;
import microtrafficsim.core.vis.context.RenderContext;
import microtrafficsim.core.vis.glui.Component;
import microtrafficsim.core.vis.glui.events.MouseAdapter;
import microtrafficsim.core.vis.glui.events.MouseEvent;
import microtrafficsim.core.vis.glui.renderer.Batch;
import microtrafficsim.core.vis.glui.renderer.BatchBuilder;
import microtrafficsim.core.vis.glui.renderer.ComponentRenderPass;
import microtrafficsim.core.vis.mesh.impl.SingleFloatAttributeIndexedMesh;
import microtrafficsim.core.vis.opengl.shader.ShaderCompileException;
import microtrafficsim.core.vis.opengl.shader.ShaderLinkException;
import microtrafficsim.core.vis.opengl.shader.ShaderProgram;
import microtrafficsim.core.vis.opengl.shader.attributes.VertexArrayObject;
import microtrafficsim.core.vis.opengl.shader.resources.ShaderProgramSource;
import microtrafficsim.core.vis.opengl.shader.resources.ShaderSource;
import microtrafficsim.core.vis.opengl.shader.uniforms.UniformVec4f;
import microtrafficsim.core.vis.opengl.utils.Color;
import microtrafficsim.core.vis.opengl.utils.Colors;
import microtrafficsim.core.vis.scenario.areas.ScenarioAreaOverlay;
import microtrafficsim.math.Mat3d;
import microtrafficsim.math.Rect2d;
import microtrafficsim.math.Vec2d;
import microtrafficsim.math.Vec3d;
import microtrafficsim.math.geometry.polygons.Polygon;
import microtrafficsim.math.geometry.polygons.SweepLineTriangulator;
import microtrafficsim.math.geometry.polygons.Triangulator;
import microtrafficsim.utils.resources.PackagedResource;

import java.io.IOException;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;


public class AreaComponent extends Component {

    private static final Color FILL_COLOR_ORIGIN = Color.fromRGBA(0x48CF9430);
    private static final Color FILL_COLOR_DESTINATION = Color.fromRGBA(0xE03A5330);

    private static final Color OUTLINE_COLOR = Colors.white();
    private static final float OUTLINE_LINE_WIDTH = 1.f;

    private static final ShaderProgramSource FILL_SHADER = new ShaderProgramSource("/shaders/basic",
            new ShaderSource(GL3.GL_VERTEX_SHADER, new  PackagedResource(AreaComponent.class, "/shaders/basic.vs")),
            new ShaderSource(GL3.GL_FRAGMENT_SHADER, new PackagedResource(AreaComponent.class, "/shaders/basic.fs"))
    );

    private static final ShaderProgramSource OUTLINE_SHADER = new ShaderProgramSource("/shaders/basic",
            new ShaderSource(GL3.GL_VERTEX_SHADER, new PackagedResource(AreaComponent.class, "/shaders/basic.vs")),
            new ShaderSource(GL3.GL_FRAGMENT_SHADER, new PackagedResource(AreaComponent.class, "/shaders/basic.fs"))
    );


    private static final FillPass RENDER_PASS_DEFAULT = new FillPass();
    private static final OutlinePass RENDER_PASS_SELECTED = new OutlinePass();

    public enum Type { ORIGIN, DESTINATION }

    private ScenarioAreaOverlay root;
    private Polygon area;
    private Triangulator.Result cached;
    private Type type;

    private boolean selected = false;


    public AreaComponent(ScenarioAreaOverlay root, Polygon area, Type type) {
        super(RENDER_PASS_DEFAULT, RENDER_PASS_SELECTED);

        this.root = root;
        this.area = area;
        this.type = type;
        this.focusable = false;

        addMouseListener(new MouseListenerImpl());

        for (Vec2d v : area.outline) {
            AreaVertex c = new AreaVertex(root, v);
            c.setVisible(selected);

            this.addComponent(c);
        }

        updateBounds();
    }


    public void setSelected(boolean selected) {
        if (this.selected == selected) return;

        this.selected = selected;
        for (Component c : getComponents())
            c.setVisible(selected);

        redraw();
    }

    public boolean isSelected() {
        return selected;
    }


    public void move(Vec2d delta) {
        for (Vec2d v : area.outline)
            v.add(delta);

        this.cached = null;
        redraw(true);
    }


    @Override
    public boolean contains(Vec2d p) {
        return area.contains(p);
    }

    @Override
    public Rect2d getBounds() {
        updateBounds();
        return aabb;
    }

    @Override
    protected void updateBounds() {
        if (!selected) {
            Rect2d aabb = new Rect2d(Double.MAX_VALUE, Double.MAX_VALUE, -Double.MAX_VALUE, -Double.MAX_VALUE);

            for (Vec2d v : area.outline) {
                if (aabb.xmin > v.x)
                    aabb.xmin = v.x;
                else if (aabb.xmax < v.x)
                    aabb.xmax = v.x;

                if (aabb.ymin > v.y)
                    aabb.ymin = v.y;
                else if (aabb.ymax < v.y)
                    aabb.ymax = v.y;
            }

            this.aabb = aabb;

        } else {
            super.updateBounds();
        }
    }

    @Override
    public void redraw(boolean recursive) {
        this.cached = null;
        super.redraw(recursive);
    }


    private class MouseListenerImpl extends MouseAdapter {
        private Vec2d down = null;

        @Override
        public void mouseClicked(MouseEvent e) {
            getUIManager().getContext().addTask(c -> {
                if (!isSelected() || (isSelected() && root.getSelectedAreas().size() == 1 && !e.isControlDown()))
                    root.clearVertexSelection();

                if (e.isControlDown()) {
                    if (root.getSelectedVertices().isEmpty()) {
                        if (isSelected())
                            root.deselect(AreaComponent.this);
                        else
                            root.select(AreaComponent.this);
                    }
                } else {
                    root.clearAreaSelection();
                    root.select(AreaComponent.this);
                }

                return null;
            });
            e.setConsumed(true);
        }

        @Override
        public void mousePressed(MouseEvent e) {
            if (e.isControlDown()) {
                down = e.getPointer();
            }
        }

        @Override
        public void mouseReleased(MouseEvent e) {
            down = null;
        }

        @Override
        public void mouseDragged(MouseEvent e) {
            if (down == null) return;

            Vec2d delta = Vec2d.sub(e.getPointer(), down);
            getUIManager().getContext().addTask(c -> {
                root.select(AreaComponent.this);
                root.moveSelectedAreas(delta);

                root.clearVertexSelection();
                return null;
            });

            down = e.getPointer();
            e.setConsumed(true);
        }
    }


    private static class FillBatch implements Batch {

        private Color color;
        private SingleFloatAttributeIndexedMesh mesh;
        private ShaderProgramSource shaderSource;

        private ShaderProgram shader;
        private UniformVec4f uColor;

        private VertexArrayObject vao;


        FillBatch(Color color, SingleFloatAttributeIndexedMesh mesh, ShaderProgramSource shader) {
            this.color = color;
            this.mesh = mesh;
            this.shaderSource = shader;
        }

        @Override
        public void initialize(RenderContext context) throws ShaderLinkException, IOException, ShaderCompileException {
            shader = context.getShaderManager().load(shaderSource);
            uColor = (UniformVec4f) shader.getUniform("u_color");

            mesh.initialize(context);
            mesh.load(context);

            vao = mesh.createVAO(context, shader);
        }

        @Override
        public void dispose(RenderContext context) {
            GL3 gl = context.getDrawable().getGL().getGL3();

            vao.dispose(gl);
            mesh.dispose(context);
            shader.dispose(gl);
        }

        @Override
        public void display(RenderContext context) {
            GL3 gl = context.getDrawable().getGL().getGL3();

            // enable blending
            context.BlendMode.enable(gl);
            context.BlendMode.setEquation(gl, GL3.GL_FUNC_ADD);
            context.BlendMode.setFactors(gl, GL3.GL_SRC_ALPHA, GL3.GL_ONE_MINUS_SRC_ALPHA, GL3.GL_ONE,
                    GL3.GL_ONE_MINUS_SRC_ALPHA);

            shader.bind(gl);
            uColor.set(color);
            mesh.display(context, vao);
        }


        public static class Builder implements BatchBuilder {

            private ShaderProgramSource shader;
            private Color color = null;
            private ArrayList<Triangulator.Result> polygons = new ArrayList<>();


            Builder(ShaderProgramSource shader) {
                this.shader = shader;
            }

            @Override
            public boolean isApplicableFor(Component component, ComponentRenderPass pass) {
                return component instanceof AreaComponent && pass instanceof FillPass
                        && color.equals(((FillPass) pass).getColor((AreaComponent) component));
            }

            @Override
            public BatchBuilder add(Component component, ComponentRenderPass pass, Mat3d transform) {
                AreaComponent area = (AreaComponent) component;
                FillPass renderer = (FillPass) pass;

                if (color == null)
                    color = renderer.getColor(area);

                Triangulator.Result triangles = renderer.getPolygon(area);
                ArrayList<Vec2d> vertices = new ArrayList<>(triangles.vertices.size());
                for (Vec2d v : triangles.vertices) {
                    Vec3d x = transform.mul(new Vec3d(v.x, v.y, 1.0f));
                    vertices.add(new Vec2d(x.x / x.z, x.y / x.z));
                }

                triangles.vertices = vertices;
                polygons.add(triangles);

                return this;
            }

            @Override
            public Batch build(RenderContext context) {
                int numIndices = 0;
                int numVertices = 0;

                for (Triangulator.Result polygon : polygons) {
                    numIndices += polygon.indices.size();
                    numVertices += polygon.vertices.size();
                }

                FloatBuffer vertices = FloatBuffer.allocate(numVertices * 2);
                IntBuffer indices = IntBuffer.allocate(numIndices);

                int base = 0;
                for (Triangulator.Result polygon : polygons) {
                    for (Vec2d v : polygon.vertices) {
                        vertices.put((float) v.x);
                        vertices.put((float) v.y);
                    }

                    for (int i : polygon.indices)
                        indices.put(base + i);

                    base += polygon.vertices.size();
                }

                vertices.rewind();
                indices.rewind();

                SingleFloatAttributeIndexedMesh mesh = SingleFloatAttributeIndexedMesh.newPos2Mesh(
                        GL3.GL_STATIC_DRAW, GL3.GL_TRIANGLES, vertices, indices);
                return new FillBatch(color, mesh, shader);
            }
        }
    }

    private static class FillPass implements ComponentRenderPass {

        private Triangulator triangulator = new SweepLineTriangulator();


        @Override
        public boolean isActiveFor(Component component) {
            return (component instanceof AreaComponent);
        }

        @Override
        public BatchBuilder createBuilder() {
            return new FillBatch.Builder(FILL_SHADER);
        }


        Color getColor(AreaComponent area) {
            switch (area.type) {
                case ORIGIN:      return FILL_COLOR_ORIGIN;
                case DESTINATION: return FILL_COLOR_DESTINATION;
            }

            return Colors.black();
        }

        Triangulator.Result getPolygon(AreaComponent area) {
            if (area.cached == null)
                area.cached = triangulator.triangulate(area.area);

            return area.cached;
        }
    }


    private static class OutlineBatch implements Batch {

        private float linewidth;
        private Color color;
        private SingleFloatAttributeIndexedMesh mesh;
        private ShaderProgramSource shaderSource;

        private ShaderProgram shader;
        private UniformVec4f uColor;

        private VertexArrayObject vao;


        OutlineBatch(float linewidth, Color color, SingleFloatAttributeIndexedMesh mesh, ShaderProgramSource shader) {
            this.linewidth = linewidth;
            this.color = color;
            this.mesh = mesh;
            this.shaderSource = shader;
        }

        @Override
        public void initialize(RenderContext context) throws ShaderLinkException, IOException, ShaderCompileException {
            shader = context.getShaderManager().load(shaderSource);
            uColor = (UniformVec4f) shader.getUniform("u_color");

            mesh.initialize(context);
            mesh.load(context);

            vao = mesh.createVAO(context, shader);
        }

        @Override
        public void dispose(RenderContext context) {
            GL3 gl = context.getDrawable().getGL().getGL3();

            vao.dispose(gl);
            mesh.dispose(context);
            shader.dispose(gl);
        }

        @Override
        public void display(RenderContext context) {
            GL3 gl = context.getDrawable().getGL().getGL3();

            // enable blending
            context.BlendMode.enable(gl);
            context.BlendMode.setEquation(gl, GL3.GL_FUNC_ADD);
            context.BlendMode.setFactors(gl, GL3.GL_SRC_ALPHA, GL3.GL_ONE_MINUS_SRC_ALPHA, GL3.GL_ONE,
                    GL3.GL_ONE_MINUS_SRC_ALPHA);

            // set line style
            context.Lines.setLineSmoothEnabled(gl, true);
            context.Lines.setLineSmoothHint(gl, GL3.GL_NICEST);
            context.Lines.setLineWidth(gl, linewidth);

            shader.bind(gl);
            uColor.set(color);
            mesh.display(context, vao);
        }


        public static class Builder implements BatchBuilder {
            private float linewidth;
            private Color color;
            private ShaderProgramSource shader;
            private ArrayList<Vec2d[]> outlines = new ArrayList<>();


            Builder(Color color, float linewidth, ShaderProgramSource shader) {
                this.color = color;
                this.linewidth = linewidth;
                this.shader = shader;
            }

            @Override
            public boolean isApplicableFor(Component component, ComponentRenderPass pass) {
                return component instanceof AreaComponent && pass instanceof OutlinePass;
            }

            @Override
            public BatchBuilder add(Component component, ComponentRenderPass pass, Mat3d transform) {
                AreaComponent area = (AreaComponent) component;

                Vec2d[] outline = area.area.outline.clone();
                for (int i = 0; i < outline.length; i++) {
                    Vec3d v = transform.mul(new Vec3d(outline[i].x, outline[i].y, 1.0));
                    outline[i] = new Vec2d(v.x / v.z, v.y / v.z);
                }

                outlines.add(outline);
                return this;
            }

            @Override
            public Batch build(RenderContext context) {
                int restart = context.PrimitiveRestart.getIndex();

                int numIndices = 0;
                int numVertices = 0;

                for (Vec2d[] outline : outlines) {
                    numIndices += outline.length + 2;
                    numVertices += outline.length;
                }

                FloatBuffer vertices = FloatBuffer.allocate(numVertices * 3);
                IntBuffer indices = IntBuffer.allocate(numIndices);

                int base = 0;
                for (Vec2d[] outline : outlines) {
                    for (int i = 0; i < outline.length; i++) {
                        Vec2d v = outline[i];
                        vertices.put((float) v.x);
                        vertices.put((float) v.y);
                        indices.put(base + i);
                    }

                    indices.put(base);
                    indices.put(restart);

                    base += outline.length;
                }

                vertices.rewind();
                indices.rewind();

                SingleFloatAttributeIndexedMesh mesh = SingleFloatAttributeIndexedMesh.newPos2Mesh(
                        GL3.GL_STATIC_DRAW, GL3.GL_LINE_STRIP, vertices, indices);
                return new OutlineBatch(linewidth, color, mesh, shader);
            }
        }
    }

    public static class OutlinePass implements ComponentRenderPass {

        @Override
        public boolean isActiveFor(Component component) {
            return (component instanceof AreaComponent) && ((AreaComponent) component).selected;
        }

        @Override
        public BatchBuilder createBuilder() {
            return new OutlineBatch.Builder(OUTLINE_COLOR, OUTLINE_LINE_WIDTH, OUTLINE_SHADER);
        }
    }
}
