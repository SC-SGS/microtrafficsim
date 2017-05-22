package microtrafficsim.core.exfmt.injector.simulation;

import microtrafficsim.core.exfmt.Container;
import microtrafficsim.core.exfmt.ExchangeFormat;
import microtrafficsim.core.exfmt.base.ScenarioRouteInfo;
import microtrafficsim.core.exfmt.exceptions.ExchangeFormatException;
import microtrafficsim.core.logic.streetgraph.GraphGUID;
import microtrafficsim.core.simulation.utils.RouteContainer;

/**
 * @author Dominic Parga Cacheiro
 */
public class RouteContainerInjector implements ExchangeFormat.Injector<RouteContainer> {

    @Override
    public void inject(ExchangeFormat fmt, ExchangeFormat.Context ctx, Container dst, RouteContainer src)
            throws ExchangeFormatException {
        Config cfg = fmt.getConfig().get(Config.class);
        if (cfg == null) throw new ExchangeFormatException(
                "Config for " + getClass().getSimpleName() + " missing");

        ScenarioRouteInfo info = new ScenarioRouteInfo(cfg.graphGUID, src);
        dst.set(info);
    }


    public static class Config extends microtrafficsim.core.exfmt.Config.Entry {
        public GraphGUID graphGUID;
    }
}
