package microtrafficsim.core.exfmt.ecs;

import microtrafficsim.core.exfmt.Config;
import microtrafficsim.core.exfmt.Container;
import microtrafficsim.core.exfmt.ExchangeFormat;

import java.util.ArrayList;


/**
 * @author Maximilian Luz
 */
public class EntityManager extends Config.Entry {
    private ArrayList<Processor> processors = new ArrayList<>();


    public void addProcessor(Processor processor) {
        this.processors.add(processor);
    }

    public void removeProcessor(Processor processor) {
        this.processors.remove(processor);
    }

    public boolean containsProcessor(Processor processor) {
        return this.processors.contains(processor);
    }


    public void process(ExchangeFormat fmt, ExchangeFormat.Context ctx, Container container, Entity entity) {
        for (Processor processor : processors)
            processor.process(fmt, ctx, container, entity);
    }


    /**
     * This processor can be added to an {@link EntityManager entity manager}, so it will be processed in
     * injections/extractions.
     */
    public interface Processor {
        /**
         * Takes information from the given {@code context} and adds it to the given {@code entity}.
         *
         * @param fmt
         * @param ctx
         * @param container
         * @param entity
         */
        void process(ExchangeFormat fmt, ExchangeFormat.Context ctx, Container container, Entity entity);
    }
}
