package microtrafficsim.examples.simulation;

import microtrafficsim.core.convenience.exfmt.ExfmtStorage;
import microtrafficsim.core.convenience.mapviewer.TileBasedMapViewer;
import microtrafficsim.core.convenience.parser.DefaultParserConfig;
import microtrafficsim.core.logic.streetgraph.Graph;
import microtrafficsim.core.parser.OSMParser;
import microtrafficsim.core.simulation.builder.impl.VehicleScenarioBuilder;
import microtrafficsim.core.simulation.configs.SimulationConfig;
import microtrafficsim.core.simulation.core.MonitoringVehicleSimulation;
import microtrafficsim.core.simulation.core.Simulation;
import microtrafficsim.core.simulation.scenarios.impl.AreaScenario;
import microtrafficsim.utils.logging.EasyMarkableLogger;
import microtrafficsim.utils.logging.LoggingLevel;
import org.slf4j.Logger;

import java.io.File;

/**
 * This class can be used as introduction for own simulation implementations, e.g. own GUIs
 *
 * @author Dominic Parga Cacheiro, Maximilian Luz
 */
public class MeasurementExample {
    public static final Logger logger = new EasyMarkableLogger(MeasurementExample.class);


    private ExfmtStorage storage;
    private TileBasedMapViewer mapViewer;

    private SimulationConfig config;
    private Graph streetgraph;


    private MeasurementExample(File cfgFile) {
        config = new SimulationConfig();

        mapViewer = new TileBasedMapViewer();
        storage = new ExfmtStorage(config, mapViewer);
    }


    public void loadMap(File file) throws InterruptedException {
        streetgraph = storage.loadMap(file).obj0;
    }


    /*
    |=======|
    | utils |
    |=======|
    */
    public static void main(String[] args) {
        /* build setup */
        LoggingLevel.setEnabledGlobally(false, false, true, true, true);


        /* setup from user input */
        // todo
        SimulationConfig config = new SimulationConfig();
        File mapfile = null;


        /* setup graph */
        Graph graph;
        try {
            OSMParser parser = DefaultParserConfig.get(config).build();
            graph = parser.parse(mapfile).streetgraph;
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

        logger.debug("\n" + graph);


        /* setup scenario */
        AreaScenario scenario = new AreaScenario(config.seed, config, graph);
        scenario.redefineMetaRoutes();


        VehicleScenarioBuilder scenarioBuilder = new VehicleScenarioBuilder(config.seed);
        try {
            scenarioBuilder.prepare(scenario);
        } catch (Exception e) {
            e.printStackTrace();
        }
        Simulation simulation = new MonitoringVehicleSimulation();
        simulation.setAndInitPreparedScenario(scenario);
    }


    private static void printUsage() {
//        File in = DEFAULT_FILE_IN;
//        File out = DEFAULT_FILE_OUT;
//        Bounds clip = DEFAULT_CLIP;
//
//        Options options = new Options();
//        options.addOption(Option
//                .builder("h")
//                .longOpt("help")
//                .desc("Print this message")
//                .build());
//
//        options.addOption(Option
//                .builder("i")
//                .longOpt("input")
//                .hasArg()
//                .argName("IN_FILE")
//                .desc("Input file")
//                .build());
//
//        options.addOption(Option
//                .builder("o")
//                .longOpt("output")
//                .hasArg()
//                .argName("OUT_FILE")
//                .desc("Output file")
//                .build());
//
//        options.addOption(Option
//                .builder("c")
//                .longOpt("clip")
//                .hasArg()
//                .numberOfArgs(4)
//                .valueSeparator(';')
//                .argName("MINLAT;MINLON;MAXLAT;MAXLON")
//                .desc("Clip to specified bounds")
//                .build());
//
//        options.addOption(Option
//                .builder("m")
//                .longOpt("multilane")
//                .hasArg()
//                .type(Boolean.class)
//                .argName("MULTILANE")
//                .desc("Enable or disable multi-lane output (defaults to true)")
//                .build());
//
//        boolean multilane = true;
//        try {
//            CommandLine line = new DefaultParser().parse(options, args);
//
//            if (line.hasOption("help")) {
//                HelpFormatter formatter = new HelpFormatter();
//                formatter.printHelp("exfmtconv", options );
//                System.exit(0);
//            }
//
//            if (line.hasOption("input")) {
//                in = new File(line.getOptionValue("input"));
//            }
//
//            if (line.hasOption("output")) {
//                out = new File(line.getOptionValue("output"));
//            }
//
//            if (line.hasOption("clip")) {
//                String[] bounds = line.getOptionValues("clip");
//
//                try {
//                    clip = new Bounds(
//                            Double.parseDouble(bounds[0]),
//                            Double.parseDouble(bounds[1]),
//                            Double.parseDouble(bounds[2]),
//                            Double.parseDouble(bounds[3])
//                    );
//                } catch (NumberFormatException e) {
//                    System.err.println("\nError: Invalid argument for option '-c | --clip'");
//                }
//            }
//
//            if (line.hasOption("multilane")) {
//                multilane = Boolean.parseBoolean(line.getOptionValue("multilane"));
//            }
//
//        } catch (Exception e) {
//            e.printStackTrace();
//            System.err.flush();
//            System.err.println("\nError:");
//            System.err.println("    " + e.getMessage());
//            System.exit(1);
//        }
//
//        try {
//            new ExchangeFormatConverter().convert(in, out, clip, multilane);
//        } catch (Exception e) {
//            System.err.flush();
//            System.err.println("\nError: Failed to convert files:");
//            System.err.println("    " + e.getMessage());
//            System.exit(1);
//        }
    }
}
