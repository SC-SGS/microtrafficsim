package microtrafficsim.core.logic.streets;

import microtrafficsim.core.entities.street.LogicStreetEntity;
import microtrafficsim.core.entities.street.StreetEntity;
import microtrafficsim.core.logic.nodes.Node;
import microtrafficsim.core.logic.streets.information.FullStreetInfo;
import microtrafficsim.core.logic.streets.information.RawStreetInfo;
import microtrafficsim.core.shortestpath.ShortestPathEdge;
import microtrafficsim.core.simulation.configs.ScenarioConfig;
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
public class DirectedEdge implements ShortestPathEdge, LogicStreetEntity, Resettable {

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
                        float lengthInMeters,
                        Vec2d originDirection,
                        Vec2d destinationDirection,
                        Node origin,
                        Node destination,
                        float metersPerCell,
                        int noOfLines,
                        float maxVelocity,
                        byte priorityLevel) {
        this(new RawStreetInfo(
                id, lengthInMeters, originDirection, destinationDirection, origin, destination,
                metersPerCell, noOfLines, maxVelocity, priorityLevel
        ));
    }

    /**
     * In addition to standard initialization, this constructor also calculates
     * the number of cells of this edge and adds this edge to the origin node's
     * leaving edges.
     *
     * For detailed parameter information
     * see {@link RawStreetInfo#RawStreetInfo(long, float, Vec2d, Vec2d, Node, Node, float, int, float, byte)}
     *
     * @param rawStreetInfo contains all relevant, "persistent" information about this edge
     */
    public DirectedEdge(RawStreetInfo rawStreetInfo) {

        streetInfo = new FullStreetInfo(rawStreetInfo);

        lanes    = new Lane[rawStreetInfo.noOfLines];
        lanes[0] = new Lane(this, 0);
    }

    @Override
    public int hashCode() {
        return new FNVHashBuilder()
                .add(streetInfo.raw.id)
                // origin and destination needed because the id is used by forward and backward edge of the same street
                .add(streetInfo.raw.origin)
                .add(streetInfo.raw.destination)
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
        return streetInfo.raw.priorityLevel;
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
        LevelStringBuilder stringBuilder = new LevelStringBuilder();
        stringBuilder.appendln("<DirectedEdge>");
        stringBuilder.incLevel();

        stringBuilder.appendln("id = " + streetInfo.raw.id);
        stringBuilder.appendln("hash = " + hashCode());
        stringBuilder.appendln("info = ("
                + streetInfo.raw.origin.id
                + " -" + streetInfo.numberOfCells + "-> "
                + streetInfo.raw.destination.id + ")");

        stringBuilder.decLevel();
        stringBuilder.appendln("<\\DirectedEdge>");
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

    @Override
    public float getTimeCostMillis() {
        // 1000f because velocity is in cells/s = cells/1000ms
        return (1000f * getLength()) / streetInfo.maxVelocity;
    }

    @Override
    public Node getOrigin() {
        return streetInfo.raw.origin;
    }

    @Override
    @SuppressWarnings("unchecked")
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