package microtrafficsim.core.parser;

import microtrafficsim.core.logic.StreetGraph;
import microtrafficsim.core.map.Bounds;
import microtrafficsim.core.map.Feature;
import microtrafficsim.core.map.MapSegment;
import microtrafficsim.osm.parser.Parser;
import microtrafficsim.osm.parser.base.DataSet;
import microtrafficsim.osm.parser.ecs.Component;
import microtrafficsim.osm.parser.ecs.ComponentFactory;
import microtrafficsim.osm.parser.features.FeatureDefinition;
import microtrafficsim.osm.parser.features.FeatureGenerator;
import microtrafficsim.osm.parser.features.FeatureSystem;
import microtrafficsim.osm.parser.processing.osm.OSMProcessor;
import microtrafficsim.osm.parser.relations.RelationFactory;
import microtrafficsim.osm.primitives.Node;
import microtrafficsim.osm.primitives.Way;

import javax.xml.stream.XMLStreamException;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collector;


public class OSMParser {
	
	public static class Config {
		private int genindexBefore;
		private int genindexStreetGraph;
		
		private StreetGraphFeatureDefinition streetgraph;
		private Map<String, MapFeatureDefinition<?>> features;
		
		private Map<Class<? extends Component>, ComponentFactory<? extends Component, Node>> nodeInitializers;
		private Map<Class<? extends Component>, ComponentFactory<? extends Component, Way>> wayInitializers;
		private Map<String, RelationFactory> relationInitializers;
		
		public Config() {
			this.streetgraph = null;
			this.features = new HashMap<>();
			this.nodeInitializers = new HashMap<>();
			this.wayInitializers = new HashMap<>();
			this.relationInitializers = new HashMap<>();
		}
		
		public Config(Config other) {
			this.genindexBefore = other.genindexBefore;
			this.genindexStreetGraph = other.genindexStreetGraph;
			this.streetgraph = other.streetgraph;
			this.features = new HashMap<>(other.features);
			this.nodeInitializers = new HashMap<>(other.nodeInitializers);
			this.wayInitializers = new HashMap<>(other.wayInitializers);
			this.relationInitializers = new HashMap<>(other.relationInitializers);
		}
		
		
		public Config setGeneratorIndexBefore(int genindexBefore) {
			this.genindexBefore = genindexBefore;
			return this;
		}
		
		public Config setGeneratorIndexStreetGraph(int genindexStreetGraph) {
			this.genindexStreetGraph = genindexStreetGraph;
			return this;
		}
		
		
		public Config putMapFeatureDefinition(MapFeatureDefinition<?> feature) {
			features.put(feature.getName(), feature);
			return this;
		}
		
		public Config setStreetGraphFeatureDefinition(StreetGraphFeatureDefinition sg) {
			this.streetgraph = sg;
			return this;
		}
		
		
		public <T extends Component> Config putNodeInitializer(Class<T> type, ComponentFactory<T, Node> factory) {
			nodeInitializers.put(type, factory);
			return this;
		}
		
		public <T extends Component> Config putWayInitializer(Class<T> type, ComponentFactory<T, Way> factory) {
			wayInitializers.put(type, factory);
			return this;
		}
		
		public Config putRelationInitializer(String name, RelationFactory factory) {
			relationInitializers.put(name, factory);
			return this;
		}
		
		// TODO: extends functionality
		
		public OSMParser createParser() {
			return OSMParser.create(this);
		}
	}
	
	public static class Result {
		public final MapSegment segment;
		public final StreetGraph streetgraph;
		
		private Result(MapSegment segment, StreetGraph streetgraph) {
			this.segment = segment;
			this.streetgraph = streetgraph;
		}
	}
	
	
	private Parser parser;
	private Config config;
	private DataSetExtractor extractor;
	
	private OSMParser(Parser parser, Config config, DataSetExtractor extractor) {
		this.parser = parser;
		this.config = config;
		this.extractor = extractor;
	}
	
	public static OSMParser create(Config config) {
		// create parser
		OSMProcessor processor = new OSMProcessor(config.genindexBefore, config.genindexStreetGraph);
		Parser parser = new Parser(processor);
		
		// add features
		FeatureSystem featuresys = parser.getFeatureSystem();
		if (config.streetgraph != null) featuresys.putFeature(config.streetgraph);
		featuresys.putFeatures(config.features.values());
		
		// add initializers
		parser.getNodeEntityManager().getInitializerMap().putAll(config.nodeInitializers);
		parser.getWayEntityManager().getInitializerMap().putAll(config.wayInitializers);
		parser.getRelationManager().putFactories(config.relationInitializers);
		
		// add extractor for bounds
		DataSetExtractor extractor = new DataSetExtractor();
		FeatureDefinition extractordef = new FeatureDefinition(
				"microtrafficsim.core.parser.OSMParser.BoundsExtractor",
				Integer.MIN_VALUE,		// assure this is generated before everything else
				extractor,
				(Node n) -> false,
				(Way w) -> false);
		
		featuresys.putFeature(extractordef);
		
		return new OSMParser(parser, new Config(config), extractor);
	}
	
	public Result parse(File file) throws XMLStreamException, IOException {
		parser.parse(file);
		
		// get feature set
		HashMap<String, Feature<?>> featureset = config.features.values().stream()
				.map(f -> f.getGenerator().getGeneratedFeatures())
				.collect(Collector.of(HashMap::new, HashMap::putAll, (l, r) -> {l.putAll(r); return l;}));

		// get StreetGraph
		StreetGraph streetgraph = null;
		if (config.streetgraph != null)
			streetgraph = config.streetgraph.getGenerator().getStreetGraph();
		
		return new Result(new MapSegment(extractor.bounds, featureset), streetgraph);
	}
	
	
	private static class DataSetExtractor implements FeatureGenerator {
		
		public Bounds bounds;

		@Override
		public void execute(DataSet dataset, FeatureDefinition feature) {
			this.bounds = new Bounds(dataset.bounds);
		}
	}
}
