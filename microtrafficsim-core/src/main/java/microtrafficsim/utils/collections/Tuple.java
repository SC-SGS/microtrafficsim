package microtrafficsim.utils.collections;

import microtrafficsim.utils.hashing.FNVHashBuilder;

/**
 * Java has no tuples..
 *
 * @author Dominic Parga Cacheiro
 */
public class Tuple<U, V> {

    public final U obj0;
    public final V obj1;

    public Tuple(U obj0, V obj1) {
        this.obj0 = obj0;
        this.obj1 = obj1;
    }

    @Override
    public int hashCode() {
        return new FNVHashBuilder().add(obj0).add(obj1).getHash();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this)
            return true;

        if (!(obj instanceof Triple))
            return false;

        Triple<?, ?, ?> other = (Triple<?, ?, ?>) obj;
        return obj0.equals(other.obj0) && obj1.equals(other.obj1);
    }
}
