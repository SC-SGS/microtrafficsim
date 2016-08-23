package microtrafficsim.core.map;

import microtrafficsim.core.map.tiles.TileRect;


/**
 * {@code FeaturePrimitive}-container for tiles.
 *
 * @param <T> the type of the feature.
 */
public class TileFeature<T extends FeaturePrimitive> {
    private final String   name;
    private final Class<T> type;
    private final TileRect bounds;
    private final T[]      data;

    /**
     * Constructs a ne {@code TileFeature}.
     *
     * @param name   the name of the feature.
     * @param type   the type of the feature.
     * @param bounds the bounds of the tile.
     * @param data   the feature-data.
     */
    public TileFeature(String name, Class<T> type, TileRect bounds, T[] data) {
        this.name   = name;
        this.type   = type;
        this.bounds = bounds;
        this.data   = data;
    }

    /**
     * Returns the name of the feature.
     *
     * @return the name of the feature.
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the type of the feature.
     *
     * @return the type of the feature.
     */
    public Class<T> getType() {
        return type;
    }

    /**
     * Returns the tiles the container stores the feature-data for.
     *
     * @return the tiles the container stores the feature-data for.
     */
    public TileRect getBounds() {
        return new TileRect(bounds);
    }

    /**
     * Returns the feature-data stored in this container.
     *
     * @return the feature-data stored in this container.
     */
    public T[] getData() {
        return data;
    }
}
