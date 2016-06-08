package microtrafficsim.examples.mapviewer.tilebased;

import com.jogamp.newt.event.KeyEvent;
import microtrafficsim.core.map.layers.TileLayerDefinition;
import microtrafficsim.core.map.style.StyleSheet;
import microtrafficsim.core.map.tiles.QuadTreeTilingScheme;
import microtrafficsim.core.map.tiles.TilingScheme;
import microtrafficsim.core.parser.MapFeatureDefinition;
import microtrafficsim.core.parser.OSMParser;
import microtrafficsim.core.vis.UnsupportedFeatureException;
import microtrafficsim.core.vis.VisualizationPanel;
import microtrafficsim.core.vis.VisualizerConfig;
import microtrafficsim.core.vis.map.projections.MercatorProjection;
import microtrafficsim.core.vis.map.projections.Projection;
import microtrafficsim.core.vis.map.tiles.TileProvider;
import microtrafficsim.core.vis.map.tiles.layers.FeatureTileLayerGenerator;
import microtrafficsim.core.vis.map.tiles.layers.FeatureTileLayerSource;
import microtrafficsim.core.vis.map.tiles.layers.LayeredTileMap;
import microtrafficsim.core.vis.map.tiles.layers.TileLayerProvider;
import microtrafficsim.core.vis.tilebased.TileBasedVisualization;
import microtrafficsim.core.vis.tilebased.TileGridOverlay;
import microtrafficsim.osm.parser.features.streets.StreetComponent;
import microtrafficsim.osm.parser.features.streets.StreetComponentFactory;
import microtrafficsim.osm.parser.processing.osm.sanitizer.SanitizerWayComponent;
import microtrafficsim.osm.parser.processing.osm.sanitizer.SanitizerWayComponentFactory;
import microtrafficsim.osm.parser.relations.restriction.RestrictionRelationFactory;

import java.util.Collection;


public class Example {

	static final String DEFAULT_OSM_XML = "map.osm";
	static final boolean PRINT_FRAME_STATS = false;

	static final int WINDOW_WIDTH = 1600;
	static final int WINDOW_HEIGHT = 900;
	static final int MSAA = 0;

	static final int NUM_SEGMENT_WORKERS = Math.max(Runtime.getRuntime().availableProcessors() - 2, 2);

	static final Projection PROJECTION = new MercatorProjection(256);    // tiles will be 512x512 pixel
	static final QuadTreeTilingScheme TILING_SCHEME = new QuadTreeTilingScheme(PROJECTION, 0, 19);

	static final int TILE_GRID_LEVEL = 12;

	static final StyleSheet STYLE = new LightStyleSheet();
	
	
	static TileBasedVisualization createVisualization(TileProvider provider) {
		TileBasedVisualization vis = new TileBasedVisualization(
				WINDOW_WIDTH,
				WINDOW_HEIGHT,
				provider,
				NUM_SEGMENT_WORKERS);

		vis.apply(STYLE);

		vis.getKeyController().addKeyCommand(
				KeyEvent.EVENT_KEY_PRESSED,
				KeyEvent.VK_F12,
				e -> Utils.asyncScreenshot(vis.getRenderContext()));
		
		vis.getKeyController().addKeyCommand(
				KeyEvent.EVENT_KEY_PRESSED,
				KeyEvent.VK_ESCAPE,
				e -> Runtime.getRuntime().halt(0));
		
		vis.getRenderContext().setUncaughtExceptionHandler(new Utils.DebugExceptionHandler());

		// vis.putOverlay(0, new TileGridOverlay(provider.getTilingScheme()));

		return vis;
	}
	
	static VisualizationPanel createVisualizationPanel(TileBasedVisualization vis) throws UnsupportedFeatureException {
		VisualizerConfig config = vis.getDefaultConfig();
		
		if (MSAA > 1) {
			config.glcapabilities.setSampleBuffers(true);
			config.glcapabilities.setNumSamples(MSAA);
		}
		
		return new VisualizationPanel(vis, config);
	}
	
	
	static OSMParser getParser() {
		StyleSheet.ParserConfig styleconfig = STYLE.getParserConfiguration();
		OSMParser.Config config = new OSMParser.Config()
				.setGeneratorIndexBefore(styleconfig.generatorIndexOfUnification)
				.setGeneratorIndexStreetGraph(styleconfig.generatorIndexOfStreetGraph)
				.putWayInitializer(StreetComponent.class, new StreetComponentFactory())
				.putWayInitializer(SanitizerWayComponent.class, new SanitizerWayComponentFactory())
				.putRelationInitializer("restriction", new RestrictionRelationFactory());

		for (MapFeatureDefinition<?> feature : STYLE.getFeatureDefinitions())
			config.putMapFeatureDefinition(feature);

		return config.createParser();
	}

	
	static Collection<TileLayerDefinition> getLayerDefinitions() {
		return STYLE.getLayers();
	}

	static TileLayerProvider getLayerProvider(TilingScheme scheme, Collection<TileLayerDefinition> layers) {
        LayeredTileMap provider = new LayeredTileMap(scheme);
        FeatureTileLayerGenerator generator = new FeatureTileLayerGenerator();
        provider.putGenerator(FeatureTileLayerSource.class, generator);

        layers.forEach(provider::addLayer);

		return provider;
	}
}
