package microtrafficsim.core.exfmt.injector.map.features.primitives;

import microtrafficsim.core.exfmt.Container;
import microtrafficsim.core.exfmt.base.GeometryEntitySet;
import microtrafficsim.core.exfmt.ExchangeFormat;
import microtrafficsim.core.exfmt.ecs.EntityManager;
import microtrafficsim.core.exfmt.ecs.entities.PolygonEntity;
import microtrafficsim.core.map.features.Polygon;


public class PolygonInjector implements ExchangeFormat.Injector<Polygon> {

    @Override
    public void inject(ExchangeFormat fmt, ExchangeFormat.Context ctx, Container dst, Polygon src) {
        GeometryEntitySet ecs = dst.get(GeometryEntitySet.class, GeometryEntitySet::new);
        PolygonEntity entity = ecs.getPolygons().computeIfAbsent(src.id, c -> new PolygonEntity(src.id, src.outline));

        EntityManager mgr = fmt.getConfig().get(EntityManager.class);
        if (mgr != null) mgr.process(fmt, ctx, dst, entity);
    }
}
