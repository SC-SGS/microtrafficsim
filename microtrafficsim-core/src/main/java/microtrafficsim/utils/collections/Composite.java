package microtrafficsim.utils.collections;

import java.util.HashMap;
import java.util.Map;


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
     * Return the {@code Map} internally used to store the objects. Any changes to this map will be reflected in this
     * {@code Composite}.
     *
     * @return the {@code Map} containing all objects of this {@code Composite}.
     */
    public Map<Class<? extends Base>, Base> getAll() {
        return content;
    }
}
