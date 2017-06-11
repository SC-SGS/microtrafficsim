package microtrafficsim.core.logic.streets;

import microtrafficsim.core.entities.street.LogicStreetEntity;
import microtrafficsim.core.entities.street.StreetEntity;
import microtrafficsim.core.logic.nodes.Node;
import microtrafficsim.core.logic.streets.information.FullStreetInfo;
import microtrafficsim.core.logic.streets.information.Orientation;
import microtrafficsim.core.logic.streets.information.RawStreetInfo;
import microtrafficsim.core.map.StreetType;
import microtrafficsim.core.shortestpath.ShortestPathEdge;
import microtrafficsim.core.simulation.configs.SimulationConfig;
import microtrafficsim.math.Vec2d;
import microtrafficsim.utils.Resettable;
import microtrafficsim.utils.hashing.FNVHashBuilder;
import microtrafficsim.utils.strings.builder.LevelStringBuilder;

import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;


/**
 * This class is a container for lanes from one node to another. Furthermore, it
 * stores attributes that are important for all lanes, e.g. number of cells, max
 * velocity (yes, max velocity belongs to one directed edge, not to one lane).
 *
 * @author Jan-Oliver Schmidt, Dominic Parga Cacheiro
 */
public class DirectedEdge implements ShortestPathEdge<Node>, LogicStreetEntity, Resettable, Comparable<DirectedEdge> {

    private FullStreetInfo streetInfo;
    private StreetEntity entity;
    private Lane[] lanes;

    /**
     * @see #DirectedEdge(RawStreetInfo)
     */
    public DirectedEdge(long id,
                        double lengthInMeters,
                        Vec2d originDirection, Vec2d destinationDirection,
                        Orientation orientation,
                        Node origin, Node destination,
                        StreetType type,
                        int nLanes,
                        float maxVelocity,
                        float metersPerCell, SimulationConfig.StreetPriorityFunction priorityFn) {
        this(new RawStreetInfo(
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
    }

    /**
     * In addition to standard initialization, this constructor also calculates
     * the number of cells of this edge and adds this edge to the origin node's
     * leaving edges.
     *
     * For detailed parameter information
     * see
     * {@link RawStreetInfo#RawStreetInfo(long, double, Vec2d, Vec2d, Orientation, Node, Node, StreetType, int, float, float, SimulationConfig.StreetPriorityFunction)}
     *
     * @param rawStreetInfo contains all relevant, "persistent" information about this edge
     */
    public DirectedEdge(RawStreetInfo rawStreetInfo) {
        streetInfo = new FullStreetInfo(rawStreetInfo);

        lanes    = new Lane[rawStreetInfo.nLanes];
        for (int i = 0; i < rawStreetInfo.nLanes; i++) {
            lanes[i] = new Lane(this, i);
        }
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

    public int getNumberOfLanes() {
        return streetInfo.raw.nLanes;
    }


    public Key key() {
        return new Key(this);
    }


    public Collection<Lane> getLanes() {
        return Arrays.asList(lanes);
    }

    /**
     * @return lane of index i counted from right to left, starting with 0
     */
    public Lane getLane(int i) {
        return lanes[i];
    }

    /**
     * @return max allowed velocity in cells/s
     */
    public int getMaxVelocity() {
        return streetInfo.maxVelocity;
    }

    /**
     * @return max allowed velocity in km/h
     */
    public float getRawMaxVelocity() {
        return streetInfo.raw.maxVelocity;
    }

    public byte getPriorityLevel() {
        return streetInfo.priorityLevel;
    }

    /**
     * Sets the value {@code metersPerCell} to the given one and calls {@link #reset()} afterwards.
     *
     * @param metersPerCell new value
     */
    public void setMetersPerCell(float metersPerCell) {
        streetInfo.raw.metersPerCell = metersPerCell;
        reset();
    }

    @Override
    public String toString() {
        LevelStringBuilder stringBuilder = new LevelStringBuilder()
                .setDefaultLevelSeparator()
                .setDefaultLevelSubString();

        stringBuilder.appendln("<" + getClass().getSimpleName() + ">").incLevel(); {
            stringBuilder.appendln(key());
            stringBuilder.appendln("hash = " + hashCode());
            stringBuilder.appendln("info = ("
                    + streetInfo.raw.origin.getId()
                    + " -" + streetInfo.numberOfCells + "-> "
                    + streetInfo.raw.destination.getId() + ")");
        } stringBuilder.decLevel().append("</" + getClass().getSimpleName() + ">");

        return stringBuilder.toString();
    }


    /*
    |================|
    | (i) Resettable |
    |================|
    */
    /**
     * Resets the {@code streetInfo} and all lanes.
     */
    @Override
    public void reset() {
        streetInfo.reset();
        for (Lane lane : lanes)
            lane.reset();
    }

    /*
    |===============|
    | visualization |
    |===============|
    */
    public Vec2d getOriginDirection() {
        return streetInfo.raw.originDirection;
    }

    public Vec2d getDestinationDirection() {
        return streetInfo.raw.destinationDirection;
    }

    /*
    |======================|
    | (i) IDijkstrableEdge |
    |======================|
    */
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

    /*
    |=======================|
    | (i) LogicStreetEntity |
    |=======================|
    */
    @Override
    public StreetEntity getEntity() {
        return entity;
    }

    @Override
    public void setEntity(StreetEntity entity) {
        this.entity = entity;
    }



    @Override
    public int compareTo(DirectedEdge o) {
        return key().compareTo(o.key());
    }

    public static class Key implements Comparable<Key> {
        private long edgeId;
//        private int orientation;
        private Orientation orientation;

        private Key() {

        }

        private Key(DirectedEdge edge) {
            edgeId = edge.streetInfo.raw.id;
//            orientation = edge.streetInfo.raw.orientation == Orientation.FORWARD ? 1 : 0;
            orientation = edge.streetInfo.raw.orientation;
        }

        @Override
        public String toString() {
            return "id = " + edgeId + " (" +
//                    (orientation == 1 ? Orientation.FORWARD : Orientation.BACKWARD)
                    orientation
                    + ")";
        }

        @Override
        public int compareTo(Key o) {
            int cmp = Long.compare(edgeId, o.edgeId);
            if (cmp == 0) {
//                cmp = Integer.compare(orientation, o.orientation);
                int i = orientation == Orientation.FORWARD ? 1 : 0;
                int j = o.orientation == Orientation.FORWARD ? 1 : 0;
                cmp = i - j;
            }
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
