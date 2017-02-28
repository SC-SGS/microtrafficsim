package microtrafficsim.utils.collections;

import microtrafficsim.utils.collections.skiplist.SkipList;
import microtrafficsim.utils.hashing.FNVHashBuilder;

import java.util.Collection;

/**
 * Java has no triple...
 *
 * @author Dominic Parga Cacheiro
 */
public class Triple<T, U, V> {

    public final T obj0;
    public final U obj1;
    public final V obj2;

    public Triple(T obj0, U obj1, V obj2) {
        this.obj0 = obj0;
        this.obj1 = obj1;
        this.obj2 = obj2;
    }

    @Override
    public int hashCode() {
        return new FNVHashBuilder().add(obj0).add(obj1).add(obj2).getHash();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this)
            return true;

        if (!(obj instanceof Triple))
            return false;

        Triple<?, ?, ?> other = (Triple<?, ?, ?>) obj;
        return obj0.equals(other.obj0) && obj1.equals(other.obj1) && obj2.equals(other.obj2);
    }
}
