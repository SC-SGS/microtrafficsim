package microtrafficsim.osm.parser.features;

import microtrafficsim.core.map.Feature;
import microtrafficsim.osm.parser.base.DataSet;
import microtrafficsim.osm.primitives.Node;
import microtrafficsim.osm.primitives.Way;

import java.util.*;
import java.util.stream.Collectors;


/**
 * Base-system for categorizing OpenStreetMap Way and Node elements into
 * Features.
 * 
 * @author Maximilian Luz
 */
public class FeatureSystem implements FeatureMatcher {
	
	private HashMap<String, FeatureDefinition> features;
	
	
	public FeatureSystem() {
		this.features = new HashMap<>();
	}
	
	
	/**
	 * Registers a {@code FeatureDefinition} by its name.
	 * 
	 * @param feature the {@code FeatureDefinition} to register.
	 * @return the {@code FeatureDefinition} previously associated with the given
	 * definition's name.
	 */
	public FeatureDefinition putFeature(FeatureDefinition feature) {
		return this.features.put(feature.getName(), feature);
	}
	
	/**
	 * Registers all {@code FeatureDefinitions} by their name, overwrites any
	 * entries with the same name.
	 * 
	 * @param features the {@code FeatureDefinitions} to register.
	 */
	public void putFeatures(Collection<? extends FeatureDefinition> features) {
		for (FeatureDefinition def : features)
			this.features.put(def.getName(), def);
	}
	
	/**
	 * Returns the {@code FeatureDefinition} associated with the specified name.
	 * 
	 * @param name the name of the {@code FeatureDefinition} to be retrieved.
	 * @return the {@code FeatureDefinition} associated with the specified name.
	 */
	public FeatureDefinition getFeature(String name) {
		return this.features.get(name);
	}
	
	/**
	 * Remove the {@code FeatureDefinition} associated with the specified name.
	 * 
	 * @param name the name of the {@code FeatureDefinition} to be removed.
	 * @return the {@code FeatureDefinition} previously associated with the
	 * specified name.
	 */
	public FeatureDefinition removeFeature(String name) {
		return this.features.remove(name);
	}
	
	/**
	 * Checks if this system contains a {@code FeatureDefinition} associated with
	 * the given name.
	 * 
	 * @param name the name of the {@code FeatureDefinition} to check for.
	 * @return true if there exits a {@code FeatureDefinition} with the specified
	 * name.
	 */
	public boolean hasFeature(String name) {
		return this.features.containsKey(name);
	}
	
	/**
	 * Returns a Set containing the names of all associated {@code
	 * FeatureDefinition}s.
	 * 
	 * @return the names of all associated {@code FeatureDefinition}s.
	 */
	public Set<String> getAllFeatureNames() {
		return features.keySet();
	}
	
	/**
	 * Returns a Collection containing all associated {@code FeatureDefinition}s.
	 * @return all associated {@code FeatureDefinition}s.
	 */
	public Collection<FeatureDefinition> getAllFeatures() {
		return features.values();
	}
	
	/**
	 * Returns all features with the given generator-index contained in this
	 * FeatureSystem.
	 * 
	 * @param genindex			the generator-index of the features to return.
	 * @return all features with the given index.
	 */
	public List<FeatureDefinition> getAllFeatures(int genindex) {
		return features.values().stream()
				.filter(d -> d.getGeneratorIndex() == genindex)
				.collect(Collectors.toList());
	}
	
	/**
	 * Returns all features with a generator-index between the given bounds 
	 * (both inclusive) in this FeatureSystem.
	 * 
	 * @param lower		the lower bounds in which the generator-index may lie (inclusive).
	 * @param upper		the upper bounds in which the generator-index may lie (inclusive).
	 * @return all features where the generator-index satisfies
	 * {@code lower <= genindex <= upper}
	 */
	public List<FeatureDefinition> getAllFeatures(int lower, int upper) {
		return features.values().stream()
				.filter(d -> lower <= d.getGeneratorIndex() && d.getGeneratorIndex() <= upper)
				.collect(Collectors.toList());
	}
	
	/**
	 * Execute the generator for the given FeatureDefinition on the DataSet.
	 * 
	 * @param dataset	the DataSet on which the generators should be executed.
	 * @param feature	the Feature to be generated.
	 */
	public void generateFeature(DataSet dataset, FeatureDefinition feature) {
		feature.getGenerator().execute(dataset, feature);
	}
	
	/**
	 * Execute all generators for the given generator-index on the DataSet.
	 * 
	 * @param dataset		the DataSet on which the generators should be executed.
	 * @param genindex		the generator-index of the Features for which the
	 * 						generators should be executed.
	 */
	public void generateAllFeatures(DataSet dataset, int genindex) {
		for (FeatureDefinition fd : getAllFeatures(genindex))
			fd.getGenerator().execute(dataset, fd);
	}
	
	/**
	 * Execute all generators for the given range of generator-indices on the DataSet
	 * ({@code lower <= genindex <= upper}).
	 * 
	 * @param dataset	the DataSet on which the generators should be executed.
	 * @param lower		the lower bounds in which the generator-index may lie (inclusive).
	 * @param upper		the upper bounds in which the generator-index may lie (inclusive).
	 */
	public void generateAllFeatures(DataSet dataset, int lower, int upper) {
		for (FeatureDefinition fd : getAllFeatures(lower, upper))
			fd.getGenerator().execute(dataset, fd);
	}
	

	@Override
	public Set<FeatureDefinition> getFeatures(Node n) {
		return features.values().stream()
				.filter(d -> d.matches(n))
				.collect(Collectors.toSet());
	}

	@Override
	public Set<FeatureDefinition> getFeatures(Way w) {
		return features.values().stream()
				.filter(d -> d.matches(w))
				.collect(Collectors.toSet());
	}
}
