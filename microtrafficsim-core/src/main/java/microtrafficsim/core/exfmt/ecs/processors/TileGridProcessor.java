package microtrafficsim.core.exfmt.ecs.processors;

import microtrafficsim.core.exfmt.Container;
import microtrafficsim.core.exfmt.ExchangeFormat;
import microtrafficsim.core.exfmt.base.TileGridSet;
import microtrafficsim.core.exfmt.context.TileGridContext;
import microtrafficsim.core.exfmt.ecs.Entity;
import microtrafficsim.core.exfmt.ecs.EntityManager;
import microtrafficsim.core.exfmt.ecs.components.TileGridComponent;


public class TileGridProcessor implements EntityManager.Processor {

    @Override
    public void process(ExchangeFormat fmt, ExchangeFormat.Context ctx, Container container, Entity entity) {
        TileGridContext state = ctx.get(TileGridContext.class);
        if (state == null) return;

        // add component
        TileGridComponent component = entity.get(TileGridComponent.class, () -> new TileGridComponent(entity));
        component.add(new TileGridComponent.Entry(state.scheme, state.grid, state.x, state.y));

        // insert in tile-grid-set
        TileGridSet.TileGrid grid = container.get(TileGridSet.class, TileGridSet::new).getOrCreate(state.scheme, state.grid);
        grid.entities.add(entity);
    }
}
