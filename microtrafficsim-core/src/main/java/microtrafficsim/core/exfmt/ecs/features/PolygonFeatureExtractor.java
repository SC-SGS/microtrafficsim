package microtrafficsim.core.exfmt.ecs.features;

import microtrafficsim.core.exfmt.Container;
import microtrafficsim.core.exfmt.ExchangeFormat;
import microtrafficsim.core.exfmt.base.EntitySet;
import microtrafficsim.core.exfmt.ecs.Entity;
import microtrafficsim.core.exfmt.ecs.entities.PolygonEntity;
import microtrafficsim.core.exfmt.ecs.FeatureManager;
import microtrafficsim.core.map.features.Polygon;


public class PolygonFeatureExtractor implements FeatureManager.Extractor<Polygon> {

    @Override
    public Polygon extract(ExchangeFormat fmt, ExchangeFormat.Context ctx, Container src, EntitySet ecs, Entity entity) {
        if (!(entity instanceof PolygonEntity)) return null;

        return new Polygon(entity.getId(), ((PolygonEntity) entity).getOutline());
    }
}
