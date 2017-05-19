package microtrafficsim.osm.parser.ecs;

import java.util.Collection;
import java.util.HashSet;


/**
 * Base-class for Components of the parsers Entity-Component-System.
 *
 * @author Maximilian Luz
 */
public abstract class Component implements Cloneable {

    protected Entity entity;


    public Component(Entity entity) {
        this.entity = entity;
    }

    /**
     * Gets the {@code Entity} this {@code Component} belongs to.
     *
     * @return the {@code Entity} this {@code Component} belongs to.
     */
    public Entity getEntity() {
        return entity;
    }

    /**
     * Sets the {@code Entity} this {@code Component} belongs to. This method does
     * not add the {@code Component} to this {@code Entity}.
     *
     * @param entity the {@code Entity} to which to set the internal reference
     *               to.
     */
    public void setEntity(Entity entity) {
        this.entity = entity;
    }

    /**
     * Return the actual type of this {@code Component} as {@code Class}. This function
     * may be used to implement polymorphism for Components, i.e. to store a component of
     * different class (e.g. child-class) in the same slot of the entity. By default this
     * returns the class of this component using {@link Object#getClass()}. Note that the
     * following expression should always be satisfied: {@code entity.get(x).getType() == x}.
     *
     * @return the type of this {@code Component}.
     */
    public Class<? extends Component> getType() {
        return this.getClass();
    };

    /**
     * Clones this component and all contained objects.
     *
     * @param e the {@code Entity} the new {@code Component} belongs to.
     * @return a clone of this {@code Component}.
     */
    public abstract Component clone(Entity e);

    /**
     * Clones this component and all contained objects, sets the {@code Entity}
     * of the newly created {@code Component} to {@code null}.
     */
    @Override
    public Component clone() {
        return clone(entity);
    }


    /**
     * Returns a set of references to nodes required as transitive dependencies
     * by this {@code Component}. This method should <em>never</em> return
     * {@code null}. {@code Component}-types requiring nodes as transitive
     * dependency should overwrite this method, to forward these dependencies
     * to the parser framework.
     *
     * @return a Collection of long-references to nodes required by this component.
     */
    public Collection<Long> getRequiredNodes() {
        return new HashSet<>();
    }

    /**
     * Returns a set of references to ways required as transitive dependencies
     * by this {@code Component}. This method should <em>never</em> return
     * {@code null}. {@code Component}-types requiring ways as transitive
     * dependency should overwrite this method, to forward these dependencies
     * to the parser framework.
     *
     * @return a Collection of long-references to ways required by this component.
     */
    public Collection<Long> getRequiredWays() {
        return new HashSet<>();
    }
}
