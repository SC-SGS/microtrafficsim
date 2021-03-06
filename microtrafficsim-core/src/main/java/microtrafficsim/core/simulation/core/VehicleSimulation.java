package microtrafficsim.core.simulation.core;

import microtrafficsim.core.logic.vehicles.machines.Vehicle;
import microtrafficsim.core.simulation.core.stepexecutors.MultiThreadedVehicleStepExecutor;
import microtrafficsim.core.simulation.core.stepexecutors.SingleThreadedVehicleStepExecutor;
import microtrafficsim.core.simulation.core.stepexecutors.VehicleStepExecutor;
import microtrafficsim.core.simulation.scenarios.Scenario;
import microtrafficsim.utils.logging.EasyMarkableLogger;
import microtrafficsim.utils.strings.StringUtils;
import org.slf4j.Logger;

import java.util.LinkedList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;


/**
 * This class is an implementation of {@link Simulation} for {@link Vehicle}s.
 *
 * @author Dominic Parga Cacheiro
 */
public class VehicleSimulation implements Simulation {
    private Logger logger = new EasyMarkableLogger(VehicleSimulation.class);

    private Scenario scenario;
    protected VehicleStepExecutor vehicleStepExecutor;

    // simulation steps
    private boolean            paused;
    private TimerTask          timerTask;
    private final Lock         executionLock;
    private int                age;
    private List<StepListener> stepListeners;

    // logging
    private long time;

    /**
     * Default constructor. Before this simulation can be used, it needs a scenario!
     */
    public VehicleSimulation() {
        paused = true;
        executionLock = new ReentrantLock(true); // fairness is important for cancelling simulation
        this.stepListeners = new LinkedList<>();
    }

    /**
     * Default constructor. Adopts the given scenario by calling {@link #setAndInitPreparedScenario(Scenario)}.
     *
     * @param scenario This scenario is executed later.
     */
    public VehicleSimulation(Scenario scenario) {
        this();
        setAndInitPreparedScenario(scenario);
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

        if (!scenario.isPrepared())
            throw new RuntimeException("The simulation sets a new scenario but the scenario is not prepared.");

        /* remove old scenario */
        removeCurrentScenario();

        /* add new scenario */
        age = 0;
        this.scenario = scenario;
        addStepListener(scenario);
        int nThreads = scenario.getConfig().multiThreading.nThreads;
        vehicleStepExecutor =
                nThreads > 1 ?
                        new MultiThreadedVehicleStepExecutor(nThreads) :
                        new SingleThreadedVehicleStepExecutor();

        vehicleStepExecutor.updateNodes(this.scenario);
    }

    @Override
    public void removeCurrentScenario() {
        if (!isPaused())
            throw new RuntimeException("The simulation removes its current scenario but is not paused.");

        if (scenario == null)
            return;

        do
            stepListeners.remove(scenario);
        while (stepListeners.contains(scenario));

        scenario = null;
        age = -1;
        vehicleStepExecutor = null;
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

    /**
     * The internal collection used to store listeners is a {@link LinkedList} for easy iterating. Due to its
     * runtime in O(n) for checking whether an Object is contained or not, this method {@code addStepListener} DOES
     * NOT check for duplicates. Thus if you have added a listener twice, it is only removed once due to
     * {@link LinkedList#remove(Object)}
     */
    @Override
    public void removeStepListener(StepListener stepListener) {
        stepListeners.remove(stepListener);
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
            new Timer().schedule(timerTask, 0, Math.max(1, 1000 / scenario.getConfig().speedup));
            paused = false;
        }
    }

    @Override
    public void willRunOneStep() {
        for (StepListener stepListener : stepListeners)
            stepListener.willDoOneStep(this);

        if (logger.isTraceEnabled()) {
            logger.trace("########## ###d####### ########## ########## ##");

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
        executionLock.lock();
        unsecureDoRunOneStep();
        executionLock.unlock();
    }

    /**
     * Not thread safe!
     */
    protected void unsecureDoRunOneStep() {
        willRunOneStep();

        if (scenario.isPrepared()) {
            if (logger.isTraceEnabled()) {
                time = System.nanoTime();
                vehicleStepExecutor.accelerateAll(scenario);
                logger.trace(
                        StringUtils.buildTimeString(
                                "time accelerate() and changeLane() = ",
                                System.nanoTime() - time, "ns").toString()
                );

                long stamp = System.nanoTime();
                vehicleStepExecutor.willChangeLaneAll(scenario);
                logger.trace(
                        StringUtils.buildTimeString("time willChangeLane() etc. = ", System.nanoTime() - stamp, "ns")
                                .toString()
                );

                stamp = System.nanoTime();
                vehicleStepExecutor.changeLaneAll(scenario);
                logger.trace(
                        StringUtils.buildTimeString("time changeLane() etc. = ", System.nanoTime() - stamp, "ns")
                                .toString()
                );

                stamp = System.nanoTime();
                vehicleStepExecutor.brakeAll(scenario);
                logger.trace(
                        StringUtils.buildTimeString("time brake() etc. = ", System.nanoTime() - stamp, "ns").toString()
                );

                stamp = System.nanoTime();
                vehicleStepExecutor.moveAll(scenario);
                logger.trace(
                        StringUtils.buildTimeString("time move() = ", System.nanoTime() - stamp, "ns").toString()
                );

                stamp = System.nanoTime();
                vehicleStepExecutor.didMoveAll(scenario);
                logger.trace(
                        StringUtils.buildTimeString("time didMove() = ", System.nanoTime() - stamp, "ns").toString()
                );

                stamp = System.nanoTime();
                vehicleStepExecutor.spawnAll(scenario);
                logger.trace(
                        StringUtils.buildTimeString("time spawn() = ", System.nanoTime() - stamp, "ns").toString()
                );

                stamp = System.nanoTime();
                vehicleStepExecutor.updateNodes(scenario);
                logger.trace(
                        StringUtils.buildTimeString("time updateNodes() = ", System.nanoTime() - stamp, "ns").toString()
                );
            } else {
                vehicleStepExecutor.accelerateAll(scenario);
                vehicleStepExecutor.willChangeLaneAll(scenario);
                vehicleStepExecutor.changeLaneAll(scenario);
                vehicleStepExecutor.brakeAll(scenario);
                vehicleStepExecutor.moveAll(scenario);
                vehicleStepExecutor.didMoveAll(scenario);
                vehicleStepExecutor.spawnAll(scenario);
                vehicleStepExecutor.updateNodes(scenario);
            }
            incAge();
        }

        didRunOneStep();
    }

    protected void incAge() {
        age++;
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
            executionLock.lock();
            timerTask.cancel();
            timerTask = null;
            executionLock.unlock();
        }
        paused = true;
    }

    @Override
    public final boolean isPaused() {
        return paused;
    }
}