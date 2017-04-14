package microtrafficsim.core.exfmt;

import microtrafficsim.utils.collections.Composite;


// TODO (ExFmt):
// - StreetComponent
//      - functionality to transform runtime data (only visual at beginning) to ExFmt (EntitySet & FeatureSet) and back
//      - functionality to serialize ExFmt using kryo
// - extend with GraphEdgeComponent and GraphNodeComponent
// - extend with TileGridSet
// - extend with TileLayerSourceSet


/**
 * Container for the exchange-format.
 *
 * @author Maximilian Luz
 */
public class Container extends Composite<Container.Entry> {

    public Container.Entry set(Container.Entry entry) {
        return getAll().put(entry.getType(), entry);
    }


    /**
     * Base-class for Container Entries.
     */
    public static abstract class Entry {

        /**
         * Return the actual type of this {@code Entry} as {@code Class}. This function may be used to implement
         * polymorphism for Entries, i.e. to store an entry of different class (e.g. child-class) in the same slot of
         * the container. By default this returns the class of this entry using {@link Object#getClass()}. Note that
         * the following expression should always be satisfied: {@code container.get(x).getType() == x}.
         *
         * @return the type of this {@code Component}.
         */
        public Class<? extends Entry> getType() {
            return this.getClass();
        }
    }
}
