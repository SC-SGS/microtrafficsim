package microtrafficsim.core.exfmt;

import microtrafficsim.utils.collections.Composite;

/**
 * Describes how entries are stored/loaded. In contrary, {@link Container} describes, what is stored/loaded.
 *
 * @author Maximilian Luz
 */
public class Config extends Composite<Config.Entry> {

    public Config.Entry set(Config.Entry entry) {
        return getAll().put(entry.getType(), entry);
    }


    /**
     * Base-class for Config Entries.
     */
    public static abstract class Entry {

        /**
         * Return the actual type of this {@code Entry} as {@code Class}. This function may be used to implement
         * polymorphism for Entries, i.e. to store an entry of different class (e.g. child-class) in the same slot of
         * the container. By default this returns the class of this entry using {@link Object#getClass()}. Note that
         * the following expression must always be satisfied: {@code config.get(x).getType() == x}.
         *
         * @return the type of this {@code Component}.
         */
        public Class<? extends Entry> getType() {
            return this.getClass();
        }
    }
}
