package microtrafficsim.osm.parser.relations;

import java.util.HashMap;
import java.util.Map;

import microtrafficsim.osm.primitives.Relation;


/**
 * Relation-Manager for abstracting and creating RelationBase objects from
 * OpenStreetMap Relation primitives.
 * 
 * @author Maximilian Luz
 */
public class RelationManager implements RelationFactory {

	private HashMap<String, RelationFactory> factories;
	
	/**
	 * Create a new Relation manager without any RelationFactories.
	 */
	public RelationManager() {
		this.factories = new HashMap<>();
	}

	
	/**
	 * Set the RelationFactory for the specified relation-type.
	 * 
	 * @param type		the OpenStreetMap type-value of the Relation.
	 * @param factory	the Factory used to create the RelationBase.
	 * @return the RelationFactory previously associated with the given type if
	 * such exists, otherwise {@code null}.
	 */
	public RelationFactory putFactory(String type, RelationFactory factory) {
		return factories.put(type, factory);
	}

	/**
	 * Add all RelationFactories from the given Map. Essentially performs
	 * {@linkplain RelationManager#putFactory(String, RelationFactory)}
	 * for each entry on the given map.
	 * 
	 * @param factories the {@code RelationFactories} to add.
	 */
	public void putFactories(Map<String, ? extends RelationFactory> factories) {
		for (Map.Entry<String, ? extends RelationFactory> e : factories.entrySet())
			this.factories.put(e.getKey(), e.getValue());
	}
	
	/**
	 * Returns the RelationFactory associated with the specified relation-type.
	 * 
	 * @param type	the OpenStreetMap type-value for which the RelationFactory
	 * 				should be returned.
	 * @return the RelationFactory associated with the given type if such exists,
	 * otherwise {@code null}.
	 */
	public RelationFactory getFactory(String type) {
		return factories.get(type);
	}
	
	/**
	 * Removes the RelationFactory associated with the specified relation-type.
	 * 
	 * @param type	the OpenStreetMap type-value for which the RelationFactory
	 * 				should be removed.
	 * @return the RelationFactory associated with the given type if such exists,
	 * otherwise {@code null}.
	 */
	public RelationFactory removeFactory(String type) {
		return factories.remove(type);
	}
	
	/**
	 * Tests whether there exists a RelationFactory associated with the given type.
	 * 
	 * @param type	the OpenStreetMap type-value for which the existens of a
	 * 				RelationFactory should be checked.
	 * @return true if the specified type has a RelationFactory associated with it,
	 * false otherwise.
	 */
	public boolean hasFactory(String type) {
		return factories.containsKey(type);
	}
	

	@Override
	public RelationBase create(Relation r) {
		RelationFactory factory = factories.get(r.tags.get("type"));
		
		if (factory != null)
			return factory.create(r);
		else
			return null;
	}
}
