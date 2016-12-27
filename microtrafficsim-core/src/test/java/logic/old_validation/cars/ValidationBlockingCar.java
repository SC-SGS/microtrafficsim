package logic.old_validation.cars;

import microtrafficsim.core.logic.Node;
import microtrafficsim.core.logic.Route;
import microtrafficsim.core.logic.vehicles.VehicleStateListener;
import microtrafficsim.core.simulation.configs.SimulationConfig;


/**
 * @author Dominic Parga Cacheiro
 */
public class ValidationBlockingCar extends ValidationCar {

    private boolean blocking;

    public ValidationBlockingCar(SimulationConfig config, VehicleStateListener stateListener, Route<Node> route) {
        super(config, stateListener, route);
        blocking = false;
    }

    public ValidationBlockingCar(SimulationConfig config, VehicleStateListener stateListener, Route<Node> route,
                                 int spawnDelay) {
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