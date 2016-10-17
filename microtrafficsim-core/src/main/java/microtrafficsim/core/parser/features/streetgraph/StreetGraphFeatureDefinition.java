package microtrafficsim.core.parser.features.streetgraph;

import microtrafficsim.osm.parser.ecs.Component;
import microtrafficsim.osm.parser.features.FeatureDefinition;
import microtrafficsim.osm.parser.features.FeatureDependency;
import microtrafficsim.osm.primitives.Node;
import microtrafficsim.osm.primitives.Way;

import java.util.Set;
import java.util.function.Predicate;


/**
 * Feature definition for the street-graph. Defines what belongs
 * to the street-graph and how it is generated.
 *
 * @author Maximilian Luz
 */
public class StreetGraphFeatureDefinition extends FeatureDefinition {

    /**
     * Creates a new {@code StreetGraphFeatureDefinition} with the given properties.
     *
     * @param name           the name of the feature/street-graph.
     * @param dependency     the dependencies of the feature/street-graph.
     * @param generator      the generator for the street-graph.
     * @param nodeMatcher    the predicate to select the nodes that belong to the street-graph.
     * @param wayMatcher     the predicate to select the ways that belong to the street-graph.
     * @param nodeComponents the type of {@code NodeEntity}'s {@code Component}s to be initialized
     *                       besides the ones specified by the generator.
     * @param wayComponents  the type of {@code WayEntity}'s {@code Component}s to be initialized
     *                       besides the ones specified by the generator.
     */
    public StreetGraphFeatureDefinition(String name, FeatureDependency dependency, StreetGraphGenerator generator,
                                        Predicate<Node> nodeMatcher, Predicate<Way> wayMatcher,
                                        Set<Class<? extends Component>> nodeComponents,
                                        Set<Class<? extends Component>> wayComponents) {

        super(name, dependency, generator, nodeMatcher, wayMatcher, nodeComponents, wayComponents);
    }

    /**
     * Creates a new {@code StreetGraphFeatureDefinition} with the given properties.
     *
     * @param name           the name of the feature/street-graph.
     * @param dependency     the dependencies of the feature/street-graph.
     * @param generator      the generator for the street-graph.
     * @param nodeMatcher    the predicate to select the nodes that belong to the street-graph.
     * @param wayMatcher     the predicate to select the ways that belong to the street-graph.
     */
    public StreetGraphFeatureDefinition(String name, FeatureDependency dependency, StreetGraphGenerator generator,
                                        Predicate<Node> nodeMatcher, Predicate<Way> wayMatcher) {

        super(name, dependency, generator, nodeMatcher, wayMatcher);
    }

    @Override
    public StreetGraphGenerator getGenerator() {
        return (StreetGraphGenerator) super.getGenerator();
    }
}
