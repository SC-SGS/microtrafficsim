package logic.old_validation.cars;

import microtrafficsim.core.logic.Node;
import microtrafficsim.core.logic.Route;
import microtrafficsim.core.logic.vehicles.VehicleStateListener;
import microtrafficsim.core.logic.vehicles.impl.Car;
import microtrafficsim.core.simulation.configs.SimulationConfig;

import java.util.function.Function;


/**
 * @author Dominic Parga Cacheiro
 */
public class ValidationCar extends Car {
    public ValidationCar(long ID, long seed, VehicleStateListener stateListener, Route<Node>
            route) {
        super(ID, seed, stateListener, route);
    }

    public ValidationCar(long ID, long seed, VehicleStateListener stateListener, Route<Node> route, int
            spawnDelay) {
        super(ID, seed, stateListener, route, spawnDelay);
    }

    /*
    |=====================|
    | (c) AbstractVehicle |
    |=====================|
    */
    @Override
    protected Function<Integer, Integer> createAccelerationFunction() {
        return v -> v + 1;
    }

    @Override
    protected Function<Integer, Integer> createDawdleFunction() {
        return v -> (v < 1) ? 0 : v - 1;
    }

    @Override
    protected int getMaxVelocity() {
        return 5;
    }

    @Override
    protected float getDawdleFactor() {
        return 0;
    }

    @Override
    protected float getDashFactor() {
        return 0;
    }
}