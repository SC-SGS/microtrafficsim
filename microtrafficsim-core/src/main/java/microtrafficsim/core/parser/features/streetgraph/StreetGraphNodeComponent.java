package microtrafficsim.core.parser.features.streetgraph;

import microtrafficsim.core.logic.Node;
import microtrafficsim.osm.parser.ecs.Component;
import microtrafficsim.osm.parser.ecs.Entity;


/**
 * Component for {@code NodeEntities} used in the {@code StreetGraphGenerator}
 * to associate a street-graph {@code Node} with the {@code NodeEntity}.
 *
 * @author Maximilian Luz
 */
public class StreetGraphNodeComponent extends Component {

    public Node node;

    public StreetGraphNodeComponent(Entity entity, Node node) {
        super(entity);
        this.node = node;
    }

    @Override
    public Class<? extends Component> getType() {
        return StreetGraphNodeComponent.class;
    }

    /**
     * Clones this component and all contained objects.
     * <p>
     * Note: only returns a shallow copy!
     *
     * @param e the {@code Entity} the new {@code Component} belongs to.
     * @return a clone of this {@code Component}.
     */
    @Override
    public StreetGraphNodeComponent clone(Entity e) {
        return new StreetGraphNodeComponent(e, node);
    }
}
