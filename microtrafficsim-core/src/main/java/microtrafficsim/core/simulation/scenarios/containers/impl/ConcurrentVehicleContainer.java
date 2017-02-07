package microtrafficsim.core.simulation.scenarios.containers.impl;

import microtrafficsim.core.logic.vehicles.VehicleState;
import microtrafficsim.core.logic.vehicles.machines.Vehicle;
import microtrafficsim.core.simulation.scenarios.containers.VehicleContainer;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;


/**
 * This implementation of {@code VehicleContainer} uses a few sets for managing the vehicles. All methods are
 * synchronized.
 *
 * @author Dominic Parga Cacheiro
 */
public class ConcurrentVehicleContainer implements VehicleContainer {

    protected Set<Vehicle> spawnedVehicles, notSpawnedVehicles, vehicles;

    /**
     * Default constructor. It initializes the used sets as concurrent ones, so they can be edited while iterated.
     */
    public ConcurrentVehicleContainer() {
        spawnedVehicles     = new HashSet<>();
        notSpawnedVehicles  = new HashSet<>();
        vehicles            = new HashSet<>();
    }

    /*
    |======================|
    | (i) VehicleContainer |
    |======================|
    */
    @Override
    public synchronized void addVehicle(Vehicle vehicle) {
        notSpawnedVehicles.add(vehicle);
        vehicles.add(vehicle);
    }

    @Override
    public synchronized void clearAll() {
        spawnedVehicles.clear();
        notSpawnedVehicles.clear();
        vehicles.clear();
    }

    @Override
    public synchronized int getVehicleCount() {
        return vehicles.size();
    }

    @Override
    public synchronized int getSpawnedCount() {
        return spawnedVehicles.size();
    }

    @Override
    public synchronized int getNotSpawnedCount() {
        return notSpawnedVehicles.size();
    }

    /**
     * Addition to superclass: Due to concurrency, this method returns a shallow copy created synchronized.
     */
    @Override
    public synchronized Set<Vehicle> getVehicles() {
        return new HashSet<>(vehicles);
    }

    /**
     * Addition to superclass: Due to concurrency, this method returns a shallow copy created synchronized.
     */
    @Override
    public synchronized Set<Vehicle> getSpawnedVehicles() {
        return new HashSet<>(vehicles);
    }

    /**
     * Addition to superclass: Due to concurrency, this method returns a shallow copy created synchronized.
     */
    @Override
    public synchronized Set<Vehicle> getNotSpawnedVehicles() {
        return new HashSet<>(vehicles);
    }

    /*
    |==========================|
    | (i) VehicleStateListener |
    |==========================|
    */
    @Override
    public synchronized void stateChanged(Vehicle vehicle) {
        if (vehicle.getState() == VehicleState.DESPAWNED) {
            spawnedVehicles.remove(vehicle);
            notSpawnedVehicles.remove(vehicle);
            vehicles.remove(vehicle);
        } else if (vehicle.getState() == VehicleState.SPAWNED) {
            notSpawnedVehicles.remove(vehicle);
            spawnedVehicles.add(vehicle);
        }
    }

    /*
    |==============|
    | (i) Iterable |
    |==============|
    */
    @Override
    public Iterator<Vehicle> iterator() {
        return getVehicles().iterator();
    }
}