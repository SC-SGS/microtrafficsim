package logic.validation.scenarios;

import microtrafficsim.core.logic.StreetGraph;
import microtrafficsim.core.logic.vehicles.impl.Car;
import microtrafficsim.core.shortestpath.ShortestPathAlgorithm;
import microtrafficsim.core.shortestpath.astar.impl.LinearDistanceBidirectionalAStar;
import microtrafficsim.core.simulation.builder.ScenarioBuilder;
import microtrafficsim.core.simulation.configs.SimulationConfig;
import microtrafficsim.core.simulation.core.Simulation;
import microtrafficsim.core.simulation.scenarios.Scenario;
import microtrafficsim.core.simulation.scenarios.containers.VehicleContainer;
import microtrafficsim.core.simulation.scenarios.impl.BasicScenario;
import microtrafficsim.core.simulation.utils.ODMatrix;
import microtrafficsim.core.simulation.utils.SparseODMatrix;
import microtrafficsim.core.simulation.utils.UnmodifiableODMatrix;
import microtrafficsim.utils.id.ConcurrentLongIDGenerator;
import microtrafficsim.utils.id.ConcurrentSeedGenerator;

import java.util.function.Supplier;

/**
 * @author Dominic Parga Cacheiro
 */
public abstract class ValidationScenario extends BasicScenario {

    private ShortestPathAlgorithm scout;
    protected ODMatrix spawnDelayMatrix;

    protected ValidationScenario(SimulationConfig config, StreetGraph graph, VehicleContainer vehicleContainer) {
        super(config, graph, vehicleContainer);
        scout            = new LinearDistanceBidirectionalAStar(config.metersPerCell);
        spawnDelayMatrix = new SparseODMatrix();
    }

    /**
     *
     * @see BasicScenario#BasicScenario(SimulationConfig, StreetGraph)
     */
    protected ValidationScenario(SimulationConfig config, StreetGraph graph) {
        super(config, graph);
        scout            = new LinearDistanceBidirectionalAStar(config.metersPerCell);
        spawnDelayMatrix = new SparseODMatrix();
    }

    public static SimulationConfig setupConfig(SimulationConfig config) {

        config.metersPerCell           = 7.5f;
        config.longIDGenerator         = new ConcurrentLongIDGenerator();
        config.seed                    = 1455374755807L;
        config.seedGenerator           = new ConcurrentSeedGenerator(config.seed);
        config.multiThreading.nThreads = 1;

        config.speedup                                 = 5;
        config.crossingLogic.drivingOnTheRight         = true;
        config.crossingLogic.edgePriorityEnabled       = true;
        config.crossingLogic.priorityToTheRightEnabled = true;
        config.crossingLogic.setOnlyOneVehicle(false);
        Car.setDashAndDawdleFactor(0, 0);

        return config;
    }

    public abstract void prepare();

    /*
    |==================|
    | (i) StepListener |
    |==================|
    */
    @Override
    public void didOneStep(Simulation simulation) {
        if (getVehicleContainer().getVehicleCount() == 0) {
            boolean isPaused = simulation.isPaused();
            simulation.cancel();
            prepare();
            simulation.setAndInitScenario(this);
            if (isPaused)
                simulation.runOneStep();
            else
                simulation.run();
        }
    }

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
