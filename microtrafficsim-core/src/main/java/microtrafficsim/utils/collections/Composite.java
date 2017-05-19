package microtrafficsim.utils.collections;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;


/**
 * A composite class containing multiple objects in a map-like way, stored by their type.
 *
 * @param <Base> the base type of all stored objects.
 * @author Maximilian Luz
 */
public class Composite<Base> {
    private Map<Class<? extends Base>, Base> content = new HashMap<>();


    /**
     * Associate the given class with the given object.
     *
     * @param key   the class used as key.
     * @param entry the entry to store, must be of type {@code T} or a child-type.
     * @param <T>   the type described by the key.
     * @return the object previously associated with the given key or {@code null} if no such object exists.
     */
    @SuppressWarnings("unchecked")
    public <T extends Base> T set(Class<T> key, T entry) {
        return (T) content.put(key, entry);
    }

    /**
     * Return the object associated with the given class.
     *
     * @param key the class used as key.
     * @param <T> the type described by the key.
     * @return the object associated with the given key or {@code null} if no such object exists.
     */
    @SuppressWarnings("unchecked")
    public <T extends Base> T get(Class<T> key) {
        return (T) content.get(key);
    }

    /**
     * Return the object associated with the given class. If no such object exists invoke the supplier and associate
     * the computed object with the given class.
     *
     * @param key      the class for which the associated object should be returned.
     * @param fallback the supplier providing a fallback-object to be associated with the given key if no previous
     *                 association exists.
     * @param <T>      the type described by the given key, with which the object should be associated.
     * @return the object associated with the given key, if no such object exists prior to the call to this method,
     * the object returned by the provided supplier will be associated with the given key and returned.
     */
    @SuppressWarnings("unchecked")
    public <T extends Base> T get(Class<T> key, Supplier<? extends T> fallback) {
        return (T) content.computeIfAbsent(key, (x) -> fallback.get());
    }

    /**
     * Return the object associated with the given class or the fallback object returned by the supplier, if no object
     * is associated with the given key.
     *
     * @param key the class used as key.
     * @param <T> the type described by the key.
     * @return the object associated with the given key or the result of {@code fallback.get()} if no such object
     * exists.
     */
    @SuppressWarnings("unchecked")
    public <T extends Base> T getOr(Class<T> key, Supplier<? extends T> fallback) {
        T stored = (T) content.get(key);
        return stored != null ? stored : fallback.get();
    }

    /**
     * Remove and return the object associated with the given class.
     *
     * @param key the class used as key.
     * @param <T> the type described by the key.
     * @return the object associated with the given key or {@code null} if no such object exists.
     */
    @SuppressWarnings("unchecked")
    public <T extends Base> T remove(Class<T> key) {
        return (T) content.remove(key);
    }

    /**
     * Checks if this composite contains an object associated with the given key.
     *
     * @param key the key to check for.
     * @return {@code true} iff this composite contains an object associated with the given key.
     */
    public boolean contains(Class<? extends Base> key) {
        return content.containsKey(key);
    }


    /**
     * Return the {@code Map} internally used to store the objects. Any changes to this map will be reflected in this
     * {@code Composite}.
     *
     * @return the {@code Map} containing all objects of this {@code Composite}.
     */
    public Map<Class<? extends Base>, Base> getAll() {
        return content;
    }
}
