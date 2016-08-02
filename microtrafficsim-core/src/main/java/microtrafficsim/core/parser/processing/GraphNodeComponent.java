package microtrafficsim.core.parser.processing;

import microtrafficsim.osm.parser.ecs.Component;
import microtrafficsim.osm.parser.ecs.Entity;
import microtrafficsim.osm.parser.ecs.entities.WayEntity;
import microtrafficsim.utils.collections.HashMultiSet;


/**
 * Component used in the {@code OSMProcessor} to keep track of all connected
 * WayEntities as well as determining intersections.
 *
 * @author Maximilian Luz
 */
public class GraphNodeComponent extends Component {
    public HashMultiSet<WayEntity> ways;

    /**
     * Constructs a new {@code GraphNodeComponent} for the given entity.
     *
     * @param entity the entity to construct this component for.
     */
    public GraphNodeComponent(Entity entity) {
        super(entity);

        this.ways = new HashMultiSet<>();
    }

    @Override
    public Class<? extends Component> getType() {
        return GraphNodeComponent.class;
    }

    @Override
    public GraphNodeComponent clone(Entity e) {
        GraphNodeComponent uc = new GraphNodeComponent(e);
        uc.ways               = new HashMultiSet<>(this.ways);
        return uc;
    }
}
