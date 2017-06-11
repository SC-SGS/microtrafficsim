package microtrafficsim.core.logic.streets;

import microtrafficsim.core.logic.vehicles.machines.Vehicle;

import java.util.ArrayList;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class ArrayListLaneContainer implements LaneContainer {
    private ArrayList<ArrayList<Cell>> lanes;
    private final Lock[] lock;


    public ArrayListLaneContainer(int nLanes) {
        lanes = new ArrayList<>(nLanes);
        lock = new ReentrantLock[nLanes];

        for (int i = 0; i < nLanes; i++) {
            lanes.add(new ArrayList<>());
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
        ArrayList<Cell> lane = lanes.get(laneNo);
        int index = indexOf(lane, cellNo);
        if (index < 0)
            return null;
        return lane.get(index).vehicle;
    }

    @Override
    public Vehicle getFirstVehicle(int laneNo) {
        ArrayList<Cell> lane = lanes.get(laneNo);
        return lane.get(lane.size() - 1).vehicle;
    }

    @Override
    public Vehicle getLastVehicle(int laneNo) {
        return lanes.get(laneNo).get(0).vehicle;
    }

    @Override
    public Vehicle getPrevOf(int laneNo, int cellNo) {
        Vehicle prev = null;

        ArrayList<Cell> lane = lanes.get(laneNo);
        if (!isEmpty(laneNo)) {
            int index = indexOfSupremum(lane, cellNo);
            index--;
            if (0 <= index)
                prev = lane.get(index).vehicle;
        }

        return prev;
    }

    @Override
    public Vehicle getNextOf(int laneNo, int cellNo) {
        Vehicle next = null;

        if (!isEmpty(laneNo)) {
            ArrayList<Cell> lane = lanes.get(laneNo);
            int index = indexOfInfimum(lane, cellNo);
            index++;
            if (index < lane.size())
                next = lane.get(index).vehicle;
        }

        return next;
    }

    @Override
    public Vehicle set(Vehicle vehicle, int laneNo, int cellNo) {
        Vehicle removed = null;

        ArrayList<Cell> lane = lanes.get(laneNo);
        int index = indexOfInfimum(lane, cellNo);

        if (0 <= index && index < lane.size())
            if (lane.get(index).number == cellNo)
                removed = lane.set(index, new Cell(vehicle, cellNo)).vehicle;
        if (removed == null) {
            lane.add(index + 1, new Cell(vehicle, cellNo));
        }

        return removed;
    }

    @Override
    public Vehicle remove(int laneNo, int cellNo) {
        Vehicle removed = null;

        ArrayList<Cell> lane = lanes.get(laneNo);
        int listIndex = indexOf(lane, cellNo);
        if (listIndex >= 0)
            removed = lane.remove(listIndex).vehicle;

        return removed;
    }

    @Override
    public void clear() {
        for (int i = 0; i < lanes.size(); i++)
            lanes.get(i).clear();
    }


    /*
    |=======|
    | utils |
    |=======|
    */
    public int indexOf(ArrayList<Cell> lane, int cellNo) {
        int index = indexOfInfimum(lane, cellNo);

        if (index < 0 || index >= lane.size())
            return -1;
        return lane.get(index).number == cellNo ? index : -1;
    }

    public int indexOfInfimum(ArrayList<Cell> lane, int cellNo) {
        if (lane.isEmpty())
            return -1;

        int low = 0;
        int high = lane.size() - 1;

        while (low <= high) {
            int mid = (low + high) >>> 1;
            int cmp = lane.get(mid).number - cellNo;

            if (cmp < 0)
                low = mid + 1;
            else if (cmp > 0)
                high = mid - 1;
            else
                return mid;
        }

        return low - 1;
    }

    public int indexOfSupremum(ArrayList<Cell> lane, int cellNo) {
        if (lane.isEmpty())
            return -1;

        int low = 0;
        int high = lane.size() - 1;

        while (low <= high) {
            int mid = (low + high) >>> 1;
            int cmp = lane.get(mid).number - cellNo;

            if (cmp < 0)
                low = mid + 1;
            else if (cmp > 0)
                high = mid - 1;
            else
                return mid;
        }

        return high + 1;
    }


    private class Cell implements Comparable<Cell> {
        private Vehicle vehicle;
        private int number;

        public Cell(int number) {
            this(null, number);
        }

        public Cell(Vehicle vehicle, int number) {
            this.vehicle = vehicle;
            this.number = number;
        }


        @Override
        public int compareTo(Cell o) {
            return Integer.compare(number, o.number);
        }
    }
}