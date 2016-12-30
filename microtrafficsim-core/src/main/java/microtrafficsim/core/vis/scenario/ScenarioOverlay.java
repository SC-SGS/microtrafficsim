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
import microtrafficsim.core.vis.opengl.utils.Colors;
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


// TODO: replace pos3 with pos2 meshes


public class ScenarioOverlay implements Overlay {

    private static final ShaderProgramSource POLYGON_FILL_SHADER = new ShaderProgramSource(
            "/shaders/features/polygons/polygons",
            new ShaderSource(GL3.GL_VERTEX_SHADER, new PackagedResource(ScenarioOverlay.class,
                    "/shaders/features/polygons/polygons.vs")),
            new ShaderSource(GL3.GL_FRAGMENT_SHADER, new PackagedResource(ScenarioOverlay.class,
                    "/shaders/features/polygons/polygons.fs"))
    );

    private static final ShaderProgramSource OUTLINE_SHADER = new ShaderProgramSource(
            "/shaders/basic",
            new ShaderSource(GL3.GL_VERTEX_SHADER, new PackagedResource(ScenarioOverlay.class,
                    "/shaders/basic.vs")),
            new ShaderSource(GL3.GL_FRAGMENT_SHADER, new PackagedResource(ScenarioOverlay.class,
                    "/shaders/basic.fs"))
    );

    private static final Color COLOR_SPAWN_FILL             = Color.fromRGBA(0x48CF9430);
    private static final Color COLOR_SPAWN_OUTLINE_SELECTED = Colors.white();
    private static final Color COLOR_DEST_FILL              = Color.fromRGBA(0xE03A5330);
    private static final Color COLOR_DEST_OUTLINE_SELECTED  = Colors.white();

    private static final Color COLOR_POINTS_FILL            = Colors.black();
    private static final Color COLOR_POINTS_OUTLINE         = Colors.white();

    private static final float WIDTH_OUTLINE_SELECTED       = 2.f;

    private static final float SIZE_POINTS_FILL             = 5.f;
    private static final float SIZE_POINTS_OUTLINE          = 7.f;

    private static final Comparator<Area> AREA_BATCH_CMP = Comparator.comparingInt(a -> a.style.hashCode());

    private static final FloatBuffer EMPTY_FLOAT_BUFFER = FloatBuffer.allocate(0);
    private static final IntBuffer EMPTY_INT_BUFFER = IntBuffer.allocate(0);

    private boolean enabled;

    private RenderContext context;

    private MouseListener mouseListener;

    private Triangulator triangulator;

    private Projection projection;
    private OrthographicView view;

    private ShaderProgram shaderPolygon;
    private UniformVec4f uPolygonColor;

    private ShaderProgram shaderOutline;
    private UniformVec4f uOutlineColor;

    private SortedArrayList<Area> areas;
    private ArrayList<Batch> areaBatches;

    private SortedArrayList<Area> selected;
    private ArrayList<Batch> selectedBatches;
    private boolean rebatchSelected;
    private boolean rebuildSelected;


    public ScenarioOverlay(Projection projection) {
        this.enabled = true;
        this.mouseListener = new MouseListenerImpl();
        this.triangulator = new SweepLineTriangulator();
        this.projection = projection;

        this.areas = new SortedArrayList<>(AREA_BATCH_CMP);
        this.selected = new SortedArrayList<>(AREA_BATCH_CMP);

        this.areaBatches = new ArrayList<>();
        this.selectedBatches = new ArrayList<>();

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
                }),
                new Polygon(new Vec2d[]{
                        projection.project(new Coordinate (35.63937247821449, 139.73230627569114)),
                        projection.project(new Coordinate (35.63870966518994, 139.73208384882096)),
                        projection.project(new Coordinate (35.63590771303082, 139.7315648527906)),
                        projection.project(new Coordinate (35.635726938550484, 139.73301062744665)),
                        projection.project(new Coordinate (35.63532019447478, 139.73464175782783)),
                        projection.project(new Coordinate (35.63482306001563, 139.73577242775113)),
                        projection.project(new Coordinate (35.634536829681956, 139.73640263721657)),
                        projection.project(new Coordinate (35.63431085764183, 139.7368660265294)),
                        projection.project(new Coordinate (35.63417527411111, 139.7375518427124)),
                        projection.project(new Coordinate (35.63408488496287, 139.7379596253077)),
                        projection.project(new Coordinate (35.63547084067009, 139.73846008576558)),
                        projection.project(new Coordinate (35.636434969596365, 139.73883079721583)),
                        projection.project(new Coordinate (35.637188187227544, 139.73914590194855)),
                        projection.project(new Coordinate (35.63795646189825, 139.73946100668127)),
                        projection.project(new Coordinate (35.63889043292365, 139.73998000271166)),
                        projection.project(new Coordinate (35.63935741434317, 139.74022096515432))
                }),

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
        this.context = context;

        shaderPolygon = context.getShaderManager().load(POLYGON_FILL_SHADER);
        uPolygonColor = (UniformVec4f) shaderPolygon.getUniform("u_color");

        shaderOutline = context.getShaderManager().load(OUTLINE_SHADER);
        uOutlineColor = (UniformVec4f) shaderOutline.getUniform("u_color");

        updateAreas(context);
    }

    @Override
    public void dispose(RenderContext context) throws Exception {
        GL3 gl = context.getDrawable().getGL().getGL3();

        for (Batch batch : areaBatches)
            batch.dispose(context);
        areaBatches.clear();

        for (Batch batch : selectedBatches)
            batch.dispose(context);
        selectedBatches.clear();

        shaderPolygon.dispose(gl);
    }

    @Override
    public void resize(RenderContext context) throws Exception {}

    @Override
    public void display(RenderContext context, MapBuffer map) throws Exception {
        if (!enabled) return;

        updateAreas(context);
        updateSelected(context);

        if (areas.isEmpty())
            return;

        GL3 gl = context.getDrawable().getGL().getGL3();

        // disable depth test
        context.DepthTest.disable(gl);

        // enable blending
        context.BlendMode.enable(gl);
        context.BlendMode.setEquation(gl, GL3.GL_FUNC_ADD);
        context.BlendMode.setFactors(gl, GL3.GL_SRC_ALPHA, GL3.GL_ONE_MINUS_SRC_ALPHA, GL3.GL_ONE,
                GL3.GL_ONE_MINUS_SRC_ALPHA);

        // draw interior
        for (Batch batch : areaBatches)
            batch.display(context);

        if (!selectedBatches.isEmpty()) {
            // set line style
            context.Lines.setLineSmoothEnabled(gl, true);
            context.Lines.setLineSmoothHint(gl, GL3.GL_NICEST);
            context.Lines.setLineWidth(gl, WIDTH_OUTLINE_SELECTED);

            // draw outline
            for (Batch batch : selectedBatches)
                batch.display(context);

            // set point style
            context.Points.setPointSize(gl, SIZE_POINTS_OUTLINE);

            // draw points
            for (Batch batch : selectedBatches)
                batch.display(context, GL3.GL_POINTS, COLOR_POINTS_OUTLINE);

            // set point style
            context.Points.setPointSize(gl, SIZE_POINTS_FILL);

            // draw points
            for (Batch batch : selectedBatches)
                batch.display(context, GL3.GL_POINTS, COLOR_POINTS_FILL);
        }

        context.ShaderState.unbind(gl);
    }


    private ArrayList<List<Area>> batchByStyle(SortedArrayList<Area> from) {
        ArrayList<List<Area>> groups = new ArrayList<>();

        // group
        int begin = 0;
        for (int i = 0; i < from.size(); i++) {
            AreaStyle style = from.get(i).style;

            for (; i < from.size(); i++) {
                if (!style.equals(from.get(i).style))
                    break;
            }

            groups.add(from.subList(begin, i));
            begin = i;
        }

        if (begin != from.size())
            groups.add(from.subList(begin, from.size()));

        return groups;
    }


    private void updateAreas(RenderContext context) {
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

            rebuildAreaBatches(context);
        }

        rebatchSelected |= rebatch;
        rebuildSelected |= rebuild;
    }

    private void rebuildAreaBatches(RenderContext context) {
        ArrayList<List<Area>> groups = batchByStyle(areas);

        // create batches
        ArrayList<Batch> batches = new ArrayList<>(groups.size());
        batches.addAll(this.areaBatches.subList(0, Math.min(this.areaBatches.size(), groups.size())));

        if (groups.size() > batches.size()) {                   // create new batches, if necessary
            int num = groups.size() - batches.size();
            for (int i = 0; i < num; i++) {
                Batch batch = new Batch(GL3.GL_DYNAMIC_DRAW, GL3.GL_TRIANGLES, shaderPolygon, uPolygonColor);
                batch.initalize(context);
                batches.add(batch);
            }

        } else if (groups.size() < this.areaBatches.size()) {       // dispose batches that are not required any more
            for (Batch batch : this.areaBatches.subList(groups.size(), this.areaBatches.size()))
                batch.dispose(context);
        }

        // load batches
        for (int i = 0; i < groups.size(); i++) {
            List<Area> group = groups.get(i);
            Batch batch = batches.get(i);

            loadAreaGroupToPolygonBatch(context, group, batch);
        }

        this.areaBatches = batches;
    }

    private void loadAreaGroupToPolygonBatch(RenderContext context, List<Area> src, Batch dst) {
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


    private void updateSelected(RenderContext context) {
        if (!rebuildSelected && !rebatchSelected) return;

        if (rebatchSelected) {
            selected.sort();
            rebatchSelected = false;
        }

        rebuildSelectedBatches(context);

        rebuildSelected = false;
    }

    private void rebuildSelectedBatches(RenderContext context) {
        ArrayList<List<Area>> groups = batchByStyle(selected);

        // create batches
        ArrayList<Batch> batches = new ArrayList<>(groups.size());
        batches.addAll(this.selectedBatches.subList(0, Math.min(this.selectedBatches.size(), groups.size())));

        if (groups.size() > batches.size()) {                   // create new batches, if necessary
            int num = groups.size() - batches.size();
            for (int i = 0; i < num; i++) {
                Batch batch = new Batch(GL3.GL_DYNAMIC_DRAW, GL3.GL_LINE_STRIP, shaderOutline, uOutlineColor);
                batch.initalize(context);
                batches.add(batch);
            }

        } else if (groups.size() < this.selectedBatches.size()) {       // dispose batches that are not required any more
            for (Batch batch : this.selectedBatches.subList(groups.size(), this.selectedBatches.size()))
                batch.dispose(context);
        }

        // load batches
        for (int i = 0; i < groups.size(); i++) {
            List<Area> group = groups.get(i);
            Batch batch = batches.get(i);

            loadAreaGroupToOutlineBatch(context, group, batch);
        }

        this.selectedBatches = batches;
    }

    private void loadAreaGroupToOutlineBatch(RenderContext context, List<Area> src, Batch dst) {
        dst.color = new Color(src.get(0).style.getSelectedColor());

        int restart = context.PrimitiveRestart.getIndex();

        int numIndices = 0;
        int numVertices = 0;

        for (Area area : src) {
            numIndices += area.polygon.outline.length + 2;
            numVertices += area.polygon.outline.length;
        }

        FloatBuffer vertices = FloatBuffer.allocate(numVertices * 3);
        IntBuffer indices = IntBuffer.allocate(numIndices);

        int base = 0;
        for (Area area : src) {
            for (Vec2d v : area.polygon.outline) {
                vertices.put((float) v.x);
                vertices.put((float) v.y);
                vertices.put(0.0f);
            }

            for (int i = 0; i < area.polygon.outline.length; i++)
                indices.put(base + i);

            indices.put(base);
            indices.put(restart);

            base += area.polygon.outline.length;
        }

        vertices.rewind();
        indices.rewind();

        dst.mesh.setVertexBuffer(vertices);
        dst.mesh.setIndexBuffer(indices);
        dst.mesh.load(context, true);
    }


    private Vec2d screenToWorld(int x, int y) {
        Vec2i viewport = view.getSize();
        Rect2d bounds = view.getViewportBounds();

        y = viewport.y - y;

        return new Vec2d(
                (x / (double) viewport.x) * (bounds.xmax - bounds.xmin) + bounds.xmin,
                (y / (double) viewport.y) * (bounds.ymax - bounds.ymin) + bounds.ymin
        );
    }

    private Area lookupArea(Vec2d p) {
        for (int i = areas.size() - 1; i >= 0; i--) {
            Area area = areas.get(i);

            if (area.polygon.contains(p))
                return area;

        }

        return null;
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
                    style.setColor(COLOR_SPAWN_FILL);
                    style.setSelectedColor(COLOR_SPAWN_OUTLINE_SELECTED);
                    break;
                case DEST:
                    style.setColor(COLOR_DEST_FILL);
                    style.setSelectedColor(COLOR_DEST_OUTLINE_SELECTED);
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
        private Color selectedColor;

        AreaStyle() {
            this(Colors.black(), Colors.black());
        }

        AreaStyle(Color color, Color selectedColor) {
            this.color = color;
            this.selectedColor = selectedColor;
            this.changed = true;
        }


        void setColor(Color color) {
            if (color.equals(this.color)) return;
            this.color = color;
            this.changed = true;
        }

        Color getColor() {
            return color;
        }


        void setSelectedColor(Color selectedColor) {
            if (selectedColor.equals(this.selectedColor)) return;
            this.selectedColor = selectedColor;
            this.changed = true;
        }

        Color getSelectedColor() {
            return selectedColor;
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


    private static class Batch {
        private Color color;

        private ShaderProgram shader;
        private UniformVec4f uColor;
        private Pos3IndexedMesh mesh;
        private VertexArrayObject vao;

        Batch(int usage, int mode, ShaderProgram shader, UniformVec4f uColor) {
            this(usage, mode, shader, uColor, Color.fromRGB(0x000000));
        }

        Batch(int usage, int mode, ShaderProgram shader, UniformVec4f uColor, Color color) {
            this.shader = shader;
            this.uColor = uColor;
            this.color = color;
            this.mesh = new Pos3IndexedMesh(usage, mode, EMPTY_FLOAT_BUFFER, EMPTY_INT_BUFFER);
            this.vao = null;
        }


        void initalize(RenderContext context) {
            mesh.initialize(context);
            vao = mesh.createVAO(context, shader);
        }

        void display(RenderContext context) {
            GL3 gl = context.getDrawable().getGL().getGL3();

            uColor.set(color);
            shader.bind(gl);
            mesh.display(context, vao);
        }

        void display(RenderContext context, int mode, Color color) {
            GL3 gl = context.getDrawable().getGL().getGL3();

            uColor.set(color);
            shader.bind(gl);
            mesh.display(context, vao, mode);
        }

        void dispose(RenderContext context) {
            GL3 gl = context.getDrawable().getGL().getGL3();

            vao.dispose(gl);
            mesh.dispose(context);
        }
    }


    private class MouseListenerImpl implements MouseListener {

        private Vec2d pos;
        private Area area;


        @Override
        public boolean mouseClicked(MouseEvent e) {
            if (e.getButton() != MouseEvent.BUTTON1)
                return false;

            Vec2d pos = screenToWorld(e.getX(), e.getY());
            Area area = lookupArea(pos);

            context.addTask(c -> {
                if (!e.isControlDown()) selected.clear();

                if (area != null) {
                    if (selected.contains(area))
                        selected.remove(area);
                    else
                        selected.add(area);
                }

                rebatchSelected = true;
                rebuildSelected = true;
                return null;
            });

            return area != null;
        }

        @Override
        public boolean mousePressed(MouseEvent e) {
            if (e.getButton() != MouseEvent.BUTTON1)
                return false;

            if (!e.isControlDown()) {
                this.area = null;
                this.pos = null;
                return false;
            }

            Vec2d pos = screenToWorld(e.getX(), e.getY());
            Area area = lookupArea(pos);

            if (area != null) {
                this.area = area;
                this.pos = pos;
                return true;
            } else {
                this.area = null;
                this.pos = null;
                return false;
            }
        }

        @Override
        public boolean mouseDragged(MouseEvent e) {
            if (e.getButton() != MouseEvent.BUTTON1)
                return false;

            if (pos == null)
                return false;

            Vec2d pos = screenToWorld(e.getX(), e.getY());
            Vec2d delta = Vec2d.sub(pos, this.pos);

            Area down = this.area;

            context.addTask(c -> {
                if (down != null && !selected.contains(down))
                    selected.add(down);

                for (Area area : selected) {
                    for (Vec2d v : area.polygon.outline)
                        v.add(delta);

                    area.cached = null;
                }
                return null;
            });

            this.pos = pos;
            this.area = null;
            return true;
        }
    }
}
