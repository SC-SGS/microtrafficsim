package microtrafficsim.core.simulation.core;

import microtrafficsim.core.simulation.scenarios.Scenario;

import java.util.LinkedList;
import java.util.List;

/**
 * @author Dominic Parga Cacheiro
 */
public class MonitoringVehicleSimulation extends VehicleSimulation {
    private List<Long> stamps = new LinkedList<>();
    private final int maxCollectionSize = 100;


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
            if (stamps.size() < maxCollectionSize)
                stamps.add(System.nanoTime() - time);
            else if (stamps.size() == maxCollectionSize)
                System.out.println("Max collection size of " + maxCollectionSize + " reached.");
        }

        didRunOneStep();
    }

    public long getSampleMeanOfTime() {
        if (stamps.size() == 0)
            return -1;
        double dSize = stamps.size();

        double result = 0;
        for (long l : stamps) {
            result += l / dSize;
        }

        return (long) result;
    }

    public long getSampleVarianceOfTime() {
        if (stamps.size() == 0)
            return 0;
        double dSize = stamps.size() - 1;
        long sampleMean = getSampleMeanOfTime();

        double result = 0;
        for (long l : stamps) {
            result += Math.pow(l - sampleMean, 2) / dSize;
        }

        return (long) result;
    }

    public void clearStamps() {
        stamps.clear();
    }
}
