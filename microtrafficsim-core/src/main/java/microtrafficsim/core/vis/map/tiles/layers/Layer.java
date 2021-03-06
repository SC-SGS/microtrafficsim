package microtrafficsim.core.vis.map.tiles.layers;

import microtrafficsim.core.map.layers.TileLayerSource;
import microtrafficsim.core.map.tiles.TileId;

import java.util.ArrayList;


/**
 * Layer to group visual objects of the same style.
 *
 * @author Maximilian Luz
 */
public class Layer {

    private final String                        name;
    private int                                 index;
    private int                                 minzoom;
    private int                                 maxzoom;
    private TileLayerSource source;
    private boolean                             enabled;
    private ArrayList<LayerStateChangeListener> listeners;

    /**
     * Constructs a new {@code Layer} with the given properties.
     *
     * @param name    the name of the layer.
     * @param index   the visual index of the layer.
     * @param minzoom the minimum zoom level at which the layer will be visible
     * @param maxzoom the maximum zoom level at which the layer will be visible
     * @param source  the source of the layer.
     */
    public Layer(String name, int index, int minzoom, int maxzoom, TileLayerSource source) {
        this.name      = name;
        this.index     = index;
        this.minzoom   = minzoom;
        this.maxzoom   = maxzoom;
        this.source    = source;
        this.enabled   = true;
        this.listeners = new ArrayList<>();
    }

    /**
     * Returns the name of this layer.
     *
     * @return the name of this layer.
     */
    public String getName() {
        return this.name;
    }

    /**
     * Returns the visual index of this layer.
     *
     * @return the visual index of this layer.
     */
    public int getIndex() {
        return index;
    }

    /**
     * Sets the visual index of this layer.
     *
     * @param index the new visual index of this layer.
     */
    public void setIndex(int index) {
        this.index = index;
    }

    /**
     * Returns the source of this layer.
     *
     * @return the source of this layer.
     */
    public TileLayerSource getSource() {
        return source;
    }

    /**
     * Checks if this layer is enabled.
     *
     * @return {@code true} if this layer is enabled.
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * Enables or disables this layer.
     *
     * @param enabled set to {@code true} to enable this layer, {@code false} to disable this layer.
     */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
        listeners.forEach(l -> l.layerStateChanged(name));
    }

    /**
     * Returns the maximum zoom level at which this layer is active.
     *
     * @return the maximum zoom level.
     */
    public int getMaximumZoomLevel() {
        return maxzoom;
    }

    /**
     * Returns the minimum zoom level at which this layer is active.
     *
     * @return the minimum zoom level.
     */
    public int getMinimumZoomLevel() {
        return minzoom;
    }

    /**
     * Checks if this layer is available for the given tile.
     *
     * @param tile the tile to check for availability.
     * @return {@code true} if this layer is available for the given tile.
     */
    public boolean isAvailableFor(TileId tile) {
        return minzoom <= tile.z && tile.z <= maxzoom;
    }


    /**
     * Adds the given change-listener to this layer.
     *
     * @param listener the listener to add.
     * @return {@code true} if the underlying collection of change-listeners changed.
     */
    public boolean addLayerStateChangeListener(LayerStateChangeListener listener) {
        return listeners.add(listener);
    }

    /**
     * Removes the given change-listener
     *
     * @param listener the listener to remove.
     * @return {@code true} if the underlying collection of change-listeners changed.
     */
    public boolean removeLayerStateChangeListener(LayerStateChangeListener listener) {
        return listeners.remove(listener);
    }


    /**
     * Change-listener for layer states.
     */
    public interface LayerStateChangeListener {

        /**
         * Notified when the state of a layer changed.
         *
         * @param name the name of the layer that has been changed.
         */
        void layerStateChanged(String name);
    }
}
