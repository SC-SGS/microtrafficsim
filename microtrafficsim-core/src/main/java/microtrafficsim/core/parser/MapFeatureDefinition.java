package microtrafficsim.core.parser;

import java.util.Set;
import java.util.function.Predicate;

import microtrafficsim.core.map.FeaturePrimitive;
import microtrafficsim.core.map.features.info.LaneInfo;
import microtrafficsim.core.map.features.info.MaxspeedInfo;
import microtrafficsim.core.map.features.info.OnewayInfo;
import microtrafficsim.core.map.features.info.StreetType;
import microtrafficsim.osm.features.info.LaneInfoParser;
import microtrafficsim.osm.features.info.MaxspeedInfoParser;
import microtrafficsim.osm.features.info.OnewayInfoParser;
import microtrafficsim.osm.features.info.StreetTypeParser;
import microtrafficsim.osm.parser.ecs.Component;
import microtrafficsim.osm.parser.ecs.ComponentFactory;
import microtrafficsim.osm.parser.ecs.Entity;
import microtrafficsim.osm.parser.ecs.entities.WayEntity;
import microtrafficsim.osm.parser.features.FeatureDefinition;
import microtrafficsim.osm.parser.features.streets.StreetComponent;
import microtrafficsim.osm.primitives.Node;
import microtrafficsim.osm.primitives.Way;


public class MapFeatureDefinition<T extends FeaturePrimitive> extends FeatureDefinition {

	public MapFeatureDefinition(String name, int genindex, MapFeatureGenerator<T> generator,
			Predicate<Node> nodeMatcher, Predicate<Way> wayMatcher,
			Set<Class<? extends Component>> nodeComponents,
			Set<Class<? extends Component>> wayComponents) {
		
		super(name, genindex, generator, nodeMatcher, wayMatcher, nodeComponents, wayComponents);
	}
	
	public MapFeatureDefinition(String name, int genindex, MapFeatureGenerator<T> generator,
			Predicate<Node> nodeMatcher, Predicate<Way> wayMatcher) {
		
		super(name, genindex, generator, nodeMatcher, wayMatcher);
	}
	
	@SuppressWarnings("unchecked")
	public MapFeatureGenerator<T> getGenerator() {
		return (MapFeatureGenerator<T>) super.getGenerator();
	}

}
