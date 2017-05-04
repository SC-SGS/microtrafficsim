package microtrafficsim.core.exfmt.injector.simulation;

import microtrafficsim.core.exfmt.Container;
import microtrafficsim.core.exfmt.ExchangeFormat;
import microtrafficsim.core.exfmt.base.ScenarioRouteSet;
import microtrafficsim.core.logic.vehicles.machines.Vehicle;
import microtrafficsim.core.simulation.scenarios.containers.VehicleContainer;

/**
 * @author Dominic Parga Cacheiro
 */
public class RouteInjector implements ExchangeFormat.Injector<VehicleContainer> {

    @Override
    public void inject(ExchangeFormat fmt, ExchangeFormat.Context ctx, Container dst, VehicleContainer src)
            throws Exception {

        ScenarioRouteSet routes = new ScenarioRouteSet();
        for (Vehicle vehicle : src) {
            routes.put(vehicle.getDriver().getRoute());
        }
        dst.set(routes);
    }
}
