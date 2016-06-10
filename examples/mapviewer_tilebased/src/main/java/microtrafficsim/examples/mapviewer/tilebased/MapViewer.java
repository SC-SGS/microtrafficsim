package microtrafficsim.examples.mapviewer.tilebased;

import com.jogamp.newt.event.KeyEvent;
import microtrafficsim.core.map.layers.TileLayerDefinition;
import microtrafficsim.core.map.layers.TileLayerSource;
import microtrafficsim.core.map.style.StyleSheet;
import microtrafficsim.core.map.tiles.QuadTreeTiledMapSegment;
import microtrafficsim.core.map.tiles.QuadTreeTilingScheme;
import microtrafficsim.core.parser.OSMParser;
import microtrafficsim.core.vis.UnsupportedFeatureException;
import microtrafficsim.core.vis.VisualizationPanel;
import microtrafficsim.core.vis.Visualizer;
import microtrafficsim.core.vis.VisualizerConfig;
import microtrafficsim.core.vis.map.projections.MercatorProjection;
import microtrafficsim.core.vis.map.projections.Projection;
import microtrafficsim.core.vis.map.tiles.PreRenderedTileProvider;
import microtrafficsim.core.vis.map.tiles.TileProvider;
import microtrafficsim.core.vis.map.tiles.layers.FeatureTileLayerGenerator;
import microtrafficsim.core.vis.map.tiles.layers.FeatureTileLayerSource;
import microtrafficsim.core.vis.map.tiles.layers.LayeredTileMap;
import microtrafficsim.core.vis.map.tiles.layers.TileLayerProvider;
import microtrafficsim.core.vis.tilebased.TileBasedVisualization;
import microtrafficsim.osm.parser.features.streets.StreetComponent;
import microtrafficsim.osm.parser.features.streets.StreetComponentFactory;
import microtrafficsim.osm.parser.processing.osm.sanitizer.SanitizerWayComponent;
import microtrafficsim.osm.parser.processing.osm.sanitizer.SanitizerWayComponentFactory;
import microtrafficsim.osm.parser.relations.restriction.RestrictionRelationFactory;

import javax.swing.*;
import javax.xml.stream.XMLStreamException;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.util.Collection;


public class MapViewer {

    /* -- window parameters -------------------------------------------------------------------- */

    /**
     * The initial window width.
     */
    private static final int INITIAL_WINDOW_WIDTH = 1600;

    /**
     * The initial window height.
     */
    private static final int INITIAL_WINDOW_HEIGHT = 900;


    /* -- style parameters --------------------------------------------------------------------- */

    /**
     * The projection used to transform the spherical world coordinates to plane coordinates.
     * The resulting tile size is dependent on the scale of this projection (2x scale). For this
     * example, the tile size will be 512x512 pixel.
     */
    private static final Projection PROJECTION = new MercatorProjection(256);

    /**
     * The tiling scheme used to create the tiles.
     */
    private static final QuadTreeTilingScheme TILING_SCHEME = new QuadTreeTilingScheme(PROJECTION, 0, 19);

    /**
     * The used style sheet, defining style and content of the visualization.
     */
    private static final StyleSheet STYLE = new LightStyleSheet();

    /* -- internal settings -------------------------------------------------------------------- */

    /**
     * When using a {@code QuadTreeTiledMapSegment}, this describes the zoom level at which
     * the geometry will be stored in a grid. To reduce memory requirements, geometry is not
     * stored for each layer but just for this one.
     */
    private static final int TILE_GRID_LEVEL = 12;

    /**
     * The number of worker threads loading tiles and their geometry in parallel, during
     * the visualization.
     */
    private static final int NUM_TILE_WORKERS = Math.max(Runtime.getRuntime().availableProcessors() - 2, 2);

    /**
     * Whether to print frame statistics or not.
     */
    private static final boolean PRINT_FRAME_STATS = false;

    /**
     * Enable n-times multi-sample anti aliasing with the specified number of samples, if it is greater than one.
     */
    private static final int MSAA = 0;


    /**
     * Set up this example. Layer definitions describe the visual layers
     * to be rendered and are used to create a layer provider. With this
     * provider a tile provider is created, capable of returning drawable
     * tiles. These tiles are rendered using the visualization object. A
     * parser is created to parse the specified file (asynchronously) and
     * update the layers (respectively their sources).
     *
     * @param file the file to parse
     * @throws UnsupportedFeatureException
     *          if not all required OpenGL features are available
     */
    private static void show(File file) throws UnsupportedFeatureException {
        /* set up layer and tile provider */
        Collection<TileLayerDefinition> layers = STYLE.getLayers();
        TileLayerProvider layerProvider = createLayerProvider(layers);
        PreRenderedTileProvider provider = new PreRenderedTileProvider(layerProvider);

        /* create the visualizer */
        TileBasedVisualization visualization = createVisualization(provider);
        
        /* parse the OSM file asynchronously and update the sources */
        OSMParser parser = createParser();
        asyncParse(parser, file, layers, visualization.getVisualizer());
        
        /* create and initialize the VisualizationPanel and JFrame */
        VisualizationPanel vpanel = createVisualizationPanel(visualization);
        JFrame frame = new JFrame("MicroTrafficSim - OSM MapViewer Example");
        frame.setSize(INITIAL_WINDOW_WIDTH, INITIAL_WINDOW_HEIGHT);
        frame.add(vpanel);
        
        /*
         * Note: JOGL automatically calls glViewport, we need to make sure that this
         * function is not called with a height or width of 0! Otherwise the program
         * crashes.
         */
        frame.setMinimumSize(new Dimension(100, 100));

        /* on close: stop the visualization and exit */
        frame.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                vpanel.stop();
                System.exit(0);
            }
        });

        /* show frame and start visualization */
        frame.setVisible(true);
        vpanel.start();

        /* if specified, print frame statistics */
        if (PRINT_FRAME_STATS)
            visualization.getRenderContext().getAnimator().setUpdateFPSFrames(60, System.out);
    }

    /**
     * Create the (tile-based) visualization object. The visualization object is
     * used to bind input and display structures together.
     *
     * @param provider  the provider providing the tiles to be displayed
     * @return the created visualization object
     */
    private static TileBasedVisualization createVisualization(TileProvider provider) {
        /* create a new visualization object */
        TileBasedVisualization vis = new TileBasedVisualization(
                INITIAL_WINDOW_WIDTH,
                INITIAL_WINDOW_HEIGHT,
                provider,
                NUM_TILE_WORKERS);

        /* apply the style (background color) */
        vis.apply(STYLE);

        /* add some key commands */
        vis.getKeyController().addKeyCommand(
                KeyEvent.EVENT_KEY_PRESSED,
                KeyEvent.VK_F12,
                e -> Utils.asyncScreenshot(vis.getRenderContext()));

        vis.getKeyController().addKeyCommand(
                KeyEvent.EVENT_KEY_PRESSED,
                KeyEvent.VK_ESCAPE,
                e -> Runtime.getRuntime().halt(0));

        /* set an exception handler, catching all unhandled exceptions on the render thread (for debugging purposes) */
        vis.getRenderContext().setUncaughtExceptionHandler(new Utils.DebugExceptionHandler());

        /* add an overlay (the TileGridOverlay is used to display tile borders, so mainly for debugging) */
        // vis.putOverlay(0, new TileGridOverlay(provider.getTilingScheme()));

        return vis;
    }

    /**
     * Create a visualization panel. The visualization panel is the interface
     * between the Java UI toolkit (Swing) and the OpenGL-based visualization.
     * Thus it is used to display the visualization.
     *
     * @param vis   the visualization to show on the panel
     * @return      the created visualization panel
     *
     * @throws UnsupportedFeatureException
                    if not all required OpenGL features are available
     */
    private static VisualizationPanel createVisualizationPanel(TileBasedVisualization vis)
            throws UnsupportedFeatureException {

        /* get the default configuration for the visualization */
        VisualizerConfig config = vis.getDefaultConfig();

        /* enable multi-sample anti-aliasing if specified */
        if (MSAA > 1) {
            config.glcapabilities.setSampleBuffers(true);
            config.glcapabilities.setNumSamples(MSAA);
        }

        /* create and return a new visualization panel */
        return new VisualizationPanel(vis, config);
    }

    /**
     * Creates and sets up the parser used to parse OSM files. The parser
     * processes the file in two major steps: first a simple extraction,
     * based on the feature set requested (using {@code putMapFeatureDefinition},
     * and second a transformation of the extracted data to the final
     * representation, using the {@code FeatureGenerator}s associated with
     * the feature definition. {@code FeatureGenerator}s may require the
     * presence of certain components for ways and nodes (such as the
     * {@code StreetComponent}), or factories to initialize relations
     * (such as the {@code RestrictionRelationFactory}). The in this example
     * provided components and initializers are enough for most use-cases.
     *
     * @return the created parser
     */
    private static OSMParser createParser() {
        /* get the parser configuration from the given style */
        StyleSheet.ParserConfig styleconfig = STYLE.getParserConfiguration();

        /* create a configuration, add factories for parsed components */
        OSMParser.Config config = new OSMParser.Config()
                .setGeneratorIndexBefore(styleconfig.generatorIndexOfUnification)
                .setGeneratorIndexStreetGraph(styleconfig.generatorIndexOfStreetGraph)
                .putWayInitializer(StreetComponent.class, new StreetComponentFactory())
                .putWayInitializer(SanitizerWayComponent.class, new SanitizerWayComponentFactory())
                .putRelationInitializer("restriction", new RestrictionRelationFactory());

        /* add the features defined in the style to the parser */
        STYLE.getFeatureDefinitions().forEach(config::putMapFeatureDefinition);

        /* create and return the parser */
        return config.createParser();
    }

    /**
     * Creates a {@code TileLayerProvider} from the given layer definitions.
     * The {@code TileLayerProvider} is used to provide map-layers and their
     * style to the visualization. {@code TileLayerDefinition}s describe such
     * a layer in dependence of a source object. {@code TileLayerGenerator}s
     * are used to generate a renderable {@code TileLayer} from a specified
     * source.
     *
     * @param layers    the layer definitions for the provider
     * @return          the created layer provider
     */
    private static TileLayerProvider createLayerProvider(Collection<TileLayerDefinition> layers) {
        /* create the layer provider */
        LayeredTileMap provider = new LayeredTileMap(TILING_SCHEME);

        /* add a generator to support feature layers */
        FeatureTileLayerGenerator generator = new FeatureTileLayerGenerator();
        provider.putGenerator(FeatureTileLayerSource.class, generator);

        /* add the leyer definitions */
        layers.forEach(provider::addLayer);

        return provider;
    }


    /**
     * Asynchronously parses the given file with the given parser, updates
     * the specified layers and resets the view of the visualizer.
     *
     * @param parser    the parser with which to parse the file
     * @param file      the file to be parsed
     * @param layers    the layers of the visualization, the sources of these
     *                  layers are set to the parsed result
     * @param vis       the visualizer which is used to draw the parsed segment
     *                  and which's view should be reset
     */
    private static void asyncParse(OSMParser parser, File file, Collection<TileLayerDefinition> layers, Visualizer vis) {
        new Thread(() -> {
            try {
                /* parse file and create tiled provider */
                OSMParser.Result result = parser.parse(file);
                QuadTreeTiledMapSegment tiled = new QuadTreeTiledMapSegment.Generator()
                        .generate(result.segment, TILING_SCHEME, TILE_GRID_LEVEL);

                /* update the feature sources, so that they will use the created provider */
                for (TileLayerDefinition def : layers) {
                    TileLayerSource src = def.getSource();

                    if (src instanceof FeatureTileLayerSource)
                        ((FeatureTileLayerSource) src).setFeatureProvider(tiled);
                }

                /* center the view to the parsed area */
                vis.resetView();
            } catch (XMLStreamException | IOException | InterruptedException e) {
                e.printStackTrace();
                Runtime.getRuntime().halt(1);
            }
        }).start();
    }


    /**
     * Main method, runs this example.
     * @param args          command line arguments
     */
    public static void main(String[] args) {
        File file;

        if (args.length == 1) {
            switch(args[0]) {
                case "-h":
                case "--help":
                    printUsage();
                    return;

                default:
                    file = new File(args[0]);
            }
        } else {
            file = new File("map.osm");
        }

        try {
            show(file);
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
        System.out.println("MicroTrafficSim - OSM MapViewer Example.");
        System.out.println("");
        System.out.println("Usage:");
        System.out.println("  mapviewer                Run this example with the default map-file");
        System.out.println("  mapviewer <file>         Run this example with the specified map-file");
        System.out.println("  mapviewer --help | -h    Show this help message.");
        System.out.println("");
    }
}
