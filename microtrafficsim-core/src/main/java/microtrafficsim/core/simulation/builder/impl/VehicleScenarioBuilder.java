package microtrafficsim.core.simulation.builder.impl;

import microtrafficsim.core.entities.vehicle.VehicleEntity;
import microtrafficsim.core.entities.vehicle.VisualizationVehicleEntity;
import microtrafficsim.core.logic.nodes.Node;
import microtrafficsim.core.logic.routes.MetaRoute;
import microtrafficsim.core.logic.routes.Route;
import microtrafficsim.core.logic.routes.StackRoute;
import microtrafficsim.core.logic.streets.DirectedEdge;
import microtrafficsim.core.logic.vehicles.machines.Vehicle;
import microtrafficsim.core.shortestpath.ShortestPathAlgorithm;
import microtrafficsim.core.simulation.builder.LogicVehicleFactory;
import microtrafficsim.core.simulation.builder.ScenarioBuilder;
import microtrafficsim.core.simulation.configs.SimulationConfig;
import microtrafficsim.core.simulation.scenarios.Scenario;
import microtrafficsim.math.random.Seeded;
import microtrafficsim.utils.Resettable;
import microtrafficsim.utils.concurrency.delegation.StaticThreadDelegator;
import microtrafficsim.utils.concurrency.delegation.ThreadDelegator;
import microtrafficsim.utils.id.ConcurrentLongIDGenerator;
import microtrafficsim.utils.id.ConcurrentSeedGenerator;
import microtrafficsim.utils.logging.EasyMarkableLogger;
import microtrafficsim.utils.progressable.ProgressListener;
import microtrafficsim.utils.strings.StringUtils;
import org.slf4j.Logger;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Dominic Parga Cacheiro
 */
public class VehicleScenarioBuilder implements ScenarioBuilder, Seeded, Resettable {
    private Logger logger = new EasyMarkableLogger(VehicleScenarioBuilder.class);

    /** Used for printing vehicle creation process */
    private int lastPercentage = 0;

    protected final ConcurrentLongIDGenerator idGenerator;
    protected final ConcurrentSeedGenerator   seedGenerator;
    private final LogicVehicleFactory logicVehicleFactory;
    private final VisVehicleFactory visVehicleFactory;


    public VehicleScenarioBuilder(long seed,
                                  LogicVehicleFactory logicVehicleFactory,
                                  VisVehicleFactory visVehicleFactory)
    {
        idGenerator   = new ConcurrentLongIDGenerator();
        seedGenerator = new ConcurrentSeedGenerator(seed);
        this.logicVehicleFactory = logicVehicleFactory;
        this.visVehicleFactory = visVehicleFactory;
    }

    public VehicleScenarioBuilder(long seed, LogicVehicleFactory logicVehicleFactory) {
        this(seed, logicVehicleFactory, () -> null);
    }

    public VehicleScenarioBuilder(long seed, VisVehicleFactory visVehicleFactory) {
        this(seed, LogicVehicleFactory::defaultCreation, visVehicleFactory);
    }

    public VehicleScenarioBuilder(long seed) {
        this(seed, LogicVehicleFactory::defaultCreation, () -> null);
    }


    private Vehicle createVehicle(Scenario scenario, Route metaRoute) {
        // create vehicle components
        Vehicle logicVehicle = logicVehicleFactory.create(
                idGenerator.next(),
                seedGenerator.next(),
                scenario,
                metaRoute
        );
        VisualizationVehicleEntity visVehicle = null;
        if (visVehicleFactory != null)
            visVehicle = visVehicleFactory.create();

        // create vehicle entity and link components
        VehicleEntity entity = new VehicleEntity(logicVehicle, visVehicle);
        logicVehicle.setEntity(entity);
        if (visVehicle != null)
            visVehicle.setEntity(entity);

        return logicVehicle;
    }


    /*
    |=====================|
    | (i) ScenarioBuilder |
    |=====================|
    */
    @Override
    public Scenario prepare(final Scenario scenario, final ProgressListener listener)
            throws InterruptedException {
        logger.info("PREPARING SCENARIO started");
        long startTimestamp = System.nanoTime();


        resetBeforePreparation(scenario);
        try {
            buildVehicles(scenario, listener);
        } catch (InterruptedException e) {
            scenario.reset();
            throw e;
        }
        finishPreparation(scenario, startTimestamp);
        return scenario;
    }


    /*
    |===============|
    | prepare utils |
    |===============|
    */
    private void resetBeforePreparation(Scenario scenario) throws InterruptedException {
        /* interrupt handling */
        if (Thread.interrupted())
            throw new InterruptedException();

        /* reset all */
        reset();
        logger.info("PREPARING SCENARIO FOR BUILDING started");
        scenario.executeBeforeBuilding();
        logger.info("PREPARING SCENARIO FOR BUILDING finished");

        /* interrupt handling */
        if (Thread.interrupted()) {
            scenario.reset();
            throw new InterruptedException();
        }
    }

    private void buildVehicles(Scenario scenario, ProgressListener listener)
            throws InterruptedException {
        /* create vehicle routes */
        logger.info("CREATING VEHICLES started");
        long time_routes = System.nanoTime();

        if (scenario.getConfig().multiThreading.nThreads > 1)
            multiThreadedVehicleRouteAssignment(scenario, listener);
        else
            singleThreadedVehicleRouteAssignment(scenario, listener);

        time_routes = System.nanoTime() - time_routes;
        logger.info(StringUtils.buildTimeString(
                "CREATING VEHICLES finished after ",
                time_routes,
                "ns"
        ).toString());
    }

    private void multiThreadedVehicleRouteAssignment(Scenario scenario, ProgressListener listener)
            throws InterruptedException {
        lastPercentage = 0;

        // create vehicles with empty routes and add them to the scenario (sequentially for determinism)
        for (Route metaRoute : scenario.getRoutes()) {  // "synchronized"
            if (Thread.interrupted())
                throw new InterruptedException();

            Vehicle vehicle = createVehicle(scenario, metaRoute);
            scenario.getVehicleContainer().addVehicle(vehicle);
        }

        // calculate routes multithreaded
        final SimulationConfig config = scenario.getConfig();
        final AtomicInteger finishedVehiclesCount = new AtomicInteger(0);
        ThreadDelegator delegator = new StaticThreadDelegator(config.multiThreading.nThreads);
        delegator.doTask(
                vehicle -> {
                    Route metaRoute = vehicle.getDriver().getRoute();
                    if (metaRoute instanceof MetaRoute) {
                        StackRoute route = new StackRoute(metaRoute.getSpawnDelay());

                        ShortestPathAlgorithm<Node, DirectedEdge> scout = scenario.getScoutFactory().get();
                        scout.findShortestPath(metaRoute.getOrigin(), metaRoute.getDestination(), route);

                        vehicle.getDriver().setRoute(route);
                    }

                    vehicle.registerInGraph();
                    int bla = finishedVehiclesCount.incrementAndGet();
                    logProgress(bla, config.maxVehicleCount, listener);
                },
                scenario.getVehicleContainer().getVehicles().iterator(),
                config.multiThreading.vehiclesPerRunnable);
    }

    private void singleThreadedVehicleRouteAssignment(Scenario scenario, ProgressListener listener)
            throws InterruptedException {
        lastPercentage = 0;

        int vehicleCount = 0;
        for (Route metaRoute : scenario.getRoutes()) {
            if (Thread.interrupted())
                throw new InterruptedException();

            if (metaRoute instanceof MetaRoute) {
                StackRoute route = new StackRoute(metaRoute.getSpawnDelay());

                ShortestPathAlgorithm<Node, DirectedEdge> scout = scenario.getScoutFactory().get();
                scout.findShortestPath(metaRoute.getOrigin(), metaRoute.getDestination(), route);

                metaRoute = route;
            }
            Vehicle vehicle = createVehicle(scenario, metaRoute);
            scenario.getVehicleContainer().addVehicle(vehicle);
            vehicle.registerInGraph();
            logProgress(vehicleCount, scenario.getConfig().maxVehicleCount, listener);

            vehicleCount++;
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

    private void finishPreparation(Scenario scenario, long startTimestamp) {
        scenario.setPrepared(true);
        long duration = System.nanoTime() - startTimestamp;
        logger.info(StringUtils.buildTimeString(
                "PREPARING SCENARIOS finished after ",
                duration,
                "ns"
        ).toString());
    }

    /*
    |================|
    | (i) Resettable |
    |================|
    */
    @Override
    public void reset() {
        logger.debug("reset " + VehicleScenarioBuilder.class.getSimpleName());
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
