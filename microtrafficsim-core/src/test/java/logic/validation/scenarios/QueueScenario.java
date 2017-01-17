package logic.validation.scenarios;

import microtrafficsim.core.logic.StreetGraph;
import microtrafficsim.core.logic.vehicles.impl.Car;
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
import microtrafficsim.utils.id.ConcurrentLongIDGenerator;
import microtrafficsim.utils.id.ConcurrentSeedGenerator;

import java.util.ArrayList;
import java.util.function.Supplier;

/**
 * This scenario defines different scenarios in a queue, which can be executed after each other. The scenarios are
 * calculated on the fly, so this class is only for small scenarios.
 *
 * @author Dominic Parga Cacheiro
 */
public abstract class QueueScenario implements Scenario {

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
    /* scenario building */
    private ScenarioBuilder scenarioBuilder;

    /**
     * Default constructor. After calling super(...) you should call {@link #setScenarioBuilder(ScenarioBuilder)}
     *
     * @param config this config is used for internal settings and should be set already
     * @param graph used for route definitions etc.
     * @param vehicleContainer stores and manages vehicles running in this scenario
     */
    protected QueueScenario(ScenarioConfig config,
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
    }

    /**
     * Just calls {@code QueueScenario(config, graph, new ConcurrentVehicleContainer())}.
     *
     * @see ConcurrentVehicleContainer
     * @see QueueScenario#QueueScenario(ScenarioConfig, StreetGraph, VehicleContainer)
     */
    protected QueueScenario(ScenarioConfig config,
                         StreetGraph graph) {
        this(config, graph, new ConcurrentVehicleContainer());
    }

    protected void setScenarioBuilder(ScenarioBuilder scenarioBuilder) {
        this.scenarioBuilder = scenarioBuilder;
    }

    public static ScenarioConfig setupConfig(ScenarioConfig config) {

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

        curIdx         = (curIdx + 1) % odMatrices.size();

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
     * Empty. For adding matrices, use {@link #addSubScenario(ODMatrix)}
     */
    @Override
    public void setODMatrix(ODMatrix odMatrix) {
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
