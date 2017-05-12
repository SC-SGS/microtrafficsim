package microtrafficsim.core.exfmt;

import microtrafficsim.core.exfmt.ecs.EntityManager;
import microtrafficsim.core.exfmt.ecs.FeatureManager;
import microtrafficsim.core.exfmt.ecs.features.MultiLineFeatureExtractor;
import microtrafficsim.core.exfmt.ecs.features.PointFeatureExtractor;
import microtrafficsim.core.exfmt.ecs.features.PolygonFeatureExtractor;
import microtrafficsim.core.exfmt.ecs.features.StreetFeatureExtractor;
import microtrafficsim.core.exfmt.ecs.processors.FeatureProcessor;
import microtrafficsim.core.exfmt.ecs.processors.TileGridProcessor;
import microtrafficsim.core.exfmt.extractor.map.MapSegmentExtractor;
import microtrafficsim.core.exfmt.extractor.map.QuadTreeTiledMapSegmentExtractor;
import microtrafficsim.core.exfmt.extractor.simulation.UnprojectedAreasExtractor;
import microtrafficsim.core.exfmt.extractor.simulation.RouteContainerExtractor;
import microtrafficsim.core.exfmt.extractor.simulation.SimulationConfigExtractor;
import microtrafficsim.core.exfmt.extractor.streetgraph.StreetGraphExtractor;
import microtrafficsim.core.exfmt.injector.map.QuadTreeTiledMapSegmentInjector;
import microtrafficsim.core.exfmt.injector.map.SegmentFeatureProviderInjector;
import microtrafficsim.core.exfmt.injector.map.features.FeatureInjector;
import microtrafficsim.core.exfmt.injector.map.features.TileFeatureGridInjector;
import microtrafficsim.core.exfmt.injector.map.features.primitives.MultiLineInjector;
import microtrafficsim.core.exfmt.injector.map.features.primitives.PointInjector;
import microtrafficsim.core.exfmt.injector.map.features.primitives.PolygonInjector;
import microtrafficsim.core.exfmt.injector.map.features.primitives.StreetInjector;
import microtrafficsim.core.exfmt.injector.simulation.ProjectedAreasInjector;
import microtrafficsim.core.exfmt.injector.simulation.RouteContainerInjector;
import microtrafficsim.core.exfmt.injector.simulation.SimulationConfigInjector;
import microtrafficsim.core.exfmt.injector.simulation.UnprojectedAreasInjector;
import microtrafficsim.core.exfmt.injector.streetgraph.DirectedEdgeInjector;
import microtrafficsim.core.exfmt.injector.streetgraph.GraphInjector;
import microtrafficsim.core.exfmt.injector.streetgraph.NodeInjector;
import microtrafficsim.core.logic.nodes.Node;
import microtrafficsim.core.logic.streetgraph.Graph;
import microtrafficsim.core.logic.streetgraph.StreetGraph;
import microtrafficsim.core.logic.streets.DirectedEdge;
import microtrafficsim.core.map.*;
import microtrafficsim.core.map.features.MultiLine;
import microtrafficsim.core.map.features.Point;
import microtrafficsim.core.map.features.Polygon;
import microtrafficsim.core.map.features.Street;
import microtrafficsim.core.map.tiles.QuadTreeTiledMapSegment;
import microtrafficsim.core.map.tiles.TileFeatureGrid;
import microtrafficsim.core.simulation.configs.SimulationConfig;
import microtrafficsim.core.simulation.utils.RouteContainer;
import microtrafficsim.utils.collections.Composite;

import java.util.HashMap;


// TODO:
// - add load/unload functions to Interfaces (Injector, Extractor, EntityProcessor, PostProcessor, ...) for perf?


/**
 * @author Maximilian Luz
 */
public class ExchangeFormat {

    public static ExchangeFormat getDefault() {
        ExchangeFormat format = new ExchangeFormat();

        {   // CONFIGURATION
            Config cfg = format.getConfig();

            // entity processors (may be called by insertion of new entities)
            EntityManager ecsmgr = cfg.get(EntityManager.class, EntityManager::new);
            ecsmgr.addProcessor(new FeatureProcessor());
            ecsmgr.addProcessor(new TileGridProcessor());

            // feature extractors
            FeatureManager fmgr = cfg.get(FeatureManager.class, FeatureManager::new);
            fmgr.setExtractor(Point.class, new PointFeatureExtractor());
            fmgr.setExtractor(MultiLine.class, new MultiLineFeatureExtractor());
            fmgr.setExtractor(Street.class, new StreetFeatureExtractor());
            fmgr.setExtractor(Polygon.class, new PolygonFeatureExtractor());
        }

        {   // INJECTORS/EXTRACTORS
            // map feature primitives
            format.injector(Point.class, new PointInjector());
            format.injector(MultiLine.class, new MultiLineInjector());
            format.injector(Street.class, new StreetInjector());
            format.injector(Polygon.class, new PolygonInjector());

            // features
            format.injector(Feature.class, new FeatureInjector());
            format.injector(TileFeatureGrid.class, new TileFeatureGridInjector());

            // map segments
            format.injector(SegmentFeatureProvider.class, new SegmentFeatureProviderInjector());
            format.injector(MapSegment.class, new SegmentFeatureProviderInjector());
            format.extractor(MapSegment.class, new MapSegmentExtractor());

            format.injector(QuadTreeTiledMapSegment.class, new QuadTreeTiledMapSegmentInjector());
            format.extractor(QuadTreeTiledMapSegment.class, new QuadTreeTiledMapSegmentExtractor());

            // street-graph
            format.injector(Node.class, new NodeInjector());
            format.injector(DirectedEdge.class, new DirectedEdgeInjector());

            format.injector(Graph.class, new GraphInjector());
            format.injector(StreetGraph.class, new GraphInjector());
            format.extractor(StreetGraph.class, new StreetGraphExtractor());

            // simulation
            format.injector(SimulationConfig.class, new SimulationConfigInjector());
            format.extractor(SimulationConfig.class, new SimulationConfigExtractor());

            format.injector(RouteContainer.class, new RouteContainerInjector());
            format.extractor(RouteContainer.class, new RouteContainerExtractor());

            format.injector(ProjectedAreas.class, new ProjectedAreasInjector());
//            format.extractor(ProjectedAreas.class, new ProjectedAreasExtractor());
            format.injector(UnprojectedAreas.class, new UnprojectedAreasInjector());
            format.extractor(UnprojectedAreas.class, new UnprojectedAreasExtractor());
        }

        return format;
    }


    private Config config = new Config();
    private HashMap<Class<?>, Injector<?>>  injectors  = new HashMap<>();
    private HashMap<Class<?>, Extractor<?>> extractors = new HashMap<>();


    public <T> void injector(Class<T> type, Injector<? super T> injector) {
        if (injector != null)
            injectors.put(type, injector);
        else
            injectors.remove(type);
    }

    public <T> void extractor(Class<T> type, Extractor<T> extractor) {
        if (extractor != null)
            extractors.put(type, extractor);
        else
            extractors.remove(type);
    }


    @SuppressWarnings("unchecked")
    public <T> void inject(Context ctx, Container dst, T src) throws Exception {
        Injector<?> injector = injectors.get(src.getClass());
        if (injector == null)
            throw new IllegalArgumentException("No injector for class '" + src.getClass().getName() + "'");

        call((Injector<? super T>) injector, ctx, dst, src);
    }

    @SuppressWarnings("unchecked")
    public <T> void inject(Context ctx, Container dst, Class<T> type, T src) throws Exception {
        Injector<?> injector = injectors.get(type);
        if (injector == null)
            throw new IllegalArgumentException("No injector for class '" + type.getName() + "'");

        call((Injector<? super T>) injector, ctx, dst, src);
    }

    @SuppressWarnings("unchecked")
    public <T> T extract(Context ctx, Container src, Class<T> type) throws Exception {
        Extractor<?> extractor = extractors.get(type);
        if (extractor == null)
            throw new IllegalArgumentException("No extractor for class '" + type.getName() + "'");

        return call((Extractor<? extends T>) extractor, ctx, src);
    }


    public Config getConfig() {
        return config;
    }

    public void setConfig(Config config) {
        this.config = config;
    }


    public Manipulator manipulator() {
        return new Manipulator(this);
    }

    public Manipulator manipulator(Container container) {
        return new Manipulator(this, container);
    }


    private <T> void call(Injector<? super T> injector, Context ctx, Container dst, T src) throws Exception {
        injector.inject(this, ctx, dst, src);
    }

    private <T> T call(Extractor<T> extractor, Context ctx, Container src) throws Exception {
        return extractor.extract(this, ctx, src);
    }


    public interface Injector<T> {
        void inject(ExchangeFormat fmt, Context ctx, Container dst, T src) throws Exception;
    }

    public interface Extractor<T> {
        T extract(ExchangeFormat fmt, Context ctx, Container src) throws Exception;
    }

    /**
     * A context is used as temporary storage while injecting/extracting a map file.
     */
    public static class Context extends Composite<Object> {}

    public static class Manipulator {
        private ExchangeFormat format;
        private Context context;
        private Container container;

        public Manipulator() {
            this(ExchangeFormat.getDefault(), new Context(), new Container());
        }

        public Manipulator(ExchangeFormat format) {
            this(format, new Context(), new Container());
        }

        public Manipulator(Container container) {
            this(ExchangeFormat.getDefault(), new Context(), container);
        }

        public Manipulator(ExchangeFormat format, Container container) {
            this(format, new Context(), container);
        }

        public Manipulator(ExchangeFormat format, Context context, Container container) {
            this.format = format;
            this.context = context;
            this.container = container;
        }


        public Context getContext() {
            return context;
        }

        public ExchangeFormat getFormat() {
            return format;
        }

        public Container getContainer() {
            return container;
        }


        public <T> Manipulator inject(T obj) throws Exception {
            format.inject(context, container, obj);
            return this;
        }

        public <T> Manipulator inject(Class<T> type, T obj) throws Exception {
            format.inject(context, container, type, obj);
            return this;
        }

        public <T> T extract(Class<T> type) throws Exception {
            return format.extract(context, container, type);
        }


        public void reset() {
            this.context = new Context();
        }
    }
}
