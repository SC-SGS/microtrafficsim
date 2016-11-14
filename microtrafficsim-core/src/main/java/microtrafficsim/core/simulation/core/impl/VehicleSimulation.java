package microtrafficsim.core.simulation.core.impl;

import microtrafficsim.core.logic.vehicles.AbstractVehicle;
import microtrafficsim.core.simulation.core.Simulation;
import microtrafficsim.core.simulation.core.stepexecutors.VehicleStepExecutor;
import microtrafficsim.core.simulation.core.stepexecutors.impl.MultiThreadedVehicleStepExecutor;
import microtrafficsim.core.simulation.core.stepexecutors.impl.SingleThreadedVehicleStepExecutor;
import microtrafficsim.core.simulation.scenarios.Scenario;
import microtrafficsim.utils.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Timer;
import java.util.TimerTask;


/**
 * This class is an implementation of {@link Simulation} for {@link AbstractVehicle}s.
 *
 * @author Dominic Parga Cacheiro
 */
public abstract class VehicleSimulation implements Simulation {
    private Logger logger = LoggerFactory.getLogger(VehicleSimulation.class);

    private Scenario scenario;
    private VehicleStepExecutor vehicleStepExecutor;

    // simulation steps
    private boolean   paused;
    private Timer     timer;
    private TimerTask timerTask;
    private int       age;

    // logging
    private long time;

    /**
     * Default constructor. Adopts the given scenario by calling {@link #setScenario(Scenario)}.
     *
     * @param scenario This scenario is executed later.
     */
    public VehicleSimulation(Scenario scenario) {
        setScenario(scenario);
        timer = new Timer();
    }

    /*
    |================|
    | (i) Simulation |
    |================|
    */
    @Override
    public Scenario getScenario() {
        return scenario;
    }

    @Override
    public void setScenario(Scenario scenario) {
        if (!isPaused())
            throw new RuntimeException("The simulation sets a new scenario but is not paused.");

        this.scenario = scenario;
        age = 0;
        if (scenario.getConfig().multiThreading.nThreads > 1)
            vehicleStepExecutor = new MultiThreadedVehicleStepExecutor(this);
        else
            vehicleStepExecutor = new SingleThreadedVehicleStepExecutor();
    }

    @Override
    public int getAge() {
        return age;
    }

    @Override
    public final void run() {
        if (scenario.isPrepared() && isPaused() && scenario.getConfig().speedup > 0) {
            timerTask = new TimerTask() {
                @Override
                public void run() {
                    doRunOneStep();
                }
            };
            timer.schedule(timerTask, 0, 1000 / scenario.getConfig().speedup);
            paused = false;
        }
    }

    @Override
    public void willRunOneStep() {
        if (logger.isDebugEnabled()) {

            logger.debug("########## ########## ########## ########## ##");

            if (scenario.isPrepared()) {
                logger.debug("NEW SIMULATION STEP");
                logger.debug("simulation age before execution = " + getAge());
                time = System.nanoTime();
            } else {
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

        if (scenario.isPrepared()) {
            Collection<AbstractVehicle>
                    spawnedVehicles = scenario.getVehicleContainer().getSpawnedVehicles(),
                    notSpawnedVehicles = scenario.getVehicleContainer().getNotSpawnedVehicles();
            if (logger.isDebugEnabled()) {
                time = System.nanoTime();
                vehicleStepExecutor.willMoveAll(spawnedVehicles.iterator());
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
                vehicleStepExecutor.updateNodes(scenario.getGraph().getNodeIterator());
                logger.debug(
                        StringUtils.buildTimeString("time updateNodes() = ", System.nanoTime() - time, "ns").toString()
                );
            } else {
                vehicleStepExecutor.willMoveAll(spawnedVehicles.iterator());
                vehicleStepExecutor.moveAll(spawnedVehicles.iterator());
                vehicleStepExecutor.didMoveAll(spawnedVehicles.iterator());
                vehicleStepExecutor.spawnAll(notSpawnedVehicles.iterator());
                vehicleStepExecutor.updateNodes(scenario.getGraph().getNodeIterator());
            }
            age++;
        }

        didRunOneStep();
    }

    @Override
    public void didRunOneStep() {
        if (scenario.getConfig().ageForPause == getAge())
            cancel();

        if (logger.isDebugEnabled()) {
            if (scenario.isPrepared()) {
                logger.debug(
                        StringUtils.buildTimeString("time for this step = ", System.nanoTime() - time, "ns").toString()
                );
            }
            logger.debug("number of vehicles after run = " + scenario.getVehicleContainer().getVehicleCount());
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
    public void stateChanged(AbstractVehicle vehicle) {
        scenario.getVehicleContainer().stateChanged(vehicle);
    }
}