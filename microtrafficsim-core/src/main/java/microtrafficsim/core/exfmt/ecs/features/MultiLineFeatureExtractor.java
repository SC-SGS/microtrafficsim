package microtrafficsim.core.exfmt.ecs.features;

import microtrafficsim.core.exfmt.Container;
import microtrafficsim.core.exfmt.ExchangeFormat;
import microtrafficsim.core.exfmt.base.GeometryEntitySet;
import microtrafficsim.core.exfmt.ecs.Entity;
import microtrafficsim.core.exfmt.ecs.entities.LineEntity;
import microtrafficsim.core.exfmt.ecs.FeatureManager;
import microtrafficsim.core.map.features.MultiLine;


public class MultiLineFeatureExtractor implements FeatureManager.Extractor<MultiLine> {

    @Override
    public MultiLine extract(ExchangeFormat fmt, ExchangeFormat.Context ctx, Container src, GeometryEntitySet ecs, Entity entity) {
        if (!(entity instanceof LineEntity)) return null;
        return new MultiLine(entity.getId(), ((LineEntity) entity).getCoordinates());
    }
}
