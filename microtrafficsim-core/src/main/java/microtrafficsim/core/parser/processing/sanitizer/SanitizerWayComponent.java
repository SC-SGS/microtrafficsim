package microtrafficsim.core.parser.processing.sanitizer;

import microtrafficsim.osm.parser.ecs.Component;
import microtrafficsim.osm.parser.ecs.Entity;
import microtrafficsim.osm.parser.ecs.entities.WayEntity;
import microtrafficsim.utils.hashing.FNVHashBuilder;


/**
 * The component used to store information necessary to sanitize the {@code
 * WayEntities}.
 *
 * @author Maximilian Luz
 */
public class SanitizerWayComponent extends Component {

    public String highway;

    /**
     * Creates a new {@code SanitizerWayComponent} with {@code highway} as the
     * highway type of the {@code WayEntity}.
     *
     * @param entity  the entity for which this component is created.
     * @param highway the highway-type of the associated {@code WayEntity}.
     */
    public SanitizerWayComponent(WayEntity entity, String highway) {
        super(entity);
        this.highway = highway;
    }

    @Override
    public Class<? extends Component> getType() {
        return SanitizerWayComponent.class;
    }

    /**
     * Clones this component and all contained objects.
     * <p>
     * Note: a SanitizerWayComponent can only be cloned for a WayEntity
     * </p>
     *
     * @param e the {@code Entity} the new {@code Component} belongs to.
     * @return a clone of this {@code Component}.
     */
    @Override
    public Component clone(Entity e) {
        return new SanitizerWayComponent((WayEntity) e, this.highway);
    }


    @Override
    public int hashCode() {
        return new FNVHashBuilder().add(highway).getHash();
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof SanitizerWayComponent)) return false;

        SanitizerWayComponent other = (SanitizerWayComponent) obj;
        return this.highway.equals(other.highway);
    }
}
