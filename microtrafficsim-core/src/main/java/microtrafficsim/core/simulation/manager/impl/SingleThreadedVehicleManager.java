package microtrafficsim.core.simulation.manager.impl;

import microtrafficsim.core.frameworks.vehicle.IVisualizationVehicle;
import microtrafficsim.core.logic.vehicles.AbstractVehicle;
import microtrafficsim.core.logic.vehicles.VehicleState;
import microtrafficsim.core.simulation.manager.VehicleManager;

import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

/**
 * @author Dominic Parga Cacheiro
 */
public class SingleThreadedVehicleManager implements VehicleManager {

    private Supplier<IVisualizationVehicle> vehicleFactory;
    private Set<AbstractVehicle> spawnedVehicles, notSpawnedVehicles, vehicles;

    public SingleThreadedVehicleManager(Supplier<IVisualizationVehicle> vehicleFactory) {
        spawnedVehicles = ConcurrentHashMap.newKeySet();
        notSpawnedVehicles = ConcurrentHashMap.newKeySet();
        vehicles = ConcurrentHashMap.newKeySet();
        this.vehicleFactory = vehicleFactory;
    }

    private void addVehicle(AbstractVehicle vehicle, boolean spawned) {
        if (spawned) {
            spawnedVehicles.add(vehicle);
        } else {
            notSpawnedVehicles.add(vehicle);
        }
        vehicles.add(vehicle);
    }

    private void removeVehicle(AbstractVehicle vehicle, boolean spawned) {
        if (spawned) {
            spawnedVehicles.remove(vehicle);
        } else {
            notSpawnedVehicles.remove(vehicle);
        }
        vehicles.remove(vehicle);
    }

    /*
    |====================|
    | (i) VehicleManager |
    |====================|
    */
    @Override
    public Supplier<IVisualizationVehicle> getVehicleFactory() {
        return vehicleFactory;
    }

    @Override
    public void unlockVehicleFactory() {
        // nothing to do because single threaded
    }

    @Override
    public void addVehicle(AbstractVehicle vehicle) {
        addVehicle(vehicle, false);
    }

    @Override
    public int getVehicleCount() {
        return vehicles.size();
    }

    @Override
    public int getSpawnedCount() {
        return spawnedVehicles.size();
    }

    @Override
    public Iterator<AbstractVehicle> iteratorSpawned() {
        return spawnedVehicles.iterator();
    }

    @Override
    public Iterator<AbstractVehicle> iteratorNotSpawned() {
        return notSpawnedVehicles.iterator();
    }

    @Override
    public void clearAll() {
        notSpawnedVehicles.clear();
        spawnedVehicles.clear();
        vehicles.clear();
    }

    @Override
    public Set<AbstractVehicle> getSpawnedVehicles() {
        return spawnedVehicles;
    }

    @Override
    public Set<AbstractVehicle> getVehicles() {
        return vehicles;
    }

    @Override
    public void stateChanged(AbstractVehicle vehicle) {

        if (vehicle.getState() == VehicleState.DESPAWNED) {
            removeVehicle(vehicle, false);
            removeVehicle(vehicle, true);
        } else if(vehicle.getState() == VehicleState.SPAWNED) {
            removeVehicle(vehicle, false);
            addVehicle(vehicle, true);
        }
    }
}
