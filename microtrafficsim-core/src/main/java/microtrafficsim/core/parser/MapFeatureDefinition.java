package microtrafficsim.core.parser;

import microtrafficsim.core.map.FeaturePrimitive;
import microtrafficsim.osm.parser.ecs.Component;
import microtrafficsim.osm.parser.features.FeatureDefinition;
import microtrafficsim.osm.primitives.Node;
import microtrafficsim.osm.primitives.Way;

import java.util.Set;
import java.util.function.Predicate;


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
