package microtrafficsim.core.logic.streets;

import microtrafficsim.core.entities.street.LogicStreetEntity;
import microtrafficsim.core.entities.street.StreetEntity;
import microtrafficsim.core.logic.nodes.Node;
import microtrafficsim.core.logic.streets.information.FullStreetInfo;
import microtrafficsim.core.logic.streets.information.Orientation;
import microtrafficsim.core.logic.streets.information.RawStreetInfo;
import microtrafficsim.core.logic.vehicles.machines.Vehicle;
import microtrafficsim.core.map.StreetType;
import microtrafficsim.core.shortestpath.ShortestPathEdge;
import microtrafficsim.core.simulation.configs.SimulationConfig;
import microtrafficsim.math.Vec2d;
import microtrafficsim.utils.Resettable;
import microtrafficsim.utils.collections.FastSortedArrayList;
import microtrafficsim.utils.collections.Tuple;
import microtrafficsim.utils.strings.builder.BasicStringBuilder;
import microtrafficsim.utils.strings.builder.LevelStringBuilder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;


/**
 * This class is a container for lanes from one node to another. Furthermore, it
 * stores attributes that are important for all lanes, e.g. number of cells, max
 * velocity (yes, max velocity belongs to one directed edge, not to one lane).
 *
 * @author Jan-Oliver Schmidt, Dominic Parga Cacheiro
 */
public class DirectedEdge
        implements ShortestPathEdge<Node>,
        LogicStreetEntity,
        Resettable,
        Iterable<DirectedEdge.Lane>,
        Comparable<DirectedEdge>
{
    private final FullStreetInfo streetInfo;
    private StreetEntity entity;
    private final LaneContainer lanes;


    /**
     * In addition to standard initialization, this constructor also calculates
     * the number of cells of this edge and adds this edge to the origin node's
     * leaving edges.
     *
     * For detailed parameter information
     * see
     * {@link RawStreetInfo#RawStreetInfo(long, double, Vec2d, Vec2d, Orientation, Node, Node, StreetType, int, float, float, SimulationConfig.StreetPriorityFunction)}
     */
    public DirectedEdge(long id,
                        double lengthInMeters,
                        Vec2d originDirection, Vec2d destinationDirection,
                        Orientation orientation,
                        Node origin, Node destination,
                        StreetType type,
                        int nLanes,
                        float maxVelocity,
                        float metersPerCell, SimulationConfig.StreetPriorityFunction priorityFn)
    {
        streetInfo = new FullStreetInfo(new RawStreetInfo(
                id,
                lengthInMeters,
                originDirection, destinationDirection,
                orientation,
                origin, destination,
                type,
                nLanes,
                maxVelocity,
                metersPerCell, priorityFn
        ));
        lanes = new LaneContainer(streetInfo.raw.nLanes);
    }


    /**
     * @return lane of index i counted from outside to inside, starting with 0
     */
    public Lane getLane(int laneNo) {
        return new Lane(this, laneNo);
    }



    public Key key() {
        return new Key(this);
    }

    @Override
    public String toString() {
        LevelStringBuilder stringBuilder = new LevelStringBuilder()
                .setDefaultLevelSeparator()
                .setDefaultLevelSubString();

        stringBuilder.appendln("<" + getClass().getSimpleName() + ">").incLevel(); {
            stringBuilder.appendln(key());
            stringBuilder.appendln("hash = " + hashCode());
            stringBuilder.appendln("(orig -len-> dest) = ("
                    + streetInfo.raw.origin.getId()
                    + " -" + streetInfo.numberOfCells + "-> "
                    + streetInfo.raw.destination.getId() + ")");
            stringBuilder.appendln();
            for (Lane lane : this)
                stringBuilder.appendln(lane);
        } stringBuilder.decLevel().append("</" + getClass().getSimpleName() + ">");

        return stringBuilder.toString();
    }

    /**
     * Resets the {@code streetInfo} and all lanes.
     */
    @Override
    public void reset() {
        streetInfo.reset();
        lanes.clear();
    }



    @Override
    public Iterator<Lane> iterator() {
        return new AscLaneIterator();
    }

    public Iterator<Lane> reverseIterator() {
        return new DescLaneIterator();
    }

    @Override
    public StreetEntity getEntity() {
        return entity;
    }

    @Override
    public void setEntity(StreetEntity entity) {
        this.entity = entity;
    }



    public int getNumberOfLanes() {
        return streetInfo.raw.nLanes;
    }

    /**
     * @return max allowed velocity in cells/s
     */
    public int getMaxVelocity() {
        return streetInfo.maxVelocity;
    }

    public byte getPriorityLevel() {
        return streetInfo.priorityLevel;
    }

    public Vec2d getOriginDirection() {
        return streetInfo.raw.originDirection;
    }

    public Vec2d getDestinationDirection() {
        return streetInfo.raw.destinationDirection;
    }

    @Override
    public long getId() {
        return streetInfo.raw.id;
    }

    public Orientation getOrientation() {
        return streetInfo.raw.orientation;
    }

    public StreetType getStreetType() {
        return streetInfo.raw.type;
    }

    @Override
    public int getLength() {
        return streetInfo.numberOfCells;
    }

    public double getLengthInMeter() {
        return streetInfo.raw.lengthInMeters;
    }

    @Override
    public double getTimeCostMillis() {
        // 1000.0 because velocity is in cells/s = cells/1000ms
        return (1000.0 * getLength()) / streetInfo.maxVelocity;
    }

    @Override
    public Node getOrigin() {
        return streetInfo.raw.origin;
    }

    @Override
    public Node getDestination() {
        return streetInfo.raw.destination;
    }




    /**
     * Just an immutable container used by vehicles as lane/edge reference.
     */
    public static class Lane implements Comparable<Lane> {
        private DirectedEdge edge;
        private int index;


        public Lane(DirectedEdge edge, int index) {
            this.edge = edge;
            this.index = index;
        }


        public DirectedEdge getEdge() {
            return edge;
        }

        public int getIndex() {
            return index;
        }

        public int getMaxVelocity() {
            return edge.getMaxVelocity();
        }


        public int getMaxInsertionIndex() {
            if (edge.lanes.isEmpty(index)) {
                return edge.getLength() - 1;
            } else {
                return edge.lanes.getLastVehicle(index).getCellPosition() - 1;
            }
        }

        public boolean hasVehicleInFront(Vehicle vehicle) {
            return edge.lanes.getNextOf(index, vehicle.getCellPosition()) != null;
        }

        public Vehicle getVehicleInFront(Vehicle vehicle) {
            return edge.lanes.getNextOf(index, vehicle.getCellPosition());
        }


        /**
         * @return true if an element was removed
         */
        public boolean insertVehicle(Vehicle vehicle, int cellPosition) {
            Vehicle removed = edge.lanes.set(vehicle, index, cellPosition);
            boolean success = removed == null;

            BasicStringBuilder builder = new BasicStringBuilder();
            builder.appendln("Inserting a vehicle to the lane removed a vehicle.")
                    .appendln("INSERTED = " + vehicle)
                    .appendln("REMOVED  = " + removed);
            assert success : builder.toString();

            return !success;
        }

        public void moveVehicle(Vehicle vehicle, int delta) {
            if (delta != 0) {
                removeVehicle(vehicle);
                insertVehicle(vehicle, vehicle.getCellPosition() + delta);
            }
        }

        /**
         * @return true if success (<=> no vehicle has been on the given position)
         */
        public boolean removeVehicle(Vehicle vehicle) {
            boolean success;

            Vehicle removedVehicle = edge.lanes.remove(index, vehicle.getCellPosition());
            success = removedVehicle == vehicle;

            BasicStringBuilder builder = new BasicStringBuilder();
            builder.appendln("Removing a vehicle from the lane removed an unexpected, different as vehicle.")
                    .appendln("EXPECTED = " + vehicle)
                    .appendln("ACTUALLY REMOVED  = " + removedVehicle);
            assert success : builder.toString();

            return success;
        }


        public Lane.Key key() {
            return new Lane.Key(this);
        }

        @Override
        public String toString() {
            LevelStringBuilder stringBuilder = new LevelStringBuilder();
            stringBuilder.appendln("<Lane>").incLevel(); {
                stringBuilder.appendln("edge id    = " + edge.getId());
                stringBuilder.appendln("edge hash  = " + edge.hashCode());
                stringBuilder.appendln("lane index = " + index);
            } stringBuilder.decLevel().appendln("</Lane>");

            return stringBuilder.toString();
        }


        @Override
        public int compareTo(Lane o) {
            return key().compareTo(o.key());
        }

        public static class Key implements Comparable<Lane.Key> {
            private DirectedEdge.Key edgeKey;
            private int index;

            private Key() {

            }

            private Key(Lane lane) {
                edgeKey = lane.edge.key();
                index = lane.index;
            }

            @Override
            public int compareTo(Lane.Key o) {
                int cmp = edgeKey.compareTo(o.edgeKey);
                if (cmp == 0)
                    cmp = Integer.compare(index, o.index);
                return cmp;
            }

            @Override
            public boolean equals(Object obj) {
                if (obj == this)
                    return true;

                if (!(obj instanceof Lane.Key))
                    return false;

                return compareTo((Lane.Key) obj) == 0;
            }
        }
    }

    public class AscLaneIterator implements Iterator<Lane> {
        int index = 0;

        @Override
        public boolean hasNext() {
            return index < getNumberOfLanes();
        }

        @Override
        public Lane next() {
            return new Lane(DirectedEdge.this, index++);
        }
    }

    public class DescLaneIterator implements Iterator<Lane> {
        int index = getNumberOfLanes() - 1;

        @Override
        public boolean hasNext() {
            return index >= 0;
        }

        @Override
        public Lane next() {
            return new Lane(DirectedEdge.this, index--);
        }
    }



    private static class LaneContainer {
        private ArrayList<FastSortedArrayList<Cell>> lanes;
        private final Lock lock;
        private final Condition[] laneIsFree;
        private final boolean[] laneIsBusy;


        private LaneContainer(int nLanes) {
            lanes = new ArrayList<>(nLanes);
            lock = new ReentrantLock(true);
            laneIsFree = new Condition[nLanes];
            laneIsBusy = new boolean[nLanes];

            for (int i = 0; i < nLanes; i++) {
                lanes.add(new FastSortedArrayList<>());
                laneIsFree[i] = lock.newCondition();
                laneIsBusy[i] = false;
            }
        }


        private synchronized boolean isEmpty(int laneNo) {
//            lock.lock();
//            try {
//                while (laneIsBusy[laneNo])
//                    laneIsFree[laneNo].await();
//
//                laneIsFree[laneNo].signal();
//            } finally {
//                lock.unlock();
//                return false;
//            }
            return lanes.get(laneNo).isEmpty();
        }

        /**
         * @return The first vehicle in the lane. 'First' means the vehicle being on the street for the longest time
         * <=> the vehicle on the lane that entered it at first.
         */
        private synchronized Vehicle getFirstVehicle(int laneNo) {
            ArrayList<Cell> lane = lanes.get(laneNo);
            return lane.get(lane.size() - 1).vehicle;
        }

        /**
         * @return The last vehicle in the lane. 'Last' means the vehicle being on the street for the shortest time
         * <=> the vehicle on the lane that entered it at last.
         */
        private synchronized Vehicle getLastVehicle(int laneNo) {
            return lanes.get(laneNo).get(0).vehicle;
        }

        private synchronized Vehicle getPrevOf(int laneNo, int cellNo) {
            if (isEmpty(laneNo))
                return null;

            Iterator<Cell> iterator = lanes.get(laneNo).iterator();
            Cell prev = iterator.next();
            while (iterator.hasNext()) {
                Cell next = iterator.next();

                if (next.number < cellNo)
                    prev = next;
                else
                    break;
            }

            if (prev.number == cellNo)
                return null;
            return prev.vehicle;
        }

        private synchronized Vehicle getNextOf(int laneNo, int cellNo) {
            if (isEmpty(laneNo))
                return null;

            Iterator<Cell> iterator = lanes.get(laneNo).descendingIterator();
            Cell next = iterator.next();
            while (iterator.hasNext()) {
                Cell prev = iterator.next();

                if (prev.number > cellNo)
                    next = prev;
                else
                    break;
            }

            if (next.number == cellNo)
                return null;
            return next.vehicle;
        }

        /**
         * @return true if an element was removed
         */
        private synchronized Vehicle set(Vehicle vehicle, int laneNo, int cellNo) {
            ArrayList<Cell> lane = lanes.get(laneNo);

            int index = lane.indexOf(new Cell(null, cellNo));
            Vehicle removed = null;
            if (index >= 0)
                removed = lane.remove(index).vehicle;

            lane.add(new Cell(vehicle, cellNo));
            return removed;
        }

        private synchronized Vehicle remove(int laneNo, int cellNo) {
            ArrayList<Cell> lane = lanes.get(laneNo);
            int listIndex = lane.indexOf(new Cell(cellNo));
            if (listIndex >= 0)
                return lane.remove(listIndex).vehicle;
            return null;
        }

        private synchronized void clear() {
            for (int i = 0; i < lanes.size(); i++) {
                lanes.get(i).clear();
            }
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



    @Override
    public int compareTo(DirectedEdge o) {
        return key().compareTo(o.key());
    }

    public static class Key implements Comparable<Key> {
        private long edgeId;
        private Orientation orientation;

        private Key() {

        }

        private Key(DirectedEdge edge) {
            edgeId = edge.streetInfo.raw.id;
            orientation = edge.streetInfo.raw.orientation;
        }

        @Override
        public String toString() {
            return "id = " + edgeId + " (" + orientation.toString() + ")";
        }

        @Override
        public int compareTo(Key o) {
            int cmp = Long.compare(edgeId, o.edgeId);
            if (cmp == 0)
                cmp = orientation.compareTo(o.orientation);
            return cmp;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this)
                return true;

            if (!(obj instanceof Key))
                return false;

            return compareTo((Key) obj) == 0;
        }
    }
}
