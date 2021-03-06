package microtrafficsim.core.exfmt.injector.map;

import microtrafficsim.core.exfmt.Container;
import microtrafficsim.core.exfmt.base.GeometryEntitySet;
import microtrafficsim.core.exfmt.ExchangeFormat;
import microtrafficsim.core.exfmt.base.MapInfo;
import microtrafficsim.core.map.Feature;
import microtrafficsim.core.map.SegmentFeatureProvider;


public class SegmentFeatureProviderInjector implements ExchangeFormat.Injector<SegmentFeatureProvider> {

    @Override
    public void inject(ExchangeFormat fmt, ExchangeFormat.Context ctx, Container dst, SegmentFeatureProvider src)
            throws Exception
    {
        GeometryEntitySet entities = dst.get(GeometryEntitySet.class, GeometryEntitySet::new);
        entities.updateBounds(src.getBounds());

        MapInfo info = dst.get(MapInfo.class, MapInfo::getDefault);
        info.setProperties(src.getProperties());

        for (Feature<?> feature : src.getFeatures().values()) {
            fmt.inject(ctx, dst, feature);
        }
    }
}
