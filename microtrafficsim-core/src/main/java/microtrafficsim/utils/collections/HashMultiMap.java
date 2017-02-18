package microtrafficsim.utils.collections;

import java.util.*;

/**
 * A fully hash-based multimap. Using a HashMap as map and HashSets as collections.
 *
 * @param <K> the key-type.
 * @param <V> the value-type.
 * @author Maximilian Luz
 */
public class HashMultiMap<K, V> extends AbstractMultiMap<K, V, HashSet<V>> {

    /**
     * Creates a new, empty {@code HashMultiMap} with the default initial capacity and load-factor of {@link HashMap}.
     */
    public HashMultiMap() {
        super(new HashMap<>(), HashSet<V>::new);
    }

    /**
     * Creates a new, empty {@code HashMultiMap} with the specified initial capacity and the default load-factor of
     * {@link HashMap}.
     *
     * @param initialCapacity the initial capacity of the constructed multi-map.
     */
    public HashMultiMap(int initialCapacity) {
        super(new HashMap<>(initialCapacity), HashSet<V>::new);
    }

    /**
     * Creates a new, empty {@code HashMultiMap} with the specified initial capacity and load-factor.
     *
     * @param initialCapacity the initial capacity of the constructed multi-map.
     * @param loadFactor      the load-factor of the constructed multi-map.
     */
    public HashMultiMap(int initialCapacity, float loadFactor) {
        super(new HashMap<>(initialCapacity, loadFactor), HashSet<V>::new);
    }

    /**
     * Creates a new {@code HashMultiMap} containing the contents of the specified map. Uses the default initial
     * capacity and load-factor of {@link HashMap}.
     *
     * @param map the map from which the contents should be copied.
     */
    public HashMultiMap(Map<? extends K, ? extends V> map) {
        super(AbstractMultiMap.baseMap(new HashMap<>(), map, HashSet<V>::new), HashSet<V>::new);
    }

    /**
     * Creates a new {@code HashMultiMap}, copying the contents of the specifed multi-map. Uses the default initial
     * capacity and load-factor of {@link HashMap}.
     *
     * @param map the multi-map from which the contents should be copied.
     */
    public HashMultiMap(MultiMap<? extends K, ? extends V, ? extends Collection<V>> map) {
        super(AbstractMultiMap.baseMap(new HashMap<K, HashSet<V>>(), map, HashSet<V>::new), HashSet<V>::new);
    }
}
