package microtrafficsim.utils.collections;

import java.util.Collection;
import java.util.List;
import java.util.Map;


public interface MultiMap<K, V, C extends Collection<V>> extends Map<K, C> {
    // TODO: documentation

    int count();
    int count(Object key);

    boolean add(K key, V value);
    boolean removeValue(V value);

    void putAll(MultiMap<? extends K, ? extends V, ? extends C> m);
    List<V> allValues();
}
