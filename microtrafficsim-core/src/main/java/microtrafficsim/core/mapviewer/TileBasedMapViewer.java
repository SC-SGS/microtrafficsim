package microtrafficsim.core.mapviewer;

import com.jogamp.newt.event.KeyEvent;
import microtrafficsim.core.map.layers.LayerDefinition;
import microtrafficsim.core.map.layers.LayerSource;
import microtrafficsim.core.map.style.StyleSheet;
import microtrafficsim.core.map.style.impl.MonochromeStyleSheet;
import microtrafficsim.core.map.tiles.QuadTreeTiledMapSegment;
import microtrafficsim.core.map.tiles.QuadTreeTilingScheme;
import microtrafficsim.core.mapviewer.utils.Utils;
import microtrafficsim.core.parser.OSMParser;
import microtrafficsim.core.parser.features.streetgraph.StreetGraphFeatureDefinition;
import microtrafficsim.core.parser.features.streetgraph.StreetGraphGenerator;
import microtrafficsim.core.parser.processing.sanitizer.SanitizerWayComponent;
import microtrafficsim.core.parser.processing.sanitizer.SanitizerWayComponentFactory;
import microtrafficsim.core.simulation.configs.SimulationConfig;
import microtrafficsim.core.vis.Overlay;
import microtrafficsim.core.vis.UnsupportedFeatureException;
import microtrafficsim.core.vis.VisualizationPanel;
import microtrafficsim.core.vis.VisualizerConfig;
import microtrafficsim.core.vis.input.KeyCommand;
import microtrafficsim.core.vis.map.projections.MercatorProjection;
import microtrafficsim.core.vis.map.projections.Projection;
import microtrafficsim.core.vis.map.tiles.PreRenderedTileProvider;
import microtrafficsim.core.vis.map.tiles.TileProvider;
import microtrafficsim.core.vis.map.tiles.layers.FeatureTileLayerGenerator;
import microtrafficsim.core.vis.map.tiles.layers.FeatureTileLayerSource;
import microtrafficsim.core.vis.map.tiles.layers.LayeredTileMap;
import microtrafficsim.core.vis.map.tiles.layers.TileLayerProvider;
import microtrafficsim.core.vis.tilebased.TileBasedVisualization;
import microtrafficsim.osm.parser.features.FeatureDependency;
import microtrafficsim.osm.parser.features.FeatureGenerator;
import microtrafficsim.osm.parser.features.streets.StreetComponent;
import microtrafficsim.osm.parser.features.streets.StreetComponentFactory;
import microtrafficsim.osm.parser.relations.restriction.RestrictionRelationFactory;
import microtrafficsim.osm.primitives.Way;

import java.io.File;
import java.util.Collection;
import java.util.function.Predicate;


/**
 * @author Dominic Parga Cacheiro
 */
public class TileBasedMapViewer implements MapViewer {

    private final int initialWindowWidth;
    private final int initialWindowHeight;

    private final StyleSheet style;
    private final Projection projection;
    private final QuadTreeTilingScheme tilingScheme;

    private final int tileGridLevel;
    private final int numTileWorkers;

    private final boolean printFrameStats;


    private VisualizationPanel          vpanel;
    private TileBasedVisualization      visualization;
    private OSMParser                   parser;
    private Collection<LayerDefinition> layers;

    /**
     * Default constructor using {@link MonochromeStyleSheet} as default style sheet.
     *
     * @see #TileBasedMapViewer(StyleSheet)
     */
    public TileBasedMapViewer() {
        this(new MonochromeStyleSheet());
    }

    /**
     * <p>
     * Default constructor initializing basic configurations: <br>
     * &bull {@code INITIAL_WINDOW_WIDTH}  = 1600 <br>
     * &bull {@code INITIAL_WINDOW_HEIGHT} = 900 <br>
     * <br>
     * &bull {@code PROJECTION} = new {@link MercatorProjection}(256)<br>
     * &bull {@code TILING_SCHEME} = new {@link QuadTreeTilingScheme}(PROJECTION, 0, 19) <br>
     * &bull {@code STYLE} = {@code style} (given as parameter) <br>
     * <br>
     * &bull {@code TILE_GRID_LEVEL} = 12 <br>
     * &bull {@code NUM_TILE_WORKERS} = at least 2 <br>
     * &bull {@code PRINT_FRAME_STATS} = false <br>
     * &bull {@code MSAA} = 0
     *
     * @param style This style sheet is used for the map style
     */
    public TileBasedMapViewer(StyleSheet style) {
        this(1600, 900, style, new MercatorProjection(256));
    }

    /**
     * Create a new map-viewer with the given properties.
     *
     * @param width      the (initial) width of the window.
     * @param height     the (initial) height of the window.
     * @param style      the style used for map-rendering.
     * @param projection the projection used to transform the spherical world coordinates to plane coordinates.
     *                   The resulting tile size is dependent on the scale of this projection (2x scale).
     */
    public TileBasedMapViewer(int width, int height, StyleSheet style, Projection projection) {
        this(
                width,
                height,
                style,
                projection,
                new QuadTreeTilingScheme(projection, 0, 19),
                12,
                Math.max(Runtime.getRuntime().availableProcessors() - 2, 2),
                false
        );
    }

    /**
     * Create a new map-viewer with the given properties.
     *
     * @param width           the (initial) width of the window.
     * @param height          the (initial) height of the window.
     * @param style           the style used for map-rendering.
     * @param projection      the projection used to transform the spherical world coordinates to plane coordinates.
     *                        The resulting tile size is dependent on the scale of this projection (2x scale).
     * @param tilingScheme    the tiling-scheme used of the tiles to be displayed.
     * @param tileGridLevel   the level for which map-features should be stored in a grid.
     * @param numTileWorkers  the number of worker-threads loading tiles in parallel in the background.
     * @param printFrameStats set to {@code true} to write frame-statistics to stdout.
     */
    public TileBasedMapViewer(int width, int height, StyleSheet style, Projection projection,
                              QuadTreeTilingScheme tilingScheme, int tileGridLevel, int numTileWorkers,
                              boolean printFrameStats) {

        /* window parameters */
        this.initialWindowWidth = width;
        this.initialWindowHeight  = height;

        /* style parameters */
        this.style = style;
        this.projection = projection;
        this.tilingScheme = tilingScheme;

        /* internal settings */
        this.tileGridLevel = tileGridLevel;
        this.numTileWorkers = numTileWorkers;
        this.printFrameStats = printFrameStats;
    }

    /*
    |===============|
    | (i) MapViewer |
    |===============|
    */
    @Override
    public Projection getProjection() {
        return projection;
    }

    @Override
    public int getInitialWindowWidth() {
        return initialWindowWidth;
    }

    @Override
    public int getInitialWindowHeight() {
        return initialWindowHeight;
    }

    @Override
    public VisualizationPanel getVisualizationPanel() {
        return vpanel;
    }

    @Override
    public void addOverlay(int index, Overlay overlay) {
        visualization.putOverlay(index, overlay);
    }

    @Override
    public void create(SimulationConfig config) throws UnsupportedFeatureException {
        /* set up layer and tile provider */
        layers                                = style.getLayers();
        TileLayerProvider       layerProvider = createLayerProvider(layers);
        PreRenderedTileProvider provider      = new PreRenderedTileProvider(layerProvider);

        /* create the visualizer */
        visualization = createVisualization(provider);

        /* parse the OSM file asynchronously and update the sources */
        if (config != null)
            parser = createParser(config);
        else
            parser = createParser();

        /* create and initialize the VisualizationPanel */
        vpanel = createVisualizationPanel(visualization);
    }

    @Override
    public void show() {
        vpanel.start();

        /* if specified, print frame statistics */
        if (printFrameStats) visualization.getRenderContext().getAnimator().setUpdateFPSFrames(60, System.out);
    }

    @Override
    public void stop() {
        vpanel.stop();
    }

    @Override
    public TileBasedVisualization createVisualization(TileProvider provider) {
        /* create a new visualization object */
        TileBasedVisualization vis
                = new TileBasedVisualization(initialWindowWidth, initialWindowHeight, provider, numTileWorkers);

        /* apply the style (background color) */
        vis.apply(style);

        /* add some key commands */
        vis.getKeyController().addKeyCommand(
                KeyEvent.EVENT_KEY_PRESSED, KeyEvent.VK_F12, e -> Utils.asyncScreenshot(vis.getRenderContext()));

        vis.getKeyController().addKeyCommand(
                KeyEvent.EVENT_KEY_PRESSED, KeyEvent.VK_ESCAPE, e -> Runtime.getRuntime().halt(0));

        /* set an exception handler, catching all unhandled exceptions on the render thread (for debugging purposes) */
        vis.getRenderContext().setUncaughtExceptionHandler(new Utils.DebugExceptionHandler());

        /* add an overlay (the TileGridOverlay is used to display tile borders, so mainly for debugging) */
        // vis.putOverlay(0, new TileGridOverlay(provider.getTilingScheme()));

        return vis;
    }

    @Override
    public VisualizationPanel createVisualizationPanel(TileBasedVisualization vis) throws UnsupportedFeatureException {
        /* get the default configuration for the visualization */
        VisualizerConfig config = vis.getDefaultConfig();

        /* create and return a new visualization panel */
        return new VisualizationPanel(vis, config);
    }

    @Override
    public void addKeyCommand(short event, short vk, KeyCommand command) {
        visualization.getKeyController().addKeyCommand(event, vk, command);
    }

    @Override
    public OSMParser createParser(SimulationConfig simconfig) {
        /* global properties for (all) generators */
        FeatureGenerator.Properties genprops = new FeatureGenerator.Properties();
        genprops.bounds = FeatureGenerator.Properties.BoundaryManagement.CLIP;

        /* create a configuration, add factories for parsed components */
        OSMParser.Config osmconfig = new OSMParser.Config().setGeneratorProperties(genprops);

        StreetGraphFeatureDefinition streetgraph = null;
        if (simconfig != null) {
            // predicates to match/select features
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
                // case "service":       return true;

                case "motorway_link": return true;
                case "trunk_link":    return true;
                case "primary_link":  return true;
                case "tertiary_link": return true;

                case "living_street": return true;
                // case "track":         return true;
                case "road":          return true;
                }

                return false;
            };

            streetgraph = new StreetGraphFeatureDefinition(
                    "streetgraph",
                    new FeatureDependency(OSMParser.PLACEHOLDER_UNIFICATION, null),
                    new StreetGraphGenerator(simconfig),
                    n -> false,
                    streetgraphMatcher
            );

            osmconfig.setStreetGraphFeatureDefinition(streetgraph);
        }

        /* replace the style-placeholders with the feature-definitions/placeholders used by the osm-processor */
        style.replaceDependencyPlaceholders(OSMParser.PLACEHOLDER_WAY_CLIPPING, OSMParser.PLACEHOLDER_UNIFICATION,
                streetgraph);

        osmconfig.putWayInitializer(StreetComponent.class, new StreetComponentFactory())
                .putWayInitializer(SanitizerWayComponent.class, new SanitizerWayComponentFactory())
                .putRelationInitializer("restriction", new RestrictionRelationFactory());

        /* add the features defined in the style to the parser */
        style.getFeatureDefinitions().forEach(osmconfig::putMapFeatureDefinition);

        /* create and return the parser */
        return osmconfig.createParser();
    }

    @Override
    public TileLayerProvider createLayerProvider(Collection<LayerDefinition> layers) {
        /* create the layer provider */
        LayeredTileMap provider = new LayeredTileMap(tilingScheme);

        /* add a generator to support feature layers */
        FeatureTileLayerGenerator generator = new FeatureTileLayerGenerator();
        provider.putGenerator(FeatureTileLayerSource.class, generator);

        /* add the leyer definitions */
        layers.forEach(provider::addLayer);

        return provider;
    }

    @Override
    public void changeMap(OSMParser.Result result) throws InterruptedException {
        QuadTreeTiledMapSegment tiled
                = new QuadTreeTiledMapSegment.Generator().generate(result.segment, tilingScheme, tileGridLevel);

        /* update the feature sources, so that they will use the created provider */
        for (LayerDefinition def : layers) {
            LayerSource src = def.getSource();

            if (src instanceof FeatureTileLayerSource) ((FeatureTileLayerSource) src).setFeatureProvider(tiled);
        }

        /* center the view to the parsed area */
        visualization.getVisualizer().resetView();
    }

    @Override
    public OSMParser.Result parse(File file) throws Exception {
        return parser.parse(file);
    }
}
