package microtrafficsim.core.exfmt.extractor.map;

import microtrafficsim.core.exfmt.Container;
import microtrafficsim.core.exfmt.ExchangeFormat;
import microtrafficsim.core.exfmt.base.GeometryEntitySet;
import microtrafficsim.core.exfmt.base.FeatureInfo;
import microtrafficsim.core.exfmt.base.MapInfo;
import microtrafficsim.core.exfmt.ecs.Entity;
import microtrafficsim.core.exfmt.ecs.FeatureManager;
import microtrafficsim.core.exfmt.ecs.components.FeatureComponent;
import microtrafficsim.core.exfmt.ecs.entities.LineEntity;
import microtrafficsim.core.exfmt.ecs.entities.PointEntity;
import microtrafficsim.core.exfmt.ecs.entities.PolygonEntity;
import microtrafficsim.core.exfmt.exceptions.NotAvailableException;
import microtrafficsim.core.map.*;

import java.lang.reflect.Array;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;


public class MapSegmentExtractor implements ExchangeFormat.Extractor<MapSegment> {

    @Override
    public MapSegment extract(ExchangeFormat fmt, ExchangeFormat.Context ctx, Container src) throws Exception {
        GeometryEntitySet entities = src.get(GeometryEntitySet.class);
        FeatureInfo features = src.get(FeatureInfo.class);
        if (entities == null || features == null)
            throw new NotAvailableException();

        HashMap<String, HashSet<FeaturePrimitive>> featureset = new HashMap<>();
        for (FeatureDescriptor desc : features.getAll().values()) {
            featureset.put(desc.getName(), new HashSet<>());
        }

        FeatureManager extractors = fmt.getConfig().getOr(FeatureManager.class, FeatureManager::new);

        // fill features
        for (PointEntity entity : entities.getPoints().values()) {
            process(fmt, ctx, src, entities, extractors, featureset, entity);
        }

        for (LineEntity entity : entities.getLines().values()) {
            process(fmt, ctx, src, entities, extractors, featureset, entity);
        }

        for (PolygonEntity entity : entities.getPolygons().values()) {
            process(fmt, ctx, src, entities, extractors, featureset, entity);
        }

        MapProperties properties = src.get(MapInfo.class, MapInfo::getDefault).getProperties();
        return new MapSegment(properties, entities.getBounds(), toFeatureSet(features, featureset));
    }


    private void process(ExchangeFormat fmt, ExchangeFormat.Context ctx, Container src, GeometryEntitySet ecs,
                         FeatureManager extractors, HashMap<String, HashSet<FeaturePrimitive>> dst,
                         Entity entity)
    {
        FeatureComponent fc = entity.get(FeatureComponent.class);
        if (fc == null) return;

        // get all feature types
        HashSet<Class<? extends FeaturePrimitive>> types = new HashSet<>();
        for (FeatureDescriptor fd : fc.getAll()) {
            types.add(fd.getType());
        }

        // generate the primitives for each type
        HashMap<Class<? extends FeaturePrimitive>, FeaturePrimitive> primitives = new HashMap<>();
        for (Class<? extends FeaturePrimitive> type : types) {
            FeatureManager.Extractor<?> extractor = extractors.getExtractor(type);
            if (extractor != null)
                primitives.put(type, extractor.extract(fmt, ctx, src, ecs, entity));
        }

        // add the primitives to the respective features and tiles
        for (FeatureDescriptor fd : fc.getAll()) {
            FeaturePrimitive primitive = primitives.get(fd.getType());
            if (primitive == null) continue;

            dst.get(fd.getName()).add(primitive);
        }
    }

    private HashMap<String, Feature<?>> toFeatureSet(FeatureInfo features,
                                                     HashMap<String, HashSet<FeaturePrimitive>> from)
    {
        HashMap<String, Feature<?>> result = new HashMap<>(from.size());
        for (Map.Entry<String, HashSet<FeaturePrimitive>> entry : from.entrySet()) {
            String name = entry.getKey();
            Class<? extends FeaturePrimitive> type = features.get(name).getType();
            HashSet<FeaturePrimitive> primitives = entry.getValue();

            if (!primitives.isEmpty())
                result.put(name, toFeature(name, type, primitives));
        }

        return result;
    }

    @SuppressWarnings({"SuspiciousToArrayCall", "unchecked"})
    private <T extends FeaturePrimitive> Feature<T> toFeature(String name, Class<T> type, HashSet<FeaturePrimitive> data)
    {
        T[] array = data.toArray((T[]) Array.newInstance(type, data.size()));
        return new Feature<>(name, type, array);
    }
}
