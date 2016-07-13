package microtrafficsim.core.map;

import java.util.*;


public class MapSegment implements SegmentFeatureProvider {

    private Bounds bounds;
    private Map<String, Feature<?>> featureset;
    private List<FeatureChangeListener> listeners;

    public MapSegment(Bounds bounds, Map<String, Feature<?>> featureset) {
        this.bounds     = bounds;
        this.featureset = featureset;
        this.listeners  = new ArrayList<>();
    }


    @Override
    public Bounds getBounds() {
        return bounds;
    }


    @Override
    public Class<? extends FeaturePrimitive> getFeatureType(String name) {
        Feature<?> feature = featureset.get(name);
        return feature != null ? feature.getType() : null;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends FeaturePrimitive> Feature<T> require(String name) {
        return (Feature<T>) featureset.get(name);
    }

    @Override
    public void release(Feature<?> feature) {}

    @Override
    public void releaseAll() {}

    @Override
    public Map<String, Feature<?>> getFeatures() {
        return Collections.unmodifiableMap(featureset);
    }


    @Override
    public Set<String> getAvailableFeatures() {
        return Collections.unmodifiableSet(featureset.keySet());
    }

    @Override
    public boolean hasFeature(String name) {
        return featureset.containsKey(name);
    }


    @Override
    public boolean addFeatureChangeListener(FeatureChangeListener listener) {
        return listeners.add(listener);
    }

    @Override
    public boolean removeFeatureChangeListener(FeatureChangeListener listener) {
        return listeners.remove(listener);
    }

    @Override
    public boolean hasFeatureChangeListener(FeatureChangeListener listener) {
        return listeners.contains(listener);
    }
}
