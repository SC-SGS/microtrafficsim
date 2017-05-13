package logic.validation;

import com.jogamp.newt.event.KeyEvent;
import logic.validation.scenarios.MotorwaySlipRoadScenario;
import logic.validation.scenarios.RoundaboutScenario;
import logic.validation.scenarios.TCrossroadScenario;
import logic.validation.scenarios.pluscrossroad.FullPlusCrossroadScenario;
import logic.validation.scenarios.pluscrossroad.PartialPlusCrossroadScenario;
import microtrafficsim.core.convenience.mapviewer.MapViewer;
import microtrafficsim.core.convenience.mapviewer.TileBasedMapViewer;
import microtrafficsim.core.convenience.parser.DefaultParserConfig;
import microtrafficsim.core.entities.vehicle.VisualizationVehicleEntity;
import microtrafficsim.core.logic.streetgraph.Graph;
import microtrafficsim.core.parser.OSMParser;
import microtrafficsim.core.simulation.configs.SimulationConfig;
import microtrafficsim.core.simulation.core.Simulation;
import microtrafficsim.core.simulation.core.impl.VehicleSimulation;
import microtrafficsim.core.simulation.scenarios.impl.QueueScenarioSmall;
import microtrafficsim.core.vis.UnsupportedFeatureException;
import microtrafficsim.core.vis.simulation.SpriteBasedVehicleOverlay;
import microtrafficsim.core.vis.simulation.VehicleOverlay;
import microtrafficsim.utils.logging.EasyMarkableLogger;
import microtrafficsim.utils.logging.LoggingLevel;
import microtrafficsim.utils.resources.PackagedResource;
import org.slf4j.Logger;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * @author Dominic Parga Cacheiro
 */
public class UIValidation {
    private static Logger logger = new EasyMarkableLogger(UIValidation.class);


    // CHOOSE YOUR CLASS
    private static final Class clazz = new Class[] {
            FullPlusCrossroadScenario.class,
            PartialPlusCrossroadScenario.class,
            MotorwaySlipRoadScenario.class,
            RoundaboutScenario.class,
            TCrossroadScenario.class,
    }[0];


    public static void main(String[] args) {
        /* build setup: scenario */
        String osmFilename;
        Consumer<SimulationConfig> setupConfig;
        ScenarioConstructor scenarioConstructor;

        if (clazz == FullPlusCrossroadScenario.class) {
            osmFilename = "plus_crossroad.osm";
            setupConfig = FullPlusCrossroadScenario::setupConfig;
            scenarioConstructor = (config, graph, visVehicleFactory) -> {
                QueueScenarioSmall scenario = new FullPlusCrossroadScenario(config, graph, visVehicleFactory);
                scenario.setLooping(true);
                return scenario;
            };
        } else if (clazz == PartialPlusCrossroadScenario.class) {
                osmFilename = "plus_crossroad.osm";
                setupConfig = PartialPlusCrossroadScenario::setupConfig;
                scenarioConstructor = (config, graph, visVehicleFactory) -> {
                    QueueScenarioSmall scenario = new PartialPlusCrossroadScenario(config, graph, visVehicleFactory);
                    scenario.setLooping(true);
                    return scenario;
                };
        } else if (clazz == MotorwaySlipRoadScenario.class) {
            osmFilename = "motorway_slip-road.osm";
            setupConfig = MotorwaySlipRoadScenario::setupConfig;
            scenarioConstructor = (config, graph, visVehicleFactory) -> {
                QueueScenarioSmall scenario = new MotorwaySlipRoadScenario(config, graph, visVehicleFactory);
                scenario.setLooping(true);
                return scenario;
            };
        } else if (clazz == RoundaboutScenario.class) {
            osmFilename = "roundabout.osm";
            setupConfig = RoundaboutScenario::setupConfig;
            scenarioConstructor = (config, graph, visVehicleFactory) -> {
                QueueScenarioSmall scenario = new RoundaboutScenario(config, graph, visVehicleFactory);
                scenario.setLooping(true);
                return scenario;
            };
        } else if (clazz == TCrossroadScenario.class) {
            osmFilename = "T_crossroad.osm";
            setupConfig = TCrossroadScenario::setupConfig;
            scenarioConstructor = (config, graph, visVehicleFactory) -> {
                QueueScenarioSmall scenario = new TCrossroadScenario(config, graph, visVehicleFactory);
                scenario.setLooping(true);
                return scenario;
            };
        } else {
            throw new UnsupportedOperationException("Your chosen scenario class is not supported.");
        }

        LoggingLevel.setEnabledGlobally(false, true, true, true, true);


        /* get map file */
        File file;
        try {
            file = new PackagedResource(UIValidation.class, osmFilename).asTemporaryFile();
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        /* simulation config */
        SimulationConfig config = new SimulationConfig();
        setupConfig.accept(config);


        SwingUtilities.invokeLater(() -> {
            OSMParser parser = DefaultParserConfig.get(config).build();

            /* visualization */
            MapViewer mapviewer    = new TileBasedMapViewer(config.visualization.style);
            VehicleOverlay overlay = new SpriteBasedVehicleOverlay(
                    mapviewer.getProjection(),
                    config.visualization.style);
            try {
                mapviewer.create(config);
            } catch (UnsupportedFeatureException e) {
                e.printStackTrace();
            }
            mapviewer.addOverlay(0, overlay);


            /* setup JFrame */
            JFrame frame = new JFrame("MicroTrafficSim - Validation Scenario");
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


            /* parse osm map */
            String oldTitle = frame.getTitle();
            frame.setTitle("Parsing new map, please wait...");
            Graph graph = null;
            try {
                /* parse file and create tiled provider */
                OSMParser.Result result = parser.parse(file);
                graph = result.streetgraph;

                mapviewer.setMap(result.segment);
                frame.setTitle(oldTitle);
            } catch (Exception e) {
                frame.setTitle(oldTitle);
                e.printStackTrace();
                Runtime.getRuntime().halt(1);
            }


            logger.debug("\n" + graph);


            /* initialize the simulation */
            QueueScenarioSmall scenario = scenarioConstructor.instantiate(config, graph, overlay.getVehicleFactory());
            Simulation sim = new VehicleSimulation();
            overlay.setSimulation(sim);
            sim.setAndInitPreparedScenario(scenario);
            sim.runOneStep();


            /* shortcuts */
            mapviewer.addKeyCommand(KeyEvent.EVENT_KEY_RELEASED,
                    KeyEvent.VK_SPACE,
                    e -> {
                        if (sim.isPaused())
                            sim.run();
                        else
                            sim.cancel();
                    }
            );

            mapviewer.addKeyCommand(KeyEvent.EVENT_KEY_RELEASED,
                    KeyEvent.VK_RIGHT,
                    e -> {
                        sim.cancel();
                        sim.runOneStep();
                    }
            );
        });
    }

    private interface ScenarioConstructor {
        QueueScenarioSmall instantiate(SimulationConfig config,
                                       Graph graph,
                                       Supplier<VisualizationVehicleEntity> visVehicleFactory);
    }
}
