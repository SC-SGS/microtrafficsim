package microtrafficsim.core.simulation.builder.impl;

import microtrafficsim.core.entities.vehicle.VehicleEntity;
import microtrafficsim.core.entities.vehicle.VisualizationVehicleEntity;
import microtrafficsim.core.logic.Node;
import microtrafficsim.core.logic.Route;
import microtrafficsim.core.logic.vehicles.AbstractVehicle;
import microtrafficsim.core.logic.vehicles.impl.Car;
import microtrafficsim.core.map.area.Area;
import microtrafficsim.core.shortestpath.ShortestPathAlgorithm;
import microtrafficsim.core.simulation.builder.Builder;
import microtrafficsim.core.simulation.configs.SimulationConfig;
import microtrafficsim.core.simulation.scenarios.Scenario;
import microtrafficsim.core.simulation.scenarios.containers.VehicleContainer;
import microtrafficsim.core.simulation.utils.ODMatrix;
import microtrafficsim.core.simulation.utils.SparseODMatrix;
import microtrafficsim.interesting.progressable.ProgressListener;
import microtrafficsim.math.Distribution;
import microtrafficsim.utils.StringUtils;
import microtrafficsim.utils.collections.Triple;
import microtrafficsim.utils.concurrency.delegation.DynamicThreadDelegator;
import microtrafficsim.utils.logging.EasyMarkableLogger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Supplier;

/**
 * This class is an implementation of {@link Builder} for {@link AbstractVehicle}s.
 *
 * @author Dominic Parga Cacheiro
 */
public class VehicleSimulationBuilder implements Builder {

    private Logger logger = new EasyMarkableLogger(VehicleSimulationBuilder.class);

    private long seed;
    private Random random;
    // used for printing vehicle creation process
    private int           lastPercentage;
    private final Integer percentageDelta; // > 0 !!!
    // used for vehicle creation
    private final Supplier<VisualizationVehicleEntity> vehicleFactory;

    /**
     * Default constructor
     *
     * @param seed This seed is used for an instance of {@link Random} used for the creation of the
     *             origin-destination-matrix in {@link #prepare(Scenario, ProgressListener)}
     * @param vehicleFactory is used for creating the visualized component of a vehicle
     */
    public VehicleSimulationBuilder(final long seed, final Supplier<VisualizationVehicleEntity> vehicleFactory) {

        lastPercentage  = 0;
        percentageDelta = 5;
        this.seed = seed;
        this.random = new Random(seed);

        this.vehicleFactory = vehicleFactory;

    }

    /**
     * Addition to superclass-doc: If the ODMatrix should be created using origin/destination fields, all nodes are
     * collected in one set, so there are no duplicates and the nodes are chosen distributed uniformly at random.
     */
    @Override
    public Scenario prepare(final Scenario scenario, final ProgressListener listener) {
        logger.info("PREPARING SCENARIO started");
        long time_preparation = System.nanoTime();

        /*
        |===========|
        | reset all |
        |===========|
        */
        logger.info("RESETTING SCENARIO started");
        scenario.setPrepared(false);
        scenario.getVehicleContainer().clearAll();
        scenario.getGraph().reset();
        logger.info("RESETTING SCENARIO finished");

        /*
        |=======================|
        | create vehicle routes |
        |=======================|
        */
        logger.info("CREATING VEHICLES started");
        long time_routes = System.nanoTime();

        if (scenario.getConfig().multiThreading.nThreads > 1)
            multiThreadedVehicleCreation(scenario, listener);
        else
            singleThreadedVehicleCreation(scenario, listener);

        time_routes = System.nanoTime() - time_routes;
        logger.info(StringUtils.buildTimeString(
                "CREATING VEHICLES finished after ",
                time_routes,
                "ns"
        ).toString());

        /*
        |==========================|
        | finish building scenario |
        |==========================|
        */
        scenario.setPrepared(true);
        time_preparation = System.nanoTime() - time_preparation;
        logger.info(StringUtils.buildTimeString(
                "PREPARING SCENARIOS finished after ",
                time_preparation,
                "ns"
        ).toString());

        return scenario;
    }

    private void multiThreadedVehicleCreation(final Scenario scenario, final ProgressListener listener) {

        // general attributes for this
        final SimulationConfig config = scenario.getConfig();
        lastPercentage = 0;
        final AtomicInteger finishedVehiclesCount = new AtomicInteger(0);

        // create vehicles with empty routes and add them to the scenario (sequentially for determinism)
        for (Triple<Node, Node, Integer> triple : scenario.getODMatrix()) {
            Node start = triple.obj0;
            Node end = triple.obj1;
            int routeCount = triple.obj2;

            for (int i = 0; i < routeCount; i++) { // "synchronized"
                long id = config.longIDGenerator.next();
                long seed = config.seedGenerator.next();
                Route<Node> route = new Route<>(start, end);
                AbstractVehicle vehicle = createVehicle(id, seed, route, scenario);
                scenario.getVehicleContainer().addVehicle(vehicle);
            }
        }

        // calculate routes multithreaded
        new DynamicThreadDelegator(config.multiThreading.nThreads).doTask(
                vehicle -> {
                    ShortestPathAlgorithm scout = scenario.getScoutFactory().get();
                    Route<Node> route = vehicle.getRoute();
                    scout.findShortestPath(route.getStart(), route.getEnd(), route);
                    vehicle.registerInGraph();
                    int bla = finishedVehiclesCount.incrementAndGet();
                    logProgress(bla, config.maxVehicleCount, listener);
                },
                scenario.getVehicleContainer().getVehicles().iterator(),
                config.multiThreading.vehiclesPerRunnable);
    }

    private void singleThreadedVehicleCreation(final Scenario scenario, final ProgressListener listener) {

        lastPercentage = 0;

        int vehicleCount = 0;
        for (Triple<Node, Node, Integer> triple : scenario.getODMatrix()) {
            Node start = triple.obj0;
            Node end = triple.obj1;
            int routeCount = triple.obj2;

            for (int i = 0; i < routeCount; i++) {
                long id = scenario.getConfig().longIDGenerator.next();
                long seed = scenario.getConfig().seedGenerator.next();
                Route<Node> route = new Route<>(start, end);
                AbstractVehicle vehicle = createVehicle(id, seed, route, scenario);
                scenario.getVehicleContainer().addVehicle(vehicle);
                scenario.getScoutFactory().get().findShortestPath(start, end, route);
                vehicle.registerInGraph();
                logProgress(vehicleCount, scenario.getConfig().maxVehicleCount, listener);
            }

            vehicleCount += routeCount;
        }
    }

    private AbstractVehicle createVehicle(final long id, final long seed, Route<Node> route, Scenario scenario) {

        // init stuff
        VehicleContainer vehicleContainer = scenario.getVehicleContainer();

        // create vehicle components
        AbstractVehicle vehicle = new Car(id, seed, vehicleContainer, route);
        VisualizationVehicleEntity visCar;
        synchronized (vehicleFactory) {
            visCar = vehicleFactory.get();
        }

        // create vehicle entity
        VehicleEntity entity = new VehicleEntity(vehicle, visCar);
        vehicle.setEntity(entity);
        visCar.setEntity(entity);

        return vehicle;
    }

    private synchronized void logProgress(int finished, int total, ProgressListener listener) {
        int percentage = (100 * finished) / total;
        if (percentage - lastPercentage >= percentageDelta) {
            logger.info(percentage + "% vehicles created.");
            if (listener != null) listener.didProgress(percentage);
            lastPercentage += percentageDelta;
        }
    }
}
