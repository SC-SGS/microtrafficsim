package microtrafficsim.core.exfmt.extractor.map;

import microtrafficsim.core.exfmt.Container;
import microtrafficsim.core.exfmt.ExchangeFormat;
import microtrafficsim.core.exfmt.base.GeometryEntitySet;
import microtrafficsim.core.exfmt.base.FeatureInfo;
import microtrafficsim.core.exfmt.base.TileGridInfo;
import microtrafficsim.core.exfmt.ecs.Entity;
import microtrafficsim.core.exfmt.ecs.FeatureManager;
import microtrafficsim.core.exfmt.ecs.components.FeatureComponent;
import microtrafficsim.core.exfmt.ecs.components.TileGridComponent;
import microtrafficsim.core.exfmt.ecs.entities.LineEntity;
import microtrafficsim.core.exfmt.ecs.entities.PointEntity;
import microtrafficsim.core.exfmt.ecs.entities.PolygonEntity;
import microtrafficsim.core.exfmt.exceptions.NotAvailableException;
import microtrafficsim.core.map.FeatureDescriptor;
import microtrafficsim.core.map.FeaturePrimitive;
import microtrafficsim.core.map.tiles.FeatureGrid;
import microtrafficsim.core.map.tiles.QuadTreeTiledMapSegment;
import microtrafficsim.core.map.tiles.QuadTreeTilingScheme;
import microtrafficsim.core.map.tiles.TilingScheme;
import microtrafficsim.core.vis.map.projections.MercatorProjection;
import microtrafficsim.math.Vec2i;
import microtrafficsim.utils.collections.Grid;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;


/**
 * @author Maximilian Luz
 */
public class QuadTreeTiledMapSegmentExtractor implements ExchangeFormat.Extractor<QuadTreeTiledMapSegment> {

    @Override
    public QuadTreeTiledMapSegment extract(ExchangeFormat fmt, ExchangeFormat.Context ctx, Container src)
            throws Exception
    {
        GeometryEntitySet entities = src.get(GeometryEntitySet.class);
        FeatureInfo features = src.get(FeatureInfo.class);
        TileGridInfo tiles = src.get(TileGridInfo.class);
        if (entities == null | features == null | tiles == null)
            throw new NotAvailableException();

        Config config = fmt.getConfig().getOr(Config.class, Config::getDefault);
        TileGridInfo.Grid grid = config.matcher.call(config, tiles);
        if (grid == null)
            throw new NotAvailableException("A TileGridSet is required to extract a QuadTreeTiledMapSegment");

        HashMap<String, FeatureGrid<?>> featureset = new HashMap<>();

        // prepare feature grid
        int nx = grid.level.getTilesX();
        int ny = grid.level.getTilesY();
        for (FeatureDescriptor desc : features.getAll().values()) {
            featureset.put(desc.getName(), createGrid(desc.getName(), desc.getType(), nx, ny));
        }

        FeatureManager extractors = fmt.getConfig().getOr(FeatureManager.class, FeatureManager::new);

        // fill feature grid
        for (PointEntity entity : entities.getPoints().values()) {
            process(fmt, ctx, src, entities, extractors, grid, featureset, entity);
        }

        for (LineEntity entity : entities.getLines().values()) {
            process(fmt, ctx, src, entities, extractors, grid, featureset, entity);
        }

        for (PolygonEntity entity : entities.getPolygons().values()) {
            process(fmt, ctx, src, entities, extractors, grid, featureset, entity);
        }

        return new QuadTreeTiledMapSegment(grid.scheme, entities.getBounds(), grid.level, featureset);
    }


    private <T extends FeaturePrimitive> FeatureGrid<T> createGrid(String name, Class<T> type, int nx, int ny) {
        Grid<List<T>> data = new Grid<>(nx, ny);

        for (int y = 0; y < ny; y++) {
            for (int x = 0; x < nx; x++) {
                data.set(x, y, new ArrayList<>());
            }
        }

        return new FeatureGrid<>(name, type, data);
    }

    @SuppressWarnings("unchecked")
    private void process(ExchangeFormat fmt, ExchangeFormat.Context ctx, Container src, GeometryEntitySet ecs,
                         FeatureManager extractors, TileGridInfo.Grid grid, HashMap<String, FeatureGrid<?>> dst,
                         Entity entity) {
        FeatureComponent fc = entity.get(FeatureComponent.class);
        if (fc == null) return;

        TileGridComponent tc = entity.get(TileGridComponent.class);
        if (tc == null) return;

        // get all tiles
        ArrayList<Vec2i> tiles = new ArrayList<>();
        for (TileGridComponent.Entry te : tc.getAll()) {
            if (!grid.scheme.equals(te.getScheme()) || !grid.level.equals(te.getLevel()))
                continue;

            tiles.add(new Vec2i(te.getX(), te.getY()));
        }

        if (tiles.isEmpty()) return;

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

            FeatureGrid<?>  fgrid = dst.get(fd.getName());
            Grid<? extends List<FeaturePrimitive>> data = (Grid<? extends List<FeaturePrimitive>>) fgrid.getData();
            for (Vec2i id : tiles)
                data.get(id.x, id.y).add(primitive);
        }
    }


    public static class Config extends microtrafficsim.core.exfmt.Config.Entry {
        public TilingScheme scheme;
        public int level;
        public GridMatcher matcher;

        public static Config getDefault() {
            return getDefault(new QuadTreeTilingScheme(new MercatorProjection()), 12);
        }

        public static Config getDefault(TilingScheme scheme, int level) {
            Config cfg = new Config();
            cfg.scheme = scheme;
            cfg.level = level;
            cfg.matcher = GridMatcher.CLOSEST;

            return cfg;
        }
    }

    public interface GridMatcher {
        TileGridInfo.Grid call(Config cfg, TileGridInfo set);

        GridMatcher EXACT = (cfg, set) -> {
            for (TileGridInfo.Grid grid : set.getAll())
                if (grid.scheme.equals(cfg.scheme) && grid.level.zoom == cfg.level)
                    return grid;

            return null;
        };

        GridMatcher CLOSEST = (cfg, set) -> {
            if (set.getAll().isEmpty()) return null;

            for (TileGridInfo.Grid grid : set.getAll())
                if (grid.scheme.equals(cfg.scheme) && grid.level.zoom == cfg.level)
                    return grid;

            for (TileGridInfo.Grid grid : set.getAll())
                if (grid.scheme.equals(cfg.scheme))
                    return grid;

            for (TileGridInfo.Grid grid : set.getAll())
                if (grid.level.zoom == cfg.level)
                    return grid;

            return set.getAll().iterator().next();
        };

        GridMatcher ANY = (cfg, set) -> {
            if (set.getAll().isEmpty()) return null;
            return set.getAll().iterator().next();
        };
    }
}
