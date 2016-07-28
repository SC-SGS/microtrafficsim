package microtrafficsim.utils.collections;


import java.util.*;
import java.util.function.Supplier;


/**
 * An abstract multi-map base class, used to simplify the creation of multi-maps.
 *
 * @param <K> the key-type.
 * @param <V> the value-type.
 * @param <C> the type of the collection containing the actual values.
 * @author Maximilian Luz
 */
public class AbstractMultiMap<K, V, C extends Collection<V>> implements MultiMap<K, V, C> {

    private Map<K, C> map;
    private Supplier<C> constructor;


    /**
     * Creates a new {@code AbstractMultiMap} using the specified map as basis and the specified supplier to construct
     * the collections containing the actual values.
     *
     * @param map         the base-map.
     * @param constructor the supplier supplying new, empty collections that will be used to actually store the
     *                    elements.
     */
    public AbstractMultiMap(Map<K, C> map, Supplier<C> constructor) {
        this.map         = map;
        this.constructor = constructor;
    }

    /**
     * Copies the specified contents to a map that can be used as base-map for a new {@code AbstractMultiMap}.
     *
     * @param to       the map that can later be used as base-map.
     * @param from     the source from which the elements should be copied.
     * @param provider the supplier providing new empty collections that will be used to acutally store the elements.
     * @param <K>      the key-type.
     * @param <V>      the value-type.
     * @param <C> the type of the collection containing the actual values.
     * @return {@code to}
     */
    public static <K, V, C extends Collection<V>> Map<K, C> baseMap(
            Map<K, C> to,
            Map<? extends K, ? extends V> from,
            Supplier<? extends C> provider)
    {
        for (Map.Entry<? extends K, ? extends V> e : from.entrySet()) {
            C bucket = provider.get();
            bucket.add(e.getValue());

            to.put(e.getKey(), bucket);
        }

        return to;
    }

    /**
     * Copies the specified contents to a map that can be used as base-map for a new {@code AbstractMultiMap}.
     *
     * @param to       the map that can later be used as base-map.
     * @param from     the source from which the elements should be copied.
     * @param provider the supplier providing new empty collections that will be used to acutally store the elements.
     * @param <K>      the key-type.
     * @param <V>      the value-type.
     * @param <C> the type of the collection containing the actual values.
     * @return {@code to}
     */
    public static <K, V, C extends Collection<V>> Map<K, C> baseMap(
            Map<K, C> to,
            MultiMap<? extends K, ? extends V, ? extends Collection<V>> from,
            Supplier<? extends C> provider)
    {
        for (Map.Entry<? extends K, ? extends Collection<V>> e : from.entrySet()) {
            C bucket = provider.get();
            bucket.addAll(e.getValue());

            to.put(e.getKey(), bucket);
        }

        return to;
    }


    @Override
    public int size() {
        return map.size();
    }

    @Override
    public int count() {
        int count = 0;

        for (C c : map.values())
            count += c.size();

        return count;
    }

    @Override
    public int count(Object key) {
        C c = map.get(key);
        return c != null ? c.size() : 0;
    }

    @Override
    public boolean isEmpty() {
        return map.isEmpty();
    }

    @Override
    public boolean containsKey(Object key) {
        return map.containsKey(key);
    }

    @Override
    public boolean containsValue(Object value) {
        for (C c : map.values())
            if (c.contains(value)) return true;

        return false;
    }

    @Override
    public C get(Object key) {
        return map.get(key);
    }

    @Override
    public C put(K key, C value) {
        return map.put(key, value);
    }

    @Override
    public boolean add(K key, V value) {
        C c = map.get(key);

        if (c == null) {
            c = constructor.get();
            map.put(key, c);
        }

        return c.add(value);
    }

    @Override
    public C remove(Object key) {
        return map.remove(key);
    }

    @Override
    public boolean removeValue(V value) {
        boolean change = false;

        for (C c : map.values())
            change |= c.remove(value);

        return change;
    }

    @Override
    public void putAll(Map<? extends K, ? extends C> m) {
        map.putAll(m);
    }

    @Override
    public void putAll(MultiMap<? extends K, ? extends V, ? extends C> m) {
        for (Map.Entry<? extends K, ? extends C> e : m.entrySet()) {
            C bucket = this.map.get(e.getKey());

            if (bucket == null) {
                bucket = constructor.get();
                map.put(e.getKey(), bucket);
            }

            bucket.addAll(e.getValue());
        }
    }

    @Override
    public void clear() {
        map.clear();
    }

    @Override
    public Set<K> keySet() {
        return map.keySet();
    }

    @Override
    public Collection<C> values() {
        return map.values();
    }

    @Override
    public List<V> allValues() {
        ArrayList<V> list = new ArrayList<>();
        map.values().forEach(list::addAll);
        return list;
    }

    @Override
    public Set<Entry<K, C>> entrySet() {
        return map.entrySet();
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof AbstractMultiMap)) return false;

        AbstractMultiMap<?, ?, ?> other = (AbstractMultiMap<?, ?, ?>) obj;

        return this.map.equals(other.map);
    }

    @Override
    public int hashCode() {
        return map.hashCode();
    }
}
