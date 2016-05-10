package microtrafficsim.core.logic.vehicles.impl;

import microtrafficsim.core.logic.Route;
import microtrafficsim.core.logic.vehicles.VehicleStateListener;
import microtrafficsim.utils.id.LongIDGenerator;

/**
 * @author Dominic Parga Cacheiro
 */
public class BlockingCar extends Car {

    private boolean blocking;

    public BlockingCar(LongIDGenerator longIDGenerator, VehicleStateListener stateListener, Route route) {
        super(longIDGenerator, stateListener, route);
        blocking = false;
    }

    public BlockingCar(LongIDGenerator longIDGenerator, VehicleStateListener stateListener, Route route, int spawnDelay) {
        super(longIDGenerator, stateListener, route, spawnDelay);
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