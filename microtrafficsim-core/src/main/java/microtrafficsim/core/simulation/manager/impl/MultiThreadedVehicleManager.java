package microtrafficsim.core.simulation.manager.impl;

import microtrafficsim.core.entities.vehicle.IVisualizationVehicle;
import microtrafficsim.core.logic.vehicles.AbstractVehicle;
import microtrafficsim.core.logic.vehicles.VehicleState;
import microtrafficsim.core.simulation.manager.VehicleManager;

import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Supplier;

/**
 * TODO iwas mit Autos sinnvoll verwalten + concurrency
 * 
 * @author Dominic Parga Cacheiro, Jan-Oliver Schmidt
 */
public class MultiThreadedVehicleManager implements VehicleManager {

	private final Supplier<IVisualizationVehicle> vehicleFactory;
	private final Set<AbstractVehicle> spawnedVehicles, notSpawnedVehicles, vehicles;
	
	public MultiThreadedVehicleManager(Supplier<IVisualizationVehicle> vehicleFactory) {
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
	private ReentrantLock vehicleFactoryLock = new ReentrantLock(true);
	@Override
	public Supplier<IVisualizationVehicle> getVehicleFactory() {
		vehicleFactoryLock.lock();
		return vehicleFactory;
	}

    @Override
	public void unlockVehicleFactory() {
		vehicleFactoryLock.unlock();
	}

	/**
	 * Adds the vehicle as unspawned.
	 *
	 * @param vehicle Vehicle that has been added to the graph successfully.
	 */
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

    /*
        |==========================|
        | (i) VehicleStateListener |
        |==========================|
        */
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