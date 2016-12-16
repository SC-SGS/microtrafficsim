package microtrafficsim.core.simulation.core;

import microtrafficsim.core.entities.vehicle.VisualizationVehicleEntity;
import microtrafficsim.core.entities.vehicle.VehicleEntity;
import microtrafficsim.core.logic.StreetGraph;
import microtrafficsim.core.logic.vehicles.AbstractVehicle;
import microtrafficsim.core.simulation.configs.SimulationConfig;
import microtrafficsim.core.simulation.core.stepexecutors.VehicleStepExecutor;
import microtrafficsim.core.simulation.scenarios.containers.VehicleContainer;
import microtrafficsim.core.simulation.core.stepexecutors.impl.MultiThreadedVehicleStepExecutor;
import microtrafficsim.core.simulation.core.stepexecutors.impl.SingleThreadedVehicleStepExecutor;
import microtrafficsim.core.simulation.scenarios.containers.impl.BasicVehicleContainer;
import microtrafficsim.core.vis.opengl.utils.Color;
import microtrafficsim.interesting.progressable.ProgressListener;
import microtrafficsim.utils.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.function.Supplier;


/**
 * <p>
 * This class manages the simulation. It serves methods for starting and pausing the simulation, but you have to
 * use it by extending it (class name: e.g. scenarios). The extension should include a static class extending
 * {@link SimulationConfig}. In this config class, you can also set the number of threads. The
 * {@link AbstractSimulation} handles alone whether the simulation steps can be executed sequentially or parallel.
 * </p>
 *
 * @author Jan-Oliver Schmidt, Dominic Parga Cacheiro
 */
public abstract class AbstractSimulation implements OldSimulation {
    private Logger logger = LoggerFactory.getLogger(AbstractSimulation.class); // TODO marker!

    protected final StreetGraph graph;
    private final SimulationConfig config;

    // manager
    private final VehicleContainer vehicleContainer;
    private final VehicleStepExecutor vehicleStepExecutor;

    // simulation steps
    private boolean   prepared;
    private boolean   paused;
    private Timer     timer;
    private TimerTask timerTask;
    private int       age;
    private long      timeDeltaMillis, timestamp, pausedTimestamp;

    // logging
    private long time;

    /**
     * Default constructor.
     */
    public AbstractSimulation(
            SimulationConfig config, StreetGraph graph, Supplier<VisualizationVehicleEntity> vehicleFactory) {
        this.config = config;
        this.graph  = graph;
        // simulation
        prepared  = false;
        paused    = true;
        timer     = new Timer();
        timestamp = -1;
        age       = 0;
        if (config.multiThreading.nThreads > 1) {
            vehicleContainer = new ConcurrentVehicleContainer(vehicleFactory);
            vehicleStepExecutor = new MultiThreadedVehicleStepExecutor(config);
        } else {
            vehicleContainer = new BasicVehicleContainer(vehicleFactory);
            vehicleStepExecutor = new SingleThreadedVehicleStepExecutor();
        }
    }

    @Override
    public SimulationConfig getConfig() {
        return config;
    }

    /*
    |=========|
    | prepare |
    |=========|
    */
    /**
     * This method should be called before the simulation starts. E.g. it can be
     * used to set start nodes, that are used in the {@link AbstractSimulation}.
     * {@link #createAndAddVehicles(ProgressListener)}.
     */
    protected abstract void prepareScenario();

    /**
     * This method should fill not spawned vehicles using {@link OldSimulation}.{@link #addVehicle(AbstractVehicle)} and
     * {@link AbstractSimulation}.{@link VehicleContainer#getSpawnedVehicles()}. The spawning and other work
     * will be done automatically.
     *
     * @param listener could be null; This listener gets informed if necessary changes are made.
     */
    protected abstract void createAndAddVehicles(ProgressListener listener);

    /**
     * This method could be called to add the given vehicle to the simulation. For visualization, this method creates an
     * instance of {@link VisualizationVehicleEntity} and connects it to the given vehicle using a {@link VehicleEntity}.
     *
     * @param vehicle An instance of {@link AbstractVehicle}
     */
    protected final void createAndAddVehicle(AbstractVehicle vehicle) {

        VisualizationVehicleEntity visCar = vehicleContainer
                .getVehicleFactory().get();
        vehicleContainer.unlockVehicleFactory();
        VehicleEntity entity = new VehicleEntity(config, vehicle, visCar);
        vehicle.setEntity(entity);
        visCar.setEntity(entity);
        if (graph.addVehicle(vehicle))
            vehicleContainer.addVehicle(vehicle);
    }

    /**
     * This method could be called to add the given vehicle to the simulation. For visualization, this method creates an
     * instance of {@link VisualizationVehicleEntity} and connects it to the given vehicle using a {@link VehicleEntity}.
     *
     * @param vehicle An instance of {@link AbstractVehicle}
     * @param color   The color of the vehicle
     */
    protected final void createAndAddVehicle(AbstractVehicle vehicle, Color color) {

        VisualizationVehicleEntity visCar = vehicleContainer
                .getVehicleFactory().get();
        vehicleContainer.unlockVehicleFactory();
        visCar.setBaseColor(color);
        VehicleEntity entity = new VehicleEntity(config, vehicle, visCar);
        vehicle.setEntity(entity);
        visCar.setEntity(entity);
        if (graph.addVehicle(vehicle))
            vehicleContainer.addVehicle(vehicle);
    }

    /*
    |================|
    | (i) Simulation |
    |================|
    */
    @Override
    public boolean isPrepared() {
        return prepared;
    }

    @Override
    public final void prepare() {
        prepare(null);
    }

    @Override
    public final void prepare(ProgressListener listener) {
        prepared = false;
        vehicleContainer.clearAll();
        prepareScenario();
        createAndAddVehicles(listener);
        vehicleStepExecutor.updateNodes(graph.getNodeIterator());
        prepared = true;
    }

    @Override
    public int getAge() {
        return age;
    }

    @Override
    public final void run() {
        if (prepared && isPaused() && config.speedup > 0) {
            timerTask = new TimerTask() {
                @Override
                public void run() {
                    doRunOneStep();
                }
            };
            timer.schedule(timerTask, 0, 1000 / config.speedup);
            paused = false;
        }
    }

    @Override
    public void willRunOneStep() {
        if (logger.isDebugEnabled()) {
            if (prepared) {
                logger.debug("########## ########## ########## ########## ##");
                logger.debug("NEW SIMULATION STEP");
                logger.debug("simulation age before execution = " + getAge());
                time = System.nanoTime();
            } else {
                logger.debug("########## ########## ########## ########## ##");
                logger.debug("NEW SIMULATION STEP (NOT PREPARED)");
            }
        }
    }

    @Override
    public final void runOneStep() {
        if (isPaused()) doRunOneStep();
    }

    @Override
    public final void doRunOneStep() {
        willRunOneStep();

        if (prepared) {
            if (timestamp < 0)    // init
                timestamp = System.currentTimeMillis();
            long now      = System.currentTimeMillis();
            //        timeDeltaMillis = Math.min(now - timestamp, 1000 / config.speedup.get());
            timeDeltaMillis = now - timestamp;
            timestamp       = now;


            Collection<AbstractVehicle>
                    spawnedVehicles = vehicleContainer.getSpawnedVehicles(),
                    notSpawnedVehicles = vehicleContainer.getNotSpawnedVehicles();
            if (logger.isDebugEnabled()) {
                time = System.nanoTime();
                vehicleStepExecutor.willMoveAll(timeDeltaMillis, spawnedVehicles.iterator());
                logger.debug(
                        StringUtils.buildTimeString("time brake() etc. = ", System.nanoTime() - time, "ns").toString()
                );

                time = System.nanoTime();
                vehicleStepExecutor.moveAll(spawnedVehicles.iterator());
                logger.debug(
                        StringUtils.buildTimeString("time move() = ", System.nanoTime() - time, "ns").toString()
                );

                time = System.nanoTime();
                vehicleStepExecutor.didMoveAll(spawnedVehicles.iterator());
                logger.debug(
                        StringUtils.buildTimeString("time didMove() = ", System.nanoTime() - time, "ns").toString()
                );

                time = System.nanoTime();
                vehicleStepExecutor.spawnAll(notSpawnedVehicles.iterator());
                logger.debug(
                        StringUtils.buildTimeString("time spawn() = ", System.nanoTime() - time, "ns").toString()
                );

                time = System.nanoTime();
                vehicleStepExecutor.updateNodes(graph.getNodeIterator());
                logger.debug(
                        StringUtils.buildTimeString("time updateNodes() = ", System.nanoTime() - time, "ns").toString()
                );
            } else {
                vehicleStepExecutor.willMoveAll(timeDeltaMillis, spawnedVehicles.iterator());
                vehicleStepExecutor.moveAll(spawnedVehicles.iterator());
                vehicleStepExecutor.didMoveAll(spawnedVehicles.iterator());
                vehicleStepExecutor.spawnAll(notSpawnedVehicles.iterator());
                vehicleStepExecutor.updateNodes(graph.getNodeIterator());
            }
            age++;
        }

        didRunOneStep();
    }

    @Override
    public void didRunOneStep() {
        if (logger.isDebugEnabled()) {
            if (prepared) {
                logger.debug(
                        StringUtils.buildTimeString("time for this step = ", System.nanoTime() - time, "ns").toString()
                );
            }
            logger.debug("number of vehicles after run = " + vehicleContainer.getVehicleCount());
        }
    }

    @Override
    public final void cancel() {
        if (timerTask != null) {
            timerTask.cancel();
            timerTask = null;
        }
        paused = true;
    }

    @Override
    public final boolean isPaused() {
        return paused;
    }

    @Override
    public VehicleContainer getVehicleContainer() {
        return vehicleContainer;
    }

    @Override
    public void stateChanged(AbstractVehicle vehicle) {
        vehicleContainer.stateChanged(vehicle);
    }
}