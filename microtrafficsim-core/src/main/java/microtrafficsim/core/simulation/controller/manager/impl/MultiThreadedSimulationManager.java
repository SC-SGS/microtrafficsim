package microtrafficsim.core.simulation.controller.manager.impl;

import microtrafficsim.core.logic.Node;
import microtrafficsim.core.logic.vehicles.AbstractVehicle;
import microtrafficsim.core.simulation.controller.configs.SimulationConfig;
import microtrafficsim.core.simulation.controller.manager.SimulationManager;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.concurrent.*;
import java.util.function.Consumer;

/**
 * @author Dominic Parga Cacheiro
 */
public class MultiThreadedSimulationManager implements SimulationManager {

    private final SimulationConfig config;
    // multithreading
    private final ExecutorService pool;

    public MultiThreadedSimulationManager(SimulationConfig config) {

        this.config = config;
        pool = Executors.newFixedThreadPool(config.multiThreading.nThreads);
    }

    @Override
    public void willMoveAll(Iterator<AbstractVehicle> iteratorSpawned) {
        doVehicleTask((AbstractVehicle v) -> {
            v.accelerate();
            v.dash();
            v.brake();
            v.dawdle();
        } , iteratorSpawned);
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
        ArrayList<Callable<Object>> todo = new ArrayList<>(config.multiThreading.nThreads);

        for (int c = 0; c < config.multiThreading.nThreads; c++)
             todo.add(Executors.callable(() -> {
                Node[] nodes = new Node[config.multiThreading.nodesPerThread];
                int nodeCount;
                do {
                    nodeCount = 0;
                    synchronized (iter) {
                        while (nodeCount < config.multiThreading.nodesPerThread && iter.hasNext()) {
                            nodes[nodeCount++] = iter.next();
                        }
                    }
                    for (int i = 0; i < nodeCount; i++)
                        nodes[i].update();
                } while (nodeCount > 0);
            }));

        // waiting for finishing the threads
        try {
            pool.invokeAll(todo);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void doVehicleTask(Consumer<AbstractVehicle> task, Iterator<AbstractVehicle> iter) {
        LinkedList<Callable<Object>> todo = new LinkedList<>();

        while (iter.hasNext()) {
            // fill current thread's vehicle list
            ArrayList<AbstractVehicle> list = new ArrayList<>(config.multiThreading.vehiclesPerRunnable);
            int c = 0;
            while (c++ < config.multiThreading.vehiclesPerRunnable && iter.hasNext()) {
                list.add(iter.next());
            }
            // let a thread work off the list
            todo.add(Executors.callable(() -> list.forEach(task)));
        }

        // waiting for finishing the threads
        try {
            pool.invokeAll(todo);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
