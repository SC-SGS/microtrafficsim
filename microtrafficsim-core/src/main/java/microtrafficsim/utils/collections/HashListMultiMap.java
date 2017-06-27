package microtrafficsim.utils.collections;

import java.util.*;

/**
 * A fully hash-based multimap. Using a HashMap as map and HashSets as collections.
 *
 * @param <K> the key-type.
 * @param <V> the value-type.
 * @author Maximilian Luz
 */
public class HashListMultiMap<K, V> extends AbstractMultiMap<K, V, ArrayList<V>> {

    /**
     * Creates a new, empty {@code HashMultiMap} with the default initial capacity and load-factor of {@link HashMap}.
     */
    public HashListMultiMap() {
        super(new HashMap<>(), ArrayList<V>::new);
    }

    /**
     * Creates a new, empty {@code HashMultiMap} with the specified initial capacity and the default load-factor of
     * {@link HashMap}.
     *
     * @param initialCapacity the initial capacity of the constructed multi-map.
     */
    public HashListMultiMap(int initialCapacity) {
        super(new HashMap<>(initialCapacity), ArrayList<V>::new);
    }

    /**
     * Creates a new, empty {@code HashMultiMap} with the specified initial capacity and load-factor.
     *
     * @param initialCapacity the initial capacity of the constructed multi-map.
     * @param loadFactor      the load-factor of the constructed multi-map.
     */
    public HashListMultiMap(int initialCapacity, float loadFactor) {
        super(new HashMap<>(initialCapacity, loadFactor), ArrayList<V>::new);
    }

    /**
     * Creates a new {@code HashMultiMap} containing the contents of the specified map. Uses the default initial
     * capacity and load-factor of {@link HashMap}.
     *
     * @param map the map from which the contents should be copied.
     */
    public HashListMultiMap(Map<? extends K, ? extends V> map) {
        super(AbstractMultiMap.baseMap(new HashMap<>(), map, ArrayList<V>::new), ArrayList<V>::new);
    }

    /**
     * Creates a new {@code HashMultiMap}, copying the contents of the specifed multi-map. Uses the default initial
     * capacity and load-factor of {@link HashMap}.
     *
     * @param map the multi-map from which the contents should be copied.
     */
    public HashListMultiMap(MultiMap<? extends K, ? extends V, ? extends Collection<V>> map) {
        super(AbstractMultiMap.baseMap(new HashMap<K, ArrayList<V>>(), map, ArrayList<V>::new), ArrayList<V>::new);
    }
}
