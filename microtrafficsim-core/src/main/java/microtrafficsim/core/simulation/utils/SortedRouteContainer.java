package microtrafficsim.core.simulation.utils;

import microtrafficsim.core.logic.routes.Route;
import microtrafficsim.core.logic.vehicles.machines.Vehicle;
import microtrafficsim.core.simulation.scenarios.Scenario;

import java.util.ArrayList;

/**
 * @author Dominic Parga Cacheiro
 */
public class SortedRouteContainer extends ArrayList<Route> implements RouteContainer {
    @Override
    public void addAll(Scenario scenario) {
        for (Vehicle vehicle : scenario.getVehicleContainer()) {
            Route route = vehicle.getDriver().getRoute();
            if (!route.isEmpty())
                add(route);
        }
    }
}
