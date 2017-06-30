package microtrafficsim.core.simulation.core;

import microtrafficsim.core.logic.vehicles.machines.Vehicle;
import microtrafficsim.core.simulation.scenarios.Scenario;

import java.util.LinkedList;
import java.util.List;

/**
 * Monitors all vehicles implementing {@link MonitoringVehicle}
 *
 * @author Dominic Parga Cacheiro
 */
public class MonitoringVehicleSimulation extends VehicleSimulation {
    private List<VehicleStamp> speedStamps = new LinkedList<>();


    @Override
    protected void unsecureDoRunOneSteup() {
        willRunOneStep();

        Scenario scenario = getScenario();
        if (scenario.isPrepared()) {
            long time = System.nanoTime();
            vehicleStepExecutor.accelerateAll(scenario);
            vehicleStepExecutor.willChangeLaneAll(scenario);
            vehicleStepExecutor.changeLaneAll(scenario);
            vehicleStepExecutor.brakeAll(scenario);
            vehicleStepExecutor.moveAll(scenario);
            vehicleStepExecutor.didMoveAll(scenario);
            vehicleStepExecutor.spawnAll(scenario);
            vehicleStepExecutor.updateNodes(scenario);
            incAge();
        }

        didRunOneStep();
    }

    @Override
    public void didRunOneStep() {
        super.didRunOneStep();

        for (Vehicle vehicle : getScenario().getVehicleContainer()) {
            if (vehicle instanceof MonitoringVehicle) {
                VehicleStamp stamp = new VehicleStamp();
                stamp.age = getAge();
                stamp.vehicleId = vehicle.getId();
                stamp.velocity = vehicle.getVelocity();
                stamp.cellPosition = vehicle.getCellPosition();
                stamp.edgeId = vehicle.getLane().getEdge().getId();
            }
        }
    }


    /**
     * Empty interface for classification purpose in {@link MonitoringVehicleSimulation}
     */
    public interface MonitoringVehicle extends Vehicle {

    }

    private static class VehicleStamp {
        private int age;
        private long vehicleId;
        private int velocity;
        private int cellPosition;
        private long edgeId;
    }
}
