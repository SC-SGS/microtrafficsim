package microtrafficsim.core.exfmt.injector.map.features;

import microtrafficsim.core.exfmt.Container;
import microtrafficsim.core.exfmt.ExchangeFormat;
import microtrafficsim.core.exfmt.base.FeatureSet;
import microtrafficsim.core.exfmt.ecs.Entity;
import microtrafficsim.core.exfmt.ecs.EntityManager;
import microtrafficsim.core.exfmt.ecs.components.FeatureComponent;
import microtrafficsim.core.map.Feature;
import microtrafficsim.core.map.FeatureDescriptor;
import microtrafficsim.core.map.FeaturePrimitive;


public class FeatureInjector implements ExchangeFormat.Injector<Feature> {

    @Override
    public void inject(ExchangeFormat fmt, ExchangeFormat.Context ctx, Container dst, Feature src) throws Exception {
        ctx.set(FeatureDescriptor.class, src.getDescriptor());

        for (FeaturePrimitive p : src.getData()) {
            fmt.inject(ctx, dst, p);
        }

        ctx.remove(FeatureDescriptor.class);
    }
}
