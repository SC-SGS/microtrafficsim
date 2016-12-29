package logic.validation;

import com.jogamp.newt.event.KeyEvent;
import logic.validation.scenarios.ValidationScenario;
import logic.validation.scenarios.impl.TCrossroadScenario;
import microtrafficsim.core.logic.StreetGraph;
import microtrafficsim.core.logic.vehicles.impl.Car;
import microtrafficsim.core.map.style.impl.LightStyleSheet;
import microtrafficsim.core.mapviewer.MapViewer;
import microtrafficsim.core.mapviewer.TileBasedMapViewer;
import microtrafficsim.core.parser.OSMParser;
import microtrafficsim.core.simulation.builder.ScenarioBuilder;
import microtrafficsim.core.simulation.builder.impl.VehicleScenarioBuilder;
import microtrafficsim.core.simulation.configs.SimulationConfig;
import microtrafficsim.core.simulation.core.Simulation;
import microtrafficsim.core.simulation.core.impl.VehicleSimulation;
import microtrafficsim.core.simulation.scenarios.Scenario;
import microtrafficsim.core.vis.UnsupportedFeatureException;
import microtrafficsim.core.vis.simulation.SpriteBasedVehicleOverlay;
import microtrafficsim.core.vis.simulation.VehicleOverlay;
import microtrafficsim.utils.logging.EasyMarkableLogger;
import microtrafficsim.utils.resources.PackagedResource;
import org.slf4j.Logger;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;

/**
 * @author Dominic Parga Cacheiro
 */
public class Main {

    private static final Logger logger = new EasyMarkableLogger(Main.class);
    private static final ScenarioType scenarioType = ScenarioType.T_CROSSROAD;

    public static void main(String[] args) {

        /* build setup: logging */
        EasyMarkableLogger.TRACE_ENABLED = false;
        EasyMarkableLogger.DEBUG_ENABLED = false;
        EasyMarkableLogger.INFO_ENABLED  = true;
        EasyMarkableLogger.WARN_ENABLED  = true;
        EasyMarkableLogger.ERROR_ENABLED = true;

        /* get map file */
        File file;
        try {
            file = new PackagedResource(Main.class, scenarioType.getOSMFilename()).asTemporaryFile();
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        /* simulation config */
        SimulationConfig config = new SimulationConfig();
        scenarioType.setupConfig(config);


        SwingUtilities.invokeLater(() -> {

            /* visualization */
            MapViewer mapviewer    = new TileBasedMapViewer(new LightStyleSheet());
            VehicleOverlay overlay = new SpriteBasedVehicleOverlay(mapviewer.getProjection());
            try {
                mapviewer.create(config);
            } catch (UnsupportedFeatureException e) {
                e.printStackTrace();
            }
            mapviewer.addOverlay(0, overlay);


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


            /* initialize the simulation */
            ValidationScenario scenario = null;
            switch (scenarioType) {
                case MOTORWAY_SLIP_ROAD:
                    break;
                case PLUS_CROSSROAD:
                    break;
                case ROUNDABOUT:
                    break;
                case T_CROSSROAD:
                    scenario = new TCrossroadScenario(config, graph, overlay.getVehicleFactory());
                    break;
            }

            Simulation sim = new VehicleSimulation();
            overlay.setSimulation(sim);
            ValidationScenario finalScenario = scenario;
            sim.addStepListener(() -> {
                if (finalScenario.getVehicleContainer().getVehicleCount() == 0) {
                    sim.cancel();
                    finalScenario.prepare();
                    sim.setAndInitScenario(finalScenario);
                    sim.run();
                }
            });
            scenario.prepare();
            sim.setAndInitScenario(scenario);
            sim.runOneStep();


            /* shortcuts */
            mapviewer.addKeyCommand(KeyEvent.EVENT_KEY_RELEASED,
                    KeyEvent.VK_SPACE,
                    e -> {
                        if (sim.isPaused()) {
                            sim.run();
                        } else {
                            sim.cancel();
                        }
                    }
            );

            mapviewer.addKeyCommand(KeyEvent.EVENT_KEY_RELEASED,
                    KeyEvent.VK_RIGHT,
                    e -> {
                        if (!sim.isPaused()) {
                            sim.cancel();
                        }
                        sim.runOneStep();
                    }
            );
        });
    }
}
