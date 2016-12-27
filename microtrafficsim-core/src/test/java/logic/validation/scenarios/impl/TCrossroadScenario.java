package logic.validation.scenarios.impl;

import logic.validation.scenarios.ValidationScenario;
import microtrafficsim.core.logic.StreetGraph;
import microtrafficsim.core.shortestpath.ShortestPathAlgorithm;
import microtrafficsim.core.shortestpath.astar.impl.LinearDistanceBidirectionalAStar;
import microtrafficsim.core.simulation.configs.SimulationConfig;
import microtrafficsim.utils.id.ConcurrentLongIDGenerator;
import microtrafficsim.utils.id.ConcurrentSeedGenerator;

import java.util.function.Supplier;

/**
 * @author Dominic Parga Cacheiro
 */
public class TCrossroadScenario extends ValidationScenario {

    private SimulationConfig config;
    private ShortestPathAlgorithm scout;

    public TCrossroadScenario() {
        super("T_crossroad.osm");

        // setup config
        config = new SimulationConfig();
        config.speedup                                 = 5;
        config.maxVehicleCount                         = 3;
        config.crossingLogic.drivingOnTheRight         = true;
        config.crossingLogic.edgePriorityEnabled       = true;
        config.crossingLogic.priorityToTheRightEnabled = true;
        config.crossingLogic.setOnlyOneVehicle(false);
        config.crossingLogic.friendlyStandingInJamEnabled = false;
        config.ageForPause = -1;

        // routing
        scout = new LinearDistanceBidirectionalAStar(config.metersPerCell);
    }

    /*
    |==============|
    | (i) Scenario |
    |==============|
    */
    @Override
    public SimulationConfig getConfig() {
        return config;
    }

    @Override
    public StreetGraph getGraph() {
        return null;
    }

    @Override
    public Supplier<ShortestPathAlgorithm> getScoutFactory() {
        return () -> scout;
    }
}
