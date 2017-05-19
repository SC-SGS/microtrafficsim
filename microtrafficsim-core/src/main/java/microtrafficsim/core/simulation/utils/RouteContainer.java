package microtrafficsim.core.simulation.utils;

import microtrafficsim.core.logic.nodes.Node;
import microtrafficsim.core.logic.routes.MetaRoute;
import microtrafficsim.core.logic.routes.Route;
import microtrafficsim.core.simulation.scenarios.Scenario;

import java.util.Collection;

/**
 * @author Dominic Parga Cacheiro
 */
public interface RouteContainer extends Collection<Route> {
    /**
     * Adds a {@link MetaRoute} with given parameters
     */
    default void add(Node origin, Node destination) {
        add(new MetaRoute(origin, destination));
    }

    /**
     * Adds a {@link MetaRoute} with given parameters
     */
    default void add(Node origin, Node destination, int spawnDelay) {
        add(new MetaRoute(origin, destination, spawnDelay));
    }

    /**
     * Adds a {@link MetaRoute} with given parameters multiple (= parameter {@code count}) times
     */
    default void add(int count, Node origin, Node destination) {
        for (int i = 0; i < count; i++)
            add(new MetaRoute(origin, destination));
    }

    /**
     * Adds a {@link MetaRoute} with given parameters multiple (= parameter {@code count}) times
     */
    default void add(int count, Node origin, Node destination, int spawnDelay) {
        for (int i = 0; i < count; i++)
            add(new MetaRoute(origin, destination, spawnDelay));
    }

    void addAll(Scenario scenario);
}
