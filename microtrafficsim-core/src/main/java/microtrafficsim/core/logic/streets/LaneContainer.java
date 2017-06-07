package microtrafficsim.core.logic.streets;

import microtrafficsim.core.logic.vehicles.machines.Vehicle;

public interface LaneContainer {
    void lockLane(int laneNo);

    void unlockLane(int laneNo);

    boolean isEmpty(int laneNo);

    /**
     * @return The first vehicle in the lane. 'First' means the vehicle being on the street for the longest time
     * <=> the vehicle on the lane that entered it at first.
     */
    Vehicle getFirstVehicle(int laneNo);

    /**
     * @return The last vehicle in the lane. 'Last' means the vehicle being on the street for the shortest time
     * <=> the vehicle on the lane that entered it at last.
     */
    Vehicle getLastVehicle(int laneNo);

    Vehicle getPrevOf(int laneNo, int cellNo);

    Vehicle getNextOf(int laneNo, int cellNo);

    /**
     * @return true if an element was removed
     */
    Vehicle set(Vehicle vehicle, int laneNo, int cellNo);

    Vehicle remove(int laneNo, int cellNo);

    void clear();
}