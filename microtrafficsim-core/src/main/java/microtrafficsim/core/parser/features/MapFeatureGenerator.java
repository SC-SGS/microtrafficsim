package microtrafficsim.core.parser.features;

import microtrafficsim.core.map.Feature;
import microtrafficsim.core.map.FeaturePrimitive;
import microtrafficsim.osm.parser.features.FeatureGenerator;

import java.util.Map;


/**
 * Basic generator interface for map features.
 *
 * @param <T> the type of the feature primitive.
 * @author Maximilian Luz
 */
public interface MapFeatureGenerator<T extends FeaturePrimitive> extends FeatureGenerator {

    /**
     * Return the generated features.
     *
     * @return the generated features. May return an empty map or
     * {@code null} if no features have been generated yet.
     */
    Map<String, Feature<T>> getGeneratedFeatures();
}
