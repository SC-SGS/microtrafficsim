package microtrafficsim.examples.simulation;

import microtrafficsim.core.convenience.exfmt.ExfmtStorage;
import microtrafficsim.core.convenience.filechoosing.MTSFileChooser;
import microtrafficsim.core.convenience.mapviewer.MapViewer;
import microtrafficsim.core.convenience.mapviewer.TileBasedMapViewer;
import microtrafficsim.core.logic.streetgraph.Graph;
import microtrafficsim.core.logic.streetgraph.GraphGUID;
import microtrafficsim.core.map.MapProvider;
import microtrafficsim.core.map.UnprojectedAreas;
import microtrafficsim.core.map.tiles.QuadTreeTilingScheme;
import microtrafficsim.core.simulation.builder.impl.VehicleScenarioBuilder;
import microtrafficsim.core.simulation.configs.SimulationConfig;
import microtrafficsim.core.simulation.core.MonitoringVehicleSimulation;
import microtrafficsim.core.simulation.core.MonitoringVehicleSimulation.CSVType;
import microtrafficsim.core.simulation.scenarios.impl.AreaScenario;
import microtrafficsim.core.simulation.utils.RouteContainer;
import microtrafficsim.core.simulation.utils.SortedRouteContainer;
import microtrafficsim.core.vis.map.projections.MercatorProjection;
import microtrafficsim.core.vis.scenario.areas.ScenarioAreaOverlay;
import microtrafficsim.core.vis.simulation.SpriteBasedVehicleOverlay;
import microtrafficsim.core.vis.simulation.VehicleOverlay;
import microtrafficsim.utils.collections.Triple;
import microtrafficsim.utils.collections.Tuple;
import microtrafficsim.utils.io.FileManager;
import microtrafficsim.utils.logging.EasyMarkableLogger;
import microtrafficsim.utils.logging.LoggingLevel;
import microtrafficsim.utils.strings.builder.BasicStringBuilder;
import microtrafficsim.utils.strings.builder.StringBuilder;
import org.apache.commons.cli.*;
import org.slf4j.Logger;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.Collections;
import java.util.Iterator;
import java.util.Random;

/**
 * The multithreading does work but the code is not a masterpiece, e.g. threads are waiting using
 * {@link Thread#sleep(long) Thread.sleep(500)} instead of proper locking.
 *
 * @author Dominic Parga Cacheiro
 */
public abstract class MeasurementExample {
    public static final Logger logger = new EasyMarkableLogger(MeasurementExample.class);


    private static final int MAX_AGE = 3000;
    public static final String DEFAULT_OUTPUT_PATH = "";
//    public final static String DEFAULT_OUTPUT_PATH = "/Users/Dominic/Documents/Studium/Bachelor_of_Disaster/microtrafficsim/simulation_files/measurements";

    private static final boolean SHUFFLE_ROUTES_ENABLED = true;
    private static final boolean VISUALIZED = true;
    private static boolean shouldShutdown = false;
    private static Thread dataThread;


    public static void main(String[] args) throws Exception {
        SwingUtilities.invokeLater(() -> {
            try {
                /* build setup */
                LoggingLevel.setEnabledGlobally(false, false, true, true, true);



                /* setup from user input */
                Files files = readInputArgs(args);



                /* variables */
                SimulationConfig config;
                TileBasedMapViewer viewer = null;
                ExfmtStorage storage;
                Graph graph;
                RouteContainer routes;
                UnprojectedAreas areas;



                /* init and prepare */
                config = new SimulationConfig();
                storage = new ExfmtStorage();

                // load and update config
                config.update(storage.loadConfig(files.mtscfg, config));
                config.speedup = Integer.MAX_VALUE;



                /* setup mapviewer */
                if (VISUALIZED) {
                    viewer = new TileBasedMapViewer(config.visualization.style);
                    storage.setupMapLoading(config, viewer);
                    viewer.create(config);
                } else {
                    storage.setupMapLoading(
                            config,
                            new QuadTreeTilingScheme(new MercatorProjection()),
                            TileBasedMapViewer.DEFAULT_TILEGRID_LEVEL
                    );
                }

                // load and set graph
                Tuple<Graph, MapProvider> mapResult = storage.loadMap(files.mtsmap);
                graph = mapResult.obj0;
                if (VISUALIZED)
                    viewer.setMap(mapResult.obj1);

                // load and set routes
                Triple<GraphGUID, RouteContainer, UnprojectedAreas> routesResult = storage.loadRoutes(files.mtsroutes, graph);
                routes = routesResult.obj1;
                areas = routesResult.obj2;



                /* setup scenario */
                AreaScenario scenario = new AreaScenario(config.seed, config, graph);
                scenario.getAreaNodeContainer().addAreas(areas);
                if (SHUFFLE_ROUTES_ENABLED) {
                    SortedRouteContainer copy = new SortedRouteContainer();
                    copy.addAll(routes);

                    Collections.shuffle(copy, new Random(config.seed));
                    routes = copy;
                }
                scenario.redefineMetaRoutes(routes);



                /* setup simulation */
                MonitoringVehicleSimulation simulation = new MonitoringVehicleSimulation();



                VehicleScenarioBuilder scenarioBuilder;
                /* setup overlays */
                if (VISUALIZED) {
                    ScenarioAreaOverlay scenarioAreaOverlay = new ScenarioAreaOverlay();
                    SwingUtilities.invokeLater(() -> scenarioAreaOverlay.setEnabled(true, false, false));
                    viewer.addOverlay(1, scenarioAreaOverlay);


                    VehicleOverlay vehicleOverlay = new SpriteBasedVehicleOverlay(
                            viewer.getProjection(),
                            config.visualization.style);
                    vehicleOverlay.setSimulation(simulation);
                    viewer.addOverlay(2, vehicleOverlay);


                    scenarioBuilder = new VehicleScenarioBuilder(
                            config.seed,
                            vehicleOverlay.getVehicleFactory());
                } else {
                    scenarioBuilder = new VehicleScenarioBuilder(config.seed);
                }
                try {
                    scenarioBuilder.prepare(scenario);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                simulation.setAndInitPreparedScenario(scenario);



                /* setup frame */
                JFrame frame = setUpFrame(viewer);
                frame.setLocationRelativeTo(null);
                frame.setVisible(true);
                if (VISUALIZED)
                    viewer.show();



                /* simulate */
                simulation.addStepListener(sim -> {
                    if (shouldShutdown
                            || sim.getAge() >= MAX_AGE
                            || sim.getScenario().getVehicleContainer().isEmpty())
                    {
                        sim.cancel();
                    } else {
                        if (sim.getAge() % 50 == 0)
                            logger.info("Finished simulation steps: " + sim.getAge());
                    }
                });
                simulation.run();



                /* store collected data */
                dataThread = new Thread(() -> {
                    while (true) {
                        if (simulation.isPaused()) {
                            shouldShutdown = true;
                            break;
                        }


                        try {
                            Thread.sleep(500);
                        } catch (InterruptedException e) {
                            shouldShutdown = true;
                            break;
                        }
                    }
                });
                dataThread.start();



                /* shutdown */
                TileBasedMapViewer finalViewer = viewer;
                new Thread(() -> {
                    while (!shouldShutdown) {
                        try {
                            Thread.sleep(500);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                            break;
                        }
                    }

                    if (VISUALIZED) {
                        JOptionPane.showMessageDialog(
                                frame,
                                "Saving data starts.\nThis could take a moment.",
                                "Data saving",
                                JOptionPane.INFORMATION_MESSAGE);
                    }
                    logger.info("START saving data (this could take a moment)");
                    FileManager fileManager = new FileManager(files.outputPath);
                    for (CSVType type : CSVType.values()) {
                        logger.info("WRITE " + type.getFilename());

                        fileManager.writeDataToFile(type.getFilename(), "", true);
                        Iterator<String> iter = simulation.getCSVIterator(type);

                        while (iter.hasNext()) {
                            StringBuilder builder = new BasicStringBuilder();
                            for (int i = 0; i < 1000; i++)
                                if (iter.hasNext())
                                    builder.append(iter.next());
                                else
                                    break;

                            fileManager.writeDataToFile(type.getFilename(), builder.toString(), false);
                        }
                    }
                    logger.info("FINISHED saving data");
                    if (VISUALIZED) {
                        JOptionPane.showMessageDialog(
                                frame,
                                "Saving data finished.",
                                "Data saving",
                                JOptionPane.INFORMATION_MESSAGE);
                    }

                    if (VISUALIZED)
                        finalViewer.destroy();
                    frame.dispose();
                    dataThread.interrupt();
                    System.exit(0);

                }).start();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    private static Files readInputArgs(String[] args) {
        Files files = new Files();
        files.outputPath = DEFAULT_OUTPUT_PATH;


        Options options = new Options();
        options.addOption(Option
                .builder("h")
                .longOpt("help")
                .desc("Print this message")
                .build());

        options.addOption(Option
                .builder("c")
                .longOpt(MTSFileChooser.Filters.CONFIG_POSTFIX)
                .hasArg()
                .argName("CONFIG_FILE")
                .desc("Config file")
                .build());

        options.addOption(Option
                .builder("m")
                .longOpt(MTSFileChooser.Filters.MAP_EXFMT_POSTFIX)
                .hasArg()
                .argName("MAP_FILE")
                .desc("Map file")
                .build());

        options.addOption(Option
                .builder("r")
                .longOpt(MTSFileChooser.Filters.ROUTE_POSTFIX)
                .hasArg()
                .argName("ROUTE_FILE")
                .desc("Route file")
                .build());

        options.addOption(Option
                .builder("o")
                .longOpt("output")
                .hasArg()
                .argName("PATH_TO_CSV_FILES")
                .desc("Path to output csv file")
                .build());

        try {
            CommandLine line = new DefaultParser().parse(options, args);

            if (line.hasOption("help")) {
                HelpFormatter formatter = new HelpFormatter();
                formatter.printHelp("measurement", options);
                System.exit(0);
            }

            if (line.hasOption(MTSFileChooser.Filters.CONFIG_POSTFIX)) {
                files.mtscfg = new File(line.getOptionValue(MTSFileChooser.Filters.CONFIG_POSTFIX));
            } else {
                throw new Exception("Config file '." + MTSFileChooser.Filters.CONFIG_POSTFIX + "' is missing.");
            }

            if (line.hasOption(MTSFileChooser.Filters.MAP_EXFMT_POSTFIX)) {
                files.mtsmap = new File(line.getOptionValue(MTSFileChooser.Filters.MAP_EXFMT_POSTFIX));
            } else {
                throw new Exception("Map file '." + MTSFileChooser.Filters.MAP_EXFMT_POSTFIX + "' or '." +
                        MTSFileChooser.Filters.MAP_OSM_XML_POSTFIX + "' is missing.");
            }

            if (line.hasOption(MTSFileChooser.Filters.ROUTE_POSTFIX)) {
                files.mtsroutes = new File(line.getOptionValue(MTSFileChooser.Filters.ROUTE_POSTFIX));
            } else {
                throw new Exception("Route file '." + MTSFileChooser.Filters.ROUTE_POSTFIX + "' is missing.");
            }

            if (line.hasOption("output")) {
                files.outputPath = line.getOptionValue("output");
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.err.flush();
            System.err.println("\nError:");
            System.err.println("    " + e.getMessage());
            System.exit(1);
        }

        return files;
    }

    private static JFrame setUpFrame(MapViewer viewer) {
        /* create and initialize the JFrame */
        JFrame frame = new JFrame("MicroTrafficSim - Simulation Example");
        frame.setIconImage(new ImageIcon(MeasurementExample.class.getResource("/icon/128x128.png")).getImage());
        if (VISUALIZED) {
            frame.setSize(viewer.getInitialWindowWidth(), viewer.getInitialWindowHeight());
            frame.add(viewer.getVisualizationPanel());
        }

        /*
         * Note: JOGL automatically calls glViewport, we need to make sure that this
         * function is not called with a height or width of 0! Otherwise the program
         * crashes.
         */
        frame.setMinimumSize(new Dimension(100, 100));

        /* On close: stop the visualization and exit */
        frame.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                shouldShutdown = true;
            }
        });

        return frame;
    }



    private static class Files {
        private File mtscfg;
        private File mtsmap;
        private File mtsroutes;
        private String outputPath;
    }
}
