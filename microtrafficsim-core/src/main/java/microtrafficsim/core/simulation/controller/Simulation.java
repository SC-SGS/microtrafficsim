package microtrafficsim.core.simulation.controller;

import microtrafficsim.core.frameworks.vehicle.IVisualizationVehicle;
import microtrafficsim.core.logic.vehicles.AbstractVehicle;
import microtrafficsim.core.logic.vehicles.VehicleStateListener;

import java.util.ArrayList;
import java.util.Timer;

/**
 * This interface serves methods to organize a simulation.
 * 
 * @author Dominic Parga Cacheiro
 */
public interface Simulation extends VehicleStateListener {

    public static void printUsage() {
    }

	/*
	|========================|
	| simulation preperation |
	|========================|
	*/
    /**
     * This
     */
    public static void setUpConfig() {
        // empty
    }

    /**
	 * @return True, if {@link #prepare()} has finished;
	 *         False otherwise
	 */
	public boolean isPrepared();

	/**
	 * This method clears all vehicle lists and should initialize all important aspects, e.g. creating vehicles and
     * routes. After finishing, {@link #isPrepared()} will return true.
	 */
	public void prepare();

	/*
	|==================|
	| simulation steps |
	|==================|
	*/
	/**
	 * @return Number of finished simulation steps.
	 */
	public int getAge();
	
	/**
	 * <p>
	 * This method starts calling the simulation steps repeatedly. Nothing will
	 * be done if the simulation is already running.
	 * </p>
	 * <p>
	 * The repeated simulation steps are started by a {@link Timer}. You can
	 * stop it by calling {@link #cancel()}. You can call {@link #isPaused()} to
	 * ask if the simulation is running.
	 * </p>
	 */
	public void run();

	/**
	 * <p>
	 * This method is called before each simulation step. The default
	 * implementation does nothing.
	 * </p>
	 */
	public void willRunOneStep();

	/**
	 * <p>
	 * This method does one simulation step if the simulation is paused. Nothing will be done
	 * if the simulation is already running.
	 * </p>
     * <p>
     * Calls {@link #willRunOneStep()} and {@link #didRunOneStep()}.
     * </p>
	 */
	public void runOneStep();

    /**
     * Does the same as {@link #runOneStep()} but independant from {@link #isPaused()}
     */
    public void doRunOneStep();

	/**
	 * <p>
	 * This method is called after each simulation step. The default
	 * implementation does nothing.
	 * </p>
	 */
	public void didRunOneStep();
	
	/**
	 * This method should stop iterating the simulation steps after the
	 * currently running simulation step and letting {@link #isPaused()}
	 * returning true. One way could be using {@link Timer#cancel()} of
	 * {@link Timer}.
	 */
	public void cancel();

	/**
	 * @return True, if the simulation does nothing; False if it is running.
	 */
	public boolean isPaused();

	/*
	|==========|
	| vehicles |
	|==========|
	*/
	/**
	 * @return A copy of the list containing all spawned vehicles. This list
	 *         does not contain a copy of the vehicles, but the vehicles itself.
	 */
	public ArrayList<? extends AbstractVehicle> getSpawnedVehicles();

    /**
     * @return A copy of the list containing all vehicles (spawned and not yet spawned). This list
     *         does not contain a copy of the vehicles, but the vehicles itself.
     */
    public ArrayList<? extends AbstractVehicle> getVehicles();

	/**
	 * @return The number of spawned vehicles.
	 */
	public int getSpawnedVehiclesCount();

	/**
	 * @return The number of all vehicles (spawned and not spawned).
	 */
	public int getVehiclesCount();
	
	/**
	 * @return Instance of {@link IVisualizationVehicle}.
	 */
	public IVisualizationVehicle createVisVehicle();

	/**
	 * This method adds the given vehicle to the graph and if success, this
	 * method adds the vehicle to the list of not yet spawned vehicles, too.
	 * 
	 * @param vehicle
	 *            should be added
	 * @return True, if vehicle has been added to the graph; False otherwise
	 */
	public boolean addVehicle(AbstractVehicle vehicle);
}