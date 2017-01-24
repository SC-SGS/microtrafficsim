package microtrafficsim.core.logic;

import microtrafficsim.core.entities.street.LogicStreetEntity;
import microtrafficsim.core.entities.street.StreetEntity;
import microtrafficsim.core.shortestpath.ShortestPathEdge;
import microtrafficsim.core.simulation.configs.ScenarioConfig;
import microtrafficsim.math.Vec2d;
import microtrafficsim.utils.Resettable;


/**
 * This class is a container for lanes from one node to another. Furthermore, it
 * stores attributes that are important for all lanes, e.g. number of cells, max
 * velocity (yes, max velocity belongs to one directed edge, not to one lane).
 *
 * @author Jan-Oliver Schmidt, Dominic Parga Cacheiro
 */
public class DirectedEdge implements ShortestPathEdge, LogicStreetEntity, Resettable {

    public final long ID;
    private final int numberOfCells;
    private final int maxVelocity;
    private final byte priorityLevel;
    private final Vec2d originDirection, destinationDirection;
    private Node origin;
    private Node destination;
    private Lane[] lanes;
    // visualization
    private StreetEntity entity;

    /**
     * In addition to standard initialization, this constructor also calculates
     * the number of cells of this edge and adds this edge to the origin node's
     * leaving edges.
     *
     * @param config Just used for ID generation and meters-per-cell
     * @param lengthInMeters Real length of this edge in meters
     * @param originDirection direction vector of this edge leaving its origin node
     * @param destinationDirection direction vector of this edge entering its destination node
     * @param maxVelocity    The max velocity of this edge. It's valid for all lanes.
     * @param noOfLines      Number of lines that will be created in this constructor
     * @param origin         Origin node of this edge
     * @param destination    Destination node of this edge
     * @param priorityLevel the priority used for the crossing logic; smaller means higher priority
     */
    public DirectedEdge(ScenarioConfig config, float lengthInMeters, Vec2d originDirection,
                        Vec2d destinationDirection, float maxVelocity, int noOfLines, Node origin, Node destination,
                        byte priorityLevel) {

        ID = config.longIDGenerator.next();

        // important for shortest path: round up
        numberOfCells             = Math.max(1, (int) (Math.ceil(lengthInMeters / config.metersPerCell)));
        this.originDirection      = originDirection.normalize();
        this.destinationDirection = destinationDirection.normalize();

        lanes    = new Lane[noOfLines];
        lanes[0] = new Lane(this, 0);

        this.origin      = origin;
        this.destination = destination;
        // maxVelocity in km/h, but this.maxVelocity in cells/s
        this.maxVelocity   = Math.max(1, (int) Math.round(maxVelocity / 3.6 / config.metersPerCell));
        this.priorityLevel = priorityLevel;

        this.entity = null;
    }

    @Override
    public int hashCode() {
        return Long.hashCode(ID);
    }

    Lane[] getLanes() {
        return lanes;
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
        return maxVelocity;
    }

    byte getPriorityLevel() {
        return priorityLevel;
    }

    @Override
    public String toString() {
        return "ID=" + ID + ";hash=" + hashCode() + ":(" + origin.ID + " -" + numberOfCells + "-> " + destination.ID +
                ")";
    }

    /**
     * Resets all lanes.
     */
    @Override
    public void reset() {
        for (Lane lane : lanes)
            lane.reset();
    }

    /*
    |===============|
    | visualization |
    |===============|
    */
    public Vec2d getOriginDirection() {
        return originDirection;
    }

    public Vec2d getDestinationDirection() {
        return destinationDirection;
    }

    /*
    |======================|
    | (i) IDijkstrableEdge |
    |======================|
    */
    @Override
    public int getLength() {

        return numberOfCells;
    }

    @Override
    public float getTimeCostMillis() {
        // 1000f because velocity is in cells/s = cells/1000ms
        return (1000f * getLength()) / maxVelocity;
    }

    @Override
    public Node getOrigin() {
        return origin;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Node getDestination() {
        return destination;
    }

    /*
    |================|
    | (i) ILogicEdge |
    |================|
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