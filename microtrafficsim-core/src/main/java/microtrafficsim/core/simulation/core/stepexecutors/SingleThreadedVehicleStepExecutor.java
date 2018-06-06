package microtrafficsim.core.simulation.core.stepexecutors;

import microtrafficsim.core.logic.nodes.Node;
import microtrafficsim.core.logic.vehicles.machines.Vehicle;
import microtrafficsim.core.simulation.scenarios.Scenario;


/**
 * A single-threaded implementation of {@link VehicleStepExecutor}.
 *
 * @author Dominic Parga Cacheiro
 */
public class SingleThreadedVehicleStepExecutor implements VehicleStepExecutor {
    @Override
    public void accelerateAll(Scenario scenario) {
        for (Vehicle vehicle : scenario.getVehicleContainer().getSpawnedVehicles()) {
            vehicle.accelerate();
            vehicle.willChangeLane();
        }
    }

    @Override
    public void willChangeLaneAll(Scenario scenario) {
        for (Vehicle vehicle : scenario.getVehicleContainer().getSpawnedVehicles()) {
            vehicle.willChangeLane();
        }
    }

    @Override
    public void changeLaneAll(Scenario scenario) {
        for (Vehicle vehicle : scenario.getVehicleContainer().getSpawnedVehicles()) {
            vehicle.changeLane();
        }
    }

    @Override
    public void brakeAll(final Scenario scenario) {
        for (Vehicle vehicle : scenario.getVehicleContainer().getSpawnedVehicles()) {
            vehicle.brake();
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
