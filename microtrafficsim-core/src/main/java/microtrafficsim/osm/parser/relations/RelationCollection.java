package microtrafficsim.osm.parser.relations;

import java.util.HashMap;
import java.util.Set;


/**
 * A specialized Collection to store Relations of various types by their ID and
 * group them together.
 * 
 * @author Maximilian Luz
 */
public class RelationCollection {
	
	private HashMap<Class<? extends RelationBase>, HashMap<Long, ? extends RelationBase>> relations;
	
	
	/**
	 * Create a new empty RelationCollection.
	 */
	public RelationCollection() {
		relations = new HashMap<>();
	}
	
	
	/**
	 * Add the specified relation to this collection and store it with its ID.
	 * 
	 * @param <T>		the type of the {@code RelationBase}.
	 * @param relation	the relation to add to this collection.
	 * @return the Relation previously associated with the given Relation's ID (or
	 * {@code null}).
	 */
	@SuppressWarnings("unchecked")
	public <T extends RelationBase> T add(T relation) {
		HashMap<Long, T> map = (HashMap<Long, T>) relations.get(relation.getType());
		
		if (map == null) {
			map = (HashMap<Long, T>) putEmptyCategory(relation.getType());
		}
		
		return map.put(relation.id, relation);
	}
	
	/**
	 * Return all Relations of the given type stored in this collection as Map (ID
	 * to Relation). Changes to the returned map are reflected in this collection.
	 * 
	 * @param <T>		the type of the {@code RelationBase}.
	 * @param type	the type of the Relations to return.
	 * @return the map containing all Relations of the specified type in this
	 * collection.
	 */
	@SuppressWarnings("unchecked")
	public <T extends RelationBase> HashMap<Long, T> getAll(Class<T> type) {
		HashMap<Long, T> map = (HashMap<Long, T>) relations.get(type);
		
		if (map == null) {
			map = new HashMap<>();
			relations.put(type, map);
		}	
		
		return map;
	}
	
	
	/**
	 * Returns the Map internally used to store the Relations by their type.
	 * Changes to the returned map are reflected in this collection.
	 * 
	 * @return the Map internally used to store the Relations by their type.
	 */
	public HashMap<Class<? extends RelationBase>, HashMap<Long, ? extends RelationBase>> getTypeMap() {
		return relations;
	}
	
	/**
	 * Returns a Set containing all Types of Relations this collection may store.
	 * 
	 * @return all Types of Relations this collection stores.
	 */
	public Set<Class<? extends RelationBase>> getRelationTypes() {
		return relations.keySet();
	}
	
	
	/**
	 * Set an empty ID-map in the internal type-map for the given type and return it.
	 * 
	 * @param <T>		the type of the {@code RelationBase}.
	 * @param type	the type on which the empty map should be set.
	 * @return the newly added map.
	 */
	private <T extends RelationBase> HashMap<Long, T> putEmptyCategory(Class<T> type) {
		HashMap<Long, T> map = new HashMap<>();
		relations.put(type, map);
		return map;
	}


	/**
	 * Returns the overall number of Relations stored in this Collection.
	 * 
	 * @return the number of Relations stored.
	 */
	public int size() {
		int size = 0;
		
		for (HashMap<Long, ? extends RelationBase> rmap : relations.values()) {
			size += rmap.size();
		}
		
		return size;
	}
}
