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
import microtrafficsim.utils.collections.IndexableLinkedBand;
import microtrafficsim.utils.hashing.FNVHashBuilder;
import microtrafficsim.utils.strings.builder.BasicStringBuilder;
import microtrafficsim.utils.strings.builder.LevelStringBuilder;

import java.util.Iterator;


/**
 * This class is a container for lanes from one node to another. Furthermore, it
 * stores attributes that are important for all lanes, e.g. number of cells, max
 * velocity (yes, max velocity belongs to one directed edge, not to one lane).
 *
 * @author Jan-Oliver Schmidt, Dominic Parga Cacheiro
 */
public class DirectedEdge
        implements ShortestPathEdge<Node>, LogicStreetEntity, Resettable, Iterable<DirectedEdge.Lane>
{

    private final FullStreetInfo streetInfo;
    private StreetEntity entity;
    private final IndexableLinkedBand<Vehicle> lanes;


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
        lanes = new IndexableLinkedBand<>(streetInfo.raw.nLanes);
    }


    public Lane getLane(int laneNo) {
        return new Lane(this, laneNo);
    }



    @Override
    public int hashCode() {
        return new FNVHashBuilder()
                .add(streetInfo.raw.id)
                // origin and destination needed because the id is used by forward and backward edge of the same street
                .add(streetInfo.raw.origin.hashCode())
                .add(streetInfo.raw.destination.hashCode())
                .add(streetInfo.raw.orientation == Orientation.FORWARD)
                .getHash();
    }

    @Override
    public String toString() {
        LevelStringBuilder stringBuilder = new LevelStringBuilder()
                .setDefaultLevelSeparator()
                .setDefaultLevelSubString();

        stringBuilder.appendln("<DirectedEdge>").incLevel(); {
            stringBuilder.appendln("id = " + streetInfo.raw.id);
            stringBuilder.appendln("hash = " + hashCode());
            stringBuilder.appendln("(orig -len-> dest) = ("
                    + streetInfo.raw.origin.getId()
                    + " -" + streetInfo.numberOfCells + "-> "
                    + streetInfo.raw.destination.getId() + ")");
            stringBuilder.appendln();
            for (Lane lane : this)
                stringBuilder.appendln(lane);
        } stringBuilder.decLevel().append("</DirectedEdge>");

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
     * Just an immutable container for vehicles.
     */
    public static class Lane {
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


        public synchronized int getMaxInsertionIndex() {
            if (edge.lanes.isEmpty(index)) {
                return edge.getLength() - 1;
            } else {
                return edge.lanes.getFirst(index).getCellPosition() - 1;
            }
        }

        public synchronized boolean hasVehicleInFront(Vehicle vehicle) {
            return edge.lanes.getNext(index, vehicle.getCellPosition()) != null;
        }

        public synchronized Vehicle getVehicleInFront(Vehicle vehicle) {
            return edge.lanes.getNext(index, vehicle.getCellPosition());
        }


        /**
         * @return true if success (<=> no vehicle has been on the given position)
         */
        public synchronized boolean insertVehicle(Vehicle vehicle, int cellPosition) {
            Vehicle removedVehicle = edge.lanes.set(vehicle, index, cellPosition);
            boolean success = removedVehicle == null;

            BasicStringBuilder builder = new BasicStringBuilder();
            builder.appendln("Inserting a vehicle to the lane removed a vehicle.")
                    .appendln("INSERTED = " + vehicle)
                    .appendln("REMOVED = " + removedVehicle);
            assert success : builder.toString();

            return success;
        }

        public synchronized void moveVehicle(Vehicle vehicle, int delta) {
            if (delta != 0) {
                removeVehicle(vehicle);
                insertVehicle(vehicle, vehicle.getCellPosition() + delta);
            }
        }

        /**
         * @return true if success (<=> no vehicle has been on the given position)
         */
        public synchronized boolean removeVehicle(Vehicle vehicle) {
            boolean success;

            Vehicle removedVehicle = edge.lanes.remove(index, vehicle.getCellPosition());
            success = removedVehicle == vehicle;

            BasicStringBuilder builder = new BasicStringBuilder();
            builder.appendln("Removing a vehicle from the lane removed an unexpected, different vehicle.")
                    .appendln("EXPECTED = " + vehicle)
                    .appendln("ACTUALLY REMOVED  = " + removedVehicle);
            assert success : builder.toString();

            return success;
        }


        @Override
        public int hashCode() {
            return new FNVHashBuilder().add(edge).add(index).getHash();
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
}
