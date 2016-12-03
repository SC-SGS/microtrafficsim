package microtrafficsim.core.simulation.builder.impl;

import microtrafficsim.core.entities.vehicle.VehicleEntity;
import microtrafficsim.core.entities.vehicle.VisualizationVehicleEntity;
import microtrafficsim.core.logic.Node;
import microtrafficsim.core.logic.Route;
import microtrafficsim.core.logic.vehicles.AbstractVehicle;
import microtrafficsim.core.logic.vehicles.impl.Car;
import microtrafficsim.core.map.area.Area;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Supplier;

/**
 * This class is an implementation of {@link Builder} for {@link AbstractVehicle}s.
 *
 * @author Dominic Parga Cacheiro
 */
public class VehicleSimulationBuilder implements Builder {

    private Logger logger = LoggerFactory.getLogger(VehicleSimulationBuilder.class);

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

        // ---------- ---------- ---------- ---------- --
        // reset all
        // ---------- ---------- ---------- ---------- --
        logger.info("RESETTING SCENARIO started");
        scenario.setPrepared(false);
        scenario.getVehicleContainer().clearAll();
        scenario.getGraph().reset();
        logger.info("RESETTING SCENARIO finished");

        // ---------- ---------- ---------- ---------- --
        // check if odmatrix has to be built
        // ---------- ---------- ---------- ---------- --
        if (!scenario.isODMatrixBuilt()) {
            logger.info("BUILDING ODMatrix started");

            ArrayList<Node>
                    origins = new ArrayList<>(),
                    destinations = new ArrayList<>();

            // ---------- ---------- ---------- ---------- --
            // for each graph node, check its location relative to the origin/destination fields
            // ---------- ---------- ---------- ---------- --
            Iterator<Node> nodes = scenario.getGraph().getNodeIterator();
            while (nodes.hasNext()) {
                Node node = nodes.next();

                // for each node being in an origin field => add it
                for (Area area : scenario.getOriginFields())
                    if (area.contains(node)) {
                        origins.add(node);
                        break;
                    }

                // for each node being in a destination field => add it
                for (Area area : scenario.getDestinationFields())
                    if (area.contains(node)) {
                        destinations.add(node);
                        break;
                    }
            }

            // ---------- ---------- ---------- ---------- --
            // build matrix
            // ---------- ---------- ---------- ---------- --
            ODMatrix odmatrix = new SparseODMatrix();
            for (int i = 0; i < scenario.getConfig().maxVehicleCount; i++) {
                int rdmOrig = random.nextInt(origins.size());
                int rdmDest = random.nextInt(destinations.size());
                odmatrix.inc(origins.get(rdmOrig), destinations.get(rdmDest));
            }

            // ---------- ---------- ---------- ---------- --
            // finish creating ODMatrix
            // ---------- ---------- ---------- ---------- --
            scenario.setODMatrix(odmatrix);
            scenario.setODMatrixBuilt(true);
            logger.info("BUILDING ODMatrix finished");
        }

        // ---------- ---------- ---------- ---------- --
        // create vehicle routes
        // ---------- ---------- ---------- ---------- --
        ODMatrix odmatrix = scenario.getODMatrix();
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

        // ---------- ---------- ---------- ---------- --
        // finish building scenario
        // ---------- ---------- ---------- ---------- --
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
        SimulationConfig config = scenario.getConfig();
        // TODO bisher nur reinkopiert, aber man sollte das mit der odmatrix machen


        final int                   maxVehicleCount = config.maxVehicleCount;
        final int                   nThreads        = config.multiThreading.nThreads;
        ExecutorService pool            = Executors.newFixedThreadPool(nThreads);
        ArrayList<Callable<Object>> todo            = new ArrayList<>(nThreads);

        // deterministic/pseudo-random route + vehicle generation needs
        // variables for synchronization:
        orderIdx                                 = 0;
        final int[] addedVehicles                = {0};
        final Object lock_random                 = new Object();
        final ReentrantLock lock                 = new ReentrantLock(true);
        final               boolean[] permission = new boolean[nThreads];
        Condition[] getPermission                = new Condition[nThreads];
        for (int i = 0; i < getPermission.length; i++) {
            getPermission[i] = lock.newCondition();
            permission[i]    = i == 0;
        }
        // distribute vehicle generation uniformly over all threads
        Iterator<Integer> bucketCounts = Distribution.uniformly(maxVehicleCount, nThreads);
        while (bucketCounts.hasNext()) {
            int bucketCount = bucketCounts.next();

            todo.add(Executors.callable(() -> {
                // calculate routes and create vehicles
                int successfullyAdded = 0;
                while (successfullyAdded < bucketCount) {
                    Node start, end;
                    int  idx;
                    synchronized (lock_random) {
                        idx = orderIdx % nThreads;
                        orderIdx++;
                        Node[] bla = findRouteNodes();
                        start      = bla[0];
                        end        = bla[1];
                    }
                    // create route
                    Route<Node> route = new Route<>(start, end);
                    scoutFactory.get().findShortestPath(start, end, route);
                    // create and add vehicle
                    lock.lock();
                    // wait for permission
                    if (!permission[idx]) {
                        try {
                            getPermission[idx].await();
                        } catch (InterruptedException e) { e.printStackTrace(); }
                    }
                    // has permission to create vehicle
                    if (!route.isEmpty()) {
                        // add route to vehicle and vehicle to graph
                        createAndAddVehicle(new Car(config, this, route));
                        successfullyAdded++;
                        addedVehicles[0]++;
                    }
                    // let next thread finish its work
                    permission[idx] = false;
                    int nextIdx     = (idx + 1) % nThreads;
                    if (lock.hasWaiters(getPermission[nextIdx]))
                        getPermission[nextIdx].signal();
                    else
                        permission[nextIdx] = true;

                    lock.unlock();

                    // nice output
                    logProgress(addedVehicles[0], maxVehicleCount, listener);
                }
            }));
        }
        try {
            pool.invokeAll(todo);
        } catch (InterruptedException e) { e.printStackTrace(); }
    }

    private void singleThreadedVehicleCreation(final Scenario scenario, final ProgressListener listener) {

        lastPercentage = 0;

        int vehicleCount = 0;
        for (Triple<Node, Node, Integer> triple : scenario.getODMatrix()) {
            Node start = triple.obj0;
            Node end = triple.obj1;
            int routeCount = triple.obj2;

            for (int i = 0; i < routeCount; i++) {
                Route<Node> route = new Route<>(start, end);
                scenario.getScoutFactory().get().findShortestPath(start, end, route);
                createAndAddVehicle(scenario, route);
                logProgress(vehicleCount, scenario.getConfig().maxVehicleCount, listener);
            }
        }
    }

    private void createAndAddVehicle(Scenario scenario, Route<Node> route) {

        // init stuff
        SimulationConfig config = scenario.getConfig();
        VehicleContainer vehicleContainer = scenario.getVehicleContainer();

        // create vehicle components
        AbstractVehicle vehicle = new Car(config.longIDGenerator.next(), config.seedGenerator.next(), vehicleContainer,
                route);
        VisualizationVehicleEntity visCar;
        synchronized (vehicleFactory) {
            visCar = vehicleFactory.get();
        }

        // create vehicle entity
        VehicleEntity entity = new VehicleEntity(vehicle, visCar);
        vehicle.setEntity(entity);
        visCar.setEntity(entity);

        // add to graph
        if (vehicle.registerInGraph())
            vehicleContainer.addVehicle(vehicle);
    }

    private void logProgress(int finished, int total, ProgressListener listener) {
        int percentage = (100 * finished) / total;
        synchronized (percentageDelta) {
            if (percentage - lastPercentage >= percentageDelta) {
                logger.info(percentage + "% vehicles created.");
                if (listener != null) listener.didProgress(percentage);
                lastPercentage += percentageDelta;
            }
        }
    }
}
