package microtrafficsim.osm.parser.features;

import microtrafficsim.osm.primitives.Node;
import microtrafficsim.osm.primitives.Way;

import java.util.Set;


/**
 * Interface for categorizing Way and Node elements.
 *
 * @author Maximilian Luz
 */
public interface FeatureMatcher {

    /**
     * Returns a set of feature-definitions that match the specified Node.
     *
     * @param n the Node for which the matching definitions should be returned.
     * @return the set of feature-definitions that match the specified Node.
     */
    Set<FeatureDefinition> getFeatures(Node n);

    /**
     * Returns a set of feature-definitions that match the specified Way.
     *
     * @param w the Way for which the matching definitions should be returned.
     * @return the set of feature-definitions that match the specified Way.
     */
    Set<FeatureDefinition> getFeatures(Way w);
}
