package microtrafficsim.core.logic.streets;

import microtrafficsim.core.logic.vehicles.machines.Vehicle;

import java.util.ArrayList;
import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class NavigableMapLaneContainer implements LaneContainer {
    private ArrayList<NavigableMap<Integer, Vehicle>> lanes;
    private final Lock[] lock;


    public NavigableMapLaneContainer(int nLanes) {
        lanes = new ArrayList<>(nLanes);
        lock = new ReentrantLock[nLanes];

        for (int i = 0; i < nLanes; i++) {
            lanes.add(new TreeMap<>());
            lock[i] = new ReentrantLock(true);
        }
    }


    @Override
    public void lockLane(int laneNo) {
        lock[laneNo].lock();
    }

    @Override
    public void unlockLane(int laneNo) {
        lock[laneNo].unlock();
    }

    @Override
    public boolean isEmpty(int laneNo) {
        return lanes.get(laneNo).isEmpty();
    }

    @Override
    public Vehicle get(int laneNo, int cellNo) {
        return lanes.get(laneNo).get(cellNo);
    }

    @Override
    public Vehicle getFirstVehicle(int laneNo) {
        return lanes.get(laneNo).lastEntry().getValue();
    }

    @Override
    public Vehicle getLastVehicle(int laneNo) {
        return lanes.get(laneNo).firstEntry().getValue();
    }

    @Override
    public Vehicle getPrevOf(int laneNo, int cellNo) {
        Map.Entry<Integer, Vehicle> entry = lanes.get(laneNo).floorEntry(cellNo - 1);
        return entry != null ? entry.getValue() : null;
    }

    @Override
    public Vehicle getNextOf(int laneNo, int cellNo) {
        Map.Entry<Integer, Vehicle> entry = lanes.get(laneNo).ceilingEntry(cellNo + 1);
        return entry != null ? entry.getValue() : null;
    }

    @Override
    public Vehicle set(Vehicle vehicle, int laneNo, int cellNo) {
        return lanes.get(laneNo).put(cellNo, vehicle);
    }

    @Override
    public Vehicle remove(int laneNo, int cellNo) {
        return lanes.get(laneNo).remove(cellNo);
    }

    @Override
    public void clear() {
        for (int i = 0; i < lanes.size(); i++)
            lanes.get(i).clear();
    }
}