package microtrafficsim.core.simulation.stepexecutors;

import microtrafficsim.core.logic.Node;
import microtrafficsim.core.logic.vehicles.AbstractVehicle;

import java.util.Iterator;


/**
 * This interface serves methods for executing one simulation step of vehicles etc. (e.g. nodes).
 *
 * @author Dominic Parga Cacheiro
 */
public interface VehicleStepExecutor {

    /*
    |==========================|
    | vehicle simulation steps |
    |==========================|
     */
    /**
     * This method prepares the moving step, e.g. accelerating.
     *
     * @param timeDeltaMillis Can be used to simulate in real time.
     * @param iteratorSpawned An iterator over all spawned vehicles getting prepared for moving.
     */
    void willMoveAll(final long timeDeltaMillis, final Iterator<AbstractVehicle> iteratorSpawned);

    /**
     * This method moves all spawned vehicles.
     *
     * @param iteratorSpawned An iterator over all spawned vehicles getting moved.
     */
    void moveAll(final Iterator<AbstractVehicle> iteratorSpawned);

    /**
     * After moving all spawned vehicles, some calculations has to be done, e.g. registration at crossroads.
     *
     * @param iteratorSpawned An iterator over all spawned vehicles getting told that they moved.
     */
    void didMoveAll(final Iterator<AbstractVehicle> iteratorSpawned);

    /**
     * After executing tasks for spawned vehicles, there is space for not spawned ones => spawn them.
     *
     * @param iteratorNotSpawned An iterator over all not spawned vehicles getting told that they are allowed to spawn.
     */
    void spawnAll(final Iterator<AbstractVehicle> iteratorNotSpawned);

    /**
     * After executing all vehicle tasks, the nodes has to update their priority lists and other logical stuff (e.g.
     * traffic lights if implemented).
     *
     * @param iter An iterator over all nodes.
     */
    void updateNodes(final Iterator<Node> iter);
}