package microtrafficsim.core.simulation.manager;

import microtrafficsim.core.entities.vehicle.VisualizationVehicleEntity;
import microtrafficsim.core.logic.vehicles.AbstractVehicle;
import microtrafficsim.core.logic.vehicles.VehicleStateListener;

import java.util.Iterator;
import java.util.Set;
import java.util.function.Supplier;


/**
 * @author Dominic Parga Cacheiro
 */
public interface VehicleManager extends VehicleStateListener {

    Supplier<VisualizationVehicleEntity> getVehicleFactory();
    void                            unlockVehicleFactory();

    void addVehicle(AbstractVehicle vehicle);

    int getVehicleCount();
    int getSpawnedCount();

    /*
    |====================|
    | iterating vehicles |
    |====================|
    */
    Iterator<AbstractVehicle> iteratorSpawned();
    Iterator<AbstractVehicle> iteratorNotSpawned();

    /*
    |====================|
    | datastructure tasks|
    |====================|
    */
    void clearAll();

    Set<AbstractVehicle> getVehicles();
    Set<AbstractVehicle> getSpawnedVehicles();

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