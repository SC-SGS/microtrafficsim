package microtrafficsim.core.logic.streets.information;

import microtrafficsim.core.logic.nodes.Node;
import microtrafficsim.core.map.StreetType;
import microtrafficsim.math.Vec2d;

import java.util.function.Function;

/**
 * @author Dominic Parga Cacheiro
 */
public class RawStreetInfo {

    /* general information */
    public long id;

    /* geometry information */
    public double lengthInMeters;
    public Vec2d originDirection;
    public Vec2d destinationDirection;

    /* information for our street model (between geometry and traffic) */
    public Node origin;
    public Node destination;

    /* traffic information */
    public StreetType type;
    public int nLanes;
    public float maxVelocity;
    public float metersPerCell;
    public Function<StreetType, Byte> priorityFn;

    /**
     * For parameter information
     * see {@link #RawStreetInfo(long, double, Vec2d, Vec2d, Node, Node, StreetType, int, float, float, Function)}
     */
    public RawStreetInfo() {}

    /**
     * @param id                   id of the street this edge is belonging to. The forward and backward edge of the
     *                             same street are allowed to have the same id due to the fact that the hashcode uses
     *                             not only this id, but the origin and destination hashcode.
     * @param lengthInMeters       Real length of this edge in meters
     * @param originDirection      direction vector of this edge leaving its origin node
     * @param destinationDirection direction vector of this edge entering its destination node
     * @param origin               Origin node of this edge
     * @param destination          Destination node of this edge
     * @param metersPerCell        determines number of cells in this edge
     * @param nLanes               Number of lines that will be created in this constructor
     * @param maxVelocity          The max velocity of this edge. It's valid for all lanes.
     * @param priorityFn           the priority function used for the crossing logic; higher values for higher priority
     */
    public RawStreetInfo(long id,
                         double lengthInMeters,
                         Vec2d originDirection,
                         Vec2d destinationDirection,
                         Node origin,
                         Node destination,
                         StreetType type,
                         int nLanes,
                         float maxVelocity,
                         float metersPerCell,
                         Function<StreetType, Byte> priorityFn) {
        this.id                   = id;
        this.lengthInMeters       = lengthInMeters;
        this.originDirection      = originDirection.normalize();
        this.destinationDirection = destinationDirection.normalize();
        this.origin               = origin;
        this.destination          = destination;
        this.type                 = type;
        this.nLanes               = nLanes;
        this.maxVelocity          = maxVelocity;
        this.metersPerCell        = metersPerCell;
        this.priorityFn           = priorityFn;
    }
}
