package microtrafficsim.core.exfmt.injector.simulation;

import microtrafficsim.core.exfmt.Container;
import microtrafficsim.core.exfmt.ExchangeFormat;
import microtrafficsim.core.exfmt.base.ScenarioRouteSet;
import microtrafficsim.core.logic.vehicles.machines.Vehicle;
import microtrafficsim.core.simulation.scenarios.containers.VehicleContainer;
import microtrafficsim.core.simulation.utils.RouteMatrix;

/**
 * @author Dominic Parga Cacheiro
 */
public class RouteMatrixInjector implements ExchangeFormat.Injector<RouteMatrix> {

    @Override
    public void inject(ExchangeFormat fmt, ExchangeFormat.Context ctx, Container dst, RouteMatrix src)
            throws Exception {

        ScenarioRouteSet routes = new ScenarioRouteSet();
        routes.set(src);
        dst.set(routes);
    }
}
