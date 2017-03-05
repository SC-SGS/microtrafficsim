package microtrafficsim.core.simulation.configs;


/**
 * This class isolates the crossing logic configs from the other config
 * parameters to guarantee better overview.
 *
 * @author Dominic Parga Cacheiro
 */
public final class CrossingLogicConfig {
    public boolean drivingOnTheRight;    // or left
    public boolean edgePriorityEnabled;
    public boolean priorityToTheRightEnabled;
    public boolean friendlyStandingInJamEnabled;
    public boolean onlyOneVehicleEnabled;

    /**
     * Just calls {@link #setup()}.
     */
    public CrossingLogicConfig() {
        setup();
    }

    /**
     * Setup the parameters of this config file.
     */
    public void setup() {
        drivingOnTheRight            = true;
        edgePriorityEnabled          = true;
        priorityToTheRightEnabled    = true;
        onlyOneVehicleEnabled        = false;
        friendlyStandingInJamEnabled = true;
    }

    /**
     * Updates the parameter of this config file.
     *
     * @param config All values of the new config instance are set to this config-values.
     */
    public void update(CrossingLogicConfig config) {
        drivingOnTheRight            = config.drivingOnTheRight;
        edgePriorityEnabled          = config.edgePriorityEnabled;
        priorityToTheRightEnabled    = config.priorityToTheRightEnabled;
        onlyOneVehicleEnabled        = config.onlyOneVehicleEnabled;
        friendlyStandingInJamEnabled = config.friendlyStandingInJamEnabled;
    }
}