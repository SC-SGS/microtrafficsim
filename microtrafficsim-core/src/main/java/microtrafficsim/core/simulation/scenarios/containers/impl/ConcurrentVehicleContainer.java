package microtrafficsim.core.simulation.scenarios.containers.impl;

import microtrafficsim.core.logic.vehicles.VehicleState;
import microtrafficsim.core.logic.vehicles.machines.Vehicle;
import microtrafficsim.core.simulation.scenarios.containers.VehicleContainer;

import java.util.*;


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
        spawnedVehicles     = new TreeSet<>(Comparator.comparingLong(Vehicle::getId));
        notSpawnedVehicles  = new TreeSet<>(Comparator.comparingLong(Vehicle::getId));
        vehicles            = new TreeSet<>(Comparator.comparingLong(Vehicle::getId));
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
        TreeSet<Vehicle> set = new TreeSet<>(Comparator.comparingLong(Vehicle::getId));
        set.addAll(vehicles);
        return set;
    }

    /**
     * Addition to superclass: Due to concurrency, this method returns a shallow copy created synchronized.
     */
    @Override
    public synchronized Set<Vehicle> getSpawnedVehicles() {
        TreeSet<Vehicle> set = new TreeSet<>(Comparator.comparingLong(Vehicle::getId));
        set.addAll(spawnedVehicles);
        return set;
    }

    /**
     * Addition to superclass: Due to concurrency, this method returns a shallow copy created synchronized.
     */
    @Override
    public synchronized Set<Vehicle> getNotSpawnedVehicles() {
        TreeSet<Vehicle> set = new TreeSet<>(Comparator.comparingLong(Vehicle::getId));
        set.addAll(notSpawnedVehicles);
        return set;
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