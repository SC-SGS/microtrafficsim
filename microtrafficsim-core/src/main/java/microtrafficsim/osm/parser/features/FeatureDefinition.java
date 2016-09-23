package microtrafficsim.osm.parser.features;

import microtrafficsim.osm.parser.ecs.Component;
import microtrafficsim.osm.primitives.Node;
import microtrafficsim.osm.primitives.Way;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Predicate;


/**
 * Provides basic information to categorize OpenStreetMap primitives as a
 * Feature and abstract, process and generate these primitives.
 *
 * @author Maximilian Luz
 */
public class FeatureDefinition {

    /**
     * Creates a new placeholder {@code FeatureDefinition} with the given name.
     *
     * @param name the name of the placeholder.
     * @return the created {@code FeatureDefinition}.
     */
    public static FeatureDefinition createDependencyPlaceholder(String name) {
        return createDependencyPlaceholder(name, new FeatureDependency());
    }

    /**
     * Creates a new placeholder {@code FeatureDefinition} with the given name and dependency.
     *
     * @param name       the name of the placeholder.
     * @param dependency the dependency-description for this placeholder.
     * @return the created {@code FeatureDefinition}.
     */
    public static FeatureDefinition createDependencyPlaceholder(String name, FeatureDependency dependency) {
        return new FeatureDefinition(name, dependency, null, null, null);
    }

    private String              name;
    private FeatureDependency   dependency;
    private FeatureGenerator    generator;
    private Predicate<Node>     nodeMatcher;
    private Predicate<Way>      wayMatcher;
    private Set<Class<? extends Component>> nodeComponents;
    private Set<Class<? extends Component>> wayComponents;

    /**
     * Creates a new {@code FeatureDefinition} with the provided parameters. {@code dependency} specifies the
     * dependencies of this feature in context of generation, i.e. when this feature should be generated in relation to
     * other features (this may depend on the data-{@code Processor} used).
     * <p>
     * This call is equivalent to
     * {@link
     * FeatureDefinition#FeatureDefinition(String, FeatureDependency, FeatureGenerator, Predicate, Predicate, Set, Set)
     * FeatureDefinition(name, dependency, generator, nodeMatcher, wayMatcher, null, null)
     * }
     *
     * @param name        the (unique) name of the {@code Feature}.
     * @param dependency  the dependencies of the {@code Feature}.
     * @param generator   the generator to generate the {@code FeaturePrimitive}s of this {@code Feature}.
     * @param nodeMatcher a predicate to specify which nodes belong to this {@code Feature}.
     * @param wayMatcher  a predicate to specify which ways belong to this {@code Feature}.
     */
    public FeatureDefinition(String name, FeatureDependency dependency, FeatureGenerator generator,
                             Predicate<Node> nodeMatcher, Predicate<Way> wayMatcher) {
        this(name, dependency, generator, nodeMatcher, wayMatcher, null, null);
    }

    /**
     * Creates a new {@code FeatureDefinition} with the provided parameters. {@code dependency} specifies the
     * dependencies of this feature in context of generation, i.e. when this feature should be generated in relation to
     * other features (this may depend on the data-{@code Processor} used).
     *
     * @param name           the (unique) name of the {@code Feature}.
     * @param dependency     the dependencies of the {@code Feature}.
     * @param generator      the generator to generate the {@code FeaturePrimitive}s of this {@code Feature}.
     * @param nodeMatcher    a predicate to specify which nodes belong to this {@code Feature}.
     * @param wayMatcher     a predicate to specify which ways belong to this {@code Feature}.
     * @param nodeComponents the type of {@code NodeEntity}'s {@code Component}s to be initialized
     *                       besides the ones specified by the generator.
     * @param wayComponents  the type of {@code WayEntity}'s {@code Component}s to be initialized
     *                       besides the ones specified by the generator.
     */
    public FeatureDefinition(String name, FeatureDependency dependency, FeatureGenerator generator,
                             Predicate<Node> nodeMatcher, Predicate<Way> wayMatcher,
                             Set<Class<? extends Component>> nodeComponents,
                             Set<Class<? extends Component>> wayComponents) {
        this.name        = name;
        this.generator   = generator;
        this.dependency  = dependency;
        this.nodeMatcher = nodeMatcher != null ? nodeMatcher : n -> false;
        this.wayMatcher  = wayMatcher != null ? wayMatcher : n -> false;

        if (generator != null) {
            this.nodeComponents = new HashSet<>(generator.getRequiredNodeComponents());
            this.wayComponents = new HashSet<>(generator.getRequiredWayComponents());
        }

        if (nodeComponents != null) this.nodeComponents.addAll(nodeComponents);
        if (wayComponents != null) this.wayComponents.addAll(wayComponents);
    }

    /**
     * Returns the name of the Feature described by this definition.
     *
     * @return the name of the described Feature.
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the dependencies of the Feature described by this definition.
     *
     * @return the dependencies of this FeatureDefinition.
     */
    public FeatureDependency getDependency() {
        return dependency;
    }

    /**
     * Returns the FeatureGenerator used to generate the FeaturePrimitives for the
     * described Feature.
     *
     * @return the generator used to generate the described Feature.
     */
    public FeatureGenerator getGenerator() {
        return generator;
    }

    /**
     * Tests if the given Node matches this definition.
     *
     * @param n the Node to test.
     * @return true if the node matches this feature-definition, false otherwise.
     */
    public boolean matches(Node n) {
        return nodeMatcher.test(n);
    }

    /**
     * Tests if the given Way matches this definition.
     *
     * @param w the Way to test.
     * @return true if the way matches this feature-definition, false otherwise.
     */
    public boolean matches(Way w) {
        return wayMatcher.test(w);
    }

    /**
     * Returns all component-types which should be initialized on any node that
     * matches this feature-definition in the data-abstraction phase.
     *
     * @return a set of component-types which should be initialized on any node
     * that matches this definition.
     */
    public Set<Class<? extends Component>> getNodeComponents() {
        return nodeComponents;
    }

    /**
     * Returns all component-types which should be initialized on any way that
     * matches this feature-definition in the data-abstraction phase.
     *
     * @return a set of component-types which should be initialized on any way
     * that matches this definition.
     */
    public Set<Class<? extends Component>> getWayComponents() {
        return wayComponents;
    }
}
