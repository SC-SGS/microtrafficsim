package microtrafficsim.core.simulation.core.impl;

import microtrafficsim.core.logic.vehicles.AbstractVehicle;
import microtrafficsim.core.simulation.core.Simulation;
import microtrafficsim.core.simulation.core.StepListener;
import microtrafficsim.core.simulation.core.stepexecutors.VehicleStepExecutor;
import microtrafficsim.core.simulation.core.stepexecutors.impl.MultiThreadedVehicleStepExecutor;
import microtrafficsim.core.simulation.core.stepexecutors.impl.SingleThreadedVehicleStepExecutor;
import microtrafficsim.core.simulation.scenarios.Scenario;
import microtrafficsim.utils.StringUtils;
import microtrafficsim.utils.logging.EasyMarkableLogger;
import org.slf4j.Logger;

import java.util.LinkedList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;


/**
 * This class is an implementation of {@link Simulation} for {@link AbstractVehicle}s.
 *
 * @author Dominic Parga Cacheiro
 */
public class VehicleSimulation implements Simulation {
    private Logger logger = new EasyMarkableLogger(VehicleSimulation.class);

    private Scenario scenario;
    private VehicleStepExecutor vehicleStepExecutor;

    // simulation steps
    private boolean            paused;
    private Timer              timer;
    private TimerTask          timerTask;
    private int                age;
    private List<StepListener> stepListeners;

    // logging
    private long time;

    /**
     * Default constructor. Before this simulation can be used, it needs a scenario!
     */
    public VehicleSimulation() {
        this(null);
    }

    /**
     * Default constructor. Adopts the given scenario by calling {@link #setAndInitPreparedScenario(Scenario)}.
     *
     * @param scenario This scenario is executed later.
     */
    public VehicleSimulation(Scenario scenario) {
        paused = true;
        setAndInitPreparedScenario(scenario);
        timer = new Timer();
        this.stepListeners = new LinkedList<>();
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
    public void setAndInitPreparedScenario(Scenario scenario) {
        if (!isPaused())
            throw new RuntimeException("The simulation sets a new scenario but is not paused.");

        if (scenario == null) {
            age = -1;
            return;
        }

        if (!scenario.isPrepared())
            throw new RuntimeException("The simulation sets a new scenario but the scenario is not prepared.");

        /* remove old scenario */
        while(stepListeners.contains(this.scenario))
            stepListeners.remove(this.scenario);
        age = 0;

        /* add new scenario */
        this.scenario = scenario;
        addStepListener(scenario);
        int nThreads = scenario.getConfig().multiThreading.nThreads;
        vehicleStepExecutor =
                nThreads > 1 ?
                        new MultiThreadedVehicleStepExecutor(nThreads) :
                        new SingleThreadedVehicleStepExecutor();

        vehicleStepExecutor.updateNodes(this.scenario);
    }

    /**
     * The internal collection used to store listeners is a {@link LinkedList} for easy iterating. Due to its
     * runtime in O(n) for checking whether an Object is contained or not, this method {@code addStepListener} DOES
     * NOT check for duplicates. Thus if you add a listener twice, it is called twice.
     */
    @Override
    public void addStepListener(StepListener stepListener) {
        if (stepListener != null)
            stepListeners.add(stepListener);
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
        if (logger.isTraceEnabled()) {

            logger.trace("########## ########## ########## ########## ##");

            if (scenario.isPrepared()) {
                logger.trace("NEW SIMULATION STEP");
                logger.trace("simulation age before execution = " + getAge());
                time = System.nanoTime();
            } else {
                logger.trace("NEW SIMULATION STEP (NOT PREPARED)");
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
            if (logger.isDebugEnabled()) {
                time = System.nanoTime();
                vehicleStepExecutor.willMoveAll(scenario);
                logger.trace(
                        StringUtils.buildTimeString("time brake() etc. = ", System.nanoTime() - time, "ns").toString()
                );

                time = System.nanoTime();
                vehicleStepExecutor.moveAll(scenario);
                logger.trace(
                        StringUtils.buildTimeString("time move() = ", System.nanoTime() - time, "ns").toString()
                );

                time = System.nanoTime();
                vehicleStepExecutor.didMoveAll(scenario);
                logger.trace(
                        StringUtils.buildTimeString("time didMove() = ", System.nanoTime() - time, "ns").toString()
                );

                time = System.nanoTime();
                vehicleStepExecutor.spawnAll(scenario);
                logger.trace(
                        StringUtils.buildTimeString("time spawn() = ", System.nanoTime() - time, "ns").toString()
                );

                time = System.nanoTime();
                vehicleStepExecutor.updateNodes(scenario);
                logger.trace(
                        StringUtils.buildTimeString("time updateNodes() = ", System.nanoTime() - time, "ns").toString()
                );
            } else {
                vehicleStepExecutor.willMoveAll(scenario);
                vehicleStepExecutor.moveAll(scenario);
                vehicleStepExecutor.didMoveAll(scenario);
                vehicleStepExecutor.spawnAll(scenario);
                vehicleStepExecutor.updateNodes(scenario);
            }
            age++;
        }

        didRunOneStep();
    }

    @Override
    public void didRunOneStep() {
        for (StepListener stepListener : stepListeners)
            stepListener.didOneStep(this);

        logger.trace(StringUtils.buildTimeString(
                "time for this step = ",
                System.nanoTime() - time, "ns").toString()
        );
        logger.trace("number of vehicles after run = " + scenario.getVehicleContainer().getVehicleCount());
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
}