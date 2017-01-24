package microtrafficsim.core.simulation.builder.impl;

import microtrafficsim.core.entities.vehicle.VehicleEntity;
import microtrafficsim.core.entities.vehicle.VisualizationVehicleEntity;
import microtrafficsim.core.logic.Node;
import microtrafficsim.core.logic.Route;
import microtrafficsim.core.logic.vehicles.AbstractVehicle;
import microtrafficsim.core.logic.vehicles.impl.Car;
import microtrafficsim.core.shortestpath.ShortestPathAlgorithm;
import microtrafficsim.core.simulation.builder.ScenarioBuilder;
import microtrafficsim.core.simulation.configs.ScenarioConfig;
import microtrafficsim.core.simulation.scenarios.Scenario;
import microtrafficsim.interesting.progressable.ProgressListener;
import microtrafficsim.utils.StringUtils;
import microtrafficsim.utils.collections.Triple;
import microtrafficsim.utils.concurrency.delegation.StaticThreadDelegator;
import microtrafficsim.utils.concurrency.delegation.ThreadDelegator;
import microtrafficsim.utils.logging.EasyMarkableLogger;
import org.slf4j.Logger;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiFunction;
import java.util.function.Supplier;

/**
 * This class is an implementation of {@link ScenarioBuilder} for {@link AbstractVehicle}s.
 *
 * @author Dominic Parga Cacheiro
 */
public class VehicleScenarioBuilder implements ScenarioBuilder {

    private Logger logger = new EasyMarkableLogger(VehicleScenarioBuilder.class);

    // used for printing vehicle creation process
    private int           lastPercentage;
    private final Integer percentageDelta; // > 0 !!!
    // used for vehicle creation
    private final BiFunction<Scenario, Route<Node>, AbstractVehicle> vehicleFactory;

    /**
     * Calls {@link VehicleScenarioBuilder#VehicleScenarioBuilder(Supplier, BiFunction)} with basic logic vehicle
     * factory returning {@link Car}s.
     */
    public VehicleScenarioBuilder(Supplier<VisualizationVehicleEntity> visVehicleFactory) {
        this(visVehicleFactory, (scenario, route) -> {
            ScenarioConfig config   = scenario.getConfig();
            long ID                 = config.longIDGenerator.next();
            long vehicleSeed        = config.seedGenerator.next();

            Car car = new Car(ID, vehicleSeed, route);
            car.addStateListener(scenario.getVehicleContainer());
            return car;
        });
    }

    /**
     * Default constructor linking created vehicles (by the given vehicle factories) using {@link VehicleEntity}. The
     * {@code logicVehicleFactory} must be not null, the {@code visVehicleFactory} can be null, which means vehicles are
     * not visualized.
     *
     * @param visVehicleFactory Creates the visualization part of the vehicle entity
     * @param logicVehicleFactory Creates the logic part of the vehicle entity
     */
    public VehicleScenarioBuilder(Supplier<VisualizationVehicleEntity> visVehicleFactory,
                                  BiFunction<Scenario, Route<Node>, AbstractVehicle> logicVehicleFactory) {
        /* general setup */
        lastPercentage  = 0;
        percentageDelta = 5;

        /* vehicle factory */
        if (visVehicleFactory != null) {
            vehicleFactory = (scenario, route) -> {
                // create vehicle components
                AbstractVehicle logicVehicle;
                synchronized (logicVehicleFactory) {
                    logicVehicle = logicVehicleFactory.apply(scenario, route);
                }
                VisualizationVehicleEntity visVehicle;
                synchronized (visVehicleFactory) {
                    visVehicle = visVehicleFactory.get();
                }

                // create vehicle entity
                VehicleEntity entity = new VehicleEntity(logicVehicle, visVehicle);
                logicVehicle.setEntity(entity);
                visVehicle.setEntity(entity);

                return logicVehicle;
            };
        } else {
            vehicleFactory = (scenario, route) -> {
                // create vehicle components
                AbstractVehicle logicVehicle;
                synchronized (logicVehicleFactory) {
                    logicVehicle = logicVehicleFactory.apply(scenario, route);
                }

                // create vehicle entity
                VehicleEntity entity = new VehicleEntity(logicVehicle, null);
                logicVehicle.setEntity(entity);

                return logicVehicle;
            };
        }
    }

    /**
     * Default constructor.
     *
     * @param vehicleFactory is used for creating the vehicle. If a visualization is wished, it is important to link
     *                       the logic part of the vehicle ({@code AbstractVehicle}) with the visualization part
     *                       using a {@link VehicleEntity}. If you are not sure about this, use
     *                       {@link #VehicleScenarioBuilder(Supplier, BiFunction)} without linking.
     */
    public VehicleScenarioBuilder(BiFunction<Scenario, Route<Node>, AbstractVehicle> vehicleFactory) {

        /* general setup */
        lastPercentage  = 0;
        percentageDelta = 5;

        /* vehicle factory */
        this.vehicleFactory = vehicleFactory;
    }

    @Override
    public Scenario prepare(final Scenario scenario, final ProgressListener listener) throws InterruptedException {
        logger.info("PREPARING SCENARIO started");
        long time_preparation = System.nanoTime();

        /* interrupt handling */
        if (Thread.interrupted())
            throw new InterruptedException();

        /* reset all */
        logger.info("RESETTING SCENARIO started");
        scenario.reset();
        logger.info("RESETTING SCENARIO finished");

        /* interrupt handling */
        if (Thread.interrupted()) {
            scenario.reset();
            throw new InterruptedException();
        }

        /* create vehicle routes */
        logger.info("CREATING VEHICLES started");
        long time_routes = System.nanoTime();

        try {
            if (scenario.getConfig().multiThreading.nThreads > 1)
                multiThreadedVehicleCreation(scenario, listener);
            else
                singleThreadedVehicleCreation(scenario, listener);
        } catch (InterruptedException e) {
            scenario.reset();
            throw e;
        }

        time_routes = System.nanoTime() - time_routes;
        logger.info(StringUtils.buildTimeString(
                "CREATING VEHICLES finished after ",
                time_routes,
                "ns"
        ).toString());

        /* finish building scenario */
        scenario.setPrepared(true);
        time_preparation = System.nanoTime() - time_preparation;
        logger.info(StringUtils.buildTimeString(
                "PREPARING SCENARIOS finished after ",
                time_preparation,
                "ns"
        ).toString());

        return scenario;
    }

    private void multiThreadedVehicleCreation(final Scenario scenario, final ProgressListener listener)
            throws InterruptedException {

        // general attributes for this
        final ScenarioConfig config = scenario.getConfig();
        lastPercentage = 0;
        final AtomicInteger finishedVehiclesCount = new AtomicInteger(0);

        // create vehicles with empty routes and add them to the scenario (sequentially for determinism)
        for (Triple<Node, Node, Integer> triple : scenario.getODMatrix()) {
            // stop if interrupted
            if (Thread.interrupted())
                throw new InterruptedException();

            Node start = triple.obj0;
            Node end = triple.obj1;
            int routeCount = triple.obj2;

            for (int i = 0; i < routeCount; i++) { // "synchronized"
                if (Thread.interrupted())
                    throw new InterruptedException();

                Route<Node> route = new Route<>(start, end);
                AbstractVehicle vehicle = vehicleFactory.apply(scenario, route);
                scenario.getVehicleContainer().addVehicle(vehicle);
            }
        }

        // calculate routes multithreaded
        ThreadDelegator delegator = new StaticThreadDelegator(config.multiThreading.nThreads);
        delegator.doTask(
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

    private void singleThreadedVehicleCreation(final Scenario scenario, final ProgressListener listener)
            throws InterruptedException {

        lastPercentage = 0;

        int vehicleCount = 0;
        for (Triple<Node, Node, Integer> triple : scenario.getODMatrix()) {
            if (Thread.interrupted())
                throw new InterruptedException();

            Node start = triple.obj0;
            Node end = triple.obj1;
            int routeCount = triple.obj2;

            for (int i = 0; i < routeCount; i++) {
                if (Thread.interrupted())
                    throw new InterruptedException();

                Route<Node> route = new Route<>(start, end);
                AbstractVehicle vehicle = vehicleFactory.apply(scenario, route);
                scenario.getVehicleContainer().addVehicle(vehicle);
                scenario.getScoutFactory().get().findShortestPath(start, end, route);
                vehicle.registerInGraph();
                logProgress(vehicleCount, scenario.getConfig().maxVehicleCount, listener);
            }

            vehicleCount += routeCount;
        }
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
