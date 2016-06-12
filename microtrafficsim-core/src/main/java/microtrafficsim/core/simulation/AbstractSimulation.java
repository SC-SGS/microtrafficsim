package microtrafficsim.core.simulation;

import microtrafficsim.core.frameworks.vehicle.IVisualizationVehicle;
import microtrafficsim.core.frameworks.vehicle.VehicleEntity;
import microtrafficsim.core.logic.StreetGraph;
import microtrafficsim.core.logic.vehicles.AbstractVehicle;
import microtrafficsim.core.simulation.configs.SimulationConfig;
import microtrafficsim.core.simulation.manager.SimulationManager;
import microtrafficsim.core.simulation.manager.VehicleManager;
import microtrafficsim.core.simulation.manager.impl.MultiThreadedSimulationManager;
import microtrafficsim.core.simulation.manager.impl.MultiThreadedVehicleManager;
import microtrafficsim.core.simulation.manager.impl.SingleThreadedSimulationManager;
import microtrafficsim.core.simulation.manager.impl.SingleThreadedVehicleManager;
import microtrafficsim.core.vis.opengl.utils.Color;
import microtrafficsim.interesting.progressable.ProgressListener;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;
import java.util.function.Supplier;

/**
 * <p>
 *     This class manages the simulation. It serves methods for starting and pausing the simulation, but you have to
 *     use it by extending it (class name: e.g. scenarios). The extension should include a static class extending
 *     {@link SimulationConfig}. In this config class, you can also set the number of threads. The
 *     {@link AbstractSimulation} handles alone whether the simulation steps can be executed sequentially or parallel.
 * </p>
 * <p>
 *     Logging can be disabled by setting {@link SimulationConfig#logger#enabled}.
 * </p>
 *
 * @author Jan-Oliver Schmidt, Dominic Parga Cacheiro
 */
public abstract class AbstractSimulation implements Simulation {
  private final SimulationConfig config;
  protected final StreetGraph graph;
  // simulation steps
  private boolean prepared;
  private boolean paused;
  private Timer timer;
  private TimerTask timerTask;
  private int age; // todo replace # steps by time
  private long timeDeltaMillis, timestamp, pausedTimestamp;
  // manager
  private final VehicleManager vehicleManager;
  private final SimulationManager simManager;
  // logging
  private long time;

  /**
   * Default constructor.
   */
  public AbstractSimulation(SimulationConfig config,
                            StreetGraph graph,
                            Supplier<IVisualizationVehicle> vehicleFactory) {
    this.config = config;
    this.graph = graph;
    // simulation
    prepared = false;
    paused = true;
    timer = new Timer();
    timestamp = -1;
    age = 0;
    if (config.multiThreading.nThreads > 1) {
      vehicleManager = new MultiThreadedVehicleManager(vehicleFactory);
      simManager = new MultiThreadedSimulationManager(config);
    } else {
      vehicleManager = new SingleThreadedVehicleManager(vehicleFactory);
      simManager = new SingleThreadedSimulationManager();
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
   * This method should fill not spawned vehicles using {@link Simulation}.{@link #addVehicle(AbstractVehicle)} and
   * {@link AbstractSimulation}.{@link #getSpawnedVehicles()}. The spawning and other work
   * will be done automatically.
   *
   * @param listener could be null; This listener gets informed if necessary changes are made.
   */
  protected abstract void createAndAddVehicles(ProgressListener listener);

  /**
   * This method could be called to add the given vehicle to the simulation. For visualization, this method creates an
   * instance of {@link IVisualizationVehicle} and connects it to the given vehicle using a {@link VehicleEntity}.
   *
   * @param vehicle An instance of {@link AbstractVehicle}
   */
  protected final void createAndAddVehicle(AbstractVehicle vehicle) {

    IVisualizationVehicle visCar = createVisVehicle();
    VehicleEntity entity = new VehicleEntity(config, vehicle, visCar);
    vehicle.setEntity(entity);
    visCar.setEntity(entity);
    addVehicle(vehicle);
  }

  /**
   * This method could be called to add the given vehicle to the simulation. For visualization, this method creates an
   * instance of {@link IVisualizationVehicle} and connects it to the given vehicle using a {@link VehicleEntity}.
   *
   * @param vehicle An instance of {@link AbstractVehicle}
   * @param color The color of the vehicle
   */
  protected final void createAndAddVehicle(AbstractVehicle vehicle, Color color) {

    IVisualizationVehicle visCar = createVisVehicle();
    visCar.setBaseColor(color);
    VehicleEntity entity = new VehicleEntity(config, vehicle, visCar);
    vehicle.setEntity(entity);
    visCar.setEntity(entity);
    addVehicle(vehicle);
  }

	/*
	|==================|
	| simulation steps |
	|==================|
	*/
  /**
   * This method should be called in a loop to calculate one simulation step.
   * A simulation step contains acceleration, dashing, braking, dawdling,
   * moving and right before braking, all nodes are updating their traffic
   * logic.
   */
  private void doSimulationStep() {

    if (timestamp < 0) // init
      timestamp = System.currentTimeMillis();
    long now = System.currentTimeMillis();
//        timeDeltaMillis = Math.min(now - timestamp, 1000 / config.speedup.get());
    timeDeltaMillis = now - timestamp;
    timestamp = now;

    if (config.logger.enabled) {
      time = System.nanoTime();
      simManager.willMoveAll(timeDeltaMillis, vehicleManager.iteratorSpawned());
      config.logger.debugNanoseconds("time brake() etc. = ", System.nanoTime() - time);

      time = System.nanoTime();
      simManager.moveAll(vehicleManager.iteratorSpawned());
      config.logger.debugNanoseconds("time move() = ", System.nanoTime() - time);

      time = System.nanoTime();
      simManager.didMoveAll(vehicleManager.iteratorSpawned());
      config.logger.debugNanoseconds("time didMove() = ", System.nanoTime() - time);

      time = System.nanoTime();
      simManager.spawnAll(vehicleManager.iteratorNotSpawned());
      config.logger.debugNanoseconds("time spawn() = ", System.nanoTime() - time);

      time = System.nanoTime();
      simManager.updateNodes(graph.getNodeIterator());
      config.logger.debugNanoseconds("time updateNodes() = ", System.nanoTime() - time);
    } else {
      simManager.willMoveAll(timeDeltaMillis, vehicleManager.iteratorSpawned());
      simManager.moveAll(vehicleManager.iteratorSpawned());
      simManager.didMoveAll(vehicleManager.iteratorSpawned());
      simManager.spawnAll(vehicleManager.iteratorNotSpawned());
      simManager.updateNodes(graph.getNodeIterator());
    }
    age++;
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
    prepared = false;
    vehicleManager.clearAll();
    prepareScenario();
    createAndAddVehicles(null);
    simManager.updateNodes(graph.getNodeIterator());
    prepared = true;
  }

  @Override
  public final void prepare(ProgressListener listener) {
    prepared = false;
    vehicleManager.clearAll();
    prepareScenario();
    createAndAddVehicles(listener);
    simManager.updateNodes(graph.getNodeIterator());
    prepared = true;
  }

  @Override
  public int getAge() {
    return age;
  }

  @Override
  public final void run() {
    if (prepared && paused && config.speedup > 0) {
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
    if (config.logger.enabled) {
      if (isPrepared()) {
        config.logger.debug("########## ########## ########## ########## ##");
        config.logger.debug("NEW SIMULATION STEP");
        config.logger.debug("simulation age before execution = " + getAge());
        time = System.nanoTime();
      } else {
        config.logger.debug("########## ########## ########## ########## ##");
        config.logger.debug("NEW SIMULATION STEP (NOT PREPARED)");
      }
    }
  }

  @Override
  public final void runOneStep() {
    if (prepared && paused)
      doRunOneStep();
  }

  @Override
  public final void doRunOneStep() {
    willRunOneStep();
    if (prepared)
      doSimulationStep();
    didRunOneStep();
  }

  @Override
  public void didRunOneStep() {
    if (config.logger.enabled) {
      if (isPrepared())
        config.logger.debugNanoseconds("time for this step = ", System.nanoTime() - time);
      config.logger.debug("number of vehicles after run = " + getVehiclesCount());
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

  public final boolean isPaused() {
    return paused;
  }

  @Override
  public final ArrayList<? extends AbstractVehicle> getSpawnedVehicles() {
    return new ArrayList<>(vehicleManager.getSpawnedVehicles());
  }

  @Override
  public final ArrayList<? extends AbstractVehicle> getVehicles() {
    return new ArrayList<>(vehicleManager.getVehicles());
  }

  @Override
  public int getSpawnedVehiclesCount() {
    return vehicleManager.getSpawnedCount();
  }

  @Override
  public int getVehiclesCount() {
    return vehicleManager.getVehicleCount();
  }

  @Override
  public final IVisualizationVehicle createVisVehicle() {
    IVisualizationVehicle v = vehicleManager.getVehicleFactory().get();
    vehicleManager.unlockVehicleFactory();
    return v;
  }

  @Override
  public final boolean addVehicle(AbstractVehicle vehicle) {
    if (graph.addVehicle(vehicle)) {
      vehicleManager.addVehicle(vehicle);
      return true;
    }
    return false;
  }

  @Override
  public void stateChanged(AbstractVehicle vehicle) {
    vehicleManager.stateChanged(vehicle);
  }
}