package microtrafficsim.core.parser;

import java.util.Map;

import microtrafficsim.core.map.Feature;
import microtrafficsim.core.map.FeaturePrimitive;
import microtrafficsim.osm.parser.features.FeatureGenerator;


public interface MapFeatureGenerator<T extends FeaturePrimitive> extends FeatureGenerator {
	Map<String, Feature<T>> getGeneratedFeatures();
}
