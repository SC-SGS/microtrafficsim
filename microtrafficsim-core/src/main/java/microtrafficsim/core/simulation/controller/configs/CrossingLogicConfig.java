package microtrafficsim.core.simulation.controller.configs;

import microtrafficsim.utils.valuewrapper.LazyFinalValue;

/**
 * This class isolates the crossing logic configs from the other config
 * parameters to guarantee better overview.
 * 
 * @author Dominic Parga Cacheiro
 */
public final class CrossingLogicConfig {
	
	private LazyFinalValue<Boolean> drivingOnTheRight; // or left
	public boolean edgePriorityEnabled;
	public boolean priorityToTheRightEnabled;
	private boolean onlyOneVehicleEnabled;
	public boolean goWithoutPriorityEnabled;
	
	public CrossingLogicConfig() {
		reset();
	}

    void reset() {
        drivingOnTheRight = new LazyFinalValue<>(true);
        edgePriorityEnabled = true;
        priorityToTheRightEnabled = true;
        onlyOneVehicleEnabled = false;
        goWithoutPriorityEnabled = false;
    }

    void reset(CrossingLogicConfig config) {
        drivingOnTheRight = new LazyFinalValue<>(config.drivingOnTheRight.get());
        edgePriorityEnabled = config.edgePriorityEnabled;
        priorityToTheRightEnabled = config.priorityToTheRightEnabled;
        onlyOneVehicleEnabled = config.onlyOneVehicleEnabled;
        goWithoutPriorityEnabled = config.goWithoutPriorityEnabled;
    }

    /*
    |========|
    | getter |
    |========|
    */
    public LazyFinalValue<Boolean> drivingOnTheRight() { return drivingOnTheRight; }

	/**
	 * This method guarantees, if right-before-left (or left-before-right) is
	 * disabled, then only one vehicle is allowed to drive. In this case, the
	 * previously set value of onlyOneVehicle doesn't matter. This is needed,
	 * because otherwise, exceptions or wrong behavior would occur. E.g. two
	 * vehicles cross each others way, but caused by randomness, both of them
	 * get permission to cross the node. In worst case, they want to drive on
	 * the same destination edge and on the same cell.
	 * 
	 * @return onlyOneVehicleEnabled || !priorityToTheRightEnabled
	 */
	public boolean isOnlyOneVehicleEnabled() {
		return onlyOneVehicleEnabled || !priorityToTheRightEnabled;
	}
	
	/**
	 * For more information, see {@link #isOnlyOneVehicleEnabled()}
	 * 
	 * @param enabled The new value of onlyOneVehicleEnabled
	 */
	public void setOnlyOneVehicle(boolean enabled) {
		onlyOneVehicleEnabled = enabled;
	}
}