package microtrafficsim.core.simulation.core;

import microtrafficsim.core.logic.vehicles.VehicleStateListener;
import microtrafficsim.core.simulation.builder.Builder;
import microtrafficsim.core.simulation.scenarios.Scenario;

import java.util.Timer;


/**
 * <p>
 * A simulation setup consists of three major parts: <br>
 * &bull {@link Simulation}: the executor of simulation steps <br>
 * &bull {@link Scenario}: the definition of routes etc. <br>
 * &bull {@link Builder}: the scenario builder; e.g. pre-calculating routes by a
 * given scenario
 *
 * <p>
 * The simulation serves methods to execute a scenario after a builder
 * prepared it.
 *
 * @author Dominic Parga Cacheiro
 */
public interface Simulation extends VehicleStateListener {

    /*
    |==========|
    | scenario |
    |==========|
    */
    /**
     * @return Currently executed scenario
     */
    Scenario getScenario();

    /**
     * Sets the scenario being executed. The simulation should be paused for this call. The scenario is initialized
     * by updating all graph nodes.
     *
     * @param scenario This scenario is being executed
     */
    void initScenario(Scenario scenario);

    /*
    |======================|
    | simulation execution |
    |======================|
    */
    /**
     * @return Number of finished simulation steps.
     */
    int getAge();

    /**
     * <p>
     * This method starts calling the simulation steps repeatedly. Nothing will
     * be done if the simulation is already running.
     *
     * <p>
     * The repeated simulation steps are started by a {@link Timer}. You can
     * stop it by calling {@link #cancel()}. You can call {@link #isPaused()} to
     * ask if the simulation is paused.
     */
    void run();

    /**
     * <p>
     * This method is called before each simulation step. The default
     * implementation does nothing.
     */
    default void willRunOneStep() {}

    /**
     * Does the same as {@link #doRunOneStep()} but only if the simulation is paused
     * ({@link #isPaused()} {@code == true}). Nothing will be done if the simulation is already running.
     */
    void runOneStep();

    /**
     * <p>
     * This method does one simulation step if the simulation is prepared.
     *
     * <p>
     * Calls {@link #willRunOneStep()} before a step and {@link #didRunOneStep()} after it.
     */
    void doRunOneStep();

    /**
     * <p>
     * This method is called after each simulation step. The default
     * implementation does nothing.
     */
    default void didRunOneStep() {}

    /**
     * This method should stop iterating the simulation steps after the
     * currently running simulation step and letting {@link #isPaused()}
     * returning true. One way could be using {@link Timer#cancel()} of
     * {@link Timer}.
     */
    void cancel();

    /**
     * @return True, if the simulation does nothing; False if it is running.
     */
    boolean isPaused();
}