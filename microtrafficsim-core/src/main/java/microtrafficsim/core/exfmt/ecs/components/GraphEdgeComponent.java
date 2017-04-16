package microtrafficsim.core.exfmt.ecs.components;

import microtrafficsim.core.exfmt.ecs.Component;
import microtrafficsim.core.exfmt.ecs.Entity;
import microtrafficsim.core.map.StreetType;
import microtrafficsim.math.Vec2d;


public class GraphEdgeComponent extends Component {

    private double length;              // in meter
    private StreetType type;

    private int fwdLanes = 0;
    private int bwdLanes = 0;
    private float fwdMaxVelocity = 0;   // in km/h
    private float bwdMaxVelocity = 0;   // in km/h

    private long orig = -1;             // PointEntity
    private long dest = -1;             // PointEntity

    private Vec2d origDir = null;
    private Vec2d destDir = null;


    public GraphEdgeComponent(Entity entity) {
        super(entity);
    }


    public double getLength() {
        return length;
    }

    public void setLength(double length) {
        this.length = length;
    }


    public StreetType getStreetType() {
        return type;
    }

    public void setStreetType(StreetType type) {
        this.type = type;
    }


    public int getForwardLanes() {
        return fwdLanes;
    }

    public void setForwardLanes(int lanes) {
        this.fwdLanes = lanes;
    }

    public int getBackwardLanes() {
        return bwdLanes;
    }

    public void setBackwardLanes(int lanes) {
        this.bwdLanes = lanes;
    }


    public float getForwardMaxVelocity() {
        return fwdMaxVelocity;
    }

    public void setForwardMaxVelocity(float fwdMaxVelocity) {
        this.fwdMaxVelocity = fwdMaxVelocity;
    }

    public float getBackwardMaxVelocity() {
        return bwdMaxVelocity;
    }

    public void setBackwardMaxVelocity(float bwdMaxVelocity) {
        this.bwdMaxVelocity = bwdMaxVelocity;
    }


    public long getOrigin() {
        return orig;
    }

    public void setOrigin(long origin) {
        this.orig = origin;
    }

    public long getDestination() {
        return dest;
    }

    public void setDestination(long destination) {
        this.dest = destination;
    }


    public Vec2d getOriginDirection() {
        return origDir;
    }

    public void setOriginDirection(Vec2d direction) {
        this.origDir = direction;
    }

    public Vec2d getDestinationDirection() {
        return destDir;
    }

    public void setDestinationDirection(Vec2d direction) {
        this.destDir = direction;
    }


    public void reverse() {
        {
            float tmp = fwdMaxVelocity;
            fwdMaxVelocity = bwdMaxVelocity;
            bwdMaxVelocity = tmp;
        }

        {
            int tmp = fwdLanes;
            fwdLanes = bwdLanes;
            bwdLanes = tmp;
        }

        {
            long tmp = orig;
            orig = dest;
            dest = tmp;
        }

        {
            Vec2d tmp = origDir;
            origDir = destDir;
            destDir = tmp;
        }
    }
}
