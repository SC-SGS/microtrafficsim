package microtrafficsim.utils.collections;

import java.util.Collection;
import java.util.List;
import java.util.Map;


/**
 * A multi-map that can be used to associate a key with a collection of values.
 *
 * @param <K> the key-type.
 * @param <V> the value-type.
 * @param <C> the type of the collection containing the actual values.
 * @author Maximilian Luz
 */
public interface MultiMap<K, V, C extends Collection<V>> extends Map<K, C> {

    /**
     * Counts the total number of value-elements stored in this map.
     *
     * @return the total number of elements in this map.
     */
    int count();

    /**
     * Returns the number of value-elements associated with the specified key.
     *
     * @param key the key for which the number of elements should be returned.
     * @return the number of elements associated with the specified key.
     */
    int count(Object key);

    /**
     * Adds the given value to the collection associated with the specified key.
     *
     * @param key   the key for which the value should be added.
     * @param value the value which should be associated with the key.
     * @return {@code true} if this collection changed as a result of this call.
     * @see Collection#add(Object)
     */
    boolean add(K key, V value);

    /**
     * Removes a single instance of the given value from this collection.
     *
     * @param value the value to be removed.
     * @return {@code true} if this collection changed as a result of this call.
     * @see Collection#remove(Object)
     */
    boolean removeValue(V value);

    /**
     * Puts all elements in the given multi-map into this multi-map.
     *
     * @param m the multi-map from which the elements should be taken.
     */
    void putAll(MultiMap<? extends K, ? extends V, ? extends C> m);

    /**
     * Returns a list containing all values of this collection.
     *
     * @return all values contained in this collection.
     */
    List<V> allValues();
}
