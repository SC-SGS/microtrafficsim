package microtrafficsim.core.simulation.core.stepexecutors.impl;

import microtrafficsim.core.logic.Node;
import microtrafficsim.core.logic.vehicles.AbstractVehicle;
import microtrafficsim.core.simulation.core.stepexecutors.VehicleStepExecutor;
import microtrafficsim.core.simulation.scenarios.Scenario;
import microtrafficsim.exceptions.core.logic.NagelSchreckenbergException;

import java.util.Iterator;


/**
 * A single-threaded implementation of {@link VehicleStepExecutor}.
 *
 * @author Dominic Parga Cacheiro
 */
public class SingleThreadedVehicleStepExecutor implements VehicleStepExecutor {

    @Override
    public void willMoveAll(final Scenario scenario) {
        for (AbstractVehicle vehicle : scenario.getVehicleContainer().getSpawnedVehicles()) {
            vehicle.accelerate();
            vehicle.dash();
            try {
                vehicle.brake();
            } catch (NagelSchreckenbergException e) {
                e.printStackTrace();
            }
            vehicle.dawdle();
        }
    }

    @Override
    public void moveAll(final Scenario scenario) {
        for (AbstractVehicle vehicle : scenario.getVehicleContainer().getSpawnedVehicles())
            vehicle.move();
    }

    @Override
    public void didMoveAll(final Scenario scenario) {
        for (AbstractVehicle vehicle : scenario.getVehicleContainer().getSpawnedVehicles())
            vehicle.didMove();
    }

    @Override
    public void spawnAll(final Scenario scenario) {
        for (AbstractVehicle vehicle : scenario.getVehicleContainer().getNotSpawnedVehicles())
            vehicle.spawn();
    }

    @Override
    public void updateNodes(final Scenario scenario) {
        for (Node node : scenario.getGraph().getNodes())
            node.update();
    }
}
