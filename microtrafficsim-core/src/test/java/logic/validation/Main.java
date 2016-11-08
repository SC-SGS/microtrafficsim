package logic.validation;

import com.jogamp.newt.event.KeyEvent;
import microtrafficsim.core.logic.StreetGraph;
import microtrafficsim.core.map.layers.LayerDefinition;
import microtrafficsim.core.map.layers.LayerSource;
import microtrafficsim.core.map.style.StyleSheet;
import microtrafficsim.core.map.tiles.QuadTreeTiledMapSegment;
import microtrafficsim.core.map.tiles.QuadTreeTilingScheme;
import microtrafficsim.core.parser.OSMParser;
import microtrafficsim.core.parser.features.streetgraph.StreetGraphFeatureDefinition;
import microtrafficsim.core.parser.features.streetgraph.StreetGraphGenerator;
import microtrafficsim.core.simulation.Simulation;
import microtrafficsim.core.simulation.configs.SimulationConfig;
import microtrafficsim.core.vis.UnsupportedFeatureException;
import microtrafficsim.core.vis.VisualizationPanel;
import microtrafficsim.core.vis.VisualizerConfig;
import microtrafficsim.core.vis.map.projections.MercatorProjection;
import microtrafficsim.core.vis.map.projections.Projection;
import microtrafficsim.core.vis.map.tiles.PreRenderedTileProvider;
import microtrafficsim.core.vis.map.tiles.TileProvider;
import microtrafficsim.core.vis.map.tiles.layers.FeatureTileLayerGenerator;
import microtrafficsim.core.vis.map.tiles.layers.FeatureTileLayerSource;
import microtrafficsim.core.vis.map.tiles.layers.LayeredTileMap;
import microtrafficsim.core.vis.map.tiles.layers.TileLayerProvider;
import microtrafficsim.core.vis.simulation.SpriteBasedVehicleOverlay;
import microtrafficsim.core.vis.tilebased.TileBasedVisualization;
import microtrafficsim.math.random.ConcurrentRndGenGenerator;
import microtrafficsim.osm.parser.features.FeatureDependency;
import microtrafficsim.osm.parser.features.FeatureGenerator;
import microtrafficsim.osm.parser.features.streets.StreetComponent;
import microtrafficsim.osm.parser.features.streets.StreetComponentFactory;
import microtrafficsim.core.parser.processing.sanitizer.SanitizerWayComponent;
import microtrafficsim.core.parser.processing.sanitizer.SanitizerWayComponentFactory;
import microtrafficsim.osm.parser.relations.restriction.RestrictionRelationFactory;
import microtrafficsim.osm.primitives.Way;
import microtrafficsim.utils.id.ConcurrentLongIDGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.Collection;
import java.util.function.Predicate;
import java.util.function.Supplier;


public class Main {
    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    private static final int INITIAL_WINDOW_WIDTH  = 1600;
    private static final int INITIAL_WINDOW_HEIGHT =  900;

    private static final StyleSheet STYLE = new LightStyleSheet();

    private static final Projection PROJECTION = new MercatorProjection(256);
    private static final QuadTreeTilingScheme TILING_SCHEME = new QuadTreeTilingScheme(PROJECTION, 0, 19);

    private static final int TILE_GRID_LEVEL  = 12;
    private static final int NUM_TILE_WORKERS =  1;

    private static final boolean PRINT_FRAME_STATS = false;



    /**
     * Initialize the given {@code SimulationConfig}.
     *
     * @param config the configuration to initialize
     */
    private static void initSimulationConfig(SimulationConfig config) {
        config.metersPerCell           = 7.5f;
        config.longIDGenerator         = new ConcurrentLongIDGenerator();
        config.seed                    = 1455374755807L;
        config.rndGenGenerator         = new ConcurrentRndGenGenerator(config.seed);
        config.multiThreading.nThreads = 1;

        logger.debug("using '" + Long.toHexString(config.seed) + "' as seed");
    }

    /**
     * Create the (tile-based) visualization object.
     *
     * @param provider the provider providing the tiles to be displayed
     * @return the created visualization object
     */
    private static TileBasedVisualization createVisualization(TileProvider provider, Simulation sim) {
        /* create a new visualization object */
        TileBasedVisualization vis
                = new TileBasedVisualization(INITIAL_WINDOW_WIDTH, INITIAL_WINDOW_HEIGHT, provider, NUM_TILE_WORKERS);

        /* apply the style (background color) */
        vis.apply(STYLE);

        /* add some key commands */
        vis.getKeyController().addKeyCommand(KeyEvent.EVENT_KEY_PRESSED,
                                             KeyEvent.VK_F12,
                                             e -> Utils.asyncScreenshot(vis.getRenderContext()));

        vis.getKeyController().addKeyCommand(KeyEvent.EVENT_KEY_PRESSED, KeyEvent.VK_SPACE, e -> {
            if (sim.isPaused())
                sim.run();
            else
                sim.cancel();
        });

        vis.getKeyController().addKeyCommand(KeyEvent.EVENT_KEY_PRESSED, KeyEvent.VK_RIGHT, e -> {
            sim.cancel();
            sim.runOneStep();
        });

        vis.getKeyController().addKeyCommand(
                KeyEvent.EVENT_KEY_PRESSED, KeyEvent.VK_ESCAPE, e -> Runtime.getRuntime().halt(0));

        /* set an exception handler, catching all unhandled exceptions on the render thread (for debugging purposes) */
        vis.getRenderContext().setUncaughtExceptionHandler(new Utils.DebugExceptionHandler());

        return vis;
    }

    /**
     * Create the visualization panel.
     *
     * @param vis the visualization to show on the panel
     * @return the created visualization panel
     * @throws UnsupportedFeatureException if not all required OpenGL features are available
     */
    @SuppressWarnings("ConstantConditions")
    private static VisualizationPanel createVisualizationPanel(TileBasedVisualization vis)
            throws UnsupportedFeatureException {

        /* get the default configuration for the visualization */
        VisualizerConfig config = vis.getDefaultConfig();

        /* create and return a new visualization panel */
        return new VisualizationPanel(vis, config);
    }

    /**
     * Create the parser using the given simulation configuration.
     *
     * @param simcfg the simulation configuration to use for the stree-graph.
     * @return the created parser
     */
    private static OSMParser getParser(SimulationConfig simcfg) {
        /* predicate to match/select the streets that belong to the simulated graph */
        Predicate<Way> streetgraphMatcher = w -> {
            if (!w.visible) return false;
            if (w.tags.get("highway") == null) return false;
            if (w.tags.get("area") != null && !w.tags.get("area").equals("no")) return false;

            switch (w.tags.get("highway")) {
                case "motorway":      return true;
                case "trunk":         return true;
                case "primary":       return true;
                case "secondary":     return true;
                case "tertiary":      return true;
                case "unclassified":  return true;
                case "residential":   return true;

                case "motorway_link": return true;
                case "trunk_link":    return true;
                case "primary_link":  return true;
                case "tertiary_link": return true;

                case "living_street": return true;
                case "track":         return true;
                case "road":          return true;
            }

            return false;
        };

        /* create the feature definition for the simulated graph */
        StreetGraphFeatureDefinition streetgraph = new StreetGraphFeatureDefinition(
                "streetgraph",
                new FeatureDependency(),
                new StreetGraphGenerator(simcfg),
                n -> false,
                streetgraphMatcher
        );

        /* replace the style-placeholders with the feature-definitions/placeholders used by the osm-processor */
        STYLE.replaceDependencyPlaceholders(OSMParser.PLACEHOLDER_UNIFICATION ,OSMParser.PLACEHOLDER_UNIFICATION,
                                            streetgraph);

        /* global properties for (all) generators */
        FeatureGenerator.Properties genprops = new FeatureGenerator.Properties();
        genprops.bounds = FeatureGenerator.Properties.BoundaryManagement.CLIP;

        /* create a configuration, add factories for parsed components */
        OSMParser.Config config = new OSMParser.Config()
                .setGeneratorProperties(genprops)
                .putWayInitializer(StreetComponent.class, new StreetComponentFactory())
                .putWayInitializer(SanitizerWayComponent.class, new SanitizerWayComponentFactory())
                .putRelationInitializer("restriction", new RestrictionRelationFactory())
                .setStreetGraphFeatureDefinition(streetgraph);

        /* add the features defined in the style to the parser */
        STYLE.getFeatureDefinitions().forEach(config::putMapFeatureDefinition);

        /* create and return the parser */
        return config.createParser();
    }

    /**
     * Create the tile layer provider.
     *
     * @param layers the layer definitions for the provider
     * @return the created layer provider
     */
    private static TileLayerProvider createLayerProvider(Collection<LayerDefinition> layers) {
        /* create the layer provider */
        LayeredTileMap provider = new LayeredTileMap(TILING_SCHEME);

        /* add a generator to support feature layers */
        FeatureTileLayerGenerator generator = new FeatureTileLayerGenerator();
        provider.putGenerator(FeatureTileLayerSource.class, generator);

        /* add the leyer definitions */
        layers.forEach(provider::addLayer);

        return provider;
    }

    public static void show(Projection projection, File file, SimulationConfig scencfg,
                            Class<? extends Simulation> simClazz) throws Exception {

        /* create configuration for scenarios */
        initSimulationConfig(scencfg);

        /* set up layer and tile provider */
        Collection<LayerDefinition> layers = STYLE.getLayers();
        TileLayerProvider layerProvider        = createLayerProvider(layers);
        PreRenderedTileProvider provider       = new PreRenderedTileProvider(layerProvider);

        /* parse the OSM file */
        OSMParser        parser = getParser(scencfg);
        OSMParser.Result result = parser.parse(file);

        /* store the geometry on a grid to later generate tiles */
        QuadTreeTiledMapSegment tiled = new QuadTreeTiledMapSegment.Generator()
                .generate(result.segment, TILING_SCHEME, TILE_GRID_LEVEL);

        /* update the feature sources, so that they will use the created provider */
        for (LayerDefinition def : layers) {
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