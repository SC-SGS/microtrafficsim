package microtrafficsim.core.vis.map.tiles;

import microtrafficsim.core.map.Bounds;
import microtrafficsim.core.map.style.StyleSheet;
import microtrafficsim.core.map.tiles.TileId;
import microtrafficsim.core.map.tiles.TilingScheme;
import microtrafficsim.core.vis.context.RenderContext;
import microtrafficsim.core.vis.map.projections.Projection;
import microtrafficsim.math.Rect2d;

import java.util.concurrent.ExecutionException;


/**
 * Provider for {@code Tiles}. Manages tile creation and disposal.
 *
 * @author Maximilian Luz
 */
public interface TileProvider {

    /**
     * Change-listener for the {@code TileProvider}.
     */
    interface TileChangeListener {

        /**
         * Notified when all tiles have changed.
         */
        void tilesChanged();

        /**
         * Notified when a single tile has changed.
         *
         * @param tile the id of the changed tile.
         */
        void tileChanged(TileId tile);
    }


    /**
     * Returns the (un-projected) bounds of the provided data.
     *
     * @return the (un-projected) bounds of the provided data.
     */
    Bounds getBounds();

    /**
     * Returns the (projected) bounds of the provided data.
     *
     * @return the (projected) bounds of the provided data.
     */
    Rect2d getProjectedBounds();

    /**
     * Returns the projection with which the provider provides the tiles.
     *
     * @return the projection with which the provider provides the tiles.
     */
    Projection   getProjection();

    /**
     * Returns the tiling-scheme with which the provider provides the tiles.
     *
     * @return the tiling-scheme with which the provider provides the tiles.
     */
    TilingScheme getTilingScheme();

    /**
     * Initializes this provider.
     *
     * @param context the context on which this provider should be initialized.
     */
    void initialize(RenderContext context);

    /**
     * Disposes this provider.
     *
     * @param context the context on which this provider has been initialized.
     */
    void dispose(RenderContext context);

    /**
     * Prepares and returns the required tile.
     *
     * @param context the context on which the tile should be loaded (if necessary).
     * @param tile    the tile that should be returned.
     * @return the provided tile.
     * @throws InterruptedException if the loading thread has been interrupted.
     * @throws ExecutionException   if any exception occurs during the load-operation.
     */
    Tile require(RenderContext context, TileId tile) throws InterruptedException, ExecutionException;

    /**
     * Releases the provided tile.
     *
     * @param context the context on which the tile has been loaded.
     * @param tile    the that should be released.
     */
    void release(RenderContext context, Tile tile);

    /**
     * Callback-function to set up OpenGL state before rendering tiles provided by this provider.
     *
     * @param context the context on which the tiles should be rendered.
     */
    void beforeRendering(RenderContext context);

    /**
     * Callback-function to tear down OpenGL state after rendering tiles provided by this provider.
     *
     * @param context the context on which the tiles has been rendered.
     */
    void afterRendering(RenderContext context);

    /**
     * Applies the given style-sheet to this provider.
     *
     * @param style the style-sheet to apply.
     */
    void apply(StyleSheet style);

    /**
     * Adds the given {@code TileChangeListener} to the set of change-listeners.
     *
     * @param listener the listener to add.
     * @return {@code true} if the underlying collection of change-listeners changed.
     */
    boolean addTileChangeListener(TileChangeListener listener);

    /**
     * Removes the given {@code TileChangeListener} from the set of change-listeners.
     *
     * @param listener the listener to remove.
     * @return {@code true} if the underlying collection of change-listeners changed.
     */
    boolean removeTileChangeListener(TileChangeListener listener);

    /**
     * Checks if this provider contains the given {@code TileChangeListener}.
     *
     * @param listener the listener to check for.
     * @return {@code true} if this provider contains the specified {@code TileChangeListener}, {@code false}
     * otherwise.
     */
    boolean hasTileChangeListener(TileChangeListener listener);
}
