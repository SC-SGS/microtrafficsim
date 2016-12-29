package logic.validation.scenarios;

import microtrafficsim.core.logic.StreetGraph;
import microtrafficsim.core.shortestpath.ShortestPathAlgorithm;
import microtrafficsim.core.shortestpath.astar.impl.LinearDistanceBidirectionalAStar;
import microtrafficsim.core.simulation.builder.ScenarioBuilder;
import microtrafficsim.core.simulation.configs.SimulationConfig;
import microtrafficsim.core.simulation.scenarios.containers.VehicleContainer;
import microtrafficsim.core.simulation.scenarios.impl.BasicScenario;
import microtrafficsim.core.simulation.utils.ODMatrix;
import microtrafficsim.core.simulation.utils.UnmodifiableODMatrix;
import microtrafficsim.utils.id.ConcurrentLongIDGenerator;
import microtrafficsim.utils.id.ConcurrentSeedGenerator;

import java.util.function.Supplier;

/**
 * @author Dominic Parga Cacheiro
 */
public abstract class ValidationScenario extends BasicScenario {

    private ShortestPathAlgorithm scout;

    protected ValidationScenario(SimulationConfig config, StreetGraph graph, VehicleContainer vehicleContainer) {
        super(config, graph, vehicleContainer);
        scout = new LinearDistanceBidirectionalAStar(config.metersPerCell);
    }

    protected ValidationScenario(SimulationConfig config, StreetGraph graph) {
        super(config, graph);
        scout = new LinearDistanceBidirectionalAStar(config.metersPerCell);
    }

    public static SimulationConfig setupConfig(SimulationConfig config) {

        config.metersPerCell           = 7.5f;
        config.longIDGenerator         = new ConcurrentLongIDGenerator();
        config.seed                    = 1455374755807L;
        config.seedGenerator           = new ConcurrentSeedGenerator(config.seed);
        config.multiThreading.nThreads = 1;

        return config;
    }

    public abstract void prepare();

    /*
    |==============|
    | (i) Scenario |
    |==============|
    */
    @Override
    public Supplier<ShortestPathAlgorithm> getScoutFactory() {
        return () -> scout;
    }
}
