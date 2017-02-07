package microtrafficsim.core.simulation.scenarios.impl;

import microtrafficsim.core.logic.StreetGraph;
import microtrafficsim.core.shortestpath.ShortestPathAlgorithm;
import microtrafficsim.core.shortestpath.astar.impl.LinearDistanceBidirectionalAStar;
import microtrafficsim.core.simulation.builder.ScenarioBuilder;
import microtrafficsim.core.simulation.configs.ScenarioConfig;
import microtrafficsim.core.simulation.core.Simulation;
import microtrafficsim.core.simulation.scenarios.Scenario;
import microtrafficsim.core.simulation.scenarios.containers.VehicleContainer;
import microtrafficsim.core.simulation.scenarios.containers.impl.ConcurrentVehicleContainer;
import microtrafficsim.core.simulation.utils.ODMatrix;
import microtrafficsim.core.simulation.utils.SparseODMatrix;
import microtrafficsim.core.simulation.utils.UnmodifiableODMatrix;

import java.util.ArrayList;
import java.util.function.Supplier;

/**
 * This scenario defines different scenarios in a queue, which can be executed after each other. The scenarios are
 * getting prepared/calculated on the fly, so this class is made only for small scenarios due to runtime.
 *
 * @author Dominic Parga Cacheiro
 */
public abstract class QueueScenarioSmall implements Scenario {

    /* general */
    private final ScenarioConfig config;
    private final StreetGraph graph;
    private final VehicleContainer vehicleContainer;
    private ShortestPathAlgorithm scout;
    private boolean isPrepared;

    /* scenario definition */
    private ArrayList<ODMatrix> odMatrices;
    private ArrayList<ODMatrix> spawnDelayMatrices;
    private int curIdx;
    private boolean isLooping;

    /* scenario building */
    private ScenarioBuilder scenarioBuilder;

    /**
     * Default constructor. After calling super(...) you should call {@link #setScenarioBuilder(ScenarioBuilder)}
     *
     * @param config this config is used for internal settings and should be set already
     * @param graph used for route definitions etc.
     * @param vehicleContainer stores and manages vehicles running in this scenario
     */
    protected QueueScenarioSmall(ScenarioConfig config,
                                 StreetGraph graph,
                                 VehicleContainer vehicleContainer) {

        /* general */
        this.config           = config;
        this.graph            = graph;
        this.vehicleContainer = vehicleContainer;
        scout                 = new LinearDistanceBidirectionalAStar(config.metersPerCell);

        /* scenario definition */
        odMatrices         = new ArrayList<>();
        spawnDelayMatrices = new ArrayList<>();
        curIdx             = -1;
        isLooping          = false;
    }

    /**
     * Just calls {@code QueueScenario(config, graph, new ConcurrentVehicleContainer())}.
     *
     * @see ConcurrentVehicleContainer
     * @see QueueScenarioSmall#QueueScenarioSmall(ScenarioConfig, StreetGraph, VehicleContainer)
     */
    protected QueueScenarioSmall(ScenarioConfig config,
                                 StreetGraph graph) {
        this(config, graph, new ConcurrentVehicleContainer());
    }

    protected void setScenarioBuilder(ScenarioBuilder scenarioBuilder) {
        this.scenarioBuilder = scenarioBuilder;
    }

    public static ScenarioConfig setupConfig(ScenarioConfig config) {

        config.metersPerCell           = 7.5f;
        config.seed                    = 1455374755807L;
        config.multiThreading.nThreads = 1;

        config.speedup                                 = 5;
        config.crossingLogic.drivingOnTheRight         = true;
        config.crossingLogic.edgePriorityEnabled       = true;
        config.crossingLogic.priorityToTheRightEnabled = true;
        config.crossingLogic.setOnlyOneVehicle(false);

        return config;
    }

    public void setLooping(boolean isLooping) {
        this.isLooping = isLooping;
    }

    public boolean isLooping() {
        return isLooping;
    }

    /*
    |==============|
    | matrix setup |
    |==============|
    */
    /**
     * Calls {@code addSubScenario(odMatrix, new SparseODMatrix())}
     *
     * @see #addSubScenario(ODMatrix, ODMatrix)
     */
    public final void addSubScenario(ODMatrix odMatrix) {
        addSubScenario(odMatrix, new SparseODMatrix());
    }

    /**
     * Adds the given origin-destination-matrix connected with the given spawn-delay-matrix to this scenario's
     * sub-scenario-queue.
     *
     * @param odMatrix origin-destination-matrix
     * @param spawnDelayMatrix spawn-delay-matrix
     */
    public final void addSubScenario(ODMatrix odMatrix, ODMatrix spawnDelayMatrix) {
        odMatrices.add(odMatrix);
        spawnDelayMatrices.add(spawnDelayMatrix);
    }

    public final void prepare() {

        curIdx = curIdx + 1;
        if (!isLooping && curIdx == odMatrices.size()) {
            curIdx = -1;
            return;
        }
        curIdx %= odMatrices.size();

        try {
            scenarioBuilder.prepare(this);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * @return Peeks the first spawn-delay-matrix of the queue.
     */
    public final UnmodifiableODMatrix getSpawnDelayMatrix() {
        return new UnmodifiableODMatrix(spawnDelayMatrices.get(curIdx));
    }

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
            setPrepared(false);
            prepare();
            if (isPrepared()) {
                simulation.setAndInitPreparedScenario(this);

                if (!isPaused)
                    simulation.run();
            }
        }
    }

    /*
    |==============|
    | (i) Scenario |
    |==============|
    */
    @Override
    public final ScenarioConfig getConfig() {
        return config;
    }

    @Override
    public final StreetGraph getGraph() {
        return graph;
    }

    @Override
    public final VehicleContainer getVehicleContainer() {
        return vehicleContainer;
    }

    @Override
    public final void setPrepared(boolean isPrepared) {
        this.isPrepared = isPrepared;
    }

    @Override
    public final boolean isPrepared() {
        return isPrepared;
    }

    /**
     * @return Peeks the first origin-destination-matrix of the queue.
     */
    @Override
    public final UnmodifiableODMatrix getODMatrix() {
        return new UnmodifiableODMatrix(odMatrices.get(curIdx));
    }

    @Override
    public final Supplier<ShortestPathAlgorithm> getScoutFactory() {
        return () -> scout;
    }
}
