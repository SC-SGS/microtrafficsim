package microtrafficsim.osm.parser.ecs;

import microtrafficsim.utils.collections.Composite;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;


/**
 * Base-class for Entities of the parsers Entity-Component-System.
 *
 * @author Maximilian Luz
 */
public class Entity extends Composite<Component> implements Cloneable {

    /**
     * Constructs a new Entity without any {@code Component}s.
     */
    public Entity() {
        super();
    }


    /**
     * Set the {@code Component} associated with the specified key to the
     * specified {@code Component}. Note that the following expression should
     * always be satisfied: {@code entity.get(x).getType() == x}.
     *
     * @param <T>       the type of the {@code Component}.
     * @param key       the key as the type (Class) of the {@code Component} to set.
     * @param component the {@code Component} to add/set to this {@code Entity}
     * @return the {@code Component} previously associated with the given key
     * or {@code null} if no such {@code Component} exists.
     */
    @Override
    public <T extends Component> T set(Class<T> key, T component) {
        return super.set(key, component);
    }

    /**
     * Get the {@code Component} associated with the given key.
     *
     * @param <T> the type of the {@code Component}.
     * @param key the key as the type (Class) of the {@code Component} to get.
     * @return the {@code Component} associated with the given key.
     */
    @Override
    public <T extends Component> T get(Class<T> key) {
        return super.get(key);
    }

    /**
     * Remove the {@code Component} associated with the given key.
     *
     * @param <T> the type of the {@code Component}.
     * @param key the key as the type (Class) of the {@code Component} to remove.
     * @return the {@code Component} associated with the given key.
     */
    @Override
    public <T extends Component> T remove(Class<T> key) {
        return super.remove(key);
    }

    /**
     * Return the {@code Map} internally used to store the {@code
     * Components}. Any changes to this map will be reflected in this {@code
     * Entity}. Note that the following expression should * always be satisfied:
     * {@code entity.get(x).getType() == x}.
     *
     * @return the {@code Map} containing all {@code Components} of
     * this {@code Entity}.
     */
    @Override
    public Map<Class<? extends Component>, Component> getAll() {
        return super.getAll();
    }


    /**
     * Return long-references to nodes-elements required by this {@code Entity}
     * and all its {@code Components}.
     *
     * @return a {@code Collection} of references to nodes required by this
     * {@code Entity}.
     */
    public Collection<Long> getRequiredNodes() {
        Collection<Long> nodes = new HashSet<>();

        for (Component c : getAll().values()) {
            nodes.addAll(c.getRequiredNodes());
        }

        return nodes;
    }

    /**
     * Return long-references to ways-elements required by this {@code Entity}
     * and all its {@code Components}.
     *
     * @return a {@code Collection} of references to ways required by this
     * {@code Entity}.
     */
    public Collection<Long> getRequiredWays() {
        Collection<Long> ways = new HashSet<>();

        for (Component c : getAll().values())
            ways.addAll(c.getRequiredWays());

        return ways;
    }


    /**
     * Creates and returns a full copy of this {@code Entity} and all its
     * {@code Components}.
     */
    @Override
    public Entity clone() {
        Entity ce = new Entity();
        for (Component component : getAll().values()) {
            Component cc = component.clone(this);
            ce.getAll().put(cc.getType(), cc);
        }
        return ce;
    }
}
