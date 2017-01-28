package microtrafficsim.core.logic.streets.information;

import microtrafficsim.core.logic.nodes.Node;
import microtrafficsim.core.simulation.configs.ScenarioConfig;
import microtrafficsim.math.Vec2d;

/**
 * @author Dominic Parga Cacheiro
 */
public class RawStreetInfo {

    /* general information */
    public ScenarioConfig config;

    /* geometry information */
    public float lengthInMeters;
    public Vec2d originDirection;
    public Vec2d destinationDirection;

    /* information for our street model (between geometry and traffic) */
    public Node origin;
    public Node destination;

    /* traffic information */
    public int noOfLines;
    public float maxVelocity;
    public byte priorityLevel;

    public RawStreetInfo() {

    }

    public RawStreetInfo(ScenarioConfig config,
                        float lengthInMeters,
                        Vec2d originDirection,
                        Vec2d destinationDirection,
                        Node origin,
                        Node destination,
                        int noOfLines,
                        float maxVelocity,
                        byte priorityLevel) {
        this.config = config;
        this.lengthInMeters = lengthInMeters;
        this.originDirection = originDirection;
        this.destinationDirection = destinationDirection;
        this.origin = origin;
        this.destination = destination;
        this.noOfLines = noOfLines;
        this.maxVelocity = maxVelocity;
        this.priorityLevel = priorityLevel;
    }
}
