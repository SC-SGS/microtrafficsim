package microtrafficsim.core.simulation.core;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import microtrafficsim.core.logic.streets.DirectedEdge;
import microtrafficsim.core.logic.vehicles.machines.MonitoredVehicle;
import microtrafficsim.core.logic.vehicles.machines.Vehicle;
import microtrafficsim.core.simulation.scenarios.Scenario;
import microtrafficsim.utils.Resettable;

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
                stamp.spawnedVehicleCount = getScenario().getVehicleContainer().getSpawnedCount();

                stamp.velocity = vehicle.getVelocity();
                if (vehicle.getLane() != null) {
                    stamp.edgeStamp = new EdgeStamp();
                    stamp.edgeStamp.setup(vehicle.getLane().getEdge());
                }

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
                    return type.legend;
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
        private int spawnedVehicleCount;

        private int velocity;

        private EdgeStamp edgeStamp;
    }

    private static class EdgeStamp {
        private long edgeId;
        private int edgeVehicleCount;
        private int edgeLaneCount;
        private int edgeLength;

        public void setup(DirectedEdge edge) {
            edgeId = edge.getId();
            edgeVehicleCount = edge.getVehicleCount();
            edgeLaneCount = edge.getNumberOfLanes();
            edgeLength = edge.getLength();
        }
    }

    public enum CSVType {
        VELOCITY("velocity", "simStep", "cellsPerSecond"),
        SPAWNED_VEHICLE_COUNT("spawnedVehicleCount", "simStep", "vehicleCount"),
        EDGE_INFO("edgeInfo", "simStep", "edgeId", "lengthInCells", "laneCount"),
        EDGE_VEHICLE_COUNT("edgeVehicleCount", "simStep", "edgeId", "vehicleCount");

        private static final String SEPARATOR = " ";
        private static final String LINE_SEPARATOR = System.lineSeparator();

        private String filename;
        private String legend;

        CSVType(String filename, String... legend) {
            this.filename = filename;

            if (legend.length > 0) {
                this.legend = legend[0];

                for (int i = 1; i < legend.length; i++) {
                    this.legend += SEPARATOR + legend[i];
                }
            }
        }

        public String getFilename() {
            return filename + ".csv";
        }

        public boolean hasValidInfo(VehicleStamp stamp) {
            switch(this) {
                case VELOCITY:
                case SPAWNED_VEHICLE_COUNT:
                    return true;
                case EDGE_INFO:
                case EDGE_VEHICLE_COUNT:
                    return stamp.edgeStamp != null;
            }

            assert false : "Should not reach this point.";
            return false;
        }

        public String getInfo(VehicleStamp stamp) {
            StringBuilder builder = new StringBuilder();

            switch (this) {
                case VELOCITY:
                    builder.append(stamp.simStep).append(SEPARATOR)
                           .append(stamp.velocity);
                    break;
                case SPAWNED_VEHICLE_COUNT:
                    builder.append(stamp.simStep).append(SEPARATOR)
                           .append(stamp.spawnedVehicleCount);
                    break;
                case EDGE_INFO:
                    builder.append(stamp.simStep).append(SEPARATOR)
                           .append(stamp.edgeStamp.edgeId).append(SEPARATOR)
                           .append(stamp.edgeStamp.edgeLength).append(SEPARATOR)
                           .append(stamp.edgeStamp.edgeLaneCount);
                    break;
                case EDGE_VEHICLE_COUNT:
                    builder.append(stamp.simStep).append(SEPARATOR)
                           .append(stamp.edgeStamp.edgeId).append(SEPARATOR)
                           .append(stamp.edgeStamp.edgeVehicleCount);
                    break;
            }

            return builder.toString();
        }
    }
}
