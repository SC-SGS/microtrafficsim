package microtrafficsim.core.vis.scenario.areas.ui;


import com.jogamp.opengl.GL3;
import microtrafficsim.core.vis.context.RenderContext;
import microtrafficsim.core.vis.glui.Component;
import microtrafficsim.core.vis.glui.events.MouseAdapter;
import microtrafficsim.core.vis.glui.events.MouseEvent;
import microtrafficsim.core.vis.glui.renderer.Batch;
import microtrafficsim.core.vis.glui.renderer.BatchBuilder;
import microtrafficsim.core.vis.glui.renderer.ComponentRenderPass;
import microtrafficsim.core.vis.mesh.impl.SingleFloatAttributeMesh;
import microtrafficsim.core.vis.opengl.shader.ShaderProgram;
import microtrafficsim.core.vis.opengl.shader.attributes.VertexArrayObject;
import microtrafficsim.core.vis.opengl.shader.resources.ShaderProgramSource;
import microtrafficsim.core.vis.opengl.shader.resources.ShaderSource;
import microtrafficsim.core.vis.opengl.shader.uniforms.UniformVec4f;
import microtrafficsim.core.vis.opengl.utils.Color;
import microtrafficsim.core.vis.opengl.utils.Colors;
import microtrafficsim.core.vis.view.OrthographicView;
import microtrafficsim.math.*;
import microtrafficsim.utils.resources.PackagedResource;

import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.function.Predicate;


public class AreaVertex extends Component {

    private static final ShaderProgramSource SHADER = new ShaderProgramSource(
            "/shaders/basic",
            new ShaderSource(GL3.GL_VERTEX_SHADER, new PackagedResource(AreaVertexPass.class,
                    "/shaders/basic.vs")),
            new ShaderSource(GL3.GL_FRAGMENT_SHADER, new PackagedResource(AreaVertexPass.class,
                    "/shaders/basic.fs"))
    );


    private static final double SIZE_ACTIVE = 13.f;

    private static final float SIZE_INNER = 5.f;
    private static final float SIZE_OUTER = 7.f;
    private static final float SIZE_OUTER_SELECTED = 9.f;

    private static final Color COLOR_INNER = Colors.black();
    private static final Color COLOR_OUTER = Colors.white();
    private static final Color COLOR_OUTER_SELECTED = Color.fromRGB(0xE29B4A);


    Vec2d pos;
    private boolean selected;

    AreaVertex(Vec2d pos) {
        super(
                new AreaVertexPass(COLOR_OUTER, SIZE_OUTER, v -> !v.isSelected()),
                new AreaVertexPass(COLOR_OUTER_SELECTED, SIZE_OUTER_SELECTED, v -> v.isSelected()),
                new AreaVertexPass(COLOR_INNER, SIZE_INNER, v -> true)
        );

        this.pos = pos;
        this.selected = false;

        addMouseListener(new MouseListenerImpl());
    }


    private void setSelected(boolean selected) {
        this.selected = selected;
        redraw();
    }

    private boolean isSelected() {
        return selected;
    }


    @Override
    public boolean contains(Vec2d p) {
        return getBounds().contains(p);
    }

    @Override
    public Rect2d getBounds() {
        OrthographicView view = getUIManager().getView();

        Vec2i viewport = view.getSize();
        Rect2d bounds = view.getViewportBounds();

        double rx = (SIZE_ACTIVE / 2.0) / viewport.x * (bounds.xmax - bounds.xmin);
        double ry = (SIZE_ACTIVE / 2.0) / viewport.y * (bounds.ymax - bounds.ymin);

        return new Rect2d(pos.x - rx, pos.y - ry, pos.x + rx, pos.y + ry);
    }


    private class MouseListenerImpl extends MouseAdapter {
        private Vec2d down;

        @Override
        public void mouseClicked(MouseEvent e) {
            getUIManager().getContext().addTask(c -> {
                setSelected(!isSelected());
                return null;
            });
            e.setConsumed(true);
        }

        @Override
        public void mousePressed(MouseEvent e) {
            down = e.getPointer();
            e.setConsumed(true);
        }

        @Override
        public void mouseDragged(MouseEvent e) {
            setSelected(true);

            Vec2d delta = Vec2d.sub(e.getPointer(), down);
            getUIManager().getContext().addTask(c -> {
                pos.add(delta);
                parent.redraw();
                return null;
            });

            down = e.getPointer();
            e.setConsumed(true);
        }
    }


    public static class AreaVertexBatch implements Batch {

        private final float pointsize;
        private final Color color;
        private final SingleFloatAttributeMesh mesh;
        private final ShaderProgramSource shaderSource;

        private ShaderProgram shader;
        private UniformVec4f uColor;
        private VertexArrayObject vao;

        AreaVertexBatch(float pointsize, Color color, SingleFloatAttributeMesh mesh, ShaderProgramSource shader) {
            this.pointsize = pointsize;
            this.color = color;
            this.mesh = mesh;
            this.shaderSource = shader;
        }

        @Override
        public void initialize(RenderContext context) throws Exception {
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
        public void display(RenderContext context) throws Exception {
            GL3 gl = context.getDrawable().getGL().getGL3();

            // set point style
            context.Points.setPointSize(gl, pointsize);

            shader.bind(gl);
            uColor.set(color);
            mesh.display(context, vao);
        }


        public static class Builder implements BatchBuilder {

            private final ShaderProgramSource shader;
            private final float pointsize;
            private final Color color;
            private ArrayList<Vec2d> vertices = new ArrayList<>();

            public Builder(Color color, float poinsize, ShaderProgramSource shader) {
                this.color = color;
                this.pointsize = poinsize;
                this.shader = shader;
            }


            @Override
            public boolean isApplicableFor(Component component, ComponentRenderPass pass) {
                return component instanceof AreaVertex && pass instanceof AreaVertexPass
                        && color.equals(((AreaVertexPass) pass).getColor())
                        && pointsize == ((AreaVertexPass) pass).getPointSize();
            }

            @Override
            public BatchBuilder add(Component component, ComponentRenderPass pass, Mat3d transform) {
                AreaVertex vertex = (AreaVertex) component;

                Vec3d pos = transform.mul(new Vec3d(vertex.pos.x, vertex.pos.y, 1.0));
                vertices.add(new Vec2d(pos.x / pos.z, pos.y / pos.z));

                return this;
            }

            @Override
            public Batch build(RenderContext context) {
                FloatBuffer vb = FloatBuffer.allocate(vertices.size() * 2);

                for (int i = 0; i < vertices.size(); i++) {
                    Vec2d v = vertices.get(i);
                    vb.put((float) v.x);
                    vb.put((float) v.y);
                }

                vb.rewind();

                SingleFloatAttributeMesh mesh = SingleFloatAttributeMesh.newPos2Mesh(GL3.GL_STATIC_DRAW, GL3.GL_POINTS, vb);
                return new AreaVertexBatch(pointsize, color, mesh, shader);
            }
        }
    }

    public static class AreaVertexPass implements ComponentRenderPass {
        private Color color;
        private float pointsize;
        private Predicate<AreaVertex> filter;

        AreaVertexPass(Color color, float pointsize, Predicate<AreaVertex> filter) {
            this.color = color;
            this.pointsize = pointsize;
            this.filter = filter;
        }


        Color getColor() {
            return color;
        }

        float getPointSize() {
            return pointsize;
        }


        @Override
        public boolean isActiveFor(Component component) {
            return component instanceof AreaVertex && filter.test((AreaVertex) component);
        }

        @Override
        public BatchBuilder createBuilder() {
            return new AreaVertexBatch.Builder(color, pointsize, SHADER);
        }
    }
}
