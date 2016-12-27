package microtrafficsim.core.simulation.core.stepexecutors.impl;

import microtrafficsim.core.logic.Node;
import microtrafficsim.core.logic.vehicles.AbstractVehicle;
import microtrafficsim.core.simulation.core.stepexecutors.VehicleStepExecutor;
import microtrafficsim.core.simulation.scenarios.Scenario;
import microtrafficsim.exceptions.core.logic.NagelSchreckenbergException;
import microtrafficsim.utils.concurrency.delegation.DynamicThreadDelegator;
import microtrafficsim.utils.concurrency.delegation.StaticThreadDelegator;
import microtrafficsim.utils.concurrency.delegation.ThreadDelegator;

import java.util.concurrent.ExecutorService;


/**
 * A multi-threaded implementation of {@link VehicleStepExecutor} using a thread pool of {@link ExecutorService}.
 *
 * @author Dominic Parga Cacheiro
 */
public class MultiThreadedVehicleStepExecutor implements VehicleStepExecutor {

    // multithreading
    private final ThreadDelegator delegator;

    public MultiThreadedVehicleStepExecutor(int nThreads) {
        this(new StaticThreadDelegator(nThreads));
    }

    public MultiThreadedVehicleStepExecutor(ThreadDelegator delegator) {
        this.delegator = delegator;
    }

    @Override
    public void willMoveAll(final Scenario scenario) {
        delegator.doTask(
                (AbstractVehicle v) -> {
                    v.accelerate();
                    v.dash();
                    try {
                        v.brake();
                    } catch (NagelSchreckenbergException e) {
                        e.printStackTrace();
                    }
                },
                scenario.getVehicleContainer().getSpawnedVehicles().iterator(),
                scenario.getConfig().multiThreading.vehiclesPerRunnable
        );
    }

    @Override
    public void moveAll(final Scenario scenario) {
        delegator.doTask(AbstractVehicle::move,
                scenario.getVehicleContainer().getSpawnedVehicles().iterator(),
                scenario.getConfig().multiThreading.vehiclesPerRunnable
        );
    }

    @Override
    public void didMoveAll(final Scenario scenario) {
        delegator.doTask(AbstractVehicle::didMove,
                scenario.getVehicleContainer().getSpawnedVehicles().iterator(),
                scenario.getConfig().multiThreading.vehiclesPerRunnable
        );
    }

    @Override
    public void spawnAll(final Scenario scenario) {
        delegator.doTask(AbstractVehicle::spawn,
                scenario.getVehicleContainer().getNotSpawnedVehicles().iterator(),
                scenario.getConfig().multiThreading.vehiclesPerRunnable
        );
    }

    @Override
    public void updateNodes(final Scenario scenario) {
        delegator.doTask(
                Node::update,
                scenario.getGraph().getNodes().iterator(),
                scenario.getConfig().multiThreading.nodesPerThread);
    }
}
