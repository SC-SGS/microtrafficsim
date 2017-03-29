package microtrafficsim.examples.simulation;

import com.jogamp.newt.event.KeyEvent;
import microtrafficsim.core.convenience.DefaultParserConfig;
import microtrafficsim.core.convenience.MapViewer;
import microtrafficsim.core.convenience.TileBasedMapViewer;
import microtrafficsim.core.logic.streetgraph.Graph;
import microtrafficsim.core.map.SegmentFeatureProvider;
import microtrafficsim.core.map.style.impl.DarkStyleSheet;
import microtrafficsim.core.map.tiles.QuadTreeTiledMapSegment;
import microtrafficsim.core.parser.OSMParser;
import microtrafficsim.core.serialization.Serializer;
import microtrafficsim.core.simulation.builder.ScenarioBuilder;
import microtrafficsim.core.simulation.builder.impl.VehicleScenarioBuilder;
import microtrafficsim.core.simulation.configs.ScenarioConfig;
import microtrafficsim.core.simulation.core.Simulation;
import microtrafficsim.core.simulation.core.impl.VehicleSimulation;
import microtrafficsim.core.simulation.scenarios.Scenario;
import microtrafficsim.core.simulation.scenarios.impl.RandomRouteScenario;
import microtrafficsim.core.vis.UnsupportedFeatureException;
import microtrafficsim.core.vis.simulation.SpriteBasedVehicleOverlay;
import microtrafficsim.core.vis.simulation.VehicleOverlay;
import microtrafficsim.math.random.distributions.impl.Random;
import microtrafficsim.utils.concurrency.interruptsafe.InterruptSafeFutureTask;
import microtrafficsim.utils.logging.LoggingLevel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;


/**
 * This class can be used as introduction for own simulation implementations, e.g. own GUIs
 *
 * @author Dominic Parga Cacheiro, Maximilian Luz
 */
public class SimulationExample {

    private JFileChooser filechooser;
    private Serializer serializer;
    private OSMParser parser;

    private JFrame frame;
    private ScenarioConfig config;
    private TileBasedMapViewer viewer;
    private VehicleOverlay overlay;

    private String file = null;
    private SegmentFeatureProvider segment = null;
    private Graph graph = null;

    private Simulation simulation = null;
    private Future<Void> loading = null;


    private void run(File file) throws UnsupportedFeatureException {
        /* Set up logging */
        LoggingLevel.setEnabledGlobally(false, false, true, true, true);

        filechooser = new JFileChooser();
        serializer = Serializer.create();

        /* Create configuration for scenarios, parser and map-viewer */
        config = config();
        parser = DefaultParserConfig.get(config).build();
        viewer = setUpMapViewer(config);

        /* Create and add vehicle-overlay */
        overlay = new SpriteBasedVehicleOverlay(viewer.getProjection(), config.visualization.style);
        viewer.addOverlay(0, overlay);

        /* Create the window and display the visualization */
        frame = setUpFrame(viewer);
        show();

        /* Load the OSM file asynchronously */
        if (file != null)
            load(file);
    }


    /**
     * Create and return the ScenarioConfig used for this example.
     * @return the {@code ScenarioConfig} used for this example.
     */
    private ScenarioConfig config() {
        ScenarioConfig config = new ScenarioConfig();

        config.maxVehicleCount                            = 1000;
        config.speedup                                    = 5;
        config.seed                                       = 1455374755807L;
        config.multiThreading.nThreads                    = 8;
        config.crossingLogic.drivingOnTheRight            = true;
        config.crossingLogic.edgePriorityEnabled          = true;
        config.crossingLogic.priorityToTheRightEnabled    = true;
        config.crossingLogic.friendlyStandingInJamEnabled = true;
        config.crossingLogic.onlyOneVehicleEnabled        = false;
        config.visualization.style                        = new DarkStyleSheet();

        return config;
    }


    /**
     * Set up the {@code TileBasedMapViewer}.
     */
    private TileBasedMapViewer setUpMapViewer(ScenarioConfig config) throws UnsupportedFeatureException {
        TileBasedMapViewer viewer = new TileBasedMapViewer(config.visualization.style);
        viewer.create(config);

        setUpShortcuts(viewer);

        return viewer;
    }

    /**
     * Set up the application window.
     */
    private JFrame setUpFrame(MapViewer viewer) {
        /* create and initialize the JFrame */
        JFrame frame = new JFrame("MicroTrafficSim - Map Viewer Example");
        frame.setSize(viewer.getInitialWindowWidth(), viewer.getInitialWindowHeight());
        frame.add(viewer.getVisualizationPanel());

        /*
         * Note: JOGL automatically calls glViewport, we need to make sure that this
         * function is not called with a height or width of 0! Otherwise the program
         * crashes.
         */
        frame.setMinimumSize(new Dimension(100, 100));

        /* On close: stop the visualization and exit */
        frame.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                shutdown();
            }
        });

        return frame;
    }

    /**
     * Show frame and start visualization.
     */
    private void show() {
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
        viewer.show();
    }

    /**
     * Safely terminate the application.
     */
    private void shutdown() {
        SwingUtilities.invokeLater(() -> {
            viewer.destroy();
            frame.dispose();
            System.exit(0);
        });
    }


    /**
     * Creates and initializes a new scenario and simulation based on the given arguments.
     *
     * @param config  the configuration to use.
     * @param graph   the graph on which the simulation runs on.
     * @param overlay the overlay displaying the vehicles.
     * @return the created simulation.
     */
    private static Simulation createAndInitSimulation(ScenarioConfig config, Graph graph, VehicleOverlay overlay) {
        Scenario scenario = new RandomRouteScenario(new Random(), config, graph);
        Simulation simulation = new VehicleSimulation();
        ScenarioBuilder scenarioBuilder = new VehicleScenarioBuilder(config.seed, overlay.getVehicleFactory());

        overlay.setSimulation(simulation);
        try {
            scenarioBuilder.prepare(scenario);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        simulation.setAndInitPreparedScenario(scenario);
        simulation.runOneStep();

        return simulation;
    }


    /**
     * Reset the simulation.
     */
    private void reset() {
        // TODO
        new UnsupportedOperationException().printStackTrace();
    }


    /**
     * Set up keyboard-shortcuts
     */
    private void setUpShortcuts(MapViewer viewer) {
        /* Use <Space> to run/pause simulation */
        viewer.addKeyCommand(KeyEvent.EVENT_KEY_RELEASED, KeyEvent.VK_SPACE, e -> {
            if (simulation != null) {
                if (simulation.isPaused())
                    simulation.run();
                else
                    simulation.cancel();
            }
        });

        /* Use <Right Arrow> to run/pause simulation */
        viewer.addKeyCommand(KeyEvent.EVENT_KEY_RELEASED, KeyEvent.VK_RIGHT, e -> {
            if (simulation != null) {
                simulation.cancel();
                simulation.runOneStep();
            }
        });

        /* Use <Esc> and <Q> as shortcuts to stop and exit */
        viewer.addKeyCommand(KeyEvent.EVENT_KEY_PRESSED, KeyEvent.VK_ESCAPE, (e) -> shutdown());
        viewer.addKeyCommand(KeyEvent.EVENT_KEY_PRESSED, KeyEvent.VK_Q, (e) -> shutdown());

        /* Use <C> and as shortcut to reset the view */
        viewer.addKeyCommand(KeyEvent.EVENT_KEY_PRESSED, KeyEvent.VK_C, (e) -> viewer.resetView());

        /* Use <W> and <Ctrl-S> as shortcuts to save the current map to a binary file */
        viewer.addKeyCommand(KeyEvent.EVENT_KEY_PRESSED, KeyEvent.VK_W, (e) -> store());
        viewer.addKeyCommand(KeyEvent.EVENT_KEY_PRESSED, KeyEvent.VK_S, (e) -> {
            if (e.isControlDown()) {
                store();
            }
        });

        /* Use <E> and <Ctrl-O> as shortcuts to load binary or osm (xml) file */
        viewer.addKeyCommand(KeyEvent.EVENT_KEY_PRESSED, KeyEvent.VK_E, (e) -> load());
        viewer.addKeyCommand(KeyEvent.EVENT_KEY_PRESSED, KeyEvent.VK_O, (e) -> {
            if (e.isControlDown()) {
                load();
            }
        });

        /* Use <R> as shortcut to reset the the simulation */
        viewer.addKeyCommand(KeyEvent.EVENT_KEY_PRESSED, KeyEvent.VK_R, (e) -> reset());
    }


    /**
     * Open a JFileChooser to let the user load a binary or xml file.
     */
    private void load() {
        int status = filechooser.showOpenDialog(frame);
        if (status == JFileChooser.APPROVE_OPTION) {
            load(filechooser.getSelectedFile());
        }
    }

    /**
     * Load the specified file (asynchronously), abort a previous in-flight load-operation if there is any.
     * @param file the file to load.
     */
    private void load(File file) {
        /* pause simulation */
        if (simulation != null)
            simulation.cancel();

        /* cancel loading, if map is loading */
        if (this.loading != null) {
            int status = JOptionPane.showConfirmDialog(frame,
                    "Another file is already being loaded. Continue?", "Load File", JOptionPane.OK_CANCEL_OPTION);

            if (status != JOptionPane.OK_OPTION) {
                return;
            }

            loading.cancel(true);

            /* wait until the task has been fully cancelled, required to set the frame-title correctly */
            try {
                loading.get();
            } catch (InterruptedException | CancellationException e) {
                /* ignore */
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        }

        /* create new load-task */
        InterruptSafeFutureTask<Void> loading = new InterruptSafeFutureTask<>(() -> {
            SwingUtilities.invokeLater(() ->
                    frame.setTitle(getDefaultFrameTitle() + " - [Loading: " + file.getPath() + "]"));

            /* parse file */
            boolean xml = file.getName().endsWith(".osm");
            SegmentFeatureProvider segment;
            Graph graph;

            try {
                if (xml) {
                    OSMParser.Result result = parser.parse(file);
                    segment = new QuadTreeTiledMapSegment.Generator().generate(result.segment,
                            viewer.getPreferredTilingScheme(), viewer.getPreferredTileGridLevel());
                    graph = result.streetgraph;
                } else {
                    // TODO
                    new UnsupportedOperationException().printStackTrace();
                    return null;
                }
            } catch (InterruptedException e) {
                throw new CancellationException();
            }

            if (Thread.interrupted())
                throw new CancellationException();


            /* cancel and destroy old simulation */
            if (simulation != null) {
                simulation.cancel();
                simulation = null;
            }

            /* set segment */
            this.segment = segment;
            this.graph = graph;
            this.file = file.getPath();
            viewer.setMap(segment);

            /* create simulation */
            SwingUtilities.invokeLater(() -> frame.setTitle(getDefaultFrameTitle() + " - [Initializing Simulation]"));
            simulation = createAndInitSimulation(config, graph, overlay);

            return null;
        });

        this.loading = loading;

        /* execute load-task */
        new Thread(() -> {
            try {
                loading.run();
                loading.get();
            } catch (InterruptedException | CancellationException e) {
                /* ignore */
            } catch (ExecutionException e) {
                e.printStackTrace();
                Runtime.getRuntime().halt(1);
            } finally {
                SwingUtilities.invokeLater(() -> frame.setTitle(getDefaultFrameTitle()));
                this.loading = null;
            }
        }).start();
    }

    /**
     * Open a JFileChooser to let the user store a binary file of the map being displayed.
     */
    private void store() {
        int status = filechooser.showSaveDialog(frame);
        if (status == JFileChooser.APPROVE_OPTION) {
            File f = filechooser.getSelectedFile();

            if (f.exists()) {
                status = JOptionPane.showConfirmDialog(frame,
                        "The selected file already exists. Continue?", "Save File", JOptionPane.OK_CANCEL_OPTION);

                if (status != JOptionPane.OK_OPTION) {
                    return;
                }
            }

            store(f);
        }
    }

    /**
     * Store the current map in the specified file (synchronously).
     * @param file the file to write the current map to.
     */
    private void store(File file) {
        // TODO
        new UnsupportedOperationException().printStackTrace();
    }


    /**
     * Return the default window title.
     * @return the default frame title.
     */
    private String getDefaultFrameTitle() {
        StringBuilder title = new StringBuilder("MicroTrafficSim - Map Viewer Example");

        if (file != null) {
            title.append(" - [").append(file).append("]");
        }

        return title.toString();
    }


    /**
     * Main method, runs this example.
     *
     * @param args command line arguments
     */
    public static void main(String[] args) {
        File file;

        if (args.length == 0) {
            file = null;
        } else if (args.length == 1) {
            switch (args[0]) {
                case "-h":
                case "--help":
                    printUsage();
                    return;

                default:
                    file = new File(args[0]);
            }
        } else {
            printUsage();
            return;
        }

        try {
            new SimulationExample().run(file);
        } catch (UnsupportedFeatureException e) {
            System.out.println("It seems that your PC does not meet the requirements for this software.");
            System.out.println("Please make sure that your graphics driver is up to date.");
            System.out.println();
            System.out.println("The following OpenGL features are required:");
            for (String feature : e.getMissingFeatures())
                System.out.println("\t" + feature);

            System.exit(1);
        }
    }

    /**
     * Prints the usage of this example.
     */
    private static void printUsage() {
        System.out.println("MicroTrafficSim - Simulation Example");
        System.out.println("");
        System.out.println("Usage:");
        System.out.println("  simulation               Run this example without any map-file");
        System.out.println("  simulation <file>        Run this example with the specified map-file");
        System.out.println("  simulation --help | -h   Show this help message.");
        System.out.println("");
    }
}
