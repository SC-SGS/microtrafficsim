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


/**
 * This class is a container for lanes from one node to another. Furthermore, it
 * stores attributes that are important for all lanes, e.g. number of cells, max
 * velocity (yes, max velocity belongs to one directed edge, not to one lane).
 *
 * @author Jan-Oliver Schmidt, Dominic Parga Cacheiro
 */
public class DirectedEdge implements ShortestPathEdge<Node>, LogicStreetEntity, Resettable {

    /* street information */
    private FullStreetInfo streetInfo;

    /* visualization */
    private StreetEntity entity;

    /* geometry */
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

    public StreetType getStreetType() {
        return streetInfo.raw.type;
    }

    public int getNumberOfLanes() {
        return streetInfo.raw.nLanes;
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

        stringBuilder.appendln("<DirectedEdge>").incLevel(); {
            stringBuilder.appendln("id = " + streetInfo.raw.id);
            stringBuilder.appendln("hash = " + hashCode());
            stringBuilder.appendln("info = ("
                    + streetInfo.raw.origin.getId()
                    + " -" + streetInfo.numberOfCells + "-> "
                    + streetInfo.raw.destination.getId() + ")");

        } stringBuilder.decLevel().append("<\\DirectedEdge>");

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
}