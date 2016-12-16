package microtrafficsim.core.simulation.scenarios.impl;

import microtrafficsim.core.logic.StreetGraph;
import microtrafficsim.core.shortestpath.ShortestPathAlgorithm;
import microtrafficsim.core.simulation.configs.SimulationConfig;
import microtrafficsim.core.simulation.scenarios.containers.VehicleContainer;

import java.util.function.Supplier;

/**
 * @author Dominic Parga Cacheiro
 */
public class RandomRouteScenario extends BasicScenario {

    /**
     * Default constructor
     *
     * @param config
     * @param graph
     * @param vehicleContainer
     */
    public RandomRouteScenario(SimulationConfig config, StreetGraph graph, VehicleContainer vehicleContainer) {
        super(config, graph, vehicleContainer);
    }

    @Override
    public Supplier<ShortestPathAlgorithm> getScoutFactory() {
        return null;
    }
}
