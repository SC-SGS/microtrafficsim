package microtrafficsim.utils.collections.skiplist;

import microtrafficsim.utils.strings.builder.BasicStringBuilder;
import microtrafficsim.utils.strings.builder.StringBuilder;

/**
 * @author Dominic Parga Cacheiro
 */
class Skipnode<T> {

    public final T        value;
    public final Tower<T> tower;


    public Skipnode(T value) {
        this.value = value;
        tower      = new Tower<>();
    }


    @Override
    public String toString() {
        StringBuilder builder = new BasicStringBuilder();

        builder.append(tower + " || value = " + value);

        return builder.toString();
    }

    /**
     * This method could be needed for the default comparator in a skiplist using hashcode (e.g. {@link PrioritySkipList}).
     *
     * @return hashcode of the stored value
     */
    @Override
    public int hashCode() {
        return value.hashCode();
    }
}