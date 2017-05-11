package microtrafficsim.core.map.tiles;

import microtrafficsim.core.map.FeatureDescriptor;
import microtrafficsim.core.map.FeaturePrimitive;
import microtrafficsim.utils.collections.Grid;

import java.util.List;


public class TileFeatureGrid<T extends FeaturePrimitive> {
    private final String name;
    private final Class<T> type;

    private TilingScheme scheme;
    private TileRect level;

    private Grid<? extends List<T>> data;


    public TileFeatureGrid(String name, Class<T> type, TilingScheme scheme, TileRect level, Grid<? extends List<T>> data) {
        this.name = name;
        this.type = type;
        this.scheme = scheme;
        this.level = level;
        this.data = data;
    }

    public TileFeatureGrid(FeatureGrid<T> from, TilingScheme scheme, TileRect layer) {
        this(from.getName(), from.getType(), scheme, layer, from.getData());
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
     * Returns the tiling-scheme used for this grid;
     *
     * @return the tiling-scheme used for this grid;
     */
    public TilingScheme getTilingScheme() {
        return scheme;
    }

    /**
     * Returns the TileRectangle describing on which level and in which tiles the data is stored, in respect to the
     * TilingScheme used..
     *
     * @return the TileRectangle describing on which level and in which tiles the data is stored.
     */
    public TileRect getLevel() {
        return level;
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
