package microtrafficsim.core.exfmt.extractor.map;

import microtrafficsim.core.exfmt.Container;
import microtrafficsim.core.exfmt.ExchangeFormat;
import microtrafficsim.core.exfmt.base.EntitySet;
import microtrafficsim.core.exfmt.base.FeatureSet;
import microtrafficsim.core.exfmt.ecs.Entity;
import microtrafficsim.core.exfmt.ecs.components.StreetComponent;
import microtrafficsim.core.exfmt.ecs.entities.LineEntity;
import microtrafficsim.core.exfmt.ecs.entities.PolygonEntity;
import microtrafficsim.core.map.Feature;
import microtrafficsim.core.map.MapSegment;
import microtrafficsim.core.map.features.Polygon;
import microtrafficsim.core.map.features.Street;
import microtrafficsim.core.map.tiles.QuadTreeTiledMapSegment;
import microtrafficsim.core.map.tiles.QuadTreeTilingScheme;
import microtrafficsim.core.map.tiles.TilingScheme;
import microtrafficsim.core.vis.map.projections.MercatorProjection;

import java.util.HashMap;


// FIXME: THIS IS A TEMPORARY IMPLEMENTATION
public class QuadTreeTiledMapSegmentExtractor implements ExchangeFormat.Extractor<QuadTreeTiledMapSegment> {

    @Override
    public QuadTreeTiledMapSegment extract(ExchangeFormat fmt, ExchangeFormat.Context ctx, Container src)
            throws Exception
    {
        EntitySet entities = src.get(EntitySet.class);
        FeatureSet features = src.get(FeatureSet.class);

        HashMap<String, Feature<?>> featureset = new HashMap<>();

        for (FeatureSet.Feature<?> source : features.getAll().values()) {
            if (source.type.equals(Street.class)) {
                Street[] data = new Street[source.entities.size()];

                int i = 0;
                for (Entity e : source.entities) {
                    LineEntity entity = (LineEntity) e;
                    StreetComponent sc = entity.get(StreetComponent.class);

                    data[i++] = new Street(entity.getId(), entity.getCoordinates(), sc.getLayer(), sc.getLength(), sc.getDistances());
                }

                featureset.put(source.name, new Feature<>(source.name, (Class<Street>) source.type, data));

            } else if (source.type.equals(Polygon.class)) {
                Polygon[] data = new Polygon[source.entities.size()];

                int i = 0;
                for (Entity e : source.entities) {
                    PolygonEntity entity = (PolygonEntity) e;

                    data[i++] = new Polygon(entity.getId(), entity.getOutline());
                }

                featureset.put(source.name, new Feature<>(source.name, (Class<Polygon>) source.type, data));
            }
        }

        Config config = fmt.getConfig().getOr(Config.class, Config::getDefault);

        return new QuadTreeTiledMapSegment.Generator()
                .generate(new MapSegment(entities.getBounds(), featureset), config.scheme, config.tileGridLevel);
    }


    public static class Config extends microtrafficsim.core.exfmt.Config.Entry {
        public TilingScheme scheme;
        public int tileGridLevel;

        public Config(TilingScheme scheme, int tileGridLevel) {
            this.scheme = scheme;
            this.tileGridLevel = tileGridLevel;
        }

        public static Config getDefault() {
            return new Config(new QuadTreeTilingScheme(new MercatorProjection(), 0, 19), 12);
        }
    }
}
