package microtrafficsim.core.exfmt.injector.map.features;

import microtrafficsim.core.exfmt.Container;
import microtrafficsim.core.exfmt.ExchangeFormat;
import microtrafficsim.core.exfmt.context.TileGridContext;
import microtrafficsim.core.exfmt.ecs.processors.TileGridProcessor;
import microtrafficsim.core.map.FeatureDescriptor;
import microtrafficsim.core.map.FeaturePrimitive;
import microtrafficsim.core.map.tiles.TileFeatureGrid;
import microtrafficsim.core.map.tiles.TileRect;
import microtrafficsim.core.map.tiles.TilingScheme;
import microtrafficsim.utils.collections.Grid;

import java.util.List;


public class TileFeatureGridInjector implements ExchangeFormat.Injector<TileFeatureGrid> {

    @Override
    @SuppressWarnings("unchecked")
    public void inject(ExchangeFormat fmt, ExchangeFormat.Context ctx, Container dst, TileFeatureGrid src)
            throws Exception
    {
        TileGridContext state = new TileGridContext(src.getTilingScheme(), src.getLevel());

        ctx.set(FeatureDescriptor.class, src.getDescriptor());
        ctx.set(TileGridContext.class, state);

        Grid<? extends List<? extends FeaturePrimitive>> data = src.getData();
        int nx = data.getSizeX();
        int ny = data.getSizeY();

        for (int y = 0; y < ny; y++) {
            state.y = y;

            for (int x = 0; x < nx; x++) {
                state.x = x;

                for (FeaturePrimitive p : data.get(x, y)) {
                    fmt.inject(ctx, dst, p);
                }
            }
        }

        ctx.remove(TileGridContext.class);
        ctx.remove(FeatureDescriptor.class);
    }
}
