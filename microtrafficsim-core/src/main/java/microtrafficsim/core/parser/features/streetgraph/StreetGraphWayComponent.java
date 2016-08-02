package microtrafficsim.core.parser.features.streetgraph;

import microtrafficsim.core.logic.DirectedEdge;
import microtrafficsim.osm.parser.ecs.Component;
import microtrafficsim.osm.parser.ecs.Entity;


/**
 * Component for {@code WayEntities} used in the {@code StreetGraphGenerator} to
 * associate the {@code DirectedEdges} of a way with a {@code WayEntity}.
 *
 * @author Maximilian Luz
 */
public class StreetGraphWayComponent extends Component {

    public DirectedEdge forward;
    public DirectedEdge backward;

    public StreetGraphWayComponent(Entity entity, DirectedEdge forward, DirectedEdge backward) {
        super(entity);
        this.forward  = forward;
        this.backward = backward;
    }


    @Override
    public Class<? extends Component> getType() {
        return StreetGraphWayComponent.class;
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
    public StreetGraphWayComponent clone(Entity e) {
        return new StreetGraphWayComponent(e, forward, backward);
    }
}
