package microtrafficsim.core.exfmt.ecs.features;

import microtrafficsim.core.exfmt.Container;
import microtrafficsim.core.exfmt.ExchangeFormat;
import microtrafficsim.core.exfmt.base.GeometryEntitySet;
import microtrafficsim.core.exfmt.context.StreetFeatureMap;
import microtrafficsim.core.exfmt.ecs.Entity;
import microtrafficsim.core.exfmt.ecs.components.StreetComponent;
import microtrafficsim.core.exfmt.ecs.entities.LineEntity;
import microtrafficsim.core.exfmt.ecs.FeatureManager;
import microtrafficsim.core.map.features.Street;


public class StreetFeatureExtractor implements FeatureManager.Extractor<Street> {

    @Override
    public Street extract(ExchangeFormat fmt, ExchangeFormat.Context ctx, Container src, GeometryEntitySet ecs, Entity entity) {
        if (!(entity instanceof LineEntity)) return null;

        StreetComponent sc = entity.get(StreetComponent.class);
        if (sc == null) return null;

        Street street = new Street(
                entity.getId(),
                ((LineEntity) entity).getCoordinates(),
                sc.getLayer(), sc.getLength(), sc.getDistances()
        );

        // add street to map for StreetEntity construction
        StreetFeatureMap features = ctx.get(StreetFeatureMap.class, StreetFeatureMap::new);
        features.put(entity.getId(), street);

        return street;
    }
}
