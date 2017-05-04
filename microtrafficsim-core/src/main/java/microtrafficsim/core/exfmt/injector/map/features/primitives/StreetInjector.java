package microtrafficsim.core.exfmt.injector.map.features.primitives;

import microtrafficsim.core.exfmt.Container;
import microtrafficsim.core.exfmt.base.EntitySet;
import microtrafficsim.core.exfmt.ExchangeFormat;
import microtrafficsim.core.exfmt.ecs.EntityManager;
import microtrafficsim.core.exfmt.ecs.components.StreetComponent;
import microtrafficsim.core.exfmt.ecs.entities.LineEntity;
import microtrafficsim.core.map.features.Street;


/*
 * NOTE: To properly inject a map segment and a street-graph, it is expected that the IDs of corresponding
 * MultiLine/Street features and DirectedEdges are equivalent.
 */
public class StreetInjector implements ExchangeFormat.Injector<Street> {

    @Override
    public void inject(ExchangeFormat fmt, ExchangeFormat.Context ctx, Container dst, Street src) {
        EntitySet ecs = dst.get(EntitySet.class, EntitySet::new);
        LineEntity entity = ecs.getLines().compute(src.id, (k, v) -> {
            if (v == null) {
                return new LineEntity(src.id, src.coordinates);
            } else if (v.getCoordinates() == null) {
                v.setCoordinates(src.coordinates);
            }

            return v;
        });

        entity.set(StreetComponent.class, new StreetComponent(entity, src.layer, src.length, src.distances));

        EntityManager mgr = fmt.getConfig().get(EntityManager.class);
        if (mgr != null) mgr.process(fmt, ctx, dst, entity);
    }
}
