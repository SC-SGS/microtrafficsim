package microtrafficsim.core.vis.scenario;

import com.jogamp.newt.event.MouseEvent;
import com.jogamp.opengl.GL3;
import microtrafficsim.core.map.Coordinate;
import microtrafficsim.core.vis.Overlay;
import microtrafficsim.core.vis.context.RenderContext;
import microtrafficsim.core.vis.map.projections.Projection;
import microtrafficsim.core.vis.mesh.impl.Pos3IndexedMesh;
import microtrafficsim.core.vis.opengl.shader.ShaderProgram;
import microtrafficsim.core.vis.opengl.shader.attributes.VertexArrayObject;
import microtrafficsim.core.vis.opengl.shader.resources.ShaderProgramSource;
import microtrafficsim.core.vis.opengl.shader.resources.ShaderSource;
import microtrafficsim.core.vis.opengl.shader.uniforms.UniformVec4f;
import microtrafficsim.core.vis.opengl.utils.Color;
import microtrafficsim.core.vis.view.OrthographicView;
import microtrafficsim.math.Rect2d;
import microtrafficsim.math.Vec2d;
import microtrafficsim.math.Vec2i;
import microtrafficsim.math.geometry.polygons.Polygon;
import microtrafficsim.math.geometry.polygons.SweepLineTriangulator;
import microtrafficsim.math.geometry.polygons.Triangulator;
import microtrafficsim.utils.collections.SortedArrayList;
import microtrafficsim.utils.resources.PackagedResource;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;


public class ScenarioOverlay implements Overlay {

    private static final ShaderProgramSource POLYGON_FILL_SHADER = new ShaderProgramSource(
            "/shaders/features/polygons/polygons",
            new ShaderSource(GL3.GL_VERTEX_SHADER, new PackagedResource(ScenarioOverlay.class,
                    "/shaders/features/polygons/polygons.vs")),
            new ShaderSource(GL3.GL_FRAGMENT_SHADER, new PackagedResource(ScenarioOverlay.class,
                    "/shaders/features/polygons/polygons.fs"))
    );

    private static final Color COLOR_SPAWN  = Color.fromRGBA(0x48CF9430);
    private static final Color COLOR_DEST   = Color.fromRGBA(0xE03A5330);

    private boolean enabled;

    private MouseListener mouseListener;

    private Triangulator triangulator;

    private Projection projection;
    private OrthographicView view;

    private ShaderProgram shaderPolygon;
    private UniformVec4f uPolygonColor;

    private SortedArrayList<Area> areas;
    private ArrayList<AreaBatch> batches;

    private ArrayList<Area> selected;


    public ScenarioOverlay(Projection projection) {
        this.enabled = true;
        this.mouseListener = new MouseListenerImpl();
        this.triangulator = new SweepLineTriangulator();
        this.projection = projection;
        this.areas = new SortedArrayList<>(Comparator.comparingInt(x -> x.style.hashCode()));   // batch by style
        this.batches = new ArrayList<>();
        this.selected = new ArrayList<>();

        loadTokyoTestingPolygons();
    }

    private void loadTokyoTestingPolygons() {
        Polygon[] spawn = {
                new Polygon(new Vec2d[]{
                        projection.project(new Coordinate(35.613840948933316, 139.7211548375855)),
                        projection.project(new Coordinate(35.613462626375025, 139.75994834446493)),
                        projection.project(new Coordinate(35.622352428408696, 139.76000635856752)),
                        projection.project(new Coordinate(35.62237600747141, 139.75629345600166)),
                        projection.project(new Coordinate(35.62185726648556, 139.75577132907836)),
                        projection.project(new Coordinate(35.621621474015924, 139.75530721625762)),
                        projection.project(new Coordinate(35.62145641887346, 139.75423395535967)),
                        projection.project(new Coordinate(35.621621474015924, 139.75144927843527)),
                        projection.project(new Coordinate(35.621621474015924, 139.7507241021529)),
                        projection.project(new Coordinate(35.620395341965995, 139.74651807971503)),
                        projection.project(new Coordinate(35.62025386398122, 139.74622800920204)),
                        projection.project(new Coordinate(35.61808450353957, 139.74419751561135)),
                        projection.project(new Coordinate(35.61690547861829, 139.74222503612324)),
                        projection.project(new Coordinate(35.61681115587373, 139.74077468355847)),
                        projection.project(new Coordinate(35.61685831725991, 139.7377289431724)),
                        projection.project(new Coordinate(35.616575348525686, 139.73726483035168)),
                        projection.project(new Coordinate(35.61600940805381, 139.73685873163356)),
                        projection.project(new Coordinate(35.61626879793382, 139.73546639317135)),
                        projection.project(new Coordinate(35.617848699945796, 139.73439313227342)),
                        projection.project(new Coordinate(35.617943021466715, 139.73401604060655)),
                        projection.project(new Coordinate(35.617848699945796, 139.73334887842677)),
                        projection.project(new Coordinate(35.61775437831362, 139.73282675150344)),
                        projection.project(new Coordinate(35.61791944109692, 139.73192753291326)),
                        projection.project(new Coordinate(35.61836746693439, 139.73018710983553)),
                        projection.project(new Coordinate(35.61869758962963, 139.72949094060442)),
                        projection.project(new Coordinate(35.620772615368324, 139.72798257393706)),
                        projection.project(new Coordinate(35.62081977441848, 139.72740243291113)),
                        projection.project(new Coordinate(35.620796194896876, 139.72603910150025)),
                        projection.project(new Coordinate(35.621810108047264, 139.72426967137122)),
                        projection.project(new Coordinate(35.62294190289077, 139.7224132200883)),
                        projection.project(new Coordinate(35.623696423886855, 139.72081783226702))
                })
        };

        Polygon[] dest = {
                new Polygon(new Vec2d[]{
                        projection.project(new Coordinate(35.63945929635736, 139.72115638347853)),
                        projection.project(new Coordinate(35.62429182065404, 139.72080285401069)),
                        projection.project(new Coordinate(35.62437392569791, 139.72110587926883)),
                        projection.project(new Coordinate(35.62540023163242, 139.72163617347059)),
                        projection.project(new Coordinate(35.626672832695796, 139.72330281239036)),
                        projection.project(new Coordinate(35.62792488816708, 139.72509571183437)),
                        projection.project(new Coordinate(35.628848523077934, 139.72615630023785)),
                        projection.project(new Coordinate(35.629505323633495, 139.7268633591735)),
                        projection.project(new Coordinate(35.63012106925354, 139.727923947577)),
                        projection.project(new Coordinate(35.63071628551135, 139.72890877966597)),
                        projection.project(new Coordinate(35.631516741766916, 139.72989361175493)),
                        projection.project(new Coordinate(35.63207089909461, 139.7306006706906)),
                        projection.project(new Coordinate(35.63241981099668, 139.7307269312148)),
                        projection.project(new Coordinate(35.63346653756495, 139.73092894805356)),
                        projection.project(new Coordinate(35.63441063212425, 139.7310552085778)),
                        projection.project(new Coordinate(35.63531366865945, 139.73115621699716)),
                        projection.project(new Coordinate(35.63601146263041, 139.73125722541656)),
                        projection.project(new Coordinate(35.63709918814083, 139.73148449436016)),
                        projection.project(new Coordinate(35.638104807989826, 139.73163600698922)),
                        projection.project(new Coordinate(35.63882310013475, 139.73176226751346)),
                        projection.project(new Coordinate(35.639356684405975, 139.7320147885619))
                }),
                new Polygon(new Vec2d[]{
                        projection.project(new Coordinate(35.639347754350815, 139.7405500000003)),
                        projection.project(new Coordinate(35.63755913068822, 139.73963139919263)),
                        projection.project(new Coordinate(35.63583268205591, 139.73894244858687)),
                        projection.project(new Coordinate(35.6340128715037, 139.73833004804843)),
                        projection.project(new Coordinate(35.63189748408799, 139.73779419757727)),
                        projection.project(new Coordinate(35.62920650596128, 139.73769850999315)),
                        projection.project(new Coordinate(35.62597099036785, 139.7381003978465)),
                        projection.project(new Coordinate(35.625162091010125, 139.73848314818304)),
                        projection.project(new Coordinate(35.62541098399183, 139.73938261147387)),
                        projection.project(new Coordinate(35.62768209665832, 139.73999501201234)),
                        projection.project(new Coordinate(35.6317886023385, 139.74116240053874)),
                        projection.project(new Coordinate(35.63197525667544, 139.741353775707)),
                        projection.project(new Coordinate(35.63209969265792, 139.74160256342574)),
                        projection.project(new Coordinate(35.63211524714212, 139.7419279012118)),
                        projection.project(new Coordinate(35.63188192956144, 139.74508559148816)),
                        projection.project(new Coordinate(35.63166416587174, 139.74864516961787)),
                        projection.project(new Coordinate(35.63155528380442, 139.75004220834623)),
                        projection.project(new Coordinate(35.6314619562001, 139.7520707851298)),
                        projection.project(new Coordinate(35.63132196458931, 139.75419504949755)),
                        projection.project(new Coordinate(35.63223968290666, 139.7546543499014)),
                        projection.project(new Coordinate(35.63230190071633, 139.75758238997582)),
                        projection.project(new Coordinate(35.63208413817069, 139.75779290266092)),
                        projection.project(new Coordinate(35.63213080162328, 139.76012767971375)),
                        projection.project(new Coordinate(35.639425519688935, 139.7601659547474))
                })
        };

        for (Polygon p : spawn)
            areas.add(new Area(p, new AreaProperties(AreaType.SPAWN), new AreaStyle()));

        for (Polygon p : dest)
            areas.add(new Area(p, new AreaProperties(AreaType.DEST), new AreaStyle()));
    }



    @Override
    public void init(RenderContext context) throws Exception {
        shaderPolygon = context.getShaderManager().load(POLYGON_FILL_SHADER);
        uPolygonColor = (UniformVec4f) shaderPolygon.getUniform("u_color");

        update(context);
    }

    @Override
    public void dispose(RenderContext context) throws Exception {
        GL3 gl = context.getDrawable().getGL().getGL3();

        for (AreaBatch batch : batches)
            batch.dispose(context);
        batches.clear();

        shaderPolygon.dispose(gl);
    }

    @Override
    public void resize(RenderContext context) throws Exception {}

    @Override
    public void display(RenderContext context, MapBuffer map) throws Exception {
        if (!enabled) return;

        update(context);

        GL3 gl = context.getDrawable().getGL().getGL3();

        // disable depth test
        context.DepthTest.disable(gl);

        // enable blending
        context.BlendMode.enable(gl);
        context.BlendMode.setEquation(gl, GL3.GL_FUNC_ADD);
        context.BlendMode.setFactors(gl, GL3.GL_SRC_ALPHA, GL3.GL_ONE_MINUS_SRC_ALPHA, GL3.GL_ONE,
                GL3.GL_ONE_MINUS_SRC_ALPHA);

        // draw
        for (AreaBatch batch : batches)
            batch.display(context);

        // TODO: draw selection

        context.ShaderState.unbind(gl);
    }

    private void update(RenderContext context) {
        boolean rebuild = false;
        boolean rebatch = false;

        for (Area area : areas) {
            if (area.properties.changed) {
                area.onPropertiesChanged();
                area.properties.changed = false;
            }

            if (area.style.changed) {
                area.onStyleChanged();
                area.style.changed = false;
                rebatch = true;
            }

            if (area.cached == null) {
                area.cached = triangulator.triangulate(area.polygon);
                rebuild = true;
            }
        }

        if (rebuild || rebatch) {
            if (rebatch) areas.sort();

            rebuildBatches(context);
        }
    }

    private void rebuildBatches(RenderContext context) {
        ArrayList<List<Area>> groups = new ArrayList<>();

        // group
        if (!areas.isEmpty()) {
            int begin = 0;
            for (int i = 0; i < areas.size(); i++) {
                AreaStyle style = areas.get(i).style;

                for (; i < areas.size(); i++) {
                    if (!style.equals(areas.get(i).style))
                        break;
                }

                groups.add(areas.subList(begin, i));
                begin = i;
            }
            
            groups.add(areas.subList(begin, areas.size()));
        }

        // create batches
        ArrayList<AreaBatch> batches = new ArrayList<>(groups.size());
        batches.addAll(this.batches.subList(0, Math.min(this.batches.size(), groups.size())));

        if (groups.size() > batches.size()) {                   // create new batches, if necessary
            int num = groups.size() - batches.size();
            for (int i = 0; i < num; i++) {
                AreaBatch batch = new AreaBatch();
                batch.initalize(context);
                batches.add(batch);
            }

        } else if (groups.size() < this.batches.size()) {       // dispose batches that are not required any more
            for (AreaBatch batch : this.batches.subList(groups.size(), this.batches.size()))
                batch.dispose(context);
        }

        // load batches
        for (int i = 0; i < groups.size(); i++) {
            List<Area> group = groups.get(i);
            AreaBatch batch = batches.get(i);

            loadGroupToBatch(context, group, batch);
        }

        this.batches = batches;
    }

    private void loadGroupToBatch(RenderContext context, List<Area> src, AreaBatch dst) {
        dst.color = new Color(src.get(0).style.getColor());

        int numIndices = 0;
        int numVertices = 0;

        for (Area area : src) {
            numIndices += area.cached.indices.size();
            numVertices += area.cached.vertices.size();
        }

        FloatBuffer vertices = FloatBuffer.allocate(numVertices * 3);
        IntBuffer indices = IntBuffer.allocate(numIndices);

        int base = 0;
        for (Area area : src) {
            for (Vec2d v : area.cached.vertices) {
                vertices.put((float) v.x);
                vertices.put((float) v.y);
                vertices.put(0.0f);
            }

            for (int i : area.cached.indices)
                indices.put(base + i);

            base += area.cached.vertices.size();
        }

        vertices.rewind();
        indices.rewind();

        dst.mesh.setVertexBuffer(vertices);
        dst.mesh.setIndexBuffer(indices);
        dst.mesh.load(context, true);
    }


    @Override
    public void setView(OrthographicView view) {
        this.view = view;
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
    public KeyListener getKeyListeners() {
        return null;    // TODO
    }

    @Override
    public MouseListener getMouseListener() {
        return mouseListener;
    }


    private static class Area {
        Polygon polygon;
        Triangulator.Result cached;
        AreaProperties properties;
        AreaStyle style;

        Area(Polygon polygon, AreaProperties properties, AreaStyle style) {
            this.polygon = polygon;
            this.cached = null;
            this.properties = properties;
            this.style = style;
        }


        private void onPropertiesChanged() {
            switch (properties.type) {
                case SPAWN:
                    style.setColor(COLOR_SPAWN);
                    break;
                case DEST:
                    style.setColor(COLOR_DEST);
            }
        }

        private void onStyleChanged() {
            /* do nothing */
        }
    }

    private enum AreaType { SPAWN, DEST };

    private static class AreaProperties {
        boolean changed;
        AreaType type;

        public AreaProperties(AreaType type) {
            this.type = type;
            this.changed = true;
        }


        void setType(AreaType type) {
            if (type.equals(this.type)) return;
            this.type = type;
        }

        AreaType getType() {
            return type;
        }
    }

    private static class AreaStyle {
        private boolean changed;
        private Color color;

        AreaStyle() {
            this(Color.fromRGB(0x000000));
        }

        AreaStyle(Color color) {
            this.color = color;
            this.changed = true;
        }


        void setColor(Color color) {
            if (color.equals(this.color)) return;
            this.color = color;
        }

        Color getColor() {
            return color;
        }


        @Override
        public int hashCode() {
            return color.hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof AreaStyle)) return false;

            AreaStyle other = (AreaStyle) obj;
            return this.color.equals(other.color);
        }
    }


    private static final FloatBuffer EMPTY_F = FloatBuffer.allocate(0);
    private static final IntBuffer EMPTY_I = IntBuffer.allocate(0);

    private class AreaBatch {
        private Color color;
        private Pos3IndexedMesh mesh;
        private VertexArrayObject vao;

        AreaBatch() {
            this(Color.fromRGB(0x000000));
        }

        AreaBatch(Color color) {
            this.color = color;
            this.mesh = new Pos3IndexedMesh(GL3.GL_DYNAMIC_DRAW, GL3.GL_TRIANGLES, EMPTY_F, EMPTY_I);
            this.vao = null;
        }


        void initalize(RenderContext context) {
            mesh.initialize(context);
            vao = mesh.createVAO(context, shaderPolygon);
        }

        void display(RenderContext context) {
            GL3 gl = context.getDrawable().getGL().getGL3();

            uPolygonColor.set(color);
            shaderPolygon.bind(gl);
            mesh.display(context, vao);
        }

        void dispose(RenderContext context) {
            GL3 gl = context.getDrawable().getGL().getGL3();

            vao.dispose(gl);
            mesh.dispose(context);
        }
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

            y = viewport.y - y;

            return projection.unproject(new Vec2d(
                    (x / (double) viewport.x) * (bounds.xmax - bounds.xmin) + bounds.xmin,
                    (y / (double) viewport.y) * (bounds.ymax - bounds.ymin) + bounds.ymin
            ));
        }
    }
}
