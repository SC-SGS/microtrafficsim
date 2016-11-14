package microtrafficsim.core.simulation.scenarios.containers.impl;

import microtrafficsim.core.entities.vehicle.VisualizationVehicleEntity;
import microtrafficsim.core.simulation.scenarios.containers.VehicleContainer;

import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Supplier;


/**
 * This implementation of {@link VehicleContainer} uses a few sets for managing the vehicles. The used sets are
 * concurrent so they can be edited while iterated and the vehicle factory is protected from multithreaded-access by a
 * lock.
 *
 * @author Dominic Parga Cacheiro
 */
public class ConcurrentVehicleContainer extends BasicVehicleContainer {

    private ReentrantLock vehicleFactoryLock;

    public ConcurrentVehicleContainer(Supplier<VisualizationVehicleEntity> vehicleFactory) {
        super(vehicleFactory);
        vehicleFactoryLock = new ReentrantLock(true);
    }

    /*
    |======================|
    | (i) VehicleContainer |
    |======================|
    */
    /**
     * This method locks the access to the vehicle factory until {@link #unlockVehicleFactory()} is called.
     */
    @Override
    public Supplier<VisualizationVehicleEntity> getVehicleFactory() {
        vehicleFactoryLock.lock();
        return super.getVehicleFactory();
    }

    /**
     * This method has to be called after using the vehicle factory, because, when called, {@link #getVehicleFactory()}
     * locks any other access to it.
     */
    @Override
    public void unlockVehicleFactory() {
        vehicleFactoryLock.unlock();
    }
}