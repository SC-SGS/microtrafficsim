package microtrafficsim.core.exfmt.base;

import microtrafficsim.core.exfmt.Container;
import microtrafficsim.core.map.FeatureDescriptor;

import java.util.HashMap;
import java.util.Map;


// contains info for map-features
public class FeatureInfo extends Container.Entry {
    private Map<String, FeatureDescriptor> features = new HashMap<>();


    public FeatureDescriptor set(FeatureDescriptor feature) {
        return this.features.put(feature.getName(), feature);
    }

    public FeatureDescriptor get(String name) {
        return this.features.get(name);
    }

    public FeatureDescriptor remove(String name) {
        return this.features.remove(name);
    }

    public Map<String, FeatureDescriptor> getAll() {
        return this.features;
    }
}
