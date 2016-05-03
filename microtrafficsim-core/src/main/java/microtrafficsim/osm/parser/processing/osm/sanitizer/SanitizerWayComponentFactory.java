package microtrafficsim.osm.parser.processing.osm.sanitizer;

import java.util.Set;

import microtrafficsim.osm.parser.ecs.ComponentFactory;
import microtrafficsim.osm.parser.ecs.Entity;
import microtrafficsim.osm.parser.ecs.entities.WayEntity;
import microtrafficsim.osm.parser.features.FeatureDefinition;
import microtrafficsim.osm.primitives.Way;


/**
 * A {@code ComponentFactory} to create {@code SanitizerWayComponents}.
 * 
 * @author Maximilian Luz
 */
public class SanitizerWayComponentFactory implements ComponentFactory<SanitizerWayComponent, Way> {

	/**
	 * Creates a component from the specified source-element and its set of
	 * matching {@code FeatureDefinition}s.
	 * 
	 * <p>
	 * Note: a SanitizerWayComponent can only be created for an entity of type WayEntity.
	 * </p>
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
	@Override
	public SanitizerWayComponent create(Entity entity, Way source, Set<FeatureDefinition> features) {
		return new SanitizerWayComponent((WayEntity) entity, source.tags.get("highway"));
	}
}
