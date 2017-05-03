package microtrafficsim.core.logic.vehicles.driver;

import microtrafficsim.core.logic.Route;
import microtrafficsim.core.logic.nodes.Node;
import microtrafficsim.core.logic.streets.DirectedEdge;
import microtrafficsim.core.logic.vehicles.machines.Vehicle;
import microtrafficsim.utils.emotions.Hulk;
import microtrafficsim.utils.Resettable;

/**
 * <p>
 * The idea of this interface is separating a real vehicle's behaviour into mechanical ({@link Vehicle}) and
 * non-mechanical ({@link Driver}). This offers the opportunity of defining a driver's behaviours independently of the
 * vehicle, that the driver is currently driving.
 *
 * <p>
 * Mechanical characteristics are physical facts/conditions, e.g. the vehicle's current velocity, the vehicle's max
 * velocity, the vehicle's acceleration etc.
 *
 * <p>
 * Non-mechanical characteristics are defined as the behaviour of humans/the driver's character, e.g. the dashing,
 * the wished max velocity (could be different to the physical max velocity of the vehicle), the wish to drive faster
 * than allowed etc. For a bit more emotionally behaviour, this interface extends {@link Hulk} for describing anger.
 * Default implementation is empty/returns 0.
 *
 * @author Dominic Parga Cacheiro
 */
public interface Driver extends Hulk, Resettable {

    /*
    |==========|
    | (i) Hulk |
    |==========|
    */
    /**
     * Empty per default
     */
    @Override
    default void becomeMoreAngry() {

    }

    /**
     * Empty per default
     */
    @Override
    default void calmDown() {

    }

    /**
     * @return 0 per default
     */
    @Override
    default int getAnger() {
        return 0;
    }

    /**
     * @return 0 per default
     */
    @Override
    default int getTotalAnger() {
        return 0;
    }

    /**
     * @return 0 per default
     */
    @Override
    default int getMaxAnger() {
        return 0;
    }

    /*
    |========================|
    | non-mechanic behaviour |
    |========================|
    */
    /**
     * @param tmpV temporary velocity
     * @return preferred velocity after accelerating
     */
    int accelerate(int tmpV);

    /**
     *
     * @param tmpV temporary velocity
     * @return preferred velocity after dawdling
     */
    int dawdle(int tmpV);

    /**
     * @return The velocity this driver likes to drive at fastest.
     */
    int getMaxVelocity();

    /*
    |======================|
    | variable information |
    |======================|
    */
    void setVehicle(Vehicle vehicle);

    Route<Node> getRoute();

    void setRoute(Route<Node> route);

    default DirectedEdge peekRoute() {
        return (DirectedEdge) getRoute().peek();
    }

    default DirectedEdge popRoute() {
        return (DirectedEdge) getRoute().pop();
    }

    /*
    |=====================|
    | dynamic information |
    |=====================|
    */
    /**
     * @return the number of steps this driver is already driving.
     */
    int getTravellingTime();

    void incTravellingTime();

    int getPriorityCounter();

    void resetPriorityCounter();

    void incPriorityCounter();

    void decPriorityCounter();

    /*
    |=================|
    | fix information |
    |=================|
    */
}
