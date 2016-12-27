package logic.validation;

import logic.validation.scenarios.ValidationScenario;
import logic.validation.scenarios.impl.TCrossroadScenario;
import microtrafficsim.build.BuildSetup;
import microtrafficsim.core.logic.StreetGraph;
import microtrafficsim.core.map.layers.LayerDefinition;
import microtrafficsim.core.map.layers.LayerSource;
import microtrafficsim.core.map.style.StyleSheet;
import microtrafficsim.core.map.style.impl.LightStyleSheet;
import microtrafficsim.core.map.tiles.QuadTreeTiledMapSegment;
import microtrafficsim.core.parser.OSMParser;
import microtrafficsim.core.simulation.configs.SimulationConfig;
import microtrafficsim.core.simulation.core.Simulation;
import microtrafficsim.core.vis.UnsupportedFeatureException;
import microtrafficsim.core.vis.VisualizationPanel;
import microtrafficsim.core.vis.map.tiles.PreRenderedTileProvider;
import microtrafficsim.core.vis.map.tiles.layers.FeatureTileLayerSource;
import microtrafficsim.core.vis.map.tiles.layers.TileLayerProvider;
import microtrafficsim.core.vis.simulation.SpriteBasedVehicleOverlay;
import microtrafficsim.core.vis.tilebased.TileBasedVisualization;
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
import java.util.Collection;
import java.util.function.Supplier;

/**
 * @author Dominic Parga Cacheiro
 */
public class Main {

    private static final Logger logger = new EasyMarkableLogger(Main.class);

    public static void main(String[] args) {

        /* build setup */
        BuildSetup.init();
        ValidationScenario scenario = new TCrossroadScenario();
        SimulationConfig config = scenario.getConfig();
        StyleSheet style = new LightStyleSheet();

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

        /* set up layer and tile provider */
        TileLayerProvider layerProvider    = createLayerProvider(style.getLayers());
        PreRenderedTileProvider provider   = new PreRenderedTileProvider(layerProvider);

        /* parse the OSM file */
        OSMParser parser = getParser(scencfg);
        OSMParser.Result result = parser.parse(file);

        /* store the geometry on a grid to later generate tiles */
        QuadTreeTiledMapSegment tiled = new QuadTreeTiledMapSegment.Generator()
                .generate(result.segment, TILING_SCHEME, TILE_GRID_LEVEL);

        /* update the feature sources, so that they will use the created provider */
        for (LayerDefinition def : style.getLayers()) {
            LayerSource src = def.getSource();

            if (src instanceof FeatureTileLayerSource)
                ((FeatureTileLayerSource) src).setFeatureProvider(tiled);
        }

        /* create the visualization overlay */
        SpriteBasedVehicleOverlay overlay = new SpriteBasedVehicleOverlay(projection);

        /* create the simulation */
        Simulation sim = simClazz.getConstructor(scencfg.getClass(), StreetGraph.class, Supplier.class)
                .newInstance(scencfg, result.streetgraph, overlay.getVehicleFactory());
        overlay.setSimulation(sim);

        /* create and display the frame */
        SwingUtilities.invokeLater(() -> {

            /* create the actual visualizer */
            TileBasedVisualization visualization = createVisualization(provider, sim);
            visualization.putOverlay(0, overlay);

            VisualizationPanel vpanel;
            try {
                vpanel = createVisualizationPanel(visualization);
            } catch (UnsupportedFeatureException e) {
                e.printStackTrace();
                Runtime.getRuntime().halt(0);
                return;
            }

            /* create and initialize the JFrame */
            JFrame frame = new JFrame("MicroTrafficSim - Validation Scenario");
            frame.setSize(INITIAL_WINDOW_WIDTH, INITIAL_WINDOW_HEIGHT);
            frame.add(vpanel);

            /*
             * Note: JOGL automatically calls glViewport, we need to make
             * sure that this function is not called with a height or width
             * of 0! Otherwise the program crashes.
             */
            frame.setMinimumSize(new Dimension(100, 100));

            // on close: stop the visualization and exit
            frame.addWindowListener(new WindowAdapter() {
                public void windowClosing(WindowEvent e) {
                    vpanel.stop();
                    System.exit(0);
                }
            });

            /* show the frame and start the render-loop */
            frame.setVisible(true);
            vpanel.start();

            if (PRINT_FRAME_STATS)
                visualization.getRenderContext().getAnimator().setUpdateFPSFrames(60, System.out);
        });

        /* initialize the simulation */
        sim.prepare();
        sim.runOneStep();
    }
}
