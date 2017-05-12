package microtrafficsim.core.exfmt.injector.simulation;

import microtrafficsim.core.exfmt.Container;
import microtrafficsim.core.exfmt.ExchangeFormat;
import microtrafficsim.core.simulation.utils.RouteContainer;

/**
 * @author Dominic Parga Cacheiro
 */
public class RouteContainerInjector implements ExchangeFormat.Injector<RouteContainer> {

    @Override
    public void inject(ExchangeFormat fmt, ExchangeFormat.Context ctx, Container dst, RouteContainer src)
            throws Exception {
//        ScenarioRouteInfo info = new ScenarioRouteInfo();
//        info.set(src);
//        info.setGraphGUID(src.getGraphGUID());
//        dst.set(info);
    }
}
