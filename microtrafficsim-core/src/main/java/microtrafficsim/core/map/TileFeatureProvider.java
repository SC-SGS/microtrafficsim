package microtrafficsim.core.map;

import microtrafficsim.core.map.tiles.TileId;
import microtrafficsim.core.map.tiles.TileRect;
import microtrafficsim.core.map.tiles.TilingScheme;
import microtrafficsim.math.Rect2d;

import java.util.Set;


/**
 * Provider for tile features.
 *
 * @author Maximilian Luz
 */
public interface TileFeatureProvider {

    /**
     * Returns the tiling-scheme this provider uses.
     *
     * @return the tiling-scheme this provider uses.
     */
    TilingScheme getTilingScheme();

    /**
     * Returns the (un-projected) bounds for which the provider supplies features.
     *
     * @return the (un-projected) bounds for which the provider supplies features.
     */
    Bounds getBounds();

    /**
     * Returns the (projected) bounds for which the provider supplies features.
     *
     * @return the (projected) bounds for which the provider supplies features.
     */
    Rect2d getProjectedBounds();

    /**
     * Returns the type of the feature associated with the given name.
     *
     * @param name the name of the feature for which the type should be returned.
     * @return the type of the feature associated with the given name or {@code null} if no such feature exists.
     */
    Class<? extends FeaturePrimitive> getFeatureType(String name);

    /**
     * Returns the bounds in tiles that this provider actually provides for the specified tile and feature.
     *
     * @param name the name of the feature.
     * @param tile the tile-id.
     * @return the bounds in tiles that this provider actually provides for the specified tile and feature.
     */
    TileRect getFeatureBounds(String name, TileId tile);

    /**
     * Returns the bounds in tiles that this provider actually provides for the specified tile-rectangle and feature.
     *
     * @param name   the name of the feature.
     * @param bounds the tile-rectangle.
     * @return the bounds in tiles that this provider actually provides for the specified tile-rectangle and feature.
     */
    TileRect getFeatureBounds(String name, TileRect bounds);


    /**
     * Requires/returns the requested feature for the given tile.
     *
     * @param name the name of the feature.
     * @param tile the tile for which the feature should be returned.
     * @param <T>  the type of the feature.
     * @return the feature associated with the given name for the given tile.
     * @throws InterruptedException if this function has been interrupted.
     */
    <T extends FeaturePrimitive> TileFeature<T> require(String name, TileId tile) throws InterruptedException;

    /**
     * Requires/returns the requested feature for the given tile-rectangle
     *
     * @param name   the name of the feature.
     * @param bounds the tile-rectangle for which the feature should be returned.
     * @param <T>    the type of the feature.
     * @return the feature associated with the given name for the given tile-rectangle.
     * @throws InterruptedException if this function has been interrupted.
     */
    <T extends FeaturePrimitive> TileFeature<T> require(String name, TileRect bounds) throws InterruptedException;

    /**
     * Releases the given feature, indicating that it is not needed any more.
     *
     * @param feature the feature to release.
     */
    void release(TileFeature<?> feature);


    /**
     * Returns the names of all available features.
     *
     * @return the names of all available features.
     */
    Set<String> getAvailableFeatures();

    /**
     * Checks if this provider provides a feature associated with the given name.
     *
     * @param name the name to check for.
     * @return {@code true} if this provider provides a feature associated with the given name, {@code false}
     * otherwise.
     */
    boolean hasFeature(String name);

    /**
     * Checks if this provider provides any feature for the given tile.
     *
     * @param x the x-component of the tile-id.
     * @param y the y-component of the tile-id.
     * @param z the z-component of the tile-id.
     * @return {@code true} if this provider provides any feature for the given tile, {@code false} otherwise.
     */
    boolean hasTile(int x, int y, int z);

    /**
     * Adds the given change-listener to this provider.
     *
     * @param listener the listener to add.
     * @return {@code true} if the underlying collection of change-listeners of this provider changes.
     */
    boolean addFeatureChangeListener(FeatureChangeListener listener);

    /**
     * Removes the given change-listener from this provider.
     *
     * @param listener the listener to remove.
     * @return {@code true} if the underlying collection of change-listeners of this provider changes.
     */
    boolean removeFeatureChangeListener(FeatureChangeListener listener);

    /**
     * Checks if this provider contains the given change-listener.
     *
     * @param listener the listener to check for.
     * @return {@code true} if this provider contains the given change-listener.
     */
    boolean hasFeatureChangeListener(FeatureChangeListener listener);


    /**
     * Change-listener for tile feature providers.
     */
    interface FeatureChangeListener {

        /**
         * Notified when all features of a provider have changed.
         */
        void featuresChanged();

        /**
         * Notified when all features for a single tile have changed.
         *
         * @param tile the tile for which the features have changed.
         */
        void featuresChanged(TileId tile);

        /**
         * Notified when a single feature has changed.
         *
         * @param name the name of changed feature.
         */
        void featureChanged(String name);

        /**
         * Notified when a single feature for a single tile has changed.
         *
         * @param name the name of the feature that has changed.
         * @param tile the tile for which the feature has chagned.
         */
        void featureChanged(String name, TileId tile);
    }
}
