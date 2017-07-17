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
import microtrafficsim.core.vis.scenario.areas.Area;
import microtrafficsim.core.vis.scenario.areas.Area.Type;
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
import java.util.HashSet;


public class AreaComponent extends Component {

    private static final Color FILL_COLOR_ORIGIN = Color.fromRGBA(0x48CF9430);
    private static final Color FILL_COLOR_DESTINATION = Color.fromRGBA(0xE03A5330);

    private static final Color OUTLINE_COLOR = Colors.white();
    private static final float OUTLINE_LINE_WIDTH = 2.f;

    private static final ShaderProgramSource FILL_SHADER = new ShaderProgramSource("/shaders/basic",
            new ShaderSource(GL3.GL_VERTEX_SHADER, new PackagedResource(AreaComponent.class, "/shaders/basic.vs")),
            new ShaderSource(GL3.GL_FRAGMENT_SHADER, new PackagedResource(AreaComponent.class, "/shaders/basic.fs"))
    );

    private static final ShaderProgramSource OUTLINE_SHADER = new ShaderProgramSource("/shaders/basic",
            new ShaderSource(GL3.GL_VERTEX_SHADER, new PackagedResource(AreaComponent.class, "/shaders/basic.vs")),
            new ShaderSource(GL3.GL_FRAGMENT_SHADER, new PackagedResource(AreaComponent.class, "/shaders/basic.fs"))
    );


    private static final FillPass RENDER_PASS_DEFAULT = new FillPass();
    private static final OutlinePass RENDER_PASS_SELECTED = new OutlinePass();

    private ScenarioAreaOverlay root;
    private Area area;
    private Triangulator.Result cached;

    private boolean selected = false;
    private boolean complete = false;

    private ArrayList<AreaVertex> vertices = new ArrayList<>();


    public AreaComponent(ScenarioAreaOverlay root, Type type) {
        this(root, new Area(new Polygon(new Vec2d[0]), type));
    }

    public AreaComponent(ScenarioAreaOverlay root, Area area) {
        super(RENDER_PASS_DEFAULT, RENDER_PASS_SELECTED);

        this.root = root;
        this.area = area;
        this.focusable = false;
        this.complete = area.polygon.outline.length >= 3;

        addMouseListener(new MouseListenerImpl());

        if (area.polygon.outline.length > 0) {
            for (Vec2d b : area.polygon.outline) {
                AreaVertex c = new AreaVertex(root, b);
                c.setVisible(selected);
                this.add(c);
                vertices.add(c);
            }

            AreaVertex a = vertices.get(vertices.size() - 1);
            for (AreaVertex b : vertices) {
                EdgeSplit split = new EdgeSplit(root, a, b);

                a.right = split;
                b.left = split;
                this.add(split);

                a = b;
            }

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


    public void setComplete(boolean complete) {
        this.complete = complete;
        redraw();
    }

    public boolean isComplete() {
        return complete;
    }


    public void move(Vec2d delta) {
        for (Vec2d v : area.polygon.outline)
            v.add(delta);

        this.cached = null;
        redraw(true);
    }

    public void scale(double s) {
        Vec2d center = new Vec2d(0.0, 0.0);

        for (Vec2d v : area.polygon.outline)
            center.add(v);

        center.mul(1.0 / vertices.size());

        for (Vec2d v : area.polygon.outline)
            v.sub(center).mul(s).add(center);

        this.cached = null;
        redraw(true);
    }

    public void add(Vec2d vertex, boolean select) {
        insert(vertices.size(), vertex, select);
    }

    public void insert(int index, Vec2d vertex, boolean select) {
        Vec2d v = new Vec2d(vertex);

        Vec2d[] outline = new Vec2d[area.polygon.outline.length + 1];
        System.arraycopy(area.polygon.outline, 0, outline, 0, index);
        System.arraycopy(area.polygon.outline, index, outline, index + 1, area.polygon.outline.length - index);
        outline[index] = v;
        area.polygon.outline = outline;

        AreaVertex av = new AreaVertex(root, v);
        vertices.add(index, av);
        super.add(av);

        if (vertices.size() >= 3) {
            int leftIndex = (index - 1) >= 0 ? index - 1 : index - 1 + vertices.size();
            int rightIndex = (index + 1) < vertices.size() ? index + 1 : index + 1 - vertices.size();

            AreaVertex left = vertices.get(leftIndex);
            AreaVertex right = vertices.get(rightIndex);

            EdgeSplit edge = new EdgeSplit(root, av, right);

            left.right.b = av;
            right.left = edge;
            av.right = edge;
            av.left = left.right;

            if (select) root.select(av);
            super.add(edge);

        } else if (vertices.size() == 2) {
            AreaVertex other = vertices.get((index + 1) % vertices.size());

            EdgeSplit e1 = new EdgeSplit(root, av, other);
            EdgeSplit e2 = new EdgeSplit(root, other, av);

            av.left = e2;
            av.right = e1;

            other.left = e1;
            other.right = e2;

            super.add(e1);
            super.add(e2);
        }
    }

    public void remove(AreaVertex vertex) {
        int index = vertices.indexOf(vertex);

        Vec2d[] outline = new Vec2d[area.polygon.outline.length - 1];
        System.arraycopy(area.polygon.outline, 0, outline, 0, index);
        System.arraycopy(area.polygon.outline, index + 1, outline, index, area.polygon.outline.length - index - 1);
        area.polygon.outline = outline;

        int iright = (index + 1) < vertices.size() ? index + 1 : index + 1 - vertices.size();
        AreaVertex right = vertices.get(iright);

        vertex.left.b = right;
        right.left = vertex.left;

        vertices.remove(index);
        super.remove(vertex.right);
        super.remove(vertex);

        redraw(true);
    }

    public void removeAll(HashSet<AreaVertex> vertices) {
        for (AreaVertex v : vertices)
            this.remove(v);
    }

    public ArrayList<AreaVertex> getVertices() {
        return vertices;
    }


    public Area getArea() {
        return area;
    }

    public Vec2d[] getOutline() {
        return area.polygon.outline;
    }

    public Type getType() {
        return area.type;
    }

    public void setType(Type type) {
        this.area.type = type;
        redraw();
    }


    @Override
    public boolean contains(Vec2d p) {
        return area.polygon.contains(p);
    }

    @Override
    public Rect2d getBounds() {
        updateBounds();
        return aabb;
    }

    @Override
    protected void updateBounds() {
        if (!selected)
            this.aabb = area.polygon.bounds();
        else
            super.updateBounds();
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
            if (e.getButton() != MouseEvent.BUTTON1) return;
            if (e.isShiftDown()) return;

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
            if (e.getButton() != MouseEvent.BUTTON1) return;

            if (e.isControlDown() && !e.isShiftDown()) {
                down = e.getPointer();
            }
        }

        @Override
        public void mouseReleased(MouseEvent e) {
            down = null;
        }

        @Override
        public void mouseDragged(MouseEvent e) {
            if (e.getButton() != MouseEvent.BUTTON1) return;
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
            return (component instanceof AreaComponent) && ((AreaComponent) component).area.polygon.outline.length >= 3;
        }

        @Override
        public BatchBuilder createBuilder() {
            return new FillBatch.Builder(FILL_SHADER);
        }


        Color getColor(AreaComponent area) {
            switch (area.area.type) {
                case ORIGIN:      return FILL_COLOR_ORIGIN;
                case DESTINATION: return FILL_COLOR_DESTINATION;
            }

            return Colors.black();
        }

        Triangulator.Result getPolygon(AreaComponent area) {
            if (area.cached == null)
                area.cached = triangulator.triangulate(area.area.polygon);

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
            try {
                context.Lines.setLineSmoothEnabled(gl, true);
                context.Lines.setLineSmoothHint(gl, GL3.GL_NICEST);
                context.Lines.setLineWidth(gl, linewidth);
            } catch (Throwable t) {
                /* TODO: use proper line rendering
                 * Will throw on implementations that do not support the specified line settings.
                 * For now, we ignore those and use the default settings provided by the implementation.
                 */
            }

            shader.bind(gl);
            uColor.set(color);
            mesh.display(context, vao);
        }


        public static class Builder implements BatchBuilder {
            private Color color;
            private float linewidth;
            private ArrayList<Boolean> complete = null;
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

                if (complete == null) {
                    complete = new ArrayList<>();
                }

                Vec2d[] outline = area.area.polygon.outline.clone();
                for (int i = 0; i < outline.length; i++) {
                    Vec3d v = transform.mul(new Vec3d(outline[i].x, outline[i].y, 1.0));
                    outline[i] = new Vec2d(v.x / v.z, v.y / v.z);
                }

                outlines.add(outline);
                complete.add(area.isComplete());
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
                for (int i = 0; i < outlines.size(); i++) {
                    Vec2d[] outline = outlines.get(i);

                    for (int j = 0; j < outline.length; j++) {
                        Vec2d v = outline[j];
                        vertices.put((float) v.x);
                        vertices.put((float) v.y);
                        indices.put(base + j);
                    }

                    if (complete.get(i))
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
