package microtrafficsim.core.convenience;

import com.jogamp.newt.event.KeyEvent;
import microtrafficsim.core.convenience.utils.Utils;
import microtrafficsim.core.map.SegmentFeatureProvider;
import microtrafficsim.core.map.TileFeatureProvider;
import microtrafficsim.core.map.layers.LayerDefinition;
import microtrafficsim.core.map.layers.TileLayerSource;
import microtrafficsim.core.map.style.MapStyleSheet;
import microtrafficsim.core.map.style.impl.MonochromeStyleSheet;
import microtrafficsim.core.map.tiles.QuadTreeTiledMapSegment;
import microtrafficsim.core.map.tiles.QuadTreeTilingScheme;
import microtrafficsim.core.map.tiles.TilingScheme;
import microtrafficsim.core.vis.AbstractVisualization;
import microtrafficsim.core.vis.map.projections.MercatorProjection;
import microtrafficsim.core.vis.map.projections.Projection;
import microtrafficsim.core.vis.map.tiles.PreRenderedTileProvider;
import microtrafficsim.core.vis.map.tiles.TileProvider;
import microtrafficsim.core.vis.map.tiles.layers.FeatureTileLayerGenerator;
import microtrafficsim.core.vis.map.tiles.layers.FeatureTileLayerSource;
import microtrafficsim.core.vis.map.tiles.layers.LayeredTileMap;
import microtrafficsim.core.vis.map.tiles.layers.TileLayerProvider;
import microtrafficsim.core.vis.tilebased.TileBasedVisualization;

import java.util.Collection;


/**
 * @author Maximilian Luz, Dominic Parga Cacheiro
 */
public class TileBasedMapViewer extends BasicMapViewer {

    private TileBasedVisualization visualization;
    private TileLayerProvider layerProvider;

    private TilingScheme preferredTilingScheme;
    private int preferredTileGridLevel;

    private final int numTileWorkers;

    /**
     * Default constructor using {@link MonochromeStyleSheet} as default style sheet.
     *
     * @see #TileBasedMapViewer(MapStyleSheet)
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
     * &bull {@code STYLE} = {@code style} (given as parameter) <br>
     *
     * @param style This style sheet is used for the map style
     * @see #TileBasedMapViewer(int, int, MapStyleSheet, Projection)
     */
    public TileBasedMapViewer(MapStyleSheet style) {
        this(1600, 900, style, new MercatorProjection());
    }

    /**
     * <p>
     * Default constructor initializing basic configurations: <br>
     * &bull {@code PRINT_FRAME_STATS} = false <br>
     *
     * @see #TileBasedMapViewer(int, int, MapStyleSheet, Projection, boolean)
     */
    public TileBasedMapViewer(int width, int height, MapStyleSheet style, Projection projection) {
        this(width, height, style, projection, false);
    }

    /**
     * <p>
     * Default constructor initializing basic configurations: <br>
     * &bull {@code TILING_SCHEME} = new {@link QuadTreeTilingScheme}(PROJECTION, 0, 19) <br>
     * <br>
     * &bull {@code TILE_GRID_LEVEL} = 12 <br>
     * &bull {@code NUM_TILE_WORKERS} = at least 2 <br>
     *
     * @see TileBasedMapViewer#TileBasedMapViewer(int, int, MapStyleSheet, Projection, TilingScheme, int, int,
     * boolean)
     */
    public TileBasedMapViewer(int width, int height, MapStyleSheet style, Projection projection, boolean printFrameStats) {
        this(
                width,
                height,
                style,
                projection,
                new QuadTreeTilingScheme(projection, 0, 19),
                12,
                Math.max(Runtime.getRuntime().availableProcessors() - 2, 2),
                printFrameStats
        );
    }

    /**
     * <p>
     * Default constructor.
     *
     * @param width      the (initial) width of the window.
     * @param height     the (initial) height of the window.
     * @param style      the style used for map-rendering.
     * @param projection the projection used to transform the spherical world coordinates to plane coordinates.
     *                   The resulting tile size is dependent on the scale of this projection (2x scale).
     *
     * @param preferredTilingScheme    the tiling-scheme used of the tiles to be displayed.
     * @param preferredTileGridLevel   the level for which map-features should be stored in a grid.
     * @param numTileWorkers  the number of worker-threads loading tiles in parallel in the background.
     * @param printFrameStats set to {@code true} to write frame-statistics to stdout.
     */
    public TileBasedMapViewer(int width, int height, MapStyleSheet style, Projection projection,
                              TilingScheme preferredTilingScheme, int preferredTileGridLevel, int numTileWorkers,
                              boolean printFrameStats) {
        super(width, height, style, projection, printFrameStats);

        /* style parameters */
        this.preferredTilingScheme = preferredTilingScheme;

        /* internal settings */
        this.preferredTileGridLevel = preferredTileGridLevel;
        this.numTileWorkers = numTileWorkers;
    }

    /*
    |====================|
    | (c) BasicMapViewer |
    |====================|
    */
    @Override
    protected AbstractVisualization getVisualization() {
        return visualization;
    }

    @Override
    public void createVisualization() {
        /* set up layer and tile provider */
        layerProvider = createLayerProvider(style.getLayers());
        TileProvider provider = new PreRenderedTileProvider(layerProvider);

        /* create a new visualization object */
        visualization = new TileBasedVisualization(
                getInitialWindowWidth(),
                getInitialWindowHeight(),
                provider,
                numTileWorkers);

        /* apply the style (background color) */
        visualization.apply(style);

        /* add some key commands */
        visualization.getKeyController().addKeyCommand(
                KeyEvent.EVENT_KEY_PRESSED,
                KeyEvent.VK_F12,
                e -> Utils.asyncScreenshot(visualization.getRenderContext()));

        /* set an exception handler, catching all unhandled exceptions on the render thread (for debugging purposes) */
        visualization.getRenderContext().setUncaughtExceptionHandler(new Utils.DebugExceptionHandler());
    }


    public void setPreferredTileGridLevel(int level) {
        this.preferredTileGridLevel = level;
    }

    public int getPreferredTileGridLevel() {
        return preferredTileGridLevel;
    }

    public void setPreferredTilingScheme(TilingScheme scheme) {
        this.preferredTilingScheme = scheme;
    }

    public TilingScheme getPreferredTilingScheme() {
        return preferredTilingScheme;
    }

    /**
     * Creates a {@code TileLayerProvider} from the given layer definitions.
     * The {@code TileLayerProvider} is used to provide map-layers and their
     * style to the visualization. {@code LayerDefinition}s describe such
     * a layer in dependence of a source object. {@code TileLayerGenerator}s
     * are used to generate a renderable {@code TileLayer} from a specified
     * source.
     *
     * @param layers the layer definitions for the provider
     */
    private TileLayerProvider createLayerProvider(Collection<LayerDefinition> layers) {
        /* create the layer provider */
        LayeredTileMap provider = new LayeredTileMap(preferredTilingScheme);

        /* add a generator to support feature layers */
        FeatureTileLayerGenerator generator = new FeatureTileLayerGenerator();
        provider.putGenerator(FeatureTileLayerSource.class, generator);

        /* add the leyer definitions */
        layers.forEach(provider::addLayer);

        return provider;
    }

    @Override
    public void setMap(SegmentFeatureProvider segment) throws InterruptedException {
        TileFeatureProvider tiled = null;

        if (segment instanceof TileFeatureProvider) {
            tiled = (TileFeatureProvider) segment;
        } else {
            tiled = new QuadTreeTiledMapSegment.Generator().generate(segment, preferredTilingScheme, preferredTileGridLevel);
        }

        setMap(tiled);
    }

    public void setMap(TileFeatureProvider tiles) {
        /* Update the tiling-scheme according to the source. Note: All sources must have the same tiling scheme and the
         * visual appearance (i.e. line-thickness) may depend on the tiling-scheme (specifically its scale). It is
         * thus often better to specify the tiling-scheme while loading/generating the feature provider and other
         * sources.
         */
        layerProvider.setTilingScheme(tiles.getTilingScheme());

        /* update the feature sources, so that they will use the created provider */
        for (LayerDefinition def : style.getLayers()) {
            TileLayerSource src = def.getSource();

            if (src instanceof FeatureTileLayerSource)
                ((FeatureTileLayerSource) src).setFeatureProvider(tiles);
        }

        /* center the view to the parsed area */
        visualization.getVisualizer().resetView();
    }
}
