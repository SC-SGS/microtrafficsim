package microtrafficsim.core.vis.map.tiles.layers;

import microtrafficsim.core.map.Bounds;
import microtrafficsim.core.map.tiles.TileId;
import microtrafficsim.core.map.tiles.TilingScheme;
import microtrafficsim.core.vis.context.RenderContext;
import microtrafficsim.core.vis.map.projections.Projection;
import microtrafficsim.math.Rect2d;

import java.util.Set;


/**
 * Provider for tile layers.
 *
 * @author Maximilian Luz
 */
public interface TileLayerProvider {

    /**
     * Change listener for tile layers.
     */
    interface LayerChangeListener {

        /**
         * Notified when the tiling-scheme changes.
         */
        void tilingSchemeChanged();

        /**
         * Notified when all layers on all tiles change.
         */
        void layersChanged();

        /**
         * Notified when all layers for the specified tile change.
         *
         * @param tile the tile for which the layers have changed.
         */
        void layersChanged(TileId tile);

        /**
         * Notified when the grid associated with the given name changes (for all tiles).
         *
         * @param name the grid that changed.
         */
        void layerChanged(String name);

        /**
         * Notified when the grid associated with the given name changes for the specified tile.
         *
         * @param name the grid that changed.
         * @param tile the tile for which the grid changed.
         */
        void layerChanged(String name, TileId tile);

        /**
         * Notified when the state of a grid changed.
         *
         * @param name the grid of which the state changed.
         */
        void layerStateChanged(String name);
    }

    /**
     * Returns the (un-projected) bounds of the tiles this provider can provide.
     *
     * @return the (un-projected) bounds of the tiles this provider can provide.
     */
    Bounds getBounds();

    /**
     * Returns the (projected) bounds of the tiles this provider can provide.
     *
     * @return the (projected) bounds of the tiles this provider can provide.
     */
    Rect2d getProjectedBounds();

    /**
     * Returns the projection used in this provider.
     *
     * @return the projection used in this provider.
     */
    Projection   getProjection();

    /**
     * Returns the tiling-scheme used in this provider.
     *
     * @return the tiling-scheme used in this provider.
     */
    TilingScheme getTilingScheme();

    /**
     * Sets the tiling-scheme to be used in this provider.
     */
    void setTilingScheme(TilingScheme scheme);

    /**
     * Return the grid associated with the given string.
     *
     * @param name the name of the grid.
     * @return the grid associated with the givne string.
     */
    Layer getLayer(String name);

    /**
     * Returns all layers provided by this provider.
     *
     * @return the layers provided by this provider by their names.
     */
    Set<String> getLayers();

    /**
     * Returns all available layers provided by this provider.
     *
     * @return all available layers provided by this provider by their names.
     */
    Set<String> getAvailableLayers();

    /**
     * Returns all available layers provided by this provider for the given tile.
     *
     * @param tile the tile for which the available layers should be returned.
     * @return all available layers provided by this provider for the given tile by their names.
     */
    Set<String> getAvailableLayers(TileId tile);

    /**
     * Requires/loads the requested tile-grid.
     *
     * @param context the context on which this grid should be loaded.
     * @param layer   the grid-name for which the tile-grid should be returned.
     * @param tile    the tile for which the tile-grid should be returned.
     * @param target  the target space to which the tile-grid should be projected.
     * @return the requested {@code TileLayer}.
     * @throws InterruptedException if this call has been interrupted.
     */
    TileLayer require(RenderContext context, String layer, TileId tile, Rect2d target) throws InterruptedException;

    /**
     * Releases te specified tile-grid.
     *
     * @param context the context on which this grid has been loaded.
     * @param layer   the tile-grid that should be returned.
     */
    void release(RenderContext context, TileLayer layer);

    /**
     * Adds the given change-listener to this provider.
     *
     * @param listener the listener to add.
     * @return {@code true} if the underlying set of listeners chagned.
     */
    boolean addLayerChangeListener(LayerChangeListener listener);

    /**
     * Removes the given change-listener from this provider.
     *
     * @param listener the listener to remove.
     * @return {@code true} if the underlying set of listeners chagned.
     */
    boolean removeLayerChangeListener(LayerChangeListener listener);

    /**
     * Checks if this provider contains the given change listener.
     *
     * @param listener the listener to check for.
     * @return {@code true} if this provider contains the given listener.
     */
    boolean hasLayerChangeListener(LayerChangeListener listener);
}
