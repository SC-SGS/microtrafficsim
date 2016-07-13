package microtrafficsim.core.logic.vehicles.impl;

import microtrafficsim.core.logic.Route;
import microtrafficsim.core.logic.vehicles.VehicleStateListener;
import microtrafficsim.core.simulation.configs.SimulationConfig;


/**
 * @author Dominic Parga Cacheiro
 */
public class BlockingCar extends Car {

    private boolean blocking;

    public BlockingCar(SimulationConfig config, VehicleStateListener stateListener, Route route) {
        super(config, stateListener, route);
        blocking = false;
    }

    public BlockingCar(SimulationConfig config, VehicleStateListener stateListener, Route route, int spawnDelay) {
        super(config, stateListener, route, spawnDelay);
        blocking = false;
    }

    public void toggleBlockMode() {
        blocking = !blocking;
    }

    public void setBlockMode(boolean value) {
        blocking = value;
    }

    @Override
    protected int getMaxVelocity() {
        return blocking ? 0 : super.getMaxVelocity();
    }

    @Override
    protected float getDashFactor() {
        return blocking ? 0 : super.getDashFactor();
    }

    @Override
    protected float getDawdleFactor() {
        return blocking ? 1 : super.getDawdleFactor();
    }
}