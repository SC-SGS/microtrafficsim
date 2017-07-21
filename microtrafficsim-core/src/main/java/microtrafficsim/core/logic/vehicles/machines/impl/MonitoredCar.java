package microtrafficsim.core.logic.vehicles.machines.impl;

import microtrafficsim.core.logic.vehicles.machines.MonitoredVehicle;
import microtrafficsim.core.map.style.VehicleStyleSheet;

/**
 * @author Dominic Parga Cacheiro
 */
public class MonitoredCar extends Car implements MonitoredVehicle {
    public MonitoredCar(long id, VehicleStyleSheet style) {
        super(id, style);
    }

    public MonitoredCar(long id, int maxVelocity, VehicleStyleSheet style) {
        super(id, maxVelocity, style);
    }
}
