package microtrafficsim.core.vis.map.tiles.layers;

import microtrafficsim.core.map.Bounds;
import microtrafficsim.core.map.FeaturePrimitive;
import microtrafficsim.core.map.TileFeatureProvider;
import microtrafficsim.core.map.layers.TileLayerSource;
import microtrafficsim.core.map.tiles.TileId;
import microtrafficsim.core.map.tiles.TilingScheme;
import microtrafficsim.core.vis.mesh.style.Style;
import microtrafficsim.math.Rect2d;

import java.util.ArrayList;


public class FeatureTileLayerSource implements TileLayerSource {

    private String feature;
    private Style style;
    private TileFeatureProvider provider;

    private ArrayList<LayerSourceChangeListener> listeners;
    private TileFeatureProvider.FeatureChangeListener featureListener;


    public FeatureTileLayerSource(String feature, Style style) {
        this(feature, style, null);
    }

    public FeatureTileLayerSource(String feature, Style style, TileFeatureProvider provider) {
        this.feature = feature;
        this.style = style;
        this.provider = provider;

        this.listeners = new ArrayList<>();
        this.featureListener = new FeatureChangeListenerImpl();

        if (provider != null)
            provider.addFeatureChangeListener(featureListener);
    }


    public void setFeatureProvider(TileFeatureProvider provider) {
        if (this.provider != null)
            this.provider.removeFeatureChangeListener(featureListener);

        if (provider != null)
            provider.addFeatureChangeListener(featureListener);

        this.provider = provider;

        for (LayerSourceChangeListener listener : listeners)
            listener.sourceChanged(this);
    }

    public TileFeatureProvider getFeatureProvider() {
        return provider;
    }

    public String getFeatureName() {
        return feature;
    }

    public Class<? extends FeaturePrimitive> getFeatureType() {
        return provider != null ? provider.getFeatureType(feature) : null;
    }

    public Style getStyle() {
        return style;
    }


    @Override
    public Class<? extends TileLayerSource> getType() {
        return FeatureTileLayerSource.class;
    }

    @Override
    public boolean isAvailable() {
        return provider != null && provider.hasFeature(feature);
    }


    @Override
    public TilingScheme getTilingScheme() {
        if (provider == null || !provider.hasFeature(feature))
            return null;

        return provider.getTilingScheme();
    }


    @Override
    public Bounds getBounds() {
        if (provider == null || !provider.hasFeature(feature))
            return null;

        return provider.getBounds();
    }

    @Override
    public Rect2d getProjectedBounds() {
        if (provider == null || !provider.hasFeature(feature))
            return null;

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


    private class FeatureChangeListenerImpl implements TileFeatureProvider.FeatureChangeListener {

        @Override
        public void featuresChanged() {
            for (LayerSourceChangeListener l : listeners)
                l.sourceChanged(FeatureTileLayerSource.this);
        }

        @Override
        public void featuresChanged(TileId tile) {
            for (LayerSourceChangeListener l : listeners)
                l.sourceChanged(FeatureTileLayerSource.this, tile);
        }

        @Override
        public void featureChanged(String name) {
            if (feature != null && feature.equals(name))
                for (LayerSourceChangeListener l : listeners)
                    l.sourceChanged(FeatureTileLayerSource.this);
        }

        @Override
        public void featureChanged(String name, TileId tile) {
            if (feature != null && feature.equals(name))
                for (LayerSourceChangeListener l : listeners)
                    l.sourceChanged(FeatureTileLayerSource.this, tile);
        }
    }
}
