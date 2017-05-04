package microtrafficsim.core.exfmt.ecs.processors;

import microtrafficsim.core.exfmt.Container;
import microtrafficsim.core.exfmt.ExchangeFormat;
import microtrafficsim.core.exfmt.ecs.Entity;
import microtrafficsim.core.exfmt.ecs.EntityManager;
import microtrafficsim.core.exfmt.ecs.components.FeatureComponent;
import microtrafficsim.core.map.FeatureDescriptor;


/**
 * @author Maximilian Luz
 */
public class FeatureProcessor implements EntityManager.Processor {

    /**
     * <p>
     * Extracts the {@link FeatureDescriptor feature descriptor} from the given {@link ExchangeFormat.Context context}
     * to store it in the respective {@link  FeatureComponent component} given by {@link Entity entity}.
     *
     * <p>
     * Does nothing if the given context does not contain an entry for {@code TileGridContext}.
     *
     * @param fmt unused
     * @param container unused
     */
    @Override
    public void process(ExchangeFormat fmt, ExchangeFormat.Context ctx, Container container, Entity entity) {
        FeatureDescriptor descriptor = ctx.get(FeatureDescriptor.class);
        if (descriptor == null) return;

        // add component
        FeatureComponent component = entity.get(FeatureComponent.class, () -> new FeatureComponent(entity));
        component.add(descriptor);
    }
}
