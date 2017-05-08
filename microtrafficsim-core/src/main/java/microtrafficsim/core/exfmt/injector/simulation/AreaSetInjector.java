package microtrafficsim.core.exfmt.injector.simulation;

import microtrafficsim.core.exfmt.Container;
import microtrafficsim.core.exfmt.ExchangeFormat;
import microtrafficsim.core.exfmt.base.ScenarioAreaSet;

/**
 * @author Dominic Parga Cacheiro
 */
public class AreaSetInjector implements ExchangeFormat.Injector<ScenarioAreaSet> {

    /**
     * @param fmt
     * @param ctx
     * @param dst
     * @param src
     * @throws Exception
     */
    @Override
    public void inject(ExchangeFormat fmt, ExchangeFormat.Context ctx, Container dst, ScenarioAreaSet src) throws Exception {
        dst.set(src);
    }
}
