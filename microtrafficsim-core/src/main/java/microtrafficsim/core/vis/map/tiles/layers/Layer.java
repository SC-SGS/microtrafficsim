package microtrafficsim.core.vis.map.tiles.layers;

import microtrafficsim.core.map.layers.TileLayerSource;
import microtrafficsim.core.map.tiles.TileId;

import java.util.ArrayList;


public class Layer {

    private final String                        name;
    private int                                 index;
    private int                                 minzoom;
    private int                                 maxzoom;
    private TileLayerSource                     source;
    private boolean                             enabled;
    private ArrayList<LayerStateChangeListener> listeners;

    public Layer(String name, int index, int minzoom, int maxzoom, TileLayerSource source) {
        this.name      = name;
        this.index     = index;
        this.minzoom   = minzoom;
        this.maxzoom   = maxzoom;
        this.source    = source;
        this.enabled   = true;
        this.listeners = new ArrayList<>();
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
        listeners.forEach(l -> l.layerStateChanged(name));
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

    public void addLayerStateChangeListener(LayerStateChangeListener listener) {
        listeners.add(listener);
    }

    public void removeLayerStateChangeListener(LayerStateChangeListener listener) {
        listeners.remove(listener);
    }

    public interface LayerStateChangeListener { void layerStateChanged(String name); }
}
