package microtrafficsim.osm.parser.features;

import java.util.HashSet;
import java.util.Set;


/**
 * Description of the dependencies of a {@code FeatureDefinition} in respect to the order of generating this feature.
 *
 * @author Maximilian Luz
 */
public class FeatureDependency {
    private Set<FeatureDefinition> requires;
    private Set<FeatureDefinition> requiredBy;

    /**
     * Creates a new, empty {@code FeatureDependency}.
     */
    public FeatureDependency() {
        this.requires = new HashSet<>();
        this.requiredBy = new HashSet<>();
    }

    /**
     * Creates a new {@code FeatureDependency} with the given sets indicating dependencies. If either of the
     * parameters is {@code null}, no dependency-relation for this parameter will be added.
     *
     * @param requires   a set of {@code FeatureDefinition}s that are required to be generated <em>before</em> the
     *                   feature this dependency-description belongs to is generated, or {@code null}.
     * @param requiredBy a set of {@code FeatureDefinition}s that are required to be generated <em>after</em> the
     *                   feature this dependency-description belongs to is generated, or {@code null}.
     */
    public FeatureDependency(Set<FeatureDefinition> requires, Set<FeatureDefinition> requiredBy) {
        this();

        if (requires != null)
            this.requires.addAll(requires);

        if (requiredBy != null)
            this.requiredBy.addAll(requiredBy);
    }

    /**
     * Creates a new {@code FeatureDependency} with the given dependencies. If either of the parameters is {@code null},
     * no dependency-relation for this parameter will be added.
     *
     * @param requires   a single {@code FeatureDefinition}s that is required to be generated <em>before</em> the
     *                   feature this dependency-description belongs to is generated, or {@code null}.
     * @param requiredBy a single {@code FeatureDefinition}s that is required to be generated <em>after</em> the
     *                   feature this dependency-description belongs to is generated, or {@code null}.
     */
    public FeatureDependency(FeatureDefinition requires, FeatureDefinition requiredBy) {
        this();

        if (requires != null)
            this.requires.add(requires);

        if (requiredBy != null)
            this.requiredBy.add(requiredBy);
    }

    /**
     * Add the provided {@code FeatureDefinition} as dependency, required to be generated before the feature this
     * dependency-description belongs to.
     *
     * @param def the dependency to be added.
     * @return {@code true} if the underlying collection of dependencies has changed.
     */
    public boolean addRequires(FeatureDefinition def) {
        return requires.add(def);
    }

    /**
     * Removes the provided {@code FeatureDefinition} as dependency, required to be generated before the feature this
     * dependency-description belongs to.
     *
     * @param def the dependency to be removed.
     * @return {@code true} if the underlying collection of dependencies has changed.
     */
    public boolean removeRequires(FeatureDefinition def) {
        return requires.remove(def);
    }

    /**
     * Adds the provided {@code FeatureDefinition} as dependency, required to be generated after the feature this
     * dependency-description belongs to (or not at all).
     *
     * @param def the dependency to be added.
     * @return {@code true} if the underlying collection of dependencies has changed.
     */
    public boolean addRequiredBy(FeatureDefinition def) {
        return requiredBy.add(def);
    }

    /**
     * Removes the provided {@code FeatureDefinition} as dependency, required to be generated after the feature this
     * dependency-description belongs to (or not at all).
     *
     * @param def the dependency to be removed.
     * @return {@code true} if the underlying collection of dependencies has changed.
     */
    public boolean removeRequiredBy(FeatureDefinition def) {
        return requiredBy.remove(def);
    }

    /**
     * Returns the set of {@code FeatureDefinition}s required to be generated before the feature this
     * dependency-description belongs to.
     *
     * @return the set of {@code FeatureDefinition}s required to be generated before the feature this
     * dependency-description belongs to.
     */
    public Set<FeatureDefinition> getRequires() {
        return requires;
    }

    /**
     * Returns the set of {@code FeatureDefinition}s required to be generated after the feature this
     * dependency-description belongs to (or not at all).
     *
     * @return the set of {@code FeatureDefinition}s required to be generated after the feature this
     * dependency-description belongs to (or not at all).
     */
    public Set<FeatureDefinition> getRequiredBy() {
        return requiredBy;
    }
}
