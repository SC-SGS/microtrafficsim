package microtrafficsim.tools.exfmtconv;

import microtrafficsim.core.convenience.parser.DefaultParserConfig;
import microtrafficsim.core.exfmt.Container;
import microtrafficsim.core.exfmt.ExchangeFormat;
import microtrafficsim.core.exfmt.extractor.map.QuadTreeTiledMapSegmentExtractor;
import microtrafficsim.core.exfmt.extractor.streetgraph.StreetGraphExtractor;
import microtrafficsim.core.logic.streetgraph.Graph;
import microtrafficsim.core.map.Bounds;
import microtrafficsim.core.map.SegmentFeatureProvider;
import microtrafficsim.core.map.style.impl.LightMonochromeStyleSheet;
import microtrafficsim.core.map.tiles.QuadTreeTiledMapSegment;
import microtrafficsim.core.map.tiles.QuadTreeTilingScheme;
import microtrafficsim.core.map.tiles.TilingScheme;
import microtrafficsim.core.parser.OSMParser;
import microtrafficsim.core.serialization.ExchangeFormatSerializer;
import microtrafficsim.core.simulation.configs.SimulationConfig;
import microtrafficsim.core.vis.map.projections.MercatorProjection;
import microtrafficsim.core.vis.map.projections.Projection;
import microtrafficsim.osm.parser.features.FeatureGenerator;
import microtrafficsim.utils.collections.Tuple;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;


public class ExchangeFormatConverter {
    private final static Logger logger = LoggerFactory.getLogger(ExchangeFormatConverter.class);

    public final static Bounds CLIP = new Bounds(48.8, 9.1, 48.85, 9.28);
    public final static File FILE_IN = new File("/mnt/development/workspaces/microtrafficsim/osmfiles/stuttgart-small.osm");
    public final static File FILE_OUT = new File("/mnt/development/workspaces/microtrafficsim/osmfiles/stuttgart-small-extract.mtsmap");

    public final static Projection PROJECTION = new MercatorProjection();
    public final static TilingScheme TILING_SCHEME = new QuadTreeTilingScheme(PROJECTION);
    public final static int TILE_GRID_LEVEL = 12;


    private static SimulationConfig config() {
        SimulationConfig config = new SimulationConfig();

        config.crossingLogic.drivingOnTheRight            = true;
        config.crossingLogic.edgePriorityEnabled          = true;
        config.crossingLogic.priorityToTheRightEnabled    = true;
        config.crossingLogic.friendlyStandingInJamEnabled = true;
        config.crossingLogic.onlyOneVehicleEnabled        = false;
        config.visualization.style                        = new LightMonochromeStyleSheet();

        return config;
    }

    private static OSMParser parser(SimulationConfig config, Bounds clip) {
        FeatureGenerator.Properties props = new FeatureGenerator.Properties();
        props.clip = FeatureGenerator.Properties.BoundaryManagement.CLIP;
        props.bounds = clip;

        OSMParser.Config parser = DefaultParserConfig.get(config);
        parser.setGeneratorProperties(props);

        return parser.build();
    }

    private static ExchangeFormat exfmt(TilingScheme tilingScheme, int tileGridLevel, SimulationConfig config) {
        ExchangeFormat exfmt = ExchangeFormat.getDefault();

        exfmt.getConfig().set(QuadTreeTiledMapSegmentExtractor.Config.getDefault(tilingScheme, tileGridLevel));
        exfmt.getConfig().set(new StreetGraphExtractor.Config(config));

        return exfmt;
    }

    private static ExchangeFormatSerializer serializer() {
        return ExchangeFormatSerializer.create();
    }


    private TilingScheme tilingScheme = TILING_SCHEME;
    private int tileGridLevel = TILE_GRID_LEVEL;

    private SimulationConfig config;

    private ExchangeFormat exfmt;
    private ExchangeFormatSerializer serializer;


    public ExchangeFormatConverter() {
        this.config = config();
        this.exfmt = exfmt(tilingScheme, tileGridLevel, config);
        this.serializer = serializer();
    }


    public Tuple<SegmentFeatureProvider, Graph> load(File file, Bounds clip) throws Exception {
        logger.info("Loading map: " + file.getName());

        QuadTreeTiledMapSegment.Generator tiler = new QuadTreeTiledMapSegment.Generator();

        OSMParser.Result result = parser(config, clip).parse(file);
        QuadTreeTiledMapSegment segment = tiler.generate(result.segment, tilingScheme, tileGridLevel);
        Graph graph = result.streetgraph;

        return new Tuple<>(segment, graph);
    }

    public void store(SegmentFeatureProvider segment, Graph graph, File file) throws Exception {
        logger.info("Storing map: " + file.getName());

        Container container = exfmt.manipulator()
                .inject(segment)
                .inject(graph)
                .getContainer();

        serializer.write(file, container);
    }


    public void convert(File in, File out, Bounds clip) throws Exception {
        Tuple<SegmentFeatureProvider, Graph> result = load(in, clip);
        store(result.obj0, result.obj1, out);
    }


    public static void main(String[] args) throws Exception {
        ExchangeFormatConverter conv = new ExchangeFormatConverter();
        conv.convert(FILE_IN, FILE_OUT, CLIP);
    }
}