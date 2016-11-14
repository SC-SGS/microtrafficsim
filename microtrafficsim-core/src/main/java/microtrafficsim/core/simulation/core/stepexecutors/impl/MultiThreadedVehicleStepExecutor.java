package microtrafficsim.core.simulation.core.stepexecutors.impl;

import microtrafficsim.core.logic.Node;
import microtrafficsim.core.logic.vehicles.AbstractVehicle;
import microtrafficsim.core.simulation.configs.MultiThreadingConfig;
import microtrafficsim.core.simulation.configs.SimulationConfig;
import microtrafficsim.core.simulation.core.Simulation;
import microtrafficsim.core.simulation.core.stepexecutors.VehicleStepExecutor;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;


/**
 * A multi-threaded implementation of {@link VehicleStepExecutor} using a thread pool of {@link ExecutorService}.
 *
 * @author Dominic Parga Cacheiro
 */
public class MultiThreadedVehicleStepExecutor implements VehicleStepExecutor {

    private final Simulation simulation;
    // multithreading
    private final ExecutorService pool;

    public MultiThreadedVehicleStepExecutor(Simulation simulation) {

        this.simulation = simulation;
        pool            = Executors.newFixedThreadPool(simulation.getScenario().getConfig().multiThreading.nThreads);
    }

    @Override
    public void willMoveAll(Iterator<AbstractVehicle> iteratorSpawned) {
        doVehicleTask((AbstractVehicle v) -> {
            v.accelerate();
            v.dash();
            v.brake();
            v.dawdle();
        }, iteratorSpawned);
    }

    @Override
    public void moveAll(Iterator<AbstractVehicle> iteratorSpawned) {
        doVehicleTask(AbstractVehicle::move, iteratorSpawned);
    }

    @Override
    public void didMoveAll(Iterator<AbstractVehicle> iteratorSpawned) {
        doVehicleTask(AbstractVehicle::didMove, iteratorSpawned);
    }

    @Override
    public void spawnAll(Iterator<AbstractVehicle> iteratorNotSpawned) {
        doVehicleTask(AbstractVehicle::spawn, iteratorNotSpawned);
    }

    @Override
    public void updateNodes(final Iterator<Node> iter) {

        SimulationConfig config = simulation.getScenario().getConfig();

        ArrayList<Callable<Object>> tasks = new ArrayList<>(config.multiThreading.nThreads);

        // add this task for every thread
        for (int c = 0; c < config.multiThreading.nThreads; c++)
            tasks.add(Executors.callable(() -> {
                int nodesPerThread = config.multiThreading.nodesPerThread;
                Node[] nodes       = new Node[nodesPerThread];
                int nodeCount;
                do {
                    nodeCount = 0;
                    synchronized (iter) {
                        while (nodeCount < nodesPerThread && iter.hasNext()) {
                            nodes[nodeCount++] = iter.next();
                        }
                    }
                    for (int i = 0; i < nodeCount; i++)
                        nodes[i].update();
                } while (nodeCount > 0);
            }));

        // waiting for finishing the threads
        try {
            pool.invokeAll(tasks);
        } catch (InterruptedException e) { e.printStackTrace(); }
    }

    private void doVehicleTask(Consumer<AbstractVehicle> task, Iterator<AbstractVehicle> iter) {

        MultiThreadingConfig config = simulation.getScenario().getConfig().multiThreading;

        LinkedList<Callable<Object>> tasks = new LinkedList<>();

        while (iter.hasNext()) {
            // fill current thread's vehicle list
            ArrayList<AbstractVehicle> list = new ArrayList<>(config.vehiclesPerRunnable);
            int                        c    = 0;
            while (c++ < config.vehiclesPerRunnable && iter.hasNext()) {
                list.add(iter.next());
            }
            // let a thread work off the list
            tasks.add(Executors.callable(() -> list.forEach(task)));
        }

        // waiting for finishing the threads
        try {
            pool.invokeAll(tasks);
        } catch (InterruptedException e) { e.printStackTrace(); }
    }
}
