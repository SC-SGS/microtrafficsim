package microtrafficsim.examples.measurements;

import java.awt.Dimension;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.Iterator;
import java.util.LinkedList;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.slf4j.Logger;

import microtrafficsim.core.convenience.exfmt.ExfmtStorage;
import microtrafficsim.core.convenience.filechoosing.MTSFileChooser;
import microtrafficsim.core.convenience.mapviewer.MapViewer;
import microtrafficsim.core.convenience.mapviewer.TileBasedMapViewer;
import microtrafficsim.core.logic.streetgraph.Graph;
import microtrafficsim.core.logic.streetgraph.GraphGUID;
import microtrafficsim.core.logic.vehicles.machines.Vehicle;
import microtrafficsim.core.map.MapProvider;
import microtrafficsim.core.map.UnprojectedAreas;
import microtrafficsim.core.map.area.polygons.TypedPolygonArea;
import microtrafficsim.core.map.tiles.QuadTreeTilingScheme;
import microtrafficsim.core.simulation.builder.LogicVehicleFactory;
import microtrafficsim.core.simulation.builder.impl.VehicleScenarioBuilder;
import microtrafficsim.core.simulation.configs.SimulationConfig;
import microtrafficsim.core.simulation.core.MonitoringVehicleSimulation;
import microtrafficsim.core.simulation.core.MonitoringVehicleSimulation.CSVType;
import microtrafficsim.core.simulation.scenarios.impl.AreaScenario;
import microtrafficsim.core.simulation.utils.RouteContainer;
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

/**
 * The multithreading does work but the code is not a masterpiece, e.g. threads are waiting using
 * {@link Thread#sleep(long) Thread.sleep(500)} instead of proper locking.
 *
 * @author Dominic Parga Cacheiro
 */
public abstract class MeasurementExample {
    public static final Logger logger = new EasyMarkableLogger(MeasurementExample.class);


    private static boolean hasCollected = false;
    private static boolean shouldShutdown = false;


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



                /* init and prepare */
                config = new SimulationConfig();
                storage = new ExfmtStorage();

                // load and update config
                config.update(storage.loadConfig(files.mtscfg, config));
                logger.info("old seed = " + config.seed);
                config.seed = files.seed;
                logger.info("new seed " + config.seed);
                config.multiThreading.nThreads = files.nthreads;
                config.speedup = Integer.MAX_VALUE;
                config.scenario.showAreasWhileSimulating = true;
                if (files.maxVehicleCount != null)
                    config.maxVehicleCount = files.maxVehicleCount;



                /* setup mapviewer */
                if (files.visualized) {
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
                Tuple<Graph, MapProvider> mapResult = storage.loadMap(files.mtsmap, config.crossingLogic.drivingOnTheRight);
                graph = mapResult.obj0;
                if (files.visualized)
                    viewer.setMap(mapResult.obj1);



                /* setup scenario */
                AreaScenario areaScenario = new AreaScenario(config.seed, config, graph);


                /* load and set routes */
                {
                    areaScenario.resetAndClearRoutes();

                    Iterator<File> routeFiles = files.mtsroutes.iterator();
                    RouteContainer routes;
                    UnprojectedAreas areas;
                    do {
                        Triple<GraphGUID, RouteContainer, UnprojectedAreas>
                                routesResult = storage.loadRoutes(routeFiles.next(), graph);
                        routes = routesResult.obj1;
                        areas = routesResult.obj2;

                        areaScenario.getAreaNodeContainer().addAreas(areas);
                        areaScenario.addRoutes(routes);
                    } while (routeFiles.hasNext());

                    areaScenario.fillRdmWithRoutes(routes);
                }



                /* setup simulation */
                MonitoringVehicleSimulation simulation = new MonitoringVehicleSimulation();



                VehicleScenarioBuilder scenarioBuilder;
                LogicVehicleFactory logicVehicleFactory = (id, seed, scenario, metaRoute) -> {
                    Vehicle vehicle = LogicVehicleFactory.defaultCreation(id, seed, scenario, metaRoute);
                    if (files.dawdleFactor != null)
                        vehicle.getDriver().setDawdleFactor(files.dawdleFactor);
                    if (files.laneChangeFactor!= null)
                        vehicle.getDriver().setLaneChangeFactor(files.laneChangeFactor);
                    return vehicle;
                };
                /* setup overlays */
                if (files.visualized) {
                    ScenarioAreaOverlay scenarioAreaOverlay = new ScenarioAreaOverlay();
                    for (TypedPolygonArea area : areaScenario.getAreaNodeContainer().getAreas())
                        scenarioAreaOverlay.add(area.getProjectedArea(viewer.getProjection(), area));
                    SwingUtilities.invokeLater(() ->
                            scenarioAreaOverlay.setEnabled(config.scenario.showAreasWhileSimulating, false, false));
                    viewer.addOverlay(1, scenarioAreaOverlay);


                    VehicleOverlay vehicleOverlay = new SpriteBasedVehicleOverlay(
                            viewer.getProjection(),
                            config.visualization.style);
                    vehicleOverlay.setSimulation(simulation);
                    vehicleOverlay.setMapProperties(viewer.getMap().getProperties());
                    viewer.addOverlay(2, vehicleOverlay);


                    scenarioBuilder = new VehicleScenarioBuilder(
                            config.seed,
                            logicVehicleFactory,
                            vehicleOverlay.getVehicleFactory());
                } else {
                    scenarioBuilder = new VehicleScenarioBuilder(config.seed, logicVehicleFactory);
                }
                try {
                    scenarioBuilder.prepare(areaScenario);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                simulation.setAndInitPreparedScenario(areaScenario);



                /* setup frame */
                JFrame frame = setupFrame(viewer, files.visualized);
                frame.setLocationRelativeTo(null);
                frame.setVisible(true);
                if (files.visualized)
                    viewer.show();



                /* simulate */
                simulation.addStepListener(sim -> {
                    if (shouldShutdown
                            || sim.getAge() >= files.maxAge
                            || sim.getScenario().getVehicleContainer().isEmpty())
                    {
                        logger.info("CANCEL SIMULATION");
                        sim.cancel();
                        shouldShutdown = files.automatic;
                    } else {
                        if (sim.getAge() % 50 == 0)
                            logger.info("Finished simulation steps: " + sim.getAge());
                    }
                });
                logger.info("RUN SIMULATION");
                simulation.run();



                /* store collected data */
                new Thread(() -> {
                    while (!simulation.isPaused()) {
                        try {
                            Thread.sleep(500);
                        } catch (InterruptedException e) {
                            break;
                        }
                    }

                    if (files.visualized && !files.automatic) {
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
                    if (files.visualized && !files.automatic) {
                        JOptionPane.showMessageDialog(
                                frame,
                                "Saving data finished.",
                                "Data saving",
                                JOptionPane.INFORMATION_MESSAGE);
                    }


                    hasCollected = true;
                }).start();



                /* shutdown */
                TileBasedMapViewer finalViewer = viewer;
                new Thread(() -> {
                    while (!(shouldShutdown && hasCollected)) {
                        try {
                            Thread.sleep(500);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                            break;
                        }
                    }

                    if (files.visualized)
                        finalViewer.destroy();
                    frame.dispose();
                    System.exit(0);
                }).start();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    private static Files readInputArgs(String[] args) {
        Files files = new Files();


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
                .builder("r0")
                .longOpt(MTSFileChooser.Filters.ROUTE_POSTFIX + "_0")
                .hasArg()
                .argName("ROUTE_FILE")
                .desc("First-used route file")
                .build());

        options.addOption(Option
                .builder("r1")
                .longOpt(MTSFileChooser.Filters.ROUTE_POSTFIX + "_1")
                .hasArg()
                .argName("ROUTE_FILE")
                .desc("Second-used route file")
                .build());

        options.addOption(Option
                .builder("o")
                .longOpt("output")
                .hasArg()
                .argName("PATH_TO_CSV_FILES")
                .desc("Path to output csv file; default is current directory")
                .build());

        options.addOption(Option
                .builder()
                .longOpt("automatic")
                .hasArg()
                .argName("BOOLEAN_VALUE")
                .desc("false if user input is needed to continue/exit (default is true)")
                .build());

        options.addOption(Option
                .builder()
                .longOpt("nthreads")
                .hasArg()
                .argName("INTEGER_VALUE")
                .desc("number of threads running the simulation (default is 8)")
                .build());

        options.addOption(Option
                .builder()
                .longOpt("seed")
                .hasArg()
                .argName("LONG_VALUE")
                .desc("seed for config file (default is 42)")
                .build());

        options.addOption(Option
                .builder()
                .longOpt("maxAge")
                .hasArg()
                .argName("INTEGER_VALUE")
                .desc("max age when simulation should stop (default is 3,000)")
                .build());

        options.addOption(Option
                .builder()
                .longOpt("maxVehicleCount")
                .hasArg()
                .argName("INTEGER_VALUE")
                .desc("max vehicle count overwriting the given ."
                        + MTSFileChooser.Filters.CONFIG_POSTFIX + " file (optional)")
                .build());

        options.addOption(Option
                .builder()
                .longOpt("dawdleFactor")
                .hasArg()
                .argName("FLOAT_VALUE")
                .desc("vehicles' dawdle factor (optional)")
                .build());

        options.addOption(Option
                .builder()
                .longOpt("laneChangeFactor")
                .hasArg()
                .argName("FLOAT_VALUE")
                .desc("vehicles' lane change factor (optional)")
                .build());

        options.addOption(Option
                .builder("vis")
                .longOpt("visualized")
                .hasArg()
                .argName("[true|false]")
                .desc("whether the measurement should run the visualization (default is true)")
                .build());

        try {
            CommandLine line = new DefaultParser().parse(options, args);

            if (line.hasOption("help")) {
                HelpFormatter formatter = new HelpFormatter();
                formatter.printHelp("measurement", options);
                System.exit(0);
            }

            if (line.hasOption("automatic")) {
                files.automatic = parseBoolean(line.getOptionValue("automatic").toLowerCase());
            }

            if (line.hasOption("nthreads")) {
                files.nthreads = Integer.parseInt(line.getOptionValue("nthreads"));
            }

            if (line.hasOption("seed")) {
                files.seed = Long.parseLong(line.getOptionValue("seed"));
            }

            if (line.hasOption("maxAge")) {
                files.maxAge = Integer.parseInt(line.getOptionValue("maxAge"));
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


            files.mtsroutes = new LinkedList<>();
            if (line.hasOption(MTSFileChooser.Filters.ROUTE_POSTFIX + "_0")) {
                files.mtsroutes.add(new File(line.getOptionValue(MTSFileChooser.Filters.ROUTE_POSTFIX + "_0")));
            } else {
                throw new Exception("Route file '." + MTSFileChooser.Filters.ROUTE_POSTFIX + "' is missing.");
            }

            if (line.hasOption(MTSFileChooser.Filters.ROUTE_POSTFIX + "_1")) {
                files.mtsroutes.add(new File(line.getOptionValue(MTSFileChooser.Filters.ROUTE_POSTFIX + "_1")));
            }


            if (line.hasOption("output")) {
                files.outputPath = line.getOptionValue("output");
            }

            if (line.hasOption("maxVehicleCount")) {
                files.maxVehicleCount = Integer.parseInt(line.getOptionValue("maxVehicleCount"));
            }

            if (line.hasOption("dawdleFactor")) {
                files.dawdleFactor = Float.parseFloat(line.getOptionValue("dawdleFactor"));
            }

            if (line.hasOption("laneChangeFactor")) {
                files.laneChangeFactor = Float.parseFloat(line.getOptionValue("laneChangeFactor"));
            }

            if (line.hasOption("visualized")) {
                files.visualized = parseBoolean(line.getOptionValue("visualized").toLowerCase());
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

    private static boolean parseBoolean(String input) throws Exception {
        if (
            input.equals("true")
            || input.equals("t")
            || input.equals("y")
            || input.equals("yes")
            || input.equals("1")
        ) {
            return true;
        }

        if (
            input.equals("false")
            || input.equals("f")
            || input.equals("n")
            || input.equals("no")
            || input.equals("0")
        ) {
            return false;
        }

        throw new Exception("Couldn't parse boolean input.");
    }

    private static JFrame setupFrame(MapViewer viewer, boolean visualized) {
        /* create and initialize the JFrame */
        JFrame frame = new JFrame("MicroTrafficSim - Measurement Example");
        frame.setIconImage(new ImageIcon(MeasurementExample.class.getResource("/icon/128x128.png")).getImage());
        if (visualized) {
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
        private LinkedList<File> mtsroutes;
        private String outputPath = "";

        private boolean automatic = true;
        private int nthreads = 8;
        private long seed = 42;
        private boolean visualized = true;
        private int maxAge = 3000;

        private Integer maxVehicleCount = null;
        private Float dawdleFactor = null;
        private Float laneChangeFactor = null;
    }
}
