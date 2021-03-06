package microtrafficsim.core.exfmt.injector.streetgraph;

import microtrafficsim.core.exfmt.Container;
import microtrafficsim.core.exfmt.ExchangeFormat;
import microtrafficsim.core.exfmt.base.GeometryEntitySet;
import microtrafficsim.core.exfmt.ecs.components.GraphEdgeComponent;
import microtrafficsim.core.exfmt.ecs.components.StreetComponent;
import microtrafficsim.core.exfmt.ecs.entities.LineEntity;
import microtrafficsim.core.logic.streets.DirectedEdge;
import microtrafficsim.math.Vec2d;


public class DirectedEdgeInjector implements ExchangeFormat.Injector<DirectedEdge> {

    @Override
    public void inject(ExchangeFormat fmt, ExchangeFormat.Context ctx, Container dst, DirectedEdge src) throws Exception {
        GeometryEntitySet ecs = dst.get(GeometryEntitySet.class, GeometryEntitySet::new);
        LineEntity entity = ecs.getLines().computeIfAbsent(src.getId(), k -> new LineEntity(src.getId(), null));

        GraphEdgeComponent gec = entity.get(GraphEdgeComponent.class, () -> new GraphEdgeComponent(entity));
        gec.setLength(src.getLengthInMeter());
        gec.setStreetType(src.getStreetType());

        if (src.getEntity().getForwardEdge() == src) {
            gec.setForwardLanes(src.getNumberOfLanes());
            gec.setForwardMaxVelocity(src.getRawMaxVelocity());
            gec.setOrigin(src.getOrigin().getId());
            gec.setOriginDirection(src.getOriginDirection());
            gec.setDestination(src.getDestination().getId());
            gec.setDestinationDirection(src.getDestinationDirection());
        } else {
            gec.setBackwardLanes(src.getNumberOfLanes());
            gec.setBackwardMaxVelocity(src.getRawMaxVelocity());
            gec.setOrigin(src.getDestination().getId());
            gec.setOriginDirection(Vec2d.mul(src.getDestinationDirection(), -1.0));
            gec.setDestination(src.getOrigin().getId());
            gec.setDestinationDirection(Vec2d.mul(src.getOriginDirection(), -1.0));
        }

        // Inject the street-geometry if it has not already been injected. This might be necessary when the street
        // is not contained in any map-feature
        if (entity.get(StreetComponent.class) == null && src.getEntity().getGeometry() != null) {
            fmt.inject(ctx, dst, src.getEntity().getGeometry());
        }
    }
}
