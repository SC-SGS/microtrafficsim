package logic.validation;

import logic.validation.scenarios.ValidationScenario;
import logic.validation.scenarios.impl.TCrossroadScenario;
import microtrafficsim.core.map.style.impl.LightStyleSheet;
import microtrafficsim.core.mapviewer.MapViewer;
import microtrafficsim.core.mapviewer.TileBasedMapViewer;
import microtrafficsim.core.parser.OSMParser;
import microtrafficsim.core.simulation.builder.SimulationBuilder;
import microtrafficsim.core.simulation.builder.impl.VehicleSimulationBuilder;
import microtrafficsim.core.simulation.configs.SimulationConfig;
import microtrafficsim.core.simulation.core.Simulation;
import microtrafficsim.core.simulation.core.impl.VehicleSimulation;
import microtrafficsim.core.vis.UnsupportedFeatureException;
import microtrafficsim.core.vis.simulation.SpriteBasedVehicleOverlay;
import microtrafficsim.core.vis.simulation.VehicleOverlay;
import microtrafficsim.utils.id.ConcurrentLongIDGenerator;
import microtrafficsim.utils.id.ConcurrentSeedGenerator;
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

    public static void main(String[] args) {

        /* build setup: logging */
        EasyMarkableLogger.TRACE_ENABLED = false;
        EasyMarkableLogger.DEBUG_ENABLED = false;
        EasyMarkableLogger.INFO_ENABLED  = true;
        EasyMarkableLogger.WARN_ENABLED  = true;
        EasyMarkableLogger.ERROR_ENABLED = true;

        /* build setup: visualization */
        MapViewer mapviewer         = new TileBasedMapViewer(new LightStyleSheet());
        VehicleOverlay overlay      = new SpriteBasedVehicleOverlay(mapviewer.getProjection());

        /* build setup: logic */
        ValidationScenario scenario  = new TCrossroadScenario(); // change this for another scenario
        SimulationConfig config      = scenario.getConfig();
        SimulationBuilder simbuilder = new VehicleSimulationBuilder(
                config.seedGenerator.next(),
                overlay.getVehicleFactory()
        );
        Simulation sim               = new VehicleSimulation();

        /* get map file */
        File file;
        try {
            file = new PackagedResource(Main.class, scenario.OSM_FILENAME).asTemporaryFile();
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        /* create configuration for scenarios */
        config.metersPerCell           = 7.5f;
        config.longIDGenerator         = new ConcurrentLongIDGenerator();
        config.seed                    = 1455374755807L;
        config.seedGenerator           = new ConcurrentSeedGenerator(config.seed);
        config.multiThreading.nThreads = 1;
        logger.debug("using '" + Long.toHexString(config.seed) + "' as seed");

        SwingUtilities.invokeLater(() -> {
            /* visualization */
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

            try {
                /* parse file and create tiled provider */
                OSMParser.Result result = mapviewer.parse(file);

                mapviewer.changeMap(result);
                frame.setTitle("MicroTrafficSim - " + file.getName());
            } catch (Exception e) {
                frame.setTitle(oldTitle);
                e.printStackTrace();
                Runtime.getRuntime().halt(1);
            }
        });

        /* initialize the simulation */
        sim.setAndInitScenario(simbuilder.prepare(scenario));
        sim.runOneStep();
    }
}
