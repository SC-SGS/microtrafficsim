package microtrafficsim.core.exfmt.ecs.features;

import microtrafficsim.core.exfmt.Container;
import microtrafficsim.core.exfmt.ExchangeFormat;
import microtrafficsim.core.exfmt.base.EntitySet;
import microtrafficsim.core.exfmt.ecs.Entity;
import microtrafficsim.core.exfmt.ecs.entities.PointEntity;
import microtrafficsim.core.exfmt.ecs.FeatureManager;
import microtrafficsim.core.map.features.Point;


public class PointFeatureExtractor implements FeatureManager.Extractor<Point> {

    @Override
    public Point extract(ExchangeFormat fmt, ExchangeFormat.Context ctx, Container src, EntitySet ecs, Entity entity) {
        if (!(entity instanceof PointEntity)) return null;
        return new Point(entity.getId(), ((PointEntity) entity).getCoordinate());
    }
}
