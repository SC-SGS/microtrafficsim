package microtrafficsim.osm.parser.features.streets;

import microtrafficsim.core.parser.processing.Ways.MergePoint;
import microtrafficsim.core.parser.processing.Ways.WayLayout;
import microtrafficsim.osm.parser.base.DataSet;
import microtrafficsim.osm.parser.ecs.Component;
import microtrafficsim.osm.parser.ecs.Entity;
import microtrafficsim.osm.parser.ecs.components.traits.Mergeable;
import microtrafficsim.osm.parser.ecs.components.traits.Reversible;
import microtrafficsim.osm.parser.ecs.entities.NodeEntity;
import microtrafficsim.osm.parser.ecs.entities.WayEntity;
import microtrafficsim.osm.parser.features.streets.info.*;
import microtrafficsim.utils.hashing.FNVHashBuilder;


/**
 * Component to describe various properties of Streets.
 *
 * @author Maximilian Luz
 */
public class StreetComponent extends Component implements Mergeable<StreetComponent>, ReverseEquals, Reversible {

    public StreetType   streettype;
    public LaneInfo     lanes;
    public TurnInfo     turns;
    public MaxspeedInfo maxspeed;
    public OnewayInfo   oneway;
    public boolean      roundabout;
    public double       layer;


    /**
     * Creates a new {@code StreetComponent} based on the given properties.
     *
     * @param entity     the entity for which this component should be created.
     * @param streettype the type of the street.
     * @param lanes      the lane information of the street.
     * @param turns      the turn-lane information of the street.
     * @param maxspeed   the maximum-speed information of the street.
     * @param oneway     the one-way information of the street.
     * @param roundabout set to {@code true} if the street is a roundabout.
     * @param layer      the (vertical) layer on which this street resides.
     */
    public StreetComponent(WayEntity entity, StreetType streettype, LaneInfo lanes, TurnInfo turns,
                           MaxspeedInfo maxspeed, OnewayInfo oneway, boolean roundabout, double layer) {
        super(entity);
        this.streettype = streettype;
        this.lanes      = lanes;
        this.turns      = turns;
        this.maxspeed   = maxspeed;
        this.oneway     = oneway;
        this.roundabout = roundabout;
        this.layer      = layer;
    }


    /**
     * Clones this component and all contained objects.
     * <p>
     * Note: a StreetComponent can only be cloned for a WayEntity
     *
     * @param e the {@code Entity} the new {@code Component} belongs to.
     * @return a clone of this {@code Component}.
     */
    @Override
    public StreetComponent clone(Entity e) {
        LaneInfo     lanes    = new LaneInfo(this.lanes);
        TurnInfo     turns    = new TurnInfo(this.turns);
        MaxspeedInfo maxspeed = new MaxspeedInfo(this.maxspeed);
        return new StreetComponent((WayEntity) e, streettype, lanes, turns, maxspeed, oneway, roundabout, layer);
    }


    @Override
    public boolean forwardMergeable(DataSet dataset, WayLayout layout, NodeEntity node, StreetComponent other) {
        return this.streettype.equals(other.streettype)
                && this.lanes.equals(other.lanes)
                && this.turns.equals(other.turns)
                && this.maxspeed.equals(other.maxspeed)
                && this.oneway.equals(other.oneway)
                && this.roundabout == other.roundabout
                && this.layer == other.layer;
    }

    @Override
    public boolean reverseMergeable(DataSet dataset, WayLayout layout, NodeEntity node, StreetComponent other) {
        return this.streettype.equals(other.streettype)
                && this.lanes.reverseEquals(other.lanes)
                && this.turns.reverseEquals(other.turns)
                && this.maxspeed.reverseEquals(other.maxspeed)
                && this.oneway.reverseEquals(other.oneway)
                && this.roundabout == other.roundabout
                && this.layer == other.layer;
    }

    @Override
    public void merge(DataSet dataset, MergePoint mergepoint, StreetComponent other) {
        // nothing to do, StreetComponents are only mergeable if they are equal
    }


    @Override
    public int hashCode() {
        return new FNVHashBuilder()
                .add(lanes)
                .add(turns)
                .add(maxspeed)
                .add(oneway)
                .add(layer)
                .add(roundabout)
                .getHash();
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof StreetComponent)) return false;

        StreetComponent other = (StreetComponent) obj;
        return this.streettype.equals(other.streettype)
                && this.lanes.equals(other.lanes)
                && this.turns.equals(other.turns)
                && this.maxspeed.equals(other.maxspeed)
                && this.oneway.equals(other.oneway)
                && this.roundabout == other.roundabout
                && this.layer == other.layer;
    }

    @Override
    public boolean reverseEquals(Object obj) {
        if (!(obj instanceof StreetComponent)) return false;

        StreetComponent other = (StreetComponent) obj;
        return this.streettype.equals(other.streettype)
                && this.lanes.reverseEquals(other.lanes)
                && this.turns.reverseEquals(other.turns)
                && this.maxspeed.reverseEquals(other.maxspeed)
                && this.oneway.reverseEquals(other.oneway)
                && this.roundabout == other.roundabout
                && this.layer == other.layer;
    }

    @Override
    public void reverse() {
        oneway = OnewayInfo.getReverse(oneway);
        maxspeed.reverse();
        lanes.reverse();
        turns.reverse();
    }
}
