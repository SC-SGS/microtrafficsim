package microtrafficsim.core.vis.map.tiles.layers;

import microtrafficsim.core.map.Bounds;
import microtrafficsim.core.map.FeaturePrimitive;
import microtrafficsim.core.map.TileFeatureProvider;
import microtrafficsim.core.map.layers.LayerSource;
import microtrafficsim.core.map.tiles.TileId;
import microtrafficsim.core.map.tiles.TilingScheme;
import microtrafficsim.core.vis.mesh.style.Style;
import microtrafficsim.math.Rect2d;

import java.util.ArrayList;


/**
 * Source for a feature tile layer.
 *
 * @author Maximilian Luz
 */
public class FeatureTileLayerSource implements LayerSource {
    private String              feature;
    private Style               style;
    private TileFeatureProvider provider;
    private long                revision;

    private ArrayList<LayerSourceChangeListener>      listeners;
    private TileFeatureProvider.FeatureChangeListener featureListener;


    /**
     * Constructs a new {@code FeatureTileLayerSource} for the given feature with the given style.
     * This call is equal to
     * {@link
     *  FeatureTileLayerSource#FeatureTileLayerSource(String, Style, TileFeatureProvider)
     *  FeatureTileLayerSource(feature, style, null)
     * }
     *
     * @param feature the feature for which the source should be created.
     * @param style   the style which should be used for this source.
     */
    public FeatureTileLayerSource(String feature, Style style) {
        this(feature, style, null);
    }

    /**
     * Constructs a new {@code FeatureTileLayerSource} for the given feature, style and provider.
     *
     * @param feature  the feature for which the source should be created.
     * @param style    the style which should be used for this source.
     * @param provider the provider providing the feature.
     */
    public FeatureTileLayerSource(String feature, Style style, TileFeatureProvider provider) {
        this.feature  = feature;
        this.style    = style;
        this.provider = provider;
        this.revision = 0;

        this.listeners       = new ArrayList<>();
        this.featureListener = new FeatureChangeListenerImpl();

        if (provider != null) provider.addFeatureChangeListener(featureListener);
    }

    /**
     * Returns the feature-provider of this source.
     *
     * @return the feature-provider of this source.
     */
    public TileFeatureProvider getFeatureProvider() {
        return provider;
    }

    /**
     * Sets the feature-provider of this source.
     *
     * @param provider the new feature-provider of this source.
     */
    public void setFeatureProvider(TileFeatureProvider provider) {
        if (this.provider != null) this.provider.removeFeatureChangeListener(featureListener);

        if (provider != null) provider.addFeatureChangeListener(featureListener);

        this.provider = provider;
        this.revision++;

        for (LayerSourceChangeListener listener : listeners)
            listener.sourceChanged(this);
    }

    /**
     * Returns the feature-name of this source.
     *
     * @return the feature-name of this source.
     */
    public String getFeatureName() {
        return feature;
    }

    /**
     * Returns the type of feature provided by this source.
     *
     * @return the type of feature provided by this source.
     */
    public Class<? extends FeaturePrimitive> getFeatureType() {
        return provider != null ? provider.getFeatureType(feature) : null;
    }

    /**
     * Returns the style used for this source.
     *
     * @return the style used for this source.
     */
    public Style getStyle() {
        return style;
    }

    /**
     * Returns the revision of this source.
     *
     * @return the revision of this source.
     */
    public long getRevision() {
        return revision;
    }


    @Override
    public Class<? extends LayerSource> getType() {
        return FeatureTileLayerSource.class;
    }

    @Override
    public boolean isAvailable() {
        return provider != null && provider.hasFeature(feature);
    }


    @Override
    public TilingScheme getTilingScheme() {
        if (provider == null || !provider.hasFeature(feature)) return null;

        return provider.getTilingScheme();
    }


    @Override
    public Bounds getBounds() {
        if (provider == null || !provider.hasFeature(feature)) return null;

        return provider.getBounds();
    }

    @Override
    public Rect2d getProjectedBounds() {
        if (provider == null || !provider.hasFeature(feature)) return null;

        return provider.getProjectedBounds();
    }


    @Override
    public boolean addLayerSourceChangeListener(LayerSourceChangeListener listener) {
        return listeners.add(listener);
    }

    @Override
    public boolean removeLayerSourceChangeListener(LayerSourceChangeListener listener) {
        return listeners.remove(listener);
    }

    @Override
    public boolean hasLayerSourceChangeListener(LayerSourceChangeListener listener) {
        return listeners.contains(listener);
    }


    /**
     * Implementation of the feature-change-listener.
     */
    private class FeatureChangeListenerImpl implements TileFeatureProvider.FeatureChangeListener {

        @Override
        public void featuresChanged() {
            revision++;
            for (LayerSourceChangeListener l : listeners)
                l.sourceChanged(FeatureTileLayerSource.this);
        }

        @Override
        public void featuresChanged(TileId tile) {
            revision++;
            for (LayerSourceChangeListener l : listeners)
                l.sourceChanged(FeatureTileLayerSource.this, tile);
        }

        @Override
        public void featureChanged(String name) {
            revision++;
            if (feature != null && feature.equals(name))
                for (LayerSourceChangeListener l : listeners)
                    l.sourceChanged(FeatureTileLayerSource.this);
        }

        @Override
        public void featureChanged(String name, TileId tile) {
            revision++;
            if (feature != null && feature.equals(name))
                for (LayerSourceChangeListener l : listeners)
                    l.sourceChanged(FeatureTileLayerSource.this, tile);
        }
    }
}
