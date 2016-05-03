package microtrafficsim.osm.parser.features;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Predicate;

import microtrafficsim.osm.parser.ecs.Component;
import microtrafficsim.osm.primitives.Node;
import microtrafficsim.osm.primitives.Way;


/**
 * Provides basic information to categorize OpenStreetMap primitives as a
 * Feature and abstract, process and generate these primitives.
 * 
 * @author Maximilian Luz
 */
public class FeatureDefinition {
	
	private String name;
	private int genindex;
	private FeatureGenerator generator;
	private Predicate<Node> nodeMatcher;
	private Predicate<Way> wayMatcher;
	private Set<Class<? extends Component>> nodeComponents;
	private Set<Class<? extends Component>> wayComponents;
	
	
	/**
	 * Creates a new {@code FeatureDefinition} with the provided parameters.
	 * {@code genindex} specifies when this feature should be generated (in
	 * relation to other features), this may depend on the data-{@code
	 * Processor} used.
	 * 
	 * <p>
	 * This call is the same as 
	 * {@link
	 * 	FeatureDefinition#FeatureDefinition(String, int, FeatureGenerator,
	 * 	Predicate, Predicate, Set, Set)
	 * 	FeatureDefinition(name, genindex, generator, nodeMatcher, wayMatcher, null, null)
	 * }
	 * </p>
	 * 
	 * @param name			the (unique) name of the {@code Feature}.
	 * @param genindex		the generator-index of the {@code Feature}.
	 * @param generator		the generator to generate the {@code FeaturePrimitive}s of this {@code Feature}.
	 * @param nodeMatcher	a predicate to specify which nodes belong to this {@code Feature}.
	 * @param wayMatcher	a predicate to specify which ways belong to this {@code Feature}.
	 */
	public FeatureDefinition(String name, int genindex, FeatureGenerator generator,
			Predicate<Node> nodeMatcher, Predicate<Way> wayMatcher) {
		this(name, genindex, generator, nodeMatcher, wayMatcher, null, null);
	}
	
	/**
	 * Creates a new {@code FeatureDefinition} with the provided parameters.
	 * {@code genindex} specifies when this feature should be generated (in
	 * relation to other features), this may depend on the data-{@code
	 * Processor} used.
	 * 
	 * @param name			the (unique) name of the {@code Feature}.
	 * @param genindex		the generator-index of the {@code Feature}.
	 * @param generator		the generator to generate the {@code FeaturePrimitive}s of this {@code Feature}.
	 * @param nodeMatcher	a predicate to specify which nodes belong to this {@code Feature}.
	 * @param wayMatcher	a predicate to specify which ways belong to this {@code Feature}.
	 * @param nodeComponents	the type of {@code NodeEntity}'s {@code Component}s to be initialized
	 * 							besides the ones specified by the generator.
	 * @param wayComponents		the type of {@code WayEntity}'s {@code Component}s to be initialized
	 * 							besides the ones specified by the generator.
	 */
	public FeatureDefinition(String name, int genindex, FeatureGenerator generator,
			Predicate<Node> nodeMatcher, Predicate<Way> wayMatcher,
			Set<Class<? extends Component>> nodeComponents,
			Set<Class<? extends Component>> wayComponents) {
		
		this.name = name;
		this.generator = generator;
		this.genindex = genindex;
		this.nodeMatcher = nodeMatcher;
		this.wayMatcher = wayMatcher;
		
		this.nodeComponents = new HashSet<>(generator.getRequiredNodeComponents());
		this.wayComponents = new HashSet<>(generator.getRequiredWayComponents());
		
		if (nodeComponents != null)
			this.nodeComponents.addAll(nodeComponents);
		
		if (wayComponents != null)
			this.wayComponents.addAll(wayComponents);
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
	 * Returns the generator-index of the Feature described by this definition.
	 * 
	 * @return the generator-index of this FeatureDefinition.
	 */
	public int getGeneratorIndex() {
		return genindex;
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
	 * @param n	the Node to test.
	 * @return true if the node matches this feature-definition, false otherwise.
	 */
	public boolean matches(Node n) {
		return nodeMatcher.test(n);
	}
	
	/**
	 * Tests if the given Way matches this definition.
	 * 
	 * @param w	the Way to test.
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
	
	
	/**
	 * Tests whether the given set of FeatureDefinitions has one or more features
	 * with the specified generator-index.
	 * 
	 * @param features	the feature-definitions in which to look for the specified
	 * 					type.
	 * @param genindex	the generator-index to look for.
	 * @return true if {@code features} contains a definition with the specified
	 * generator-index.
	 */
	public static boolean hasGeneratorIndex(Collection<FeatureDefinition> features, int genindex) {
		boolean has = false;
		
		for (FeatureDefinition fd : features) {
			if (fd.getGeneratorIndex() == genindex) {
				has = true;
				break;
			}
		}
		
		return has;
	}
	
	/**
	 * Tests whether the given set of FeatureDefinitions has one or more features
	 * with a generator-index between the specified bounds (both inclusive).
	 * 
	 * @param features	the feature-definitions in which to look for the specified
	 * 					type.
	 * @param lower		the lower bounds in which the generator-index may lie (inclusive).
	 * @param upper		the upper bounds in which the generator-index may lie (inclusive).
	 * @return true if {@code features} contains a definition with a generator-index satisfying
	 * {@code lower <= genindex <= upper}
	 */
	public static boolean hasGeneratorIndex(Collection<FeatureDefinition> features, int lower, int upper) {
		boolean has = false;
		
		for (FeatureDefinition fd : features) {
			if (lower <= fd.genindex && fd.genindex <= upper) {
				has = true;
				break;
			}
		}
		
		return has;
	}
}
