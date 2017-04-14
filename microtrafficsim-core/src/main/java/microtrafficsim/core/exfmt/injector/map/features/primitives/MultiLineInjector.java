package microtrafficsim.core.exfmt.injector.map.features.primitives;

import microtrafficsim.core.exfmt.Container;
import microtrafficsim.core.exfmt.base.EntitySet;
import microtrafficsim.core.exfmt.ExchangeFormat;
import microtrafficsim.core.exfmt.ecs.EntityManager;
import microtrafficsim.core.exfmt.ecs.entities.LineEntity;
import microtrafficsim.core.map.features.MultiLine;


public class MultiLineInjector implements ExchangeFormat.Injector<MultiLine> {

    @Override
    public void inject(ExchangeFormat fmt, ExchangeFormat.Context ctx, Container dst, MultiLine src) {
        EntitySet ecs = dst.get(EntitySet.class, EntitySet::new);
        LineEntity entity = ecs.getLines().computeIfAbsent(src.id, c -> new LineEntity(src.id, src.coordinates));

        EntityManager mgr = fmt.getConfig().get(EntityManager.class);
        if (mgr != null) mgr.process(fmt, ctx, dst, entity);
    }
}
