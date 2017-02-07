package microtrafficsim.core.simulation.scenarios.containers;

import microtrafficsim.core.logic.streets.DirectedEdge;
import microtrafficsim.core.logic.vehicles.VehicleStateListener;
import microtrafficsim.core.logic.vehicles.machines.Vehicle;

import java.util.Collection;


/**
 * <p>
 * This interface serves methods for handling vehicles in a general way without knowing the container's implementation
 * details. Thus you can easily implement a new vehicle container (e.g. a concurrent one) and replace the old one by
 * simply changing its initialization call. All getter return unmodifiable collections.
 *
 * <p>
 * This interface does not need a remove-method, because it extends {@link VehicleStateListener}, which removes them if
 * needed.
 *
 * <p>
 * All getters for intern attributes should return an unmodifiable collection to guarantee only this class makes deep
 * changes.
 *
 * @author Dominic Parga Cacheiro
 */
public interface VehicleContainer extends VehicleStateListener, Iterable<Vehicle> {

    /**
     * Adds the vehicle as unspawned.
     *
     * @param vehicle Vehicle that has been added to the graph successfully.
     */
    void addVehicle(Vehicle vehicle);

    /**
     * Clears this container, so after this call it is empty.
     */
    void clearAll();

    /**
     * @return The amount of all contained vehicles
     */
    int getVehicleCount();

    /**
     * @return The amount of all spawned vehicles
     */
    int getSpawnedCount();

    /**
     * @return The amount of all not spawned vehicles
     */
    int getNotSpawnedCount();

    /**
     * @return All vehicles in a collection. The returned collection should be unmodifiable to guarantee
     * only this class makes deep changes.
     */
    Collection<Vehicle> getVehicles();

    /**
     * Returns the list of spawned (i.e. visible and ready to drive) vehicles.
     * Note: The state of the vehicles contained in this list may change due to
     * the asynchronous nature of the simulation, this means the associated
     * {@link DirectedEdge DirectedEdge} is {@code null}
     * if the vehicle has de-spawned.
     *
     * @return All spawned vehicles in a collection. The returned collection should be unmodifiable to guarantee
     * only this class makes deep changes.
     */
    Collection<Vehicle> getSpawnedVehicles();

    /**
     * @return All not spawned vehicles in a collection. The returned collection should be unmodifiable to guarantee
     * only this class makes deep changes.
     */
    Collection<Vehicle> getNotSpawnedVehicles();

    // probably unused :(
    //	/**
    //	 * Returns the index of the list of the greatest size less than or equal to
    //	 * the given size, or -1 if there is no such element.
    //	 *
    //	 * @param size
    //	 *            The size of the returning list
    //	 * @return index of the list of the greatest size less than or equal to the
    //	 *         given size, or -1 if there is no such element.
    //	 */
    //	private int floor(int size, ArrayList<? extends Set<? extends Object>> list) {
    //
    //		int mid;
    //		int bottom = 0;
    //		int top = list.size() - 1;
    //		int floor = -1;
    //		// 0 1 2 3 4 5 6
    //		// x x x x x x x; size = 3
    //
    //		while (bottom <= top) {
    //			mid = (top + bottom) / 2;
    //			if (size > list.get(mid).size()) {
    //				bottom = mid + 1;
    //			} else if (size < list.get(mid).size()) {
    //				top = mid - 1;
    //			} else {
    //				bottom = mid + 1;
    //				floor = mid; //
    //			}
    //		}
    //
    //		return floor;
    //	}
}