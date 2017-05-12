package microtrafficsim.core.simulation.scenarios.impl;

import microtrafficsim.core.logic.streetgraph.Graph;
import microtrafficsim.core.simulation.configs.SimulationConfig;
import microtrafficsim.core.simulation.scenarios.containers.VehicleContainer;
import microtrafficsim.math.random.distributions.impl.Random;

/**
 * <p>
 * Defines one origin and one destination field around the whole graph using its latitude and longitude borders.
 *
 * @author Dominic Parga Cacheiro
 */
public class RandomRouteScenario extends AreaScenario {
    public RandomRouteScenario(long seed, SimulationConfig config, Graph graph) {
        super(seed, config, graph);
    }

    public RandomRouteScenario(Random random, SimulationConfig config, Graph graph) {
        super(random, config, graph);
    }

    public RandomRouteScenario(long seed, SimulationConfig config, Graph graph, VehicleContainer vehicleContainer) {
        super(seed, config, graph, vehicleContainer);
    }

    public RandomRouteScenario(Random random, SimulationConfig config, Graph graph, VehicleContainer vehicleContainer) {
        super(random, config, graph, vehicleContainer);
    }
}