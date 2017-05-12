package microtrafficsim.core.simulation.utils;

import microtrafficsim.core.logic.routes.Route;
import microtrafficsim.core.simulation.scenarios.Scenario;

import java.util.Collection;

/**
 * @author Dominic Parga Cacheiro
 */
public interface RouteContainer extends Collection<Route> {
    void addAll(Scenario scenario);
}
