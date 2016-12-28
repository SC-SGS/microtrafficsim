package microtrafficsim.core.logic.vehicles.impl;

import microtrafficsim.core.logic.Node;
import microtrafficsim.core.logic.Route;
import microtrafficsim.core.logic.vehicles.VehicleStateListener;
import microtrafficsim.core.simulation.configs.SimulationConfig;


/**
 * This class extends the default {@link Car} by being able to stand if a boolean is set to true.
 *
 * @author Dominic Parga Cacheiro
 */
public class BlockingCar extends Car {

    private boolean blocking;

    public BlockingCar(long ID, long seed, VehicleStateListener stateListener,
                       Route<Node> route) {
        super(ID, seed, stateListener, route);
        blocking = false;
    }

    public BlockingCar(long ID, long seed, VehicleStateListener stateListener,
                       Route<Node> route, int spawnDelay) {
        super(ID, seed, stateListener, route, spawnDelay);
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