package microtrafficsim.core.simulation.controller.manager.impl;

import microtrafficsim.core.logic.Node;
import microtrafficsim.core.logic.vehicles.AbstractVehicle;
import microtrafficsim.core.simulation.controller.manager.SimulationManager;

import java.util.Iterator;

/**
 * @author Dominic Parga Cacheiro
 */
public class SingleThreadedSimulationManager implements SimulationManager {

    @Override
    public void willMoveAll(Iterator<AbstractVehicle> spawnedVehicle) {
        spawnedVehicle.forEachRemaining((AbstractVehicle vehicle) -> {
            vehicle.accelerate();
            vehicle.dash();
            vehicle.brake();
            vehicle.dawdle();
        });
    }

    @Override
    public void moveAll(Iterator<AbstractVehicle> iteratorSpawned) {
        iteratorSpawned.forEachRemaining(AbstractVehicle::move);
    }

    @Override
    public void didMoveAll(Iterator<AbstractVehicle> iteratorSpawned) {
        iteratorSpawned.forEachRemaining(AbstractVehicle::didMove);
    }

    @Override
    public void spawnAll(Iterator<AbstractVehicle> iteratorNotSpawned) {
        iteratorNotSpawned.forEachRemaining(AbstractVehicle::spawn);
    }

    @Override
    public void updateNodes(Iterator<Node> iter) {
        iter.forEachRemaining(Node::update);
    }
}
