package microtrafficsim.core.exfmt.ecs;

import microtrafficsim.core.exfmt.Config;
import microtrafficsim.core.exfmt.Container;
import microtrafficsim.core.exfmt.ExchangeFormat;
import microtrafficsim.core.exfmt.base.GeometryEntitySet;
import microtrafficsim.core.map.FeaturePrimitive;

import java.util.HashMap;
import java.util.Map;


public class FeatureManager extends Config.Entry {
    private HashMap<Class<? extends FeaturePrimitive>, Extractor<?>> extractors = new HashMap<>();


    @SuppressWarnings("unchecked")
    public <T extends FeaturePrimitive> Extractor<T> setExtractor(Class<T> type, Extractor<T> extractor) {
        return (Extractor<T>) extractors.put(type, extractor);
    }

    @SuppressWarnings("unchecked")
    public <T extends FeaturePrimitive> Extractor<T> getExtractor(Class<T> type) {
        return (Extractor<T>) extractors.get(type);
    }

    @SuppressWarnings("unchecked")
    public <T extends FeaturePrimitive> Extractor<T> removeExtractor(Class<T> type) {
        return (Extractor<T>) extractors.remove(type);
    }

    public Map<Class<? extends FeaturePrimitive>, Extractor<?>> getAllExtractors() {
        return extractors;
    }


    public interface Extractor<T extends FeaturePrimitive> {
        T extract(ExchangeFormat fmt, ExchangeFormat.Context ctx, Container src, GeometryEntitySet ecs, Entity entity);
    }
}
