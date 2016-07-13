package microtrafficsim.core.map;

import microtrafficsim.core.map.tiles.TileRect;


public class TileFeature<T extends FeaturePrimitive> {
    private final String name;
    private final Class<T> type;
    private final TileRect bounds;
    private final          T[] data;

    public TileFeature(String name, Class<T> type, TileRect bounds, T[] data) {
        this.name   = name;
        this.type   = type;
        this.bounds = bounds;
        this.data   = data;
    }

    public String getName() {
        return name;
    }
    public Class<T> getType() {
        return type;
    }

    public TileRect getBounds() {
        return new TileRect(bounds);
    }

    public T[] getData() {
        return data;
    }
}
