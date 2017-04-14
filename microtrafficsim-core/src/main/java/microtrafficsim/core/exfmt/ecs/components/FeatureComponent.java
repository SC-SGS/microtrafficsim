package microtrafficsim.core.exfmt.ecs.components;

import microtrafficsim.core.exfmt.ecs.Component;
import microtrafficsim.core.exfmt.ecs.Entity;
import microtrafficsim.core.map.FeatureDescriptor;

import java.util.Collection;
import java.util.HashSet;


public class FeatureComponent extends Component {
    private HashSet<FeatureDescriptor> features = new HashSet<>();

    public FeatureComponent(Entity entity) {
        super(entity);
    }


    public boolean add(FeatureDescriptor descriptor) {
        return features.add(descriptor);
    }

    public boolean addAll(Collection<FeatureDescriptor> descriptors) {
        return features.addAll(descriptors);
    }

    public boolean remove(FeatureDescriptor descriptor) {
        return features.remove(descriptor);
    }

    public boolean in(FeatureDescriptor descriptor) {
        return features.contains(descriptor);
    }

    public HashSet<FeatureDescriptor> getAll() {
        return features;
    }
}
