package microtrafficsim.core.parser;

import microtrafficsim.core.map.Feature;
import microtrafficsim.core.map.FeaturePrimitive;
import microtrafficsim.osm.parser.features.FeatureGenerator;

import java.util.Map;


public interface MapFeatureGenerator<T extends FeaturePrimitive> extends FeatureGenerator {
    Map<String, Feature<T>> getGeneratedFeatures();
}
