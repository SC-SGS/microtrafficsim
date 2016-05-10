package microtrafficsim.osm.parser.ecs;

import java.util.Set;

import microtrafficsim.osm.parser.features.FeatureDefinition;


/**
 * Interface for a Factory creating a {@code Component} from a specified
 * source-element.
 * 
 * @param <ComponentT>	the type of the {@code Component} to be created.
 * @param <SourceT>		the type of the source-element from which the component
 * 						should be created.
 * 
 * @author Maximilian Luz
 */
public interface ComponentFactory<ComponentT extends Component, SourceT> {
	
	/**
	 * Creates a component from the specified source-element and its set of
	 * matching {@code FeatureDefinition}s.
	 * 
	 * @param entity	the entity to which the created {@code Component}
	 * 					belongs.
	 * @param source	the source-element from which the {@code Component}
	 * 					should be created.
	 * @param features	the set of {@code FeatureDefinition}s for the
	 * 					source-element.
	 * @return a Component created from the specified source-element and its
	 * {@code FeatureDefinition}s.
	 */
	ComponentT create(Entity entity, SourceT source, Set<FeatureDefinition> features);
}
