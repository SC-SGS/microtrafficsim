package microtrafficsim.core.exfmt.ecs.processors;

import microtrafficsim.core.exfmt.Container;
import microtrafficsim.core.exfmt.ExchangeFormat;
import microtrafficsim.core.exfmt.base.FeatureSet;
import microtrafficsim.core.exfmt.ecs.Entity;
import microtrafficsim.core.exfmt.ecs.EntityManager;
import microtrafficsim.core.exfmt.ecs.components.FeatureComponent;
import microtrafficsim.core.map.FeatureDescriptor;


public class FeatureProcessor implements EntityManager.Processor {

    @Override
    public void process(ExchangeFormat fmt, ExchangeFormat.Context ctx, Container container, Entity entity) {
        FeatureDescriptor descriptor = ctx.get(FeatureDescriptor.class);
        if (descriptor == null) return;

        // add component
        FeatureComponent component = entity.get(FeatureComponent.class, () -> new FeatureComponent(entity));
        component.add(descriptor);

        // insert in feature-set
        FeatureSet.Feature<?> feature = container.get(FeatureSet.class, FeatureSet::new).getOrCreate(descriptor);
        feature.entities.add(entity);
    }
}
