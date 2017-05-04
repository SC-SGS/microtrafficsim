package microtrafficsim.core.exfmt.injector.simulation;

import microtrafficsim.core.exfmt.Container;
import microtrafficsim.core.exfmt.ExchangeFormat;
import microtrafficsim.core.exfmt.base.SimulationConfigInfo;
import microtrafficsim.core.simulation.configs.SimulationConfig;

/**
 * @author Dominic Parga Cacheiro
 */
public class SimulationConfigInjector implements ExchangeFormat.Injector<SimulationConfig> {

    /**
     * Stores the given {@link SimulationConfig src} into {@link SimulationConfigInfo an info container} and puts
     * this into the given {@link Container dst}.
     *
     * @param fmt unused
     * @param ctx unused
     * @param dst The given {@code src} is stored here.
     * @param src Is getting stored
     * @throws Exception
     */
    @Override
    public void inject(ExchangeFormat fmt, ExchangeFormat.Context ctx, Container dst, SimulationConfig src) throws Exception {

        SimulationConfigInfo sconfig = new SimulationConfigInfo();
        sconfig.set(src);
        dst.set(sconfig);
    }
}
