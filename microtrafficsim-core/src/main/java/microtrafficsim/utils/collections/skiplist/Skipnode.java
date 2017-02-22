package microtrafficsim.utils.collections.skiplist;

import microtrafficsim.utils.strings.builder.LevelStringBuilder;

/**
 * @author Dominic Parga Cacheiro
 */
class Skipnode<E> {

    public final E        value;
    public final Tower<E> tower;


    public Skipnode(E value) {
        this.value = value;
        tower      = new Tower<>();
    }


    @Override
    public String toString() {
        LevelStringBuilder builder = new LevelStringBuilder();
        builder.appendln("<skipnode>");
        builder.incLevel();

        builder.appendln("value:");
        builder.appendln(value == null ? "null" : value.toString());
        builder.appendln(tower);

        builder.decLevel();
        builder.append("</skipnode>");
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