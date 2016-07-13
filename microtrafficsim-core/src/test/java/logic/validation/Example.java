package logic.validation;

import com.jogamp.newt.event.KeyEvent;
import com.jogamp.opengl.GL3;
import microtrafficsim.core.map.features.Street;
import microtrafficsim.core.map.layers.LayerDefinition;
import microtrafficsim.core.parser.MapFeatureDefinition;
import microtrafficsim.core.parser.OSMParser;
import microtrafficsim.core.parser.StreetGraphFeatureDefinition;
import microtrafficsim.core.parser.StreetGraphGenerator;
import microtrafficsim.core.simulation.Simulation;
import microtrafficsim.core.simulation.configs.SimulationConfig;
import microtrafficsim.core.vis.UnsupportedFeatureException;
import microtrafficsim.core.vis.Visualization;
import microtrafficsim.core.vis.VisualizationPanel;
import microtrafficsim.core.vis.VisualizerConfig;
import microtrafficsim.core.vis.map.projections.Projection;
import microtrafficsim.core.vis.map.segments.FeatureSegmentLayerGenerator;
import microtrafficsim.core.vis.map.segments.FeatureSegmentLayerSource;
import microtrafficsim.core.vis.map.segments.LayeredMapSegment;
import microtrafficsim.core.vis.map.segments.SegmentLayerProvider;
import microtrafficsim.core.vis.mesh.style.Style;
import microtrafficsim.core.vis.opengl.shader.resources.ShaderProgramSource;
import microtrafficsim.core.vis.opengl.utils.Color;
import microtrafficsim.core.vis.segmentbased.SegmentBasedVisualization;
import microtrafficsim.osm.parser.features.streets.StreetComponent;
import microtrafficsim.osm.parser.features.streets.StreetComponentFactory;
import microtrafficsim.osm.parser.features.streets.StreetFeatureGenerator;
import microtrafficsim.osm.parser.processing.osm.sanitizer.SanitizerWayComponent;
import microtrafficsim.osm.parser.processing.osm.sanitizer.SanitizerWayComponentFactory;
import microtrafficsim.osm.parser.relations.restriction.RestrictionRelationFactory;
import microtrafficsim.osm.primitives.Way;
import microtrafficsim.utils.resources.PackagedResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Predicate;


public class Example {
    static final int WINDOW_WIDTH        = 1600;
    static final int WINDOW_HEIGHT       = 900;
    private static final int MSAA                = 4;
    private static final int NUM_SEGMENT_WORKERS = 2;
    private static final Logger logger          = LoggerFactory.getLogger(Example.class);

    static void printSeed(long seed) {
        logger.debug("Initializing simulation with seed '0x" + Long.toHexString(seed).toUpperCase() + "'");
    }


    static SegmentBasedVisualization createVisualization(SegmentLayerProvider provider, Simulation sim) {
        SegmentBasedVisualization vis = new SegmentBasedVisualization(WINDOW_WIDTH, WINDOW_HEIGHT,
                                                                      provider, NUM_SEGMENT_WORKERS);


        vis.getKeyController().addKeyCommand(
                KeyEvent.EVENT_KEY_PRESSED, KeyEvent.VK_F12, e -> Utils.asyncScreenshot(vis.getRenderContext()));

        vis.getKeyController().addKeyCommand(KeyEvent.EVENT_KEY_PRESSED, KeyEvent.VK_SPACE, e -> {
            if (sim.isPaused())
                sim.run();
            else
                sim.cancel();
        });

        vis.getKeyController().addKeyCommand(KeyEvent.EVENT_KEY_PRESSED, KeyEvent.VK_RIGHT, e -> {
            while (!sim.isPaused())
                sim.cancel();
            sim.runOneStep();
        });

        vis.getKeyController().addKeyCommand(
                KeyEvent.EVENT_KEY_PRESSED, KeyEvent.VK_ESCAPE, e -> Runtime.getRuntime().halt(0));

        vis.getRenderContext().setUncaughtExceptionHandler(new Utils.DebugExceptionHandler());


        return vis;
    }

    static VisualizationPanel createVisualizationPanel(SegmentBasedVisualization vis)
            throws UnsupportedFeatureException {

        VisualizerConfig config = vis.getDefaultConfig();

        //noinspection ConstantConditions
        if (MSAA > 1) {
            config.glcapabilities.setSampleBuffers(true);
            config.glcapabilities.setNumSamples(MSAA);
        }

        return new VisualizationPanel(vis, config);
    }


    static OSMParser getParser(SimulationConfig simcfg) {

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
            // case "service":			return true;

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

        // set the generator-indices
        int genindexBefore      = 256;
        int genindexStreetGraph = 512;

        // define the features
        MapFeatureDefinition<Street> streets
                = new MapFeatureDefinition<>("streets:all",
                                             genindexStreetGraph + 1,    // generate after StreetGraph
                                             new StreetFeatureGenerator(),
                                             n -> false,
                                             streetgraphMatcher);

        StreetGraphFeatureDefinition streetgraph = new StreetGraphFeatureDefinition(
                "streetgraph", genindexStreetGraph, new StreetGraphGenerator(simcfg), n -> false, streetgraphMatcher);

        return new OSMParser.Config()
                .setGeneratorIndexBefore(genindexBefore)
                .setGeneratorIndexStreetGraph(genindexStreetGraph)
                .setStreetGraphFeatureDefinition(streetgraph)
                .putMapFeatureDefinition(streets)
                .putWayInitializer(StreetComponent.class, new StreetComponentFactory())
                .putWayInitializer(SanitizerWayComponent.class, new SanitizerWayComponentFactory())
                .putRelationInitializer("restriction", new RestrictionRelationFactory())
                .createParser();
    }


    static Set<LayerDefinition> getLayerDefinitions() {
        HashSet<LayerDefinition> layers = new HashSet<>();

        ShaderProgramSource progStreets = new ShaderProgramSource("streets");
        progStreets.addSource(GL3.GL_VERTEX_SHADER, new PackagedResource(Visualization.class, "/shaders/basic.vs"));
        progStreets.addSource(GL3.GL_FRAGMENT_SHADER, new PackagedResource(Visualization.class, "/shaders/basic.fs"));

        Style streets = new Style(progStreets);
        streets.uniforms.put("u_color", () -> Color.fromRGB(0x000000).toVec4f());

        layers.add(new LayerDefinition("streets", 0, new FeatureSegmentLayerSource("streets:all", streets)));

        return layers;
    }

    static SegmentLayerProvider getSegmentLayerProvider(Projection projection, Set<LayerDefinition> layers) {
        LayeredMapSegment provider = new LayeredMapSegment(projection);

        FeatureSegmentLayerGenerator generator = new FeatureSegmentLayerGenerator();
        provider.putGenerator(FeatureSegmentLayerSource.class, generator);

        layers.forEach(provider::addLayer);
        return provider;
    }
}
