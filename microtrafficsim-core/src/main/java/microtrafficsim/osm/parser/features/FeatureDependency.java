package microtrafficsim.osm.parser.features;

import java.util.ArrayList;
import java.util.List;


public class FeatureDependency {
    private List<FeatureDefinition> requires;
    private List<FeatureDefinition> requiredBy;

    public FeatureDependency() {
        this.requires = new ArrayList<>();
        this.requiredBy = new ArrayList<>();
    }

    public FeatureDependency(List<FeatureDefinition> requires, List<FeatureDefinition> requiredBy) {
        this();

        if (requires != null)
            this.requires.addAll(requires);

        if (requiredBy != null)
            this.requiredBy.addAll(requiredBy);
    }

    public FeatureDependency(FeatureDefinition requires, FeatureDefinition requiredBy) {
        this();

        if (requires != null)
            this.requires.add(requires);

        if (requiredBy != null)
            this.requiredBy.add(requiredBy);
    }

    public boolean addRequires(FeatureDefinition def) {
        return requires.add(def);
    }

    public boolean removeRequires(FeatureDefinition def) {
        return requires.remove(def);
    }

    public boolean addRequiredBy(FeatureDefinition def) {
        return requiredBy.add(def);
    }

    public boolean removeRequiredBy(FeatureDefinition def) {
        return requiredBy.remove(def);
    }

    public List<FeatureDefinition> getRequires() {
        return requires;
    }

    public List<FeatureDefinition> getRequiredBy() {
        return requiredBy;
    }
}
