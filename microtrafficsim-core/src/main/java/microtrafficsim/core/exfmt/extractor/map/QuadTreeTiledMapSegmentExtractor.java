package microtrafficsim.core.exfmt.extractor.map;

import microtrafficsim.core.exfmt.Container;
import microtrafficsim.core.exfmt.ExchangeFormat;
import microtrafficsim.core.exfmt.base.EntitySet;
import microtrafficsim.core.exfmt.base.FeatureSet;
import microtrafficsim.core.exfmt.base.TileGridSet;
import microtrafficsim.core.exfmt.ecs.Entity;
import microtrafficsim.core.exfmt.ecs.components.StreetComponent;
import microtrafficsim.core.exfmt.ecs.components.TileGridComponent;
import microtrafficsim.core.exfmt.ecs.entities.LineEntity;
import microtrafficsim.core.exfmt.ecs.entities.PolygonEntity;
import microtrafficsim.core.map.Feature;
import microtrafficsim.core.map.FeaturePrimitive;
import microtrafficsim.core.map.MapSegment;
import microtrafficsim.core.map.features.Polygon;
import microtrafficsim.core.map.features.Street;
import microtrafficsim.core.map.tiles.FeatureGrid;
import microtrafficsim.core.map.tiles.QuadTreeTiledMapSegment;
import microtrafficsim.core.map.tiles.QuadTreeTilingScheme;
import microtrafficsim.core.map.tiles.TilingScheme;
import microtrafficsim.core.vis.map.projections.MercatorProjection;
import microtrafficsim.utils.collections.Grid;

import java.util.*;


// FIXME: THIS IS A TEMPORARY IMPLEMENTATION
public class QuadTreeTiledMapSegmentExtractor implements ExchangeFormat.Extractor<QuadTreeTiledMapSegment> {

    @Override
    public QuadTreeTiledMapSegment extract(ExchangeFormat fmt, ExchangeFormat.Context ctx, Container src)
            throws Exception
    {
        EntitySet entities = src.get(EntitySet.class);
        FeatureSet features = src.get(FeatureSet.class);
        TileGridSet tiles = src.get(TileGridSet.class);

        TileGridSet.TileGrid grid = tiles.getAll().values().iterator().next();

        HashMap<String, FeatureGrid<?>> featureset = new HashMap<>();   // TODO: some scheme to select specific grid

        int nx = grid.entities.getSizeX();
        int ny = grid.entities.getSizeY();
        for (FeatureSet.Feature<?> source : features.getAll().values()) {
            if (source.type.equals(Street.class)) {
                Grid<List<Street>> data = new Grid<>(nx, ny);

                for (int y = 0; y < ny; y++) {
                    for (int x = 0; x < nx; x++) {
                        data.set(x, y, new ArrayList<>());
                    }
                }

                for (Entity e : source.entities) {
                    LineEntity entity = (LineEntity) e;
                    StreetComponent sc = entity.get(StreetComponent.class);

                    Street street = new Street(entity.getId(), entity.getCoordinates(), sc.getLayer(), sc.getLength(), sc.getDistances());

                    TileGridComponent tc = entity.get(TileGridComponent.class);
                    for (TileGridComponent.Entry entry : tc.getAll()) {
                        if (entry.getScheme().equals(grid.scheme) && entry.getLevel().equals(grid.level))
                            data.get(entry.getX(), entry.getY()).add(street);
                    }
                }

                featureset.put(source.name, new FeatureGrid<>(source.name, (Class<Street>) source.type, data));

            } else if (source.type.equals(Polygon.class)) {
                Grid<List<Polygon>> data = new Grid<>(nx, ny);

                for (int y = 0; y < ny; y++) {
                    for (int x = 0; x < nx; x++) {
                        data.set(x, y, new ArrayList<>());
                    }
                }

                for (Entity e : source.entities) {
                    PolygonEntity entity = (PolygonEntity) e;

                    Polygon polygon = new Polygon(entity.getId(), entity.getOutline());

                    TileGridComponent tc = entity.get(TileGridComponent.class);
                    for (TileGridComponent.Entry entry : tc.getAll()) {
                        if (entry.getScheme().equals(grid.scheme) && entry.getLevel().equals(grid.level))
                            data.get(entry.getX(), entry.getY()).add(polygon);
                    }
                }

                featureset.put(source.name, new FeatureGrid<>(source.name, (Class<Polygon>) source.type, data));
            }
        }

        return new QuadTreeTiledMapSegment(grid.scheme, entities.getBounds(), grid.level, featureset);
    }
}
