package microtrafficsim.core.exfmt.ecs;

import microtrafficsim.core.exfmt.Config;
import microtrafficsim.core.exfmt.Container;
import microtrafficsim.core.exfmt.ExchangeFormat;

import java.util.ArrayList;


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


    public interface Processor {
        void process(ExchangeFormat fmt, ExchangeFormat.Context ctx, Container container, Entity entity);
    }
}
