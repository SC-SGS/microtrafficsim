package microtrafficsim.core.map;

import java.util.Map;
import java.util.Set;


public interface SegmentFeatureProvider {

	interface FeatureChangeListener {
		void featuresChanged();
		void featureChanged(String name);
	}


	Bounds getBounds();

	Class<? extends FeaturePrimitive> getFeatureType(String name);

	<T extends FeaturePrimitive> Feature<T> require(String name);
	void release(Feature<?> feature);
	void releaseAll();

	Set<String> getAvailableFeatures();
	Map<String, Feature<?>> getFeatures();
	boolean hasFeature(String name);

	boolean addFeatureChangeListener(FeatureChangeListener listener);
	boolean removeFeatureChangeListener(FeatureChangeListener listener);
	boolean hasFeatureChangeListener(FeatureChangeListener listener);
}
