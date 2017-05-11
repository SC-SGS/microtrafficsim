package microtrafficsim.core.map.tiles;

import microtrafficsim.core.map.FeatureDescriptor;
import microtrafficsim.core.map.FeaturePrimitive;
import microtrafficsim.utils.collections.Grid;

import java.util.List;


public class FeatureGrid<T extends FeaturePrimitive> {
    private final String name;
    private final Class<T> type;
    private Grid<? extends List<T>> data;

    public FeatureGrid(String name, Class<T> type, Grid<? extends List<T>> data) {
        this.name = name;
        this.type = type;
        this.data = data;
    }

    public FeatureGrid(TileFeatureGrid<T> from) {
        this(from.getName(), from.getType(), from.getData());
    }


    /**
     * Returns the name of this feature.
     *
     * @return the name of this feature.
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the type of this feature.
     *
     * @return the type of this feature.
     */
    public Class<T> getType() {
        return type;
    }

    /**
     * Returns the descriptor describing this feature.
     *
     * @return the descriptor describing this feature.
     */
    public FeatureDescriptor getDescriptor() {
        return new FeatureDescriptor(name, type);
    }

    /**
     * Returns the data of this feature.
     *
     * @return the data of this feature.
     */
    public Grid<? extends List<T>> getData() {
        return this.data;
    }
}
