package microtrafficsim.core.simulation.scenarios.containers.impl;

import microtrafficsim.core.entities.vehicle.VisualizationVehicleEntity;
import microtrafficsim.core.logic.vehicles.AbstractVehicle;
import microtrafficsim.core.logic.vehicles.VehicleState;
import microtrafficsim.core.simulation.scenarios.containers.VehicleContainer;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;


/**
 * This implementation of {@code VehicleContainer} uses a few sets for managing the vehicles. The used sets are
 * concurrent ({@link ConcurrentHashMap#newKeySet()}) so they can be edited while iterated.
 *
 * @author Dominic Parga Cacheiro
 */
public class ConcurrentVehicleContainer implements VehicleContainer {

    protected Supplier<VisualizationVehicleEntity> vehicleFactory;
    protected Set<AbstractVehicle>                 spawnedVehicles, notSpawnedVehicles, vehicles;

    /**
     * Default constructor. It initializes the used sets as concurrent ones, so they can be edited while iterated.
     *
     * @param vehicleFactory This factory is needed to create the vehicles' visualization components.
     */
    public ConcurrentVehicleContainer(Supplier<VisualizationVehicleEntity> vehicleFactory) {
        clearAll();
        this.vehicleFactory = vehicleFactory;
    }

    /*
    |======================|
    | (i) VehicleContainer |
    |======================|
    */
    @Override
    public void addVehicle(AbstractVehicle vehicle) {
        notSpawnedVehicles.add(vehicle);
        vehicles.add(vehicle);
    }

    @Override
    public void clearAll() {
        spawnedVehicles     = ConcurrentHashMap.newKeySet();
        notSpawnedVehicles  = ConcurrentHashMap.newKeySet();
        vehicles            = ConcurrentHashMap.newKeySet();
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
    public int getNotSpawnedCount() {
        return notSpawnedVehicles.size();
    }

    @Override
    public Set<AbstractVehicle> getVehicles() {
        return Collections.unmodifiableSet(vehicles);
    }

    @Override
    public Set<AbstractVehicle> getSpawnedVehicles() {
        return Collections.unmodifiableSet(spawnedVehicles);
    }

    @Override
    public Set<AbstractVehicle> getNotSpawnedVehicles() {
        return Collections.unmodifiableSet(notSpawnedVehicles);
    }

    /*
    |==========================|
    | (i) VehicleStateListener |
    |==========================|
    */
    @Override
    public void stateChanged(AbstractVehicle vehicle) {
        if (vehicle.getState() == VehicleState.DESPAWNED) {
            spawnedVehicles.remove(vehicle);
            notSpawnedVehicles.remove(vehicle);
            vehicles.remove(vehicle);
        } else if (vehicle.getState() == VehicleState.SPAWNED) {
            notSpawnedVehicles.remove(vehicle);
            spawnedVehicles.add(vehicle);
        }
    }
}