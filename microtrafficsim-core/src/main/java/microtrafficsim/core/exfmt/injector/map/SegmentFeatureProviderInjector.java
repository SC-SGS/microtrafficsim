package microtrafficsim.core.exfmt.injector.map;

import microtrafficsim.core.exfmt.Container;
import microtrafficsim.core.exfmt.base.EntitySet;
import microtrafficsim.core.exfmt.ExchangeFormat;
import microtrafficsim.core.map.Feature;
import microtrafficsim.core.map.SegmentFeatureProvider;


public class SegmentFeatureProviderInjector implements ExchangeFormat.Injector<SegmentFeatureProvider> {

    @Override
    public void inject(ExchangeFormat fmt, ExchangeFormat.Context ctx, Container dst, SegmentFeatureProvider src)
            throws Exception
    {
        EntitySet entities = dst.get(EntitySet.class, EntitySet::new);
        entities.updateBounds(src.getBounds());

        for (Feature<?> feature : src.getFeatures().values()) {
            fmt.inject(ctx, dst, feature);
        }
    }
}
