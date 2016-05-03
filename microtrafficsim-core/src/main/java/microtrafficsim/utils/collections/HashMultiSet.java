package microtrafficsim.utils.collections;

import java.util.Collection;
import java.util.HashMap;


/**
 * MultiSet Implementation based on a HashMap.
 * 
 * @param <E>	the type of elements stored in this MultiSet.
 * @author Maximilian Luz
 */
public class HashMultiSet<E> extends AbstractMapBasedMultiSet<E> {

	/**
	 * Creates a new empty {@code HashMultiSet}.
	 */
	public HashMultiSet() {
		super(new HashMap<>());
	}
	
	/**
	 * Creates a new empty {@code HashMultiSet} with the given initial capacity.
	 * 
	 * @param initialCapacity	the initial capacity.
	 */
	public HashMultiSet(int initialCapacity) {
		super(new HashMap<>(initialCapacity));
	}
	
	/**
	 * Creates a new {@code HashMultiSet} initialized with the given collection.
	 * 
	 * @param c	the Collection used to initialize this MultiSet.
	 */
	public HashMultiSet(Collection<? extends E> c) {
		super(new HashMap<>(), c);
	}
}
