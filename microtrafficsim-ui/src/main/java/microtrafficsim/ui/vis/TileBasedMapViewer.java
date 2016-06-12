package microtrafficsim.ui.vis;

import com.jogamp.newt.event.KeyEvent;
import microtrafficsim.core.map.layers.TileLayerDefinition;
import microtrafficsim.core.map.layers.TileLayerSource;
import microtrafficsim.core.map.style.StyleSheet;
import microtrafficsim.core.map.tiles.QuadTreeTiledMapSegment;
import microtrafficsim.core.map.tiles.QuadTreeTilingScheme;
import microtrafficsim.core.parser.OSMParser;
import microtrafficsim.core.parser.StreetGraphFeatureDefinition;
import microtrafficsim.core.parser.StreetGraphGenerator;
import microtrafficsim.core.simulation.configs.SimulationConfig;
import microtrafficsim.core.vis.*;
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
import microtrafficsim.osm.parser.features.streets.StreetComponent;
import microtrafficsim.osm.parser.features.streets.StreetComponentFactory;
import microtrafficsim.osm.parser.processing.osm.sanitizer.SanitizerWayComponent;
import microtrafficsim.osm.parser.processing.osm.sanitizer.SanitizerWayComponentFactory;
import microtrafficsim.osm.parser.relations.restriction.RestrictionRelationFactory;
import microtrafficsim.osm.primitives.Way;
import microtrafficsim.ui.utils.Utils;

import javax.xml.stream.XMLStreamException;
import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.function.Predicate;

/**
 * @author Dominic Parga Cacheiro
 */
public class TileBasedMapViewer implements MapViewer {
  /* -- window parameters -------------------------------------------------------------------- */

  /**
   * The initial window width.
   */
  public static final int INITIAL_WINDOW_WIDTH = 1600;

  /**
   * The initial window height.
   */
  public static final int INITIAL_WINDOW_HEIGHT = 900;


  /* -- style parameters --------------------------------------------------------------------- */

  /**
   * The projection used to transform the spherical world coordinates to plane coordinates.
   * The resulting tile size is dependent on the scale of this projection (2x scale). For this
   * example, the tile size will be 512x512 pixel.
   */
  public static final Projection PROJECTION = new MercatorProjection(256);

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


  private VisualizationPanel vpanel;
  private TileBasedVisualization visualization;
  private OSMParser parser;
  private Collection<TileLayerDefinition> layers;

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
    layers = STYLE.getLayers();
    TileLayerProvider layerProvider = createLayerProvider(layers);
    PreRenderedTileProvider provider = new PreRenderedTileProvider(layerProvider);

    /* create the visualizer */
    visualization = createVisualization(provider);

    /* parse the OSM file asynchronously and update the sources */
    if (config != null)
      parser = createParser(config);
    else
      parser = createParser();

    /* create and initialize the VisualizationPanel and JFrame */
    vpanel = createVisualizationPanel(visualization);
  }

  @Override
  public void show() {
    vpanel.start();

    /* if specified, print frame statistics */
    if (PRINT_FRAME_STATS)
      visualization.getRenderContext().getAnimator().setUpdateFPSFrames(60, System.out);
  }

  @Override
  public void stop() {
    vpanel.stop();
  }

  @Override
  public TileBasedVisualization createVisualization(TileProvider provider) {
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

  @Override
  public VisualizationPanel createVisualizationPanel(TileBasedVisualization vis) throws UnsupportedFeatureException {
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

  @Override
  public void addKeyCommand(short event, short vk, KeyCommand command) {
    visualization.getKeyController().addKeyCommand(event, vk, command);
  }

  @Override
  public OSMParser createParser(SimulationConfig simconfig) {
    /* get the parser configuration from the given style */
    StyleSheet.ParserConfig styleconfig = STYLE.getParserConfiguration();

    /* create a configuration, add factories for parsed components */
    OSMParser.Config osmconfig = new OSMParser.Config()
            .setGeneratorIndexBefore(styleconfig.generatorIndexOfUnification)
            .setGeneratorIndexStreetGraph(styleconfig.generatorIndexOfStreetGraph);

    if (simconfig != null) {
      // predicates to match/select features
      Predicate<Way> streetgraphMatcher = w -> {
        if (!w.visible) return false;
        if (w.tags.get("highway") == null) return false;
        if (w.tags.get("area") != null && !w.tags.get("area").equals("no")) return false;

        switch (w.tags.get("highway")) {
          case "motorway":
            return true;
          case "trunk":
            return true;
          case "primary":
            return true;
          case "secondary":
            return true;
          case "tertiary":
            return true;
          case "unclassified":
            return true;
          case "residential":
            return true;
          //case "service":			return true;

          case "motorway_link":
            return true;
          case "trunk_link":
            return true;
          case "primary_link":
            return true;
          case "tertiary_link":
            return true;

          case "living_street":
            return true;
          case "track":
            return true;
          case "road":
            return true;
          case "roundabout":
            return true;
          // todo case "roundabout" ?
        }

        return false;
      };

      StreetGraphFeatureDefinition streetgraph = new StreetGraphFeatureDefinition(
              "streetgraph",
              styleconfig.generatorIndexOfStreetGraph,
              new StreetGraphGenerator(simconfig),
              n -> false,
              streetgraphMatcher);

      osmconfig.setStreetGraphFeatureDefinition(streetgraph);
    }

    osmconfig.putWayInitializer(StreetComponent.class, new StreetComponentFactory())
            .putWayInitializer(SanitizerWayComponent.class, new SanitizerWayComponentFactory())
            .putRelationInitializer("restriction", new RestrictionRelationFactory());

    /* add the features defined in the style to the parser */
    STYLE.getFeatureDefinitions().forEach(osmconfig::putMapFeatureDefinition);

    /* create and return the parser */
    return osmconfig.createParser();
  }

  @Override
  public TileLayerProvider createLayerProvider(Collection<TileLayerDefinition> layers) {
    /* create the layer provider */
    LayeredTileMap provider = new LayeredTileMap(TILING_SCHEME);

    /* add a generator to support feature layers */
    FeatureTileLayerGenerator generator = new FeatureTileLayerGenerator();
    provider.putGenerator(FeatureTileLayerSource.class, generator);

    /* add the leyer definitions */
    layers.forEach(provider::addLayer);

    return provider;
  }

  @Override
  public void changeMap(OSMParser.Result result) throws InterruptedException {
    QuadTreeTiledMapSegment tiled = new QuadTreeTiledMapSegment.Generator()
            .generate(result.segment, TILING_SCHEME, TILE_GRID_LEVEL);

    /* update the feature sources, so that they will use the created provider */
    for (TileLayerDefinition def : layers) {
      TileLayerSource src = def.getSource();

      if (src instanceof FeatureTileLayerSource)
        ((FeatureTileLayerSource) src).setFeatureProvider(tiled);
    }

    /* center the view to the parsed area */
    visualization.getVisualizer().resetView();
  }

  @Override
  public OSMParser.Result parse(File file) throws IOException, XMLStreamException {
    return parser.parse(file);
  }
}
