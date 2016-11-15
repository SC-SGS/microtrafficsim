package microtrafficsim.core.simulation.core.stepexecutors;

import microtrafficsim.core.logic.Node;
import microtrafficsim.core.logic.vehicles.AbstractVehicle;
import microtrafficsim.core.simulation.scenarios.Scenario;

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
     * @param scenario The scenario holding an iterator over all spawned vehicles getting prepared for moving.
     */
    void willMoveAll(final Scenario scenario);

    /**
     * This method moves all spawned vehicles.
     *
     * @param iteratorSpawned An iterator over all spawned vehicles getting moved.
     */
    void moveAll(final Scenario scenario);

    /**
     * After moving all spawned vehicles, some calculations has to be done, e.g. registration at crossroads.
     *
     * @param iteratorSpawned An iterator over all spawned vehicles getting told that they moved.
     */
    void didMoveAll(final Scenario scenario);

    /**
     * After executing tasks for spawned vehicles, there is space for not spawned ones => spawn them.
     *
     * @param iteratorNotSpawned An iterator over all not spawned vehicles getting told that they are allowed to spawn.
     */
    void spawnAll(final Scenario scenario);

    /**
     * After executing all vehicle tasks, the nodes has to update their priority lists and other logical stuff (e.g.
     * traffic lights if implemented).
     *
     * @param iter An iterator over all nodes.
     */
    void updateNodes(final Scenario scenario);
}