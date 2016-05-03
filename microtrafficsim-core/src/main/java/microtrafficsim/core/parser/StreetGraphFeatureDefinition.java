package microtrafficsim.core.parser;

import java.util.Set;
import java.util.function.Predicate;

import microtrafficsim.osm.parser.ecs.Component;
import microtrafficsim.osm.parser.features.FeatureDefinition;
import microtrafficsim.osm.primitives.Node;
import microtrafficsim.osm.primitives.Way;


public class StreetGraphFeatureDefinition extends FeatureDefinition {

	public StreetGraphFeatureDefinition(String name, int genindex,
			StreetGraphGenerator generator,
			Predicate<Node> nodeMatcher, Predicate<Way> wayMatcher,
			Set<Class<? extends Component>> nodeComponents,
			Set<Class<? extends Component>> wayComponents) {
		
		super(name, genindex, generator, nodeMatcher, wayMatcher, nodeComponents, wayComponents);
	}
	
	public StreetGraphFeatureDefinition(String name, int genindex,
			StreetGraphGenerator generator,
			Predicate<Node> nodeMatcher, Predicate<Way> wayMatcher) {
		
		super(name, genindex, generator, nodeMatcher, wayMatcher);
	}
	
	@Override
	public StreetGraphGenerator getGenerator() {
		return (StreetGraphGenerator) super.getGenerator();
	}
}
