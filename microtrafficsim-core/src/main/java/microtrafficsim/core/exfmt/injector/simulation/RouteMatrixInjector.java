package microtrafficsim.core.exfmt.injector.simulation;

import microtrafficsim.core.exfmt.Container;
import microtrafficsim.core.exfmt.ExchangeFormat;
import microtrafficsim.core.exfmt.base.ScenarioRouteInfo;
import microtrafficsim.core.simulation.utils.RouteMatrix;

/**
 * @author Dominic Parga Cacheiro
 */
public class RouteMatrixInjector implements ExchangeFormat.Injector<RouteMatrix> {

    @Override
    public void inject(ExchangeFormat fmt, ExchangeFormat.Context ctx, Container dst, RouteMatrix src)
            throws Exception {
        ScenarioRouteInfo info = new ScenarioRouteInfo();
        info.set(src);
        dst.set(info);
    }
}
