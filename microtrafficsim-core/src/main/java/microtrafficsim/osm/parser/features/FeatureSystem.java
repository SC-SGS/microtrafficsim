package microtrafficsim.osm.parser.features;

import microtrafficsim.osm.primitives.Node;
import microtrafficsim.osm.primitives.Way;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;


/**
 * Base-system for categorizing OpenStreetMap Way and Node elements into
 * Features.
 *
 * @author Maximilian Luz
 */
public class FeatureSystem implements FeatureMatcher {
    private static final Logger logger = LoggerFactory.getLogger(FeatureSystem.class);

    private HashMap<String, FeatureDefinition> features;


    /**
     * Creates a new, empty {@code FeatureSystem}.
     */
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
     *
     * @return all associated {@code FeatureDefinition}s.
     */
    public Collection<FeatureDefinition> getAllFeatures() {
        return features.values();
    }

    public List<FeatureDefinition> getAllFeaturesInOrderOfDependency() throws CyclicDependenciesException {
        // create one-directional dependency structure
        HashMap<FeatureDefinition, DependencyNode> nodes = new HashMap<>();
        for (FeatureDefinition def : this.features.values()) {
            DependencyNode node = new DependencyNode(def);
            node.dependencies.addAll(def.getDependency().getRequires());
            nodes.put(def, node);
        }

        for (FeatureDefinition required : this.features.values()) {
            for (FeatureDefinition def : required.getDependency().getRequiredBy()) {
                if (def == null) continue;
                DependencyNode n = nodes.get(def);
                if (n == null) {
                    logger.warn("could not resolve feature dependency");
                    continue;
                }
                n.dependencies.add(required);
            }
        }

        // recursive topology-sort
        LinkedList<FeatureDefinition> sorted = new LinkedList<>();

        while (!nodes.isEmpty()) {
            DependencyNode node = nodes.values().iterator().next();
            recursiveTopologySort(node, nodes, sorted);
        }

        return sorted;
    }

    private void recursiveTopologySort(DependencyNode node, HashMap<FeatureDefinition, DependencyNode> available,
                                       LinkedList<FeatureDefinition> result) throws CyclicDependenciesException {
        if (node.visited) throw new CyclicDependenciesException();
        node.visited = true;

        for (FeatureDefinition def : node.dependencies) {
            DependencyNode n = available.get(def);
            if (n != null) recursiveTopologySort(n, available, result);
        }

        available.remove(node.feature);
        result.add(node.feature);
    }


    @Override
    public Set<FeatureDefinition> getFeatures(Node n) {
        return features.values().stream().filter(d -> d.matches(n)).collect(Collectors.toSet());
    }

    @Override
    public Set<FeatureDefinition> getFeatures(Way w) {
        return features.values().stream().filter(d -> d.matches(w)).collect(Collectors.toSet());
    }


    public static class CyclicDependenciesException extends Exception {}

    private static class DependencyNode {
        final FeatureDefinition feature;
        final ArrayList<FeatureDefinition> dependencies;
        boolean visited;

        DependencyNode(FeatureDefinition feature) {
            this.feature = feature;
            this.dependencies = new ArrayList<>();
            this.visited = false;
        }
    }
}
