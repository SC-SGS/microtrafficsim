package microtrafficsim.core.vis.map.segments;

import microtrafficsim.core.map.Bounds;
import microtrafficsim.core.map.FeaturePrimitive;
import microtrafficsim.core.map.SegmentFeatureProvider;
import microtrafficsim.core.map.layers.LayerSource;
import microtrafficsim.core.vis.mesh.style.Style;

import java.util.ArrayList;


public class FeatureSegmentLayerSource implements LayerSource {

    private String                 feature;
    private Style                  style;
    private SegmentFeatureProvider provider;
    private long                   revision;

    private ArrayList<LayerSourceChangeListener>         listeners;
    private SegmentFeatureProvider.FeatureChangeListener featureListener;


    public FeatureSegmentLayerSource(String feature, Style style) {
        this(feature, style, null);
    }

    public FeatureSegmentLayerSource(String feature, Style style, SegmentFeatureProvider provider) {
        this.feature  = feature;
        this.style    = style;
        this.provider = provider;
        this.revision = 0;

        this.listeners       = new ArrayList<>();
        this.featureListener = new FeatureChangeListenerImpl();

        if (provider != null) provider.addFeatureChangeListener(featureListener);
    }

    public SegmentFeatureProvider getFeatureProvider() {
        return provider;
    }

    public void setFeatureProvider(SegmentFeatureProvider provider) {
        if (this.provider != null)
            this.provider.removeFeatureChangeListener(featureListener);

        if (provider != null)
            provider.addFeatureChangeListener(featureListener);

        this.provider = provider;
        this.revision++;

        for (LayerSourceChangeListener cl : listeners)
            cl.sourceChanged(this);
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

    public long getRevision() {
        return revision;
    }


    @Override
    public boolean isAvailable() {
        return provider != null && provider.hasFeature(feature);
    }

    @Override
    public Bounds getBounds() {
        if (provider == null || !provider.hasFeature(feature)) return null;

        return provider.getBounds();
    }


    @Override
    public Class<? extends LayerSource> getType() {
        return FeatureSegmentLayerSource.class;
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


    private class FeatureChangeListenerImpl implements SegmentFeatureProvider.FeatureChangeListener {

        @Override
        public void featuresChanged() {
            revision++;
            for (LayerSourceChangeListener l : listeners)
                l.sourceChanged(FeatureSegmentLayerSource.this);
        }

        @Override
        public void featureChanged(String name) {
            revision++;
            if (feature != null && feature.equals(name))
                for (LayerSourceChangeListener l : listeners)
                    l.sourceChanged(FeatureSegmentLayerSource.this);
        }
    }
}
