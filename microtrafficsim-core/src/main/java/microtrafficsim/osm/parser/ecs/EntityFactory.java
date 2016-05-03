package microtrafficsim.osm.parser.ecs;

import java.util.Set;

import microtrafficsim.osm.parser.features.FeatureDefinition;


/**
 * A Factory for creating entities from source-elements and their
 * {@code FeatureDefinition}s.
 * 
 * @param <EntityT>	the type of the {@code Entity} to create.
 * @param <SourceT> the type of the source-element from which the {@code
 * 					Entity} should be created.
 * 
 * @author Maximilian Luz
 */
public interface EntityFactory<EntityT, SourceT> {
	EntityT create(SourceT source, Set<FeatureDefinition> features);
}
