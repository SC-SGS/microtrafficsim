package microtrafficsim.core.simulation.core.stepexecutors.impl;

import microtrafficsim.core.logic.Node;
import microtrafficsim.core.logic.vehicles.AbstractVehicle;
import microtrafficsim.core.simulation.core.stepexecutors.VehicleStepExecutor;
import microtrafficsim.core.simulation.scenarios.Scenario;

import java.util.Iterator;


/**
 * A single-threaded implementation of {@link VehicleStepExecutor}.
 *
 * @author Dominic Parga Cacheiro
 */
public class SingleThreadedVehicleStepExecutor implements VehicleStepExecutor {

    @Override
    public void willMoveAll(final Scenario scenario) {
        scenario.getVehicleContainer().getSpawnedVehicles().forEach((AbstractVehicle vehicle) -> {
            vehicle.accelerate();
            vehicle.dash();
            vehicle.brake();
            vehicle.dawdle();
        });
    }

    @Override
    public void moveAll(final Scenario scenario) {
        scenario.getVehicleContainer().getSpawnedVehicles().forEach(AbstractVehicle::move);
    }

    @Override
    public void didMoveAll(final Scenario scenario) {
        scenario.getVehicleContainer().getSpawnedVehicles().forEach(AbstractVehicle::didMove);
    }

    @Override
    public void spawnAll(final Scenario scenario) {
        scenario.getVehicleContainer().getNotSpawnedVehicles().forEach(AbstractVehicle::spawn);
    }

    @Override
    public void updateNodes(final Scenario scenario) {
        scenario.getGraph().getNodeIterator().forEachRemaining(Node::update);
    }
}
