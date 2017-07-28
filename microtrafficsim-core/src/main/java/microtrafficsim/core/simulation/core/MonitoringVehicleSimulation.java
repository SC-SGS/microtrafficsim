package microtrafficsim.core.simulation.core;

import microtrafficsim.core.logic.vehicles.machines.MonitoredVehicle;
import microtrafficsim.core.logic.vehicles.machines.Vehicle;
import microtrafficsim.core.simulation.scenarios.Scenario;
import microtrafficsim.utils.Resettable;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * Monitors all vehicles implementing {@link MonitoredVehicle}
 *
 * @author Dominic Parga Cacheiro
 */
public class MonitoringVehicleSimulation extends VehicleSimulation implements Resettable {
    private List<VehicleStamp> vehicleStamps = new LinkedList<>();


    @Override
    protected void unsecureDoRunOneStep() {
        long time = System.nanoTime();
        willRunOneStep();

        Scenario scenario = getScenario();
        if (scenario.isPrepared()) {
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
        time = System.nanoTime() - time;
    }

    @Override
    public void didRunOneStep() {
        super.didRunOneStep();

        for (Vehicle vehicle : getScenario().getVehicleContainer()) {
            if (vehicle instanceof MonitoredVehicle) {
                VehicleStamp stamp = new VehicleStamp();
                stamp.simStep = getAge();
                stamp.vehicleId = vehicle.getId();
                stamp.travellingTime = vehicle.getDriver().getTravellingTime();
                stamp.velocity = vehicle.getVelocity();
                stamp.cellPosition = vehicle.getCellPosition();
                if (vehicle.getLane() != null)
                    stamp.edgeId = vehicle.getLane().getEdge().getId();
                else
                    stamp.edgeId = null;

                vehicleStamps.add(stamp);
            }
        }
    }

    public Iterator<String> getCSVIterator(CSVType type) {
        Iterator<VehicleStamp> iter = vehicleStamps.iterator();

        return new Iterator<String>() {
            boolean isFirst = true;

            @Override
            public boolean hasNext() {
                return iter.hasNext();
            }

            @Override
            public String next() {
                if (isFirst) {
                    isFirst = false;
                    return type.LEGEND;
                } else {
                    return CSVType.LINE_SEPARATOR + type.getInfo(iter.next());
                }
            }
        };
    }


    @Override
    public void reset() {
        vehicleStamps.clear();
    }


    private static class VehicleStamp {
        private int simStep;
        private long vehicleId;
        private int travellingTime;
        private int velocity;
        private int cellPosition;
        private Long edgeId;
    }

    public enum CSVType {
        TRAVELLING_TIME("travellingTime"),
        VELOCITY("velocity"),
        CELL_POSITION("cellposition"),
        EDGE_ID("edgeId");

        private static final String SEPARATOR = " ";
        private static final String LINE_SEPARATOR = System.lineSeparator();

        private final String FILENAME;
        private final String LEGEND;

        CSVType(String filename) {
            FILENAME = filename;
            LEGEND = "simStep" + SEPARATOR + "vehicleId" + SEPARATOR + filename;
        }

        public String getFilename() {
            return FILENAME + ".csv";
        }

        public String getInfo(VehicleStamp stamp) {
            StringBuilder builder = new StringBuilder();
            builder.append(stamp.simStep).append(SEPARATOR).append(stamp.vehicleId);

            switch (this) {
                case TRAVELLING_TIME:
                    builder.append(SEPARATOR).append(stamp.travellingTime);
                    break;
                case VELOCITY:
                    builder.append(SEPARATOR).append(stamp.velocity);
                    break;
                case CELL_POSITION:
                    builder.append(SEPARATOR).append(stamp.cellPosition);
                    break;
                case EDGE_ID:
                    builder.append(SEPARATOR).append(stamp.edgeId);
                    break;
            }

            return builder.toString();
        }
    }
}
