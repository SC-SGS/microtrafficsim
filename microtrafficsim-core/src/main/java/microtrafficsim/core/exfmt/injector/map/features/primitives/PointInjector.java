package microtrafficsim.core.exfmt.injector.map.features.primitives;

import microtrafficsim.core.exfmt.Container;
import microtrafficsim.core.exfmt.base.EntitySet;
import microtrafficsim.core.exfmt.ExchangeFormat;
import microtrafficsim.core.exfmt.ecs.EntityManager;
import microtrafficsim.core.exfmt.ecs.entities.PointEntity;
import microtrafficsim.core.map.features.Point;


public class PointInjector implements ExchangeFormat.Injector<Point> {

    @Override
    public void inject(ExchangeFormat fmt, ExchangeFormat.Context ctx, Container dst, Point src) {
        EntitySet ecs = dst.get(EntitySet.class, EntitySet::new);
        PointEntity entity = ecs.getPoints().computeIfAbsent(src.id, c -> new PointEntity(src.id, src.coordinate));

        EntityManager mgr = fmt.getConfig().get(EntityManager.class);
        if (mgr != null) mgr.process(fmt, ctx, dst, entity);
    }
}
