package microtrafficsim.examples.simulation;

import com.jogamp.newt.event.KeyEvent;
import microtrafficsim.build.BuildSetup;
import microtrafficsim.core.logic.StreetGraph;
import microtrafficsim.core.map.style.impl.DarkStyleSheet;
import microtrafficsim.core.mapviewer.MapViewer;
import microtrafficsim.core.mapviewer.TileBasedMapViewer;
import microtrafficsim.core.parser.OSMParser;
import microtrafficsim.core.simulation.builder.ScenarioBuilder;
import microtrafficsim.core.simulation.builder.impl.VehicleScenarioBuilder;
import microtrafficsim.core.simulation.configs.SimulationConfig;
import microtrafficsim.core.simulation.core.Simulation;
import microtrafficsim.core.simulation.core.impl.VehicleSimulation;
import microtrafficsim.core.simulation.scenarios.Scenario;
import microtrafficsim.core.simulation.scenarios.impl.RandomRouteScenario;
import microtrafficsim.core.vis.UnsupportedFeatureException;
import microtrafficsim.core.vis.simulation.SpriteBasedVehicleOverlay;
import microtrafficsim.core.vis.simulation.VehicleOverlay;
import microtrafficsim.utils.logging.EasyMarkableLogger;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;

/**
 * This class can be used as introduction for own simulation implementations, e.g. own GUIs
 *
 * @author Dominic Parga Cacheiro
 */
public class Main {

    private static void printUsage() {
        System.out.println("MicroTrafficSim - Simulation Example");
        System.out.println("");
        System.out.println("Usage:");
        System.out.println("  simulation <file>         Run this example with the specified map-file");
        System.out.println("  simulation --help | -h    Show this help message.");
        System.out.println("");
    }

    public static void main(String[] args) {

        File file = analyseInputArguments(args);
        if (file == null)
            return;


        /* create configuration for scenarios */
        SimulationConfig config = createConfig();
        /* build setup: logging */
        BuildSetup.TRACE_ENABLED = false;
        BuildSetup.DEBUG_ENABLED = false;
        BuildSetup.INFO_ENABLED  = true;
        BuildSetup.WARN_ENABLED  = true;
        BuildSetup.ERROR_ENABLED = true;


        SwingUtilities.invokeLater(() -> {

            /* create map viewer and vehicle overlay */
            MapViewer mapviewer    = new TileBasedMapViewer(new DarkStyleSheet());
            VehicleOverlay overlay = new SpriteBasedVehicleOverlay(mapviewer.getProjection());
            try {
                mapviewer.create(config);
            } catch (UnsupportedFeatureException e) {
                e.printStackTrace();
            }
            mapviewer.addOverlay(0, overlay);

            JFrame frame = setupFrameAndShow(mapviewer);


            /* parse osm map */
            String oldTitle = frame.getTitle();
            frame.setTitle("Parsing new map, please wait...");
            StreetGraph graph = null;
            try {
            /* parse file and create tiled provider */
                OSMParser.Result result = mapviewer.parse(file);
                graph = result.streetgraph;

                mapviewer.changeMap(result);
                frame.setTitle(oldTitle);
            } catch (Exception e) {
                frame.setTitle(oldTitle);
                e.printStackTrace();
                Runtime.getRuntime().halt(1);
            }


            /* create simulation */
            Simulation simulation = createAndInitSimulation(config, graph, overlay);


            /* add shortcuts */
            addShortcuts(mapviewer, simulation);
        });
    }

    /*
    |============|
    | impl stuff |
    |============|
    */
    private static File analyseInputArguments(String[] args) {
        if (args.length != 1) {
            System.out.println("INFO: Exit without correct input map.");
            return null;
        }
        /* interpret argument input */
        switch (args[0]) {
            case "-h":
            case "--help":
                printUsage();
                return null;

            default:
                return new File(args[0]);
        }
    }

    private static SimulationConfig createConfig() {
        SimulationConfig config = new SimulationConfig();

        config.maxVehicleCount                            = 1000;
        config.speedup                                    = 5;
        config.seed                                       = 1455374755807L;
        config.multiThreading.nThreads                    = 8;
        config.crossingLogic.drivingOnTheRight            = true;
        config.crossingLogic.edgePriorityEnabled          = true;
        config.crossingLogic.priorityToTheRightEnabled    = true;
        config.crossingLogic.friendlyStandingInJamEnabled = true;
        config.crossingLogic.setOnlyOneVehicle(false);

        return config;
    }

    private static JFrame setupFrameAndShow(MapViewer mapviewer) {
        /* setup JFrame */
        JFrame frame = new JFrame("MicroTrafficSim - Validation Scenario");;
        frame.setSize(mapviewer.getInitialWindowWidth(), mapviewer.getInitialWindowHeight());
        frame.add(mapviewer.getVisualizationPanel());

            /*
             * Note: JOGL automatically calls glViewport, we need to make sure that this
             * function is not called with a height or width of 0! Otherwise the program
             * crashes.
             */
        frame.setMinimumSize(new Dimension(100, 100));

            /* on close: stop the visualization and exit */
        frame.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                mapviewer.stop();
                System.exit(0);
            }
        });


            /* show */
        frame.setLocationRelativeTo(null);    // center on screen; close to setVisible
        frame.setVisible(true);
        mapviewer.show();

        return frame;
    }

    private static Simulation createAndInitSimulation(
            SimulationConfig config,
            StreetGraph graph,
            VehicleOverlay overlay) {

        /* initialize the simulation */
        Scenario scenario = new RandomRouteScenario(config, graph);
        Simulation simulation = new VehicleSimulation();
        ScenarioBuilder scenarioBuilder = new VehicleScenarioBuilder(
                config.seedGenerator.next(),
                overlay.getVehicleFactory()
        );

        overlay.setSimulation(simulation);
        try {
            scenarioBuilder.prepare(scenario);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        simulation.setAndInitScenario(scenario);
        simulation.runOneStep();

        return simulation;
    }

    private static void addShortcuts(MapViewer mapviewer, Simulation simulation) {
        mapviewer.addKeyCommand(KeyEvent.EVENT_KEY_RELEASED,
                KeyEvent.VK_SPACE,
                e -> {
                    if (simulation.isPaused())
                        simulation.run();
                    else
                        simulation.cancel();
                }
        );

        mapviewer.addKeyCommand(KeyEvent.EVENT_KEY_RELEASED,
                KeyEvent.VK_RIGHT,
                e -> {
                    simulation.cancel();
                    simulation.runOneStep();
                }
        );
    }
}
