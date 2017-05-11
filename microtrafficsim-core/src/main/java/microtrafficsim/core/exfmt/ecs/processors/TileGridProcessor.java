package microtrafficsim.core.exfmt.ecs.processors;

import microtrafficsim.core.exfmt.Container;
import microtrafficsim.core.exfmt.ExchangeFormat;
import microtrafficsim.core.exfmt.context.TileGridContext;
import microtrafficsim.core.exfmt.ecs.Entity;
import microtrafficsim.core.exfmt.ecs.EntityManager;
import microtrafficsim.core.exfmt.ecs.components.TileGridComponent;


/**
 * @author Maximilian Luz
 */
public class TileGridProcessor implements EntityManager.Processor {

    /**
     * <p>
     * Extracts the {@link TileGridContext state} from the given {@link ExchangeFormat.Context context} to store it in
     * the given {@link Entity entity}.
     *
     * <p>
     * Does nothing if the given context does not contain an entry for {@code TileGridContext}.
     *
     * @param fmt unused
     * @param container unused
     */
    @Override
    public void process(ExchangeFormat fmt, ExchangeFormat.Context ctx, Container container, Entity entity) {
        /* extract state from context */
        TileGridContext state = ctx.get(TileGridContext.class);
        if (state == null) return;

        /* add component */
        TileGridComponent component = entity.get(TileGridComponent.class, () -> new TileGridComponent(entity));
        component.add(new TileGridComponent.Entry(state.scheme, state.grid, state.x, state.y));
    }
}
