package microtrafficsim.osm.parser.ecs.entities;

import microtrafficsim.osm.parser.ecs.Component;
import microtrafficsim.osm.parser.ecs.EntityManager;
import microtrafficsim.osm.parser.features.FeatureDefinition;
import microtrafficsim.osm.primitives.Way;
import microtrafficsim.utils.collections.ArrayUtils;

import java.util.HashSet;
import java.util.Set;


/**
 * Entity-manager for creating Way-Entities.
 *
 * @author Maximilian Luz
 */
public class WayEntityManager extends EntityManager<WayEntity, Way> {

    @Override
    public WayEntity create(Way source, Set<FeatureDefinition> features) {
        WayEntity entity = new WayEntity(source.id, ArrayUtils.toArray(source.nodes, null), features);

        HashSet<Class<? extends Component>> components = new HashSet<>();
        for (FeatureDefinition fd : features)
            components.addAll(fd.getWayComponents());

        this.initializeComponents(components, entity, source, features);
        return entity;
    }
}
