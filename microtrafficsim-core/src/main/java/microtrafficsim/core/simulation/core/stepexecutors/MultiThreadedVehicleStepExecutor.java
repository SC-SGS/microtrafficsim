package microtrafficsim.core.simulation.core.stepexecutors;

import microtrafficsim.core.logic.nodes.Node;
import microtrafficsim.core.logic.vehicles.machines.Vehicle;
import microtrafficsim.core.simulation.scenarios.Scenario;
import microtrafficsim.utils.concurrency.delegation.StaticThreadDelegator;
import microtrafficsim.utils.concurrency.delegation.ThreadDelegator;

import java.util.concurrent.ExecutorService;


/**
 * A multi-threaded implementation of {@link VehicleStepExecutor} using a thread pool of {@link ExecutorService}.
 *
 * @author Dominic Parga Cacheiro
 */
public class MultiThreadedVehicleStepExecutor implements VehicleStepExecutor {
    private final ThreadDelegator delegator;

    public MultiThreadedVehicleStepExecutor(int nThreads) {
        this(new StaticThreadDelegator(nThreads));
    }

    public MultiThreadedVehicleStepExecutor(ThreadDelegator delegator) {
        this.delegator = delegator;
    }

    @Override
    public void accelerateAll(Scenario scenario) {
        try {
            delegator.doTask(
                    Vehicle::accelerate,
                    scenario.getVehicleContainer().getSpawnedVehicles().iterator(),
                    scenario.getConfig().multiThreading.vehiclesPerRunnable
            );
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void willChangeLaneAll(Scenario scenario) {
        try {
            delegator.doTask(
                    Vehicle::willChangeLane,
                    scenario.getVehicleContainer().getSpawnedVehicles().iterator(),
                    scenario.getConfig().multiThreading.vehiclesPerRunnable
            );
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void changeLaneAll(Scenario scenario) {
        try {
            delegator.doTask(
                    Vehicle::changeLane,
                    scenario.getVehicleContainer().getSpawnedVehicles().iterator(),
                    scenario.getConfig().multiThreading.vehiclesPerRunnable
            );
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void brakeAll(final Scenario scenario) {
        try {
            delegator.doTask(
                    (vehicle) -> {
                        vehicle.brake();
                        vehicle.dawdle();
                    },
                    scenario.getVehicleContainer().getSpawnedVehicles().iterator(),
                    scenario.getConfig().multiThreading.vehiclesPerRunnable
            );
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void moveAll(final Scenario scenario) {
        try {
            delegator.doTask(Vehicle::move,
                    scenario.getVehicleContainer().getSpawnedVehicles().iterator(),
                    scenario.getConfig().multiThreading.vehiclesPerRunnable
            );
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void didMoveAll(final Scenario scenario) {
        try {
            delegator.doTask(Vehicle::didMove,
                    scenario.getVehicleContainer().getSpawnedVehicles().iterator(),
                    scenario.getConfig().multiThreading.vehiclesPerRunnable
            );
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void spawnAll(final Scenario scenario) {
        try {
            delegator.doTask(Vehicle::spawn,
                    scenario.getVehicleContainer().getNotSpawnedVehicles().iterator(),
                    scenario.getConfig().multiThreading.vehiclesPerRunnable
            );
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void updateNodes(final Scenario scenario) {
        try {
            delegator.doTask(
                    Node::update,
                    scenario.getGraph().getNodes().iterator(),
                    scenario.getConfig().multiThreading.nodesPerThread);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
