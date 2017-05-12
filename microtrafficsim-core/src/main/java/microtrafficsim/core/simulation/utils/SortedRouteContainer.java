package microtrafficsim.core.simulation.utils;

import microtrafficsim.core.logic.routes.Route;
import microtrafficsim.core.simulation.scenarios.Scenario;
import microtrafficsim.utils.collections.FastSortedArrayList;

/**
 * @author Dominic Parga Cacheiro
 */
public class SortedRouteContainer extends FastSortedArrayList<Route> implements RouteContainer {
    @Override
    public void addAll(Scenario scenario) {
        // todo
        throw new UnsupportedOperationException("Not implemented yet");
    }
}
