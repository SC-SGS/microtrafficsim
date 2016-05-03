package microtrafficsim.core.simulation.controller.manager;

import microtrafficsim.core.logic.Node;
import microtrafficsim.core.logic.vehicles.AbstractVehicle;

import java.util.Iterator;

/**
 * @author Dominic Parga Cacheiro
 */
public interface SimulationManager {

    /*
    |==========================|
    | vehicle simulation steps |
    |==========================|
     */
    void willMoveAll(final Iterator<AbstractVehicle> spawnedVehicle);
    void moveAll(final Iterator<AbstractVehicle> iteratorSpawned);
    void didMoveAll(final Iterator<AbstractVehicle> iteratorSpawned);
    void spawnAll(final Iterator<AbstractVehicle> iteratorNotSpawned);
    void updateNodes(final Iterator<Node> iter);
}