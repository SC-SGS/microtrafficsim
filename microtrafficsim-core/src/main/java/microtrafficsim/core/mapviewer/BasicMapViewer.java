package microtrafficsim.core.mapviewer;

import microtrafficsim.core.map.style.StyleSheet;
import microtrafficsim.core.parser.OSMParser;
import microtrafficsim.core.parser.features.streetgraph.StreetGraphFeatureDefinition;
import microtrafficsim.core.parser.features.streetgraph.StreetGraphGenerator;
import microtrafficsim.core.parser.processing.sanitizer.SanitizerWayComponent;
import microtrafficsim.core.parser.processing.sanitizer.SanitizerWayComponentFactory;
import microtrafficsim.core.simulation.configs.ScenarioConfig;
import microtrafficsim.core.vis.*;
import microtrafficsim.core.vis.input.KeyCommand;
import microtrafficsim.core.vis.map.projections.Projection;
import microtrafficsim.osm.parser.features.FeatureDependency;
import microtrafficsim.osm.parser.features.FeatureGenerator;
import microtrafficsim.osm.parser.features.streets.StreetComponent;
import microtrafficsim.osm.parser.features.streets.StreetComponentFactory;
import microtrafficsim.osm.parser.relations.restriction.RestrictionRelationFactory;
import microtrafficsim.osm.primitives.Way;

import java.io.File;
import java.util.function.Predicate;

/**
 * @author Maximilian Luz, Dominic Parga Cacheiro
 */
public abstract class BasicMapViewer implements MapViewer {

    private final int initialWindowWidth;
    private final int initialWindowHeight;

    protected final StyleSheet style;
    private final Projection   projection;

    private final boolean printFrameStats;

    private VisualizationPanel      vpanel;
    private OSMParser               parser;


    public BasicMapViewer(int width, int height, StyleSheet style, Projection projection, boolean printFrameStats) {

        /* window parameters */
        this.initialWindowWidth = width;
        this.initialWindowHeight  = height;

        /* style parameters */
        this.style = style;
        this.projection = projection;

        /* internal settings */
        this.printFrameStats = printFrameStats;
    }

    protected abstract AbstractVisualization getVisualization();

    /*
    |===============|
    | (i) MapViewer |
    |===============|
    */
    @Override
    public VisualizationPanel getVisualizationPanel() {
        return vpanel;
    }

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
    public void addOverlay(int index, Overlay overlay) {
        getVisualization().putOverlay(index, overlay);
    }

    @Override
    public void addKeyCommand(short event, short vk, KeyCommand command) {
        getVisualization().getKeyController().addKeyCommand(event, vk, command);
    }

    @Override
    public void show() {
        vpanel.start();

        /* if specified, print frame statistics */
        if (printFrameStats)
            getVisualization().getRenderContext().getAnimator().setUpdateFPSFrames(60, System.out);
    }

    @Override
    public void create(ScenarioConfig config) throws UnsupportedFeatureException {

        /* create the visualizer */
        createVisualization();

        /* create and initialize the VisualizationPanel */
        createVisualizationPanel();

        /* parse the OSM file asynchronously and update the sources */
        createParser(config);
    }

    @Override
    public void createVisualizationPanel() throws UnsupportedFeatureException {

        Visualization vis = getVisualization();

        /* get the default configuration for the visualization */
        VisualizerConfig config = vis.getDefaultConfig();

        /* create and return a new visualization panel */
        vpanel = new VisualizationPanel(vis, config);
    }

    @Override
    public void createParser(ScenarioConfig scenarioConfig) {
        /* global properties for (all) generators */
        FeatureGenerator.Properties genprops = new FeatureGenerator.Properties();
        genprops.bounds = FeatureGenerator.Properties.BoundaryManagement.CLIP;

        /* create a configuration, add factories for parsed components */
        OSMParser.Config osmconfig = new OSMParser.Config().setGeneratorProperties(genprops);

        StreetGraphFeatureDefinition streetgraph = null;
        if (scenarioConfig != null) {
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
                    new StreetGraphGenerator(scenarioConfig),
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
        parser = osmconfig.createParser();
    }

    @Override
    public OSMParser.Result parse(File file) throws Exception {
        return parser.parse(file);
    }
}
