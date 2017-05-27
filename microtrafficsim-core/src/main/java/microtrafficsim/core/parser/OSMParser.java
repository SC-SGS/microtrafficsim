package microtrafficsim.core.parser;

import microtrafficsim.core.logic.streetgraph.Graph;
import microtrafficsim.core.map.Bounds;
import microtrafficsim.core.map.Feature;
import microtrafficsim.core.map.MapSegment;
import microtrafficsim.core.parser.features.MapFeatureDefinition;
import microtrafficsim.core.parser.features.streetgraph.StreetGraphFeatureDefinition;
import microtrafficsim.core.parser.processing.OSMProcessor;
import microtrafficsim.osm.parser.Parser;
import microtrafficsim.osm.parser.base.DataSet;
import microtrafficsim.osm.parser.ecs.Component;
import microtrafficsim.osm.parser.ecs.ComponentFactory;
import microtrafficsim.osm.parser.features.FeatureDefinition;
import microtrafficsim.osm.parser.features.FeatureDependency;
import microtrafficsim.osm.parser.features.FeatureGenerator;
import microtrafficsim.osm.parser.features.FeatureSystem;
import microtrafficsim.osm.parser.relations.RelationFactory;
import microtrafficsim.osm.primitives.Node;
import microtrafficsim.osm.primitives.Way;

import javax.xml.stream.XMLStreamException;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;


/**
 * Wrapper for the OSM parser. Sets up basic processing and generation for
 * {@link MapFeatureDefinition}s and a {@code StreetGraphFeatureDefinition}.
 *
 * @author Maximilian Luz
 */
public class OSMParser {

    /**
     * Placeholder {@code FeatureDefinition} to be used to indicate the way-clipping step (of the pre-processing stage)
     * either in dependencies or (internally) in the list of generated features. The user of this class does note
     * need to explicitly add this placeholder to the list of feature-definitions.
     */
    public static final FeatureDefinition PLACEHOLDER_WAY_CLIPPING = OSMProcessor.PLACEHOLDER_WAY_CLIPPING;

    /**
     * Placeholder {@code FeatureDefinition} to be used to indicate the street-unification step (of the pre-processing
     * stage) either in dependencies or (internally) in the list of generated features. The user of this class does not
     * need to explicitly add this placeholder to the list of feature-definitions.
     */
    public static final FeatureDefinition PLACEHOLDER_UNIFICATION = OSMProcessor.PLACEHOLDER_UNIFICATION;

    private Parser           parser;
    private Config           config;
    private DataSetExtractor extractor;

    private OSMParser(Parser parser, Config config, DataSetExtractor extractor) {
        this.parser    = parser;
        this.config    = config;
        this.extractor = extractor;
    }

    /**
     * Creates a {@code OSMParser} based on the given configuration.
     *
     * @param config the configuration from which the parser should be created.
     * @return the created parser.
     */
    public static OSMParser create(Config config) {
        // create parser
        OSMProcessor processor = new OSMProcessor(config.genprops, config.streetgraph);
        Parser       parser    = new Parser(processor);

        // add features
        FeatureSystem featuresys = parser.getFeatureSystem();
        featuresys.putFeatures(config.features.values());

        if (!featuresys.getAllFeatures().contains(OSMProcessor.PLACEHOLDER_WAY_CLIPPING))
            featuresys.putFeature(OSMProcessor.PLACEHOLDER_WAY_CLIPPING);

        if (!featuresys.getAllFeatures().contains(OSMProcessor.PLACEHOLDER_UNIFICATION))
            featuresys.putFeature(OSMProcessor.PLACEHOLDER_UNIFICATION);

        if (config.streetgraph != null) {
            config.streetgraph.getDependency().addRequires(PLACEHOLDER_UNIFICATION);
            featuresys.putFeature(config.streetgraph);
        }

        // add initializers
        parser.getNodeEntityManager().getInitializerMap().putAll(config.nodeInitializers);
        parser.getWayEntityManager().getInitializerMap().putAll(config.wayInitializers);
        parser.getRelationManager().putFactories(config.relationInitializers);

        // add extractor for bounds, make sure it is generated before every other feature
        HashSet<FeatureDefinition> extractorRequires = new HashSet<>();
        extractorRequires.addAll(config.features.values());
        extractorRequires.add(PLACEHOLDER_WAY_CLIPPING);
        extractorRequires.add(PLACEHOLDER_UNIFICATION);
        if (config.streetgraph != null)
            extractorRequires.add(config.streetgraph);

        DataSetExtractor  extractor = new DataSetExtractor();
        FeatureDefinition extractordef = new FeatureDefinition(
                "microtrafficsim.core.parser.OSMParser.BoundsExtractor",
                new FeatureDependency(null, extractorRequires),
                extractor,
                (Node n) -> false,
                (Way w) -> false
        );

        featuresys.putFeature(extractordef);

        return new OSMParser(parser, new Config(config), extractor);
    }

    /**
     * Execute this parser and return the parsed result.
     *
     * @param file the file to parse.
     * @return the result parsed from the given file.
     * @throws XMLStreamException   for mal-formed XML documents.
     * @throws IOException          if the file cannot be read.
     * @throws InterruptedException if the parsing thread has been interrupted.
     * @throws Exception            if any other exception occurred during processing.
     */
    public Result parse(File file) throws Exception {
        parser.parse(file);

        // get feature set
        HashMap<String, Feature<?>> featureset = new HashMap<>();
        for (MapFeatureDefinition<?> def : config.features.values()) {
            featureset.putAll(def.getGenerator().getGeneratedFeatures());
        }

        // get StreetGraph
        Graph streetgraph = null;
        if (config.streetgraph != null)
            streetgraph = config.streetgraph
                    .getGenerator()
                    .getStreetGraph();

        return new Result(new MapSegment(extractor.bounds, featureset), streetgraph);
    }

    /**
     * Configuration for the {@code OSMParser}.
     */
    public static class Config {
        private FeatureGenerator.Properties          genprops;
        private StreetGraphFeatureDefinition         streetgraph;
        private Map<String, MapFeatureDefinition<?>> features;

        private Map<Class<? extends Component>, ComponentFactory<? extends Component, Node>> nodeInitializers;
        private Map<Class<? extends Component>, ComponentFactory<? extends Component, Way>>  wayInitializers;
        private Map<String, RelationFactory>                                                 relationInitializers;

        /**
         * Constructs a new (empty) configuration.
         */
        public Config() {
            this.genprops             = new FeatureGenerator.Properties();
            this.streetgraph          = null;
            this.features             = new HashMap<>();
            this.nodeInitializers     = new HashMap<>();
            this.wayInitializers      = new HashMap<>();
            this.relationInitializers = new HashMap<>();
        }

        /**
         * Copy-constructs a new configuration based on the given one.
         *
         * @param other the configuration from which this configuration should be copied.
         */
        public Config(Config other) {
            this.genprops             = other.genprops;
            this.streetgraph          = other.streetgraph;
            this.features             = new HashMap<>(other.features);
            this.nodeInitializers     = new HashMap<>(other.nodeInitializers);
            this.wayInitializers      = new HashMap<>(other.wayInitializers);
            this.relationInitializers = new HashMap<>(other.relationInitializers);
        }

        /**
         * Sets the properties to be used for generating the features.
         *
         * @param genprops the properties to be used for generating features.
         * @return this configuration.
         */
        public Config setGeneratorProperties(FeatureGenerator.Properties genprops) {
            this.genprops = genprops;
            return this;
        }


        /**
         * Associate the given feature definition with its feature name. An existing binding will be
         * overwritten.
         *
         * @param feature the feature definition to set the binding for.
         * @return this configuration.
         */
        public Config putMapFeatureDefinition(MapFeatureDefinition<?> feature) {
            features.put(feature.getName(), feature);
            return this;
        }

        /**
         * Sets the feature definition for the street graph. An existing definition will be overwritten.
         *
         * @param sg the feature definition to set for the street graph.
         * @return this configuration.
         */
        public Config setStreetGraphFeatureDefinition(StreetGraphFeatureDefinition sg) {
            this.streetgraph = sg;
            return this;
        }

        /**
         * Associate the given component factory with the given type. This tells the parser to initialize
         * node-components of the given type by using the specified factory. May overwrite a previous binding.
         *
         * @param type    the type to set the initializer for.
         * @param factory the initializer creating components of the given type.
         * @param <T>     the type of the component.
         * @return this configuration.
         */
        public <T extends Component> Config putNodeInitializer(Class<T> type, ComponentFactory<T, Node> factory) {
            nodeInitializers.put(type, factory);
            return this;
        }

        /**
         * Associate the given component factory with the given type. This tells the parser to initialize
         * way-components of the given type by using the specified factory. May overwrite a previous binding.
         *
         * @param type    the type to set the initializer for.
         * @param factory the initializer creating components of the given type.
         * @param <T>     the type of the component.
         * @return this configuration.
         */
        public <T extends Component> Config putWayInitializer(Class<T> type, ComponentFactory<T, Way> factory) {
            wayInitializers.put(type, factory);
            return this;
        }

        /**
         * Associates the given relation factory with the given relation name (i.e. type). This tells the parser
         * to use the specified factory to initialize/create relations for the given typename. May overwrite a
         * previous binding.
         *
         * @param name    the typename of the relation.
         * @param factory the factory that should be used to create relationfs of the specified type/name.
         * @return this configuration.
         */
        public Config putRelationInitializer(String name, RelationFactory factory) {
            relationInitializers.put(name, factory);
            return this;
        }

        // TODO: extends functionality

        /**
         * Create a parser based on this configuration.
         *
         * @return the created parser.
         * @see OSMParser#create(Config)
         */
        public OSMParser build() {
            return OSMParser.create(this);
        }
    }

    /**
     * Result type for the {@code OSMParser}. Stores the parsed map-segment and street-graph.
     */
    public static class Result {
        /**
         * The parsed map-segment.
         */
        public final MapSegment segment;

        /**
         * The parsed street-graph.
         */
        public final Graph streetgraph;

        /**
         * Constructs a new {@code Result} based on the given parameters.
         *
         * @param segment     the parsed map-segment.
         * @param streetgraph the parsed street-graph.
         */
        private Result(MapSegment segment, Graph streetgraph) {
            this.segment     = segment;
            this.streetgraph = streetgraph;
        }
    }

    /**
     * {@code FeatureGenerator} used to extract the {@code Bounds} during parsing.
     */
    private static class DataSetExtractor implements FeatureGenerator {

        /**
         * The extracted bound.
         */
        public Bounds bounds;

        @Override
        public void execute(DataSet dataset, FeatureDefinition feature, FeatureGenerator.Properties genprops) {
            if (genprops.clip == Properties.BoundaryManagement.CLIP && genprops.bounds != null) {
                this.bounds = genprops.bounds;
            } else {
                this.bounds = new Bounds(dataset.bounds);
            }
        }
    }
}
