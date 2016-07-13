package microtrafficsim.osm.parser.ecs.entities;

import microtrafficsim.osm.parser.ecs.Component;
import microtrafficsim.osm.parser.ecs.EntityManager;
import microtrafficsim.osm.parser.features.FeatureDefinition;
import microtrafficsim.osm.primitives.Node;

import java.util.HashSet;
import java.util.Set;


/**
 * Entity-manager for creating Node-Entities.
 *
 * @author Maximilian Luz
 */
public class NodeEntityManager extends EntityManager<NodeEntity, Node> {

    @Override
    public NodeEntity create(Node source, Set<FeatureDefinition> features) {
        NodeEntity entity = new NodeEntity(source.id, source.lat, source.lon, features);

        HashSet<Class<? extends Component>> components = new HashSet<>();
        for (FeatureDefinition fd : features)
            components.addAll(fd.getNodeComponents());

        this.initializeComponents(components, entity, source, features);
        return entity;
    }
}
