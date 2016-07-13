package microtrafficsim.core.logic.vehicles;


/**
 * This listener serves methods for {@link AbstractVehicle}s to notify in case their
 * {@link VehicleState} has changed.
 *
 * @author Maximilian Luz, Dominic Parga Cacheiro, Jan-Oliver Schmidt
 */
public interface VehicleStateListener {

    /**
     * Is called if a vehicle changes its {@link VehicleState}. The vehicle
     * should have a getter for its state.
     *
     * @param vehicle Vehicle that changed its state.
     */
    void stateChanged(AbstractVehicle vehicle);
}