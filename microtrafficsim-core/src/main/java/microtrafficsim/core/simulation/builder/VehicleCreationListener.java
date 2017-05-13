package microtrafficsim.core.simulation.builder;

import microtrafficsim.core.logic.vehicles.machines.Vehicle;

/**
 * @author Dominic Parga Cacheiro
 */
public interface VehicleCreationListener {
    void didCreateVehicle(Vehicle vehicle);
}
