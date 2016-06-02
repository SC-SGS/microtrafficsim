package microtrafficsim.core.vis.map.tiles.layers;

import microtrafficsim.core.map.layers.TileLayerSource;
import microtrafficsim.core.map.tiles.TileId;


public class Layer {

    private final String name;
    private int index;
    private int minzoom;
    private int maxzoom;
    private TileLayerSource source;
    private boolean enabled;


    public Layer(String name, int index, int minzoom, int maxzoom, TileLayerSource source) {
        this.name = name;
        this.index = index;
        this.minzoom = minzoom;
        this.maxzoom = maxzoom;
        this.source = source;
        this.enabled = true;
    }


    public String getName() {
        return this.name;
    }


    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }


    public TileLayerSource getSource() {
        return source;
    }


    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }


    public int getMaximumZoomLevel() {
        return maxzoom;
    }

    public int getMinimumZoomLevel() {
        return minzoom;
    }


    public boolean isAvailableFor(TileId tile) {
        return minzoom <= tile.z && tile.z <= maxzoom;
    }
}

