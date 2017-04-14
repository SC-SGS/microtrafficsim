package microtrafficsim.core.exfmt.injector.map;

import microtrafficsim.core.exfmt.Container;
import microtrafficsim.core.exfmt.base.EntitySet;
import microtrafficsim.core.exfmt.ExchangeFormat;
import microtrafficsim.core.map.tiles.FeatureGrid;
import microtrafficsim.core.map.tiles.QuadTreeTiledMapSegment;
import microtrafficsim.core.map.tiles.TileFeatureGrid;


public class QuadTreeTiledMapSegmentInjector implements ExchangeFormat.Injector<QuadTreeTiledMapSegment> {

    @Override
    public void inject(ExchangeFormat fmt, ExchangeFormat.Context ctx, Container dst, QuadTreeTiledMapSegment src)
            throws Exception
    {
        EntitySet entities = dst.get(EntitySet.class, EntitySet::new);
        entities.updateBounds(src.getBounds());

        for (FeatureGrid<?> feature : src.getFeatureSet().values()) {
            fmt.inject(ctx, dst, new TileFeatureGrid<>(feature, src.getTilingScheme(), src.getLeafTiles()));
        }
    }
}
