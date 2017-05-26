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
import org.apache.commons.cli.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;


public class ExchangeFormatConverter {
    private final static Logger logger = LoggerFactory.getLogger(ExchangeFormatConverter.class);

    public final static Bounds DEFAULT_CLIP = null;
    public final static File DEFAULT_FILE_IN = new File("map.osm");
    public final static File DEFAULT_FILE_OUT = new File("map.mtsmap");

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
        File in = DEFAULT_FILE_IN;
        File out = DEFAULT_FILE_OUT;
        Bounds clip = DEFAULT_CLIP;

        Options options = new Options();
        options.addOption(Option
                .builder("h")
                .longOpt("help")
                .desc("print this message")
                .build());

        options.addOption(Option
                .builder("i")
                .longOpt("input")
                .hasArg()
                .argName("IN_FILE")
                .desc("input file")
                .build());

        options.addOption(Option
                .builder("o")
                .longOpt("output")
                .hasArg()
                .argName("OUT_FILE")
                .desc("output file")
                .build());

        options.addOption(Option
                .builder("c")
                .longOpt("clip")
                .hasArg()
                .numberOfArgs(4)
                .valueSeparator(';')
                .argName("MINLAT;MINLON;MAXLAT;MAXLON")
                .desc("clip to specified bounds")
                .build());

        try {
            CommandLine line = new DefaultParser().parse(options, args);

            if (line.hasOption("help")) {
                HelpFormatter formatter = new HelpFormatter();
                formatter.printHelp("exfmtconv", options );
                System.exit(0);
            }

            if (line.hasOption("input")) {
                in = new File(line.getOptionValue("input"));
            }

            if (line.hasOption("output")) {
                out = new File(line.getOptionValue("output"));
            }

            if (line.hasOption("clip")) {
                String[] bounds = line.getOptionValues("clip");

                try {
                    clip = new Bounds(
                            Double.parseDouble(bounds[0]),
                            Double.parseDouble(bounds[1]),
                            Double.parseDouble(bounds[2]),
                            Double.parseDouble(bounds[3])
                    );
                } catch (NumberFormatException e) {
                    System.err.println("\nError: Invalid argument for option '-c | --clip'");
                }
            }

        } catch (Exception e) {
            System.err.flush();
            System.err.println("\nError:");
            System.err.println("    " + e.getMessage());
            System.exit(1);
        }

        try {
            new ExchangeFormatConverter().convert(in, out, clip);
        } catch (Exception e) {
            System.err.flush();
            System.err.println("\nError: Failed to convert files:");
            System.err.println("    " + e.getMessage());
            System.exit(1);
        }
    }
}