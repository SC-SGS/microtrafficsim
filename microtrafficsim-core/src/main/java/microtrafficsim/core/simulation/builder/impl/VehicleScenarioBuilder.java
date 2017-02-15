package microtrafficsim.core.simulation.builder.impl;

import microtrafficsim.core.entities.vehicle.VehicleEntity;
import microtrafficsim.core.entities.vehicle.VisualizationVehicleEntity;
import microtrafficsim.core.logic.Route;
import microtrafficsim.core.logic.nodes.Node;
import microtrafficsim.core.logic.streets.DirectedEdge;
import microtrafficsim.core.logic.vehicles.driver.BasicDriver;
import microtrafficsim.core.logic.vehicles.driver.Driver;
import microtrafficsim.core.logic.vehicles.machines.Vehicle;
import microtrafficsim.core.logic.vehicles.machines.impl.Car;
import microtrafficsim.core.shortestpath.ShortestPathAlgorithm;
import microtrafficsim.core.simulation.builder.ScenarioBuilder;
import microtrafficsim.core.simulation.configs.ScenarioConfig;
import microtrafficsim.core.simulation.scenarios.Scenario;
import microtrafficsim.interesting.progressable.ProgressListener;
import microtrafficsim.math.random.Seeded;
import microtrafficsim.utils.Resettable;
import microtrafficsim.utils.strings.StringUtils;
import microtrafficsim.utils.collections.Triple;
import microtrafficsim.utils.concurrency.delegation.StaticThreadDelegator;
import microtrafficsim.utils.concurrency.delegation.ThreadDelegator;
import microtrafficsim.utils.id.ConcurrentLongIDGenerator;
import microtrafficsim.utils.id.ConcurrentSeedGenerator;
import microtrafficsim.utils.logging.EasyMarkableLogger;
import org.slf4j.Logger;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

/**
 * @author Dominic Parga Cacheiro
 */
public class VehicleScenarioBuilder implements ScenarioBuilder, Seeded, Resettable {

    private Logger logger = new EasyMarkableLogger(VehicleScenarioBuilder.class);

    /** Used for printing vehicle creation process */
    private int lastPercentage = 0;

    protected final ConcurrentLongIDGenerator idGenerator;
    protected final ConcurrentSeedGenerator   seedGenerator;

    private final Supplier<VisualizationVehicleEntity> visVehicleFactory;


    /**
     * Default constructor. The {@code visVehicleFactory} can be null, which means vehicles are not visualized.
     *
     * @param seed              Used for {@link ConcurrentSeedGenerator}
     * @param visVehicleFactory Creates the visualization part of the vehicle entity
     */
    public VehicleScenarioBuilder(long seed, Supplier<VisualizationVehicleEntity> visVehicleFactory) {

        idGenerator   = new ConcurrentLongIDGenerator();
        seedGenerator = new ConcurrentSeedGenerator(seed);

        this.visVehicleFactory = visVehicleFactory;
    }


    protected Vehicle createVehicle(Scenario scenario, Route<Node> route) {

        // create vehicle components
        Vehicle logicVehicle = createLogicVehicle(scenario, route);
        VisualizationVehicleEntity visVehicle = null;
        if (visVehicleFactory != null)
            visVehicle = visVehicleFactory.get();

        // create vehicle entity and link components
        VehicleEntity entity = new VehicleEntity(logicVehicle, visVehicle);
        logicVehicle.setEntity(entity);
        if (visVehicle != null)
            visVehicle.setEntity(entity);

        return logicVehicle;
    }

    protected Vehicle createLogicVehicle(Scenario scenario, Route<Node> route) {

        ScenarioConfig config = scenario.getConfig();
        long id               = idGenerator.next();
        long seed             = seedGenerator.next();

        Vehicle car = new Car(id, config.visualization.style);
        Driver driver = new BasicDriver(seed);
        driver.setRoute(route);
        driver.setVehicle(car);
        car.setDriver(driver);

        car.addStateListener(scenario.getVehicleContainer());

        return car;
    }


    /*
    |=====================|
    | (i) ScenarioBuilder |
    |=====================|
    */
    @Override
    public Scenario prepare(final Scenario scenario, final ProgressListener listener) throws InterruptedException {
        logger.info("PREPARING SCENARIO started");
        long time_preparation = System.nanoTime();

        /* interrupt handling */
        if (Thread.interrupted())
            throw new InterruptedException();

        /* reset all */
        reset();
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
                Vehicle vehicle = createVehicle(scenario, route);
                scenario.getVehicleContainer().addVehicle(vehicle);
            }
        }

        // calculate routes multithreaded
        ThreadDelegator delegator = new StaticThreadDelegator(config.multiThreading.nThreads);
        delegator.doTask(
                vehicle -> {
                    ShortestPathAlgorithm scout = scenario.getScoutFactory().get();
                    Route<Node> route = vehicle.getDriver().getRoute();
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
                Vehicle vehicle = createVehicle(scenario, route);
                scenario.getVehicleContainer().addVehicle(vehicle);
                scenario.getScoutFactory().get().findShortestPath(start, end, route);
                vehicle.registerInGraph();
                logProgress(vehicleCount, scenario.getConfig().maxVehicleCount, listener);
            }

            vehicleCount += routeCount;
        }
    }

    private synchronized void logProgress(int finished, int total, ProgressListener listener) {
        final int percentageDelta = 5;

        int percentage = (100 * finished) / total;
        if (percentage - lastPercentage >= percentageDelta) {
            logger.info(percentage + "% vehicles created.");
            if (listener != null) listener.didProgress(percentage);
            lastPercentage += percentageDelta;
        }
    }


    /*
    |================|
    | (i) Resettable |
    |================|
    */
    @Override
    public void reset() {
        logger.debug("reset");
        idGenerator.reset();
        seedGenerator.reset();
    }


    /*
    |============|
    | (i) Seeded |
    |============|
    */
    @Override
    public void setSeed(long seed) {
        seedGenerator.setSeed(seed);
    }

    @Override
    public long getSeed() {
        return seedGenerator.getSeed();
    }
}

