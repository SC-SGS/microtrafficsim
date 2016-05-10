package microtrafficsim.osm.parser.ecs;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;


/**
 * Base-class for Entities of the parsers Entity-Component-System.
 * 
 * @author Maximilian Luz
 */
public class Entity implements Cloneable {
	
	protected HashMap<Class<? extends Component>, Component> components;
	
	
	/**
	 * Constructs a new Entity without any {@code Component}s.
	 */
	public Entity() {
		this.components = new HashMap<>();
	}
	

	/**
	 * Set the {@code Component} associated with the specified key to the
	 * specified {@code Component}.
	 * 
	 * @param <T>	the type of the {@code Component}.
	 * @param key		the key as the type (Class) of the {@code Component} to set.
	 * @param component the {@code Component} to add/set to this {@code Entity}
	 * @return the {@code Component} previously associated with the given key
	 * or {@code null} if no such {@code Component} exists.
	 */
	@SuppressWarnings("unchecked")
	public <T extends Component> T set(Class<T> key, T component) {
		return (T) components.put(key, component);
	}
	
	/**
	 * Get the {@code Component} associated with the given key.
	 * 
	 * @param <T>	the type of the {@code Component}.
	 * @param key	the key as the type (Class) of the {@code Component} to get.
	 * @return the {@code Component} associated with the given key.
	 */
	@SuppressWarnings("unchecked")
	public <T extends Component> T get(Class<T> key) {
		return (T) components.get(key);
	}
	
	/**
	 * Remove the {@code Component} associated with the given key.
	 * 
	 * @param <T>	the type of the {@code Component}.
	 * @param key	the key as the type (Class) of the {@code Component} to remove.
	 * @return the {@code Component} associated with the given key.
	 */
	@SuppressWarnings("unchecked")
	public <T extends Component> T remove(Class<T> key) {
		return (T) components.remove(key);
	}
	
	/**
	 * Return the {@code HashMap} internally used to store the {@code
	 * Components}. Any changes on this map will be reflected in this {@code
	 * Entity}.
	 * 
	 * @return the {@code HashMap} containing all {@code Components} of
	 * this {@code Entity}.
	 */
	public HashMap<Class<? extends Component>, Component> getAll() {
		return components;
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
		
		for (Component c : components.values()) {
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
		
		for (Component c : components.values()) {
			ways.addAll(c.getRequiredWays());
		}
		
		return ways;
	}
	
	
	/**
	 * Creates and returns a full copy of this {@code Entity} and all its
	 * {@code Components}.
	 */
	@Override
	public Entity clone() {
		Entity ce = new Entity();
		for (Component component : components.values()) {
			Component cc = component.clone(this);
			ce.components.put(cc.getType(), cc);
		}
		return ce;
	}
}
