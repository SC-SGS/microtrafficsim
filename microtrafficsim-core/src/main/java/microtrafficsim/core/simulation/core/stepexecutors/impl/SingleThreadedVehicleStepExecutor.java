package microtrafficsim.core.simulation.core.stepexecutors.impl;

import microtrafficsim.core.logic.nodes.Node;
import microtrafficsim.core.logic.vehicles.machines.Vehicle;
import microtrafficsim.core.simulation.core.stepexecutors.VehicleStepExecutor;
import microtrafficsim.core.simulation.scenarios.Scenario;
import microtrafficsim.core.logic.NagelSchreckenbergException;


/**
 * A single-threaded implementation of {@link VehicleStepExecutor}.
 *
 * @author Dominic Parga Cacheiro
 */
public class SingleThreadedVehicleStepExecutor implements VehicleStepExecutor {

    @Override
    public void willMoveAll(final Scenario scenario) {
        for (Vehicle vehicle : scenario.getVehicleContainer().getSpawnedVehicles()) {
            vehicle.accelerate();
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
        for (Vehicle vehicle : scenario.getVehicleContainer().getSpawnedVehicles())
            vehicle.move();
    }

    @Override
    public void didMoveAll(final Scenario scenario) {
        for (Vehicle vehicle : scenario.getVehicleContainer().getSpawnedVehicles())
            vehicle.didMove();
    }

    @Override
    public void spawnAll(final Scenario scenario) {
        for (Vehicle vehicle : scenario.getVehicleContainer().getNotSpawnedVehicles())
            vehicle.spawn();
    }

    @Override
    public void updateNodes(final Scenario scenario) {
        for (Node node : scenario.getGraph().getNodes())
            node.update();
    }
}
