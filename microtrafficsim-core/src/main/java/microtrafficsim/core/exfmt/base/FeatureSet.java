package microtrafficsim.core.exfmt.base;

import microtrafficsim.core.exfmt.Container;
import microtrafficsim.core.exfmt.ecs.Entity;
import microtrafficsim.core.map.FeatureDescriptor;
import microtrafficsim.core.map.FeaturePrimitive;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;


// contains map-features: links to entities in ecs grouped by feature name and type
public class FeatureSet extends Container.Entry {
    private Map<String, Feature<?>> features = new HashMap<>();


    public Feature<?> set(Feature<?> feature) {
        return this.features.put(feature.name, feature);
    }

    public Feature<?> get(String name) {
        return this.features.get(name);
    }

    public Feature<?> getOrCreate(FeatureDescriptor descriptor) {
        return features.compute(descriptor.getName(), (k, v) -> {
            if (v == null) {
                return new Feature<>(descriptor.getName(), descriptor.getType());
            } else if (!descriptor.getType().equals(v.type)) {
                throw new IllegalArgumentException("Feature types do not match (got '" + descriptor.getType()
                        + "', expected '" + v.type + "'");
            } else {
                return v;
            }
        });
    }

    public Feature<?> remove(String name) {
        return this.features.remove(name);
    }

    public Map<String, Feature<?>> getAll() {
        return this.features;
    }


    public static class Feature<T extends FeaturePrimitive> {
        public final String name;
        public final Class<T> type;
        public Set<Entity> entities;

        public Feature(String name, Class<T> type) {
            this.name = name;
            this.type = type;
            this.entities = new HashSet<>();
        }

        public Feature(String name, Class<T> type, Set<Entity> entities) {
            this.name = name;
            this.type = type;
            this.entities = entities;
        }
    }
}
