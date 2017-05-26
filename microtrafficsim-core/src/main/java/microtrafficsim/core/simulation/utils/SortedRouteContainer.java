package microtrafficsim.core.simulation.utils;

import microtrafficsim.core.logic.routes.Route;
import microtrafficsim.core.simulation.scenarios.Scenario;

import java.util.ArrayList;

/**
 * @author Dominic Parga Cacheiro
 */
public class SortedRouteContainer extends ArrayList<Route> implements RouteContainer {
    @Override
    public void addAll(Scenario scenario) {
        addAll(scenario.getRoutes());
    }
}
