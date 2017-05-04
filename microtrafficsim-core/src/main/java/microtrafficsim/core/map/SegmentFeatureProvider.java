package microtrafficsim.core.map;

import java.util.Map;
import java.util.Set;


/**
 * Provider for the segment of a map.
 *
 * @author Maximilian Luz
 */
public interface SegmentFeatureProvider extends MapProvider {

    /**
     * Returns the bounds of the segment provided by this provider.
     *
     * @return the bounds of the segment provided by this provider.
     */
    Bounds getBounds();

    /**
     * Returns the type of the feature associated with the given name.
     *
     * @param name the name of the feature for which the type should be returned.
     * @return the type of the feature associated with the given name.
     */
    Class<? extends FeaturePrimitive> getFeatureType(String name);

    /**
     * Returns and requires the requested feature.
     *
     * @param name the name of the feature to return.
     * @param <T>  the type of the feature.
     * @return the feature associated with the given name
     * @throws InterruptedException if this function has been interrupted.
     */
    <T extends FeaturePrimitive> Feature<T> require(String name) throws InterruptedException;

    /**
     * Releases the given feature, indicating that the feature is no longer beeing used.
     *
     * @param feature the feature to release.
     */
    void release(Feature<?> feature);

    /**
     * Releases all features that have been requested from this context.
     */
    void releaseAll();

    /**
     * Returns all available features available on this provider.
     *
     * @return all available features available on this provider.
     */
    Set<String> getAvailableFeatures();

    /**
     * Returns all features available on this provider.
     *
     * @return all features available on this provider.
     * @throws InterruptedException if this function has been interrupted.
     */
    Map<String, Feature<?>> getFeatures() throws InterruptedException;

    /**
     * Checks if this provider provides a feature associated with the given name.
     *
     * @param name the name to check for.
     * @return {@code true} if this provider provides a feature associated with the given name, {@code false}
     * otherwise.
     */
    boolean hasFeature(String name);


    /**
     * Adds the given change listener to this provider.
     *
     * @param listener the listener to add.
     * @return {@code true} if the underlying collection of listeners changed.
     */
    boolean addFeatureChangeListener(FeatureChangeListener listener);

    /**
     * Removes the given change listener from this provider.
     *
     * @param listener the listener to remove.
     * @return {@code true} if the underlying collection of listeners changed.
     */
    boolean removeFeatureChangeListener(FeatureChangeListener listener);

    /**
     * Checks if this provider contains the given change listener.
     *
     * @param listener the listener to check for.
     * @return {@code true} if this provider contains the given listener.
     */
    boolean hasFeatureChangeListener(FeatureChangeListener listener);

    /**
     * Change listeners for segment feature providers.
     */
    interface FeatureChangeListener {

        /**
         * Notified when all features have changed.
         */
        void featuresChanged();

        /**
         * Notified when a single feature changed.
         *
         * @param name the name of the feature.
         */
        void featureChanged(String name);
    }
}
