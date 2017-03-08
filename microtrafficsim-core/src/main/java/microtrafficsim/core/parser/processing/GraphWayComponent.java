package microtrafficsim.core.parser.processing;

import microtrafficsim.core.parser.processing.Ways.MergePoint;
import microtrafficsim.core.parser.processing.Ways.WayLayout;
import microtrafficsim.osm.parser.base.DataSet;
import microtrafficsim.osm.parser.ecs.Component;
import microtrafficsim.osm.parser.ecs.Entity;
import microtrafficsim.osm.parser.ecs.components.traits.Mergeable;
import microtrafficsim.osm.parser.ecs.components.traits.Removeable;
import microtrafficsim.osm.parser.ecs.components.traits.Reversible;
import microtrafficsim.osm.parser.ecs.components.traits.Splittable;
import microtrafficsim.osm.parser.ecs.entities.NodeEntity;
import microtrafficsim.osm.parser.ecs.entities.WayEntity;
import microtrafficsim.osm.parser.features.streets.StreetComponent;
import microtrafficsim.osm.parser.features.streets.info.OnewayInfo;
import microtrafficsim.utils.collections.HashMultiSet;
import microtrafficsim.utils.hashing.FNVHashBuilder;

import java.util.HashSet;


/**
 * Component for storing Street-Connectors and other information about the
 * Street-Network.
 *
 * @author Maximilian Luz
 */
public class GraphWayComponent extends Component implements Mergeable<GraphWayComponent>, Splittable, Reversible, Removeable {

    public HashSet<Connector> uturn;
    public HashSet<Connector> from;
    public HashSet<Connector> to;
    public boolean            cyclicStartToEnd;
    public boolean            cyclicEndToStart;

    /**
     * Create a new {@code GraphWayComponent} for the given way-entity.
     *
     * @param entity the way-entity for which the component should be created.
     */
    public GraphWayComponent(WayEntity entity) {
        super(entity);
        this.uturn       = new HashSet<>();
        this.from        = new HashSet<>();
        this.to          = new HashSet<>();
        cyclicStartToEnd = false;
        cyclicEndToStart = false;
    }

    /**
     * Create a new {@code GraphWayComponent} based on the specified properties for
     * the given way-entity.
     *
     * @param entity           the entity for which this component should be created.
     * @param uturn            the set of obj0-turn connectors.
     * @param from             the set of connectors leaving this way.
     * @param to               the set of connectors going to this way.
     * @param cyclicStartToEnd set to {@code true}, if this way is cyclic and a connector
     *                         from start to end should exists.
     * @param cyclicEndToStart set to {@code true}, if this way is cyclic and a connector
     *                         from end to start should exists.
     */
    public GraphWayComponent(WayEntity entity, HashSet<Connector> uturn, HashSet<Connector> from, HashSet<Connector> to,
                             boolean cyclicStartToEnd, boolean cyclicEndToStart) {
        super(entity);
        this.uturn            = uturn;
        this.from             = from;
        this.to               = to;
        this.cyclicStartToEnd = cyclicStartToEnd;
        this.cyclicEndToStart = cyclicEndToStart;
    }

    @Override
    public Class<? extends Component> getType() {
        return GraphWayComponent.class;
    }


    @Override
    public boolean forwardMergeable(DataSet dataset, WayLayout layout, NodeEntity node, GraphWayComponent other) {
        if (other == null) return false;

        StreetComponent scA = this.entity.get(StreetComponent.class);
        StreetComponent scB = other.entity.get(StreetComponent.class);

        // get required connectors
        boolean needsAtoB = true;
        boolean needsBtoA = true;

        if (scA != null && scB != null) {
            needsAtoB = Ways.isConnectionPossible(layout, scA.oneway, scB.oneway);
            needsBtoA = Ways.isConnectionPossible(layout.getReversed(), scA.oneway, scB.oneway);
        }

        // check if connectors exist
        if (needsAtoB) {
            Connector ab = new Connector(node, (WayEntity) this.entity, (WayEntity) other.entity);
            if ((!this.from.contains(ab)) && (!other.to.contains(ab))) return false;
        }

        if (needsBtoA) {
            Connector ba = new Connector(node, (WayEntity) other.entity, (WayEntity) this.entity);
            if ((!this.to.contains(ba)) && (!other.from.contains(ba))) return false;
        }

        return true;
    }

    @Override
    public boolean reverseMergeable(DataSet dataset, WayLayout layout, NodeEntity node, GraphWayComponent other) {
        return forwardMergeable(dataset, layout, node, other);
    }

    /**
     * Merges this {@code Component} with {@code other}.
     * <p>
     * Note: this operation modifies and invalidates {@code other}.
     * </p>
     *
     * @param dataset    the {@code DataSet} to which the {@code Components} and
     *                   {@code Entities} belong.
     * @param mergepoint the {@code MergePoint} on which the {@code Components}
     *                   should be merged.
     * @param other      the {@code Component} with which this should be merged.
     */
    @Override
    public void merge(DataSet dataset, MergePoint mergepoint, GraphWayComponent other) {
        WayEntity keep   = (WayEntity) this.getEntity();
        WayEntity remove = (WayEntity) other.getEntity();

        // remove obj0-turns at merge-point
        this.uturn.remove(new Connector(mergepoint.node, keep, keep));
        other.uturn.remove(new Connector(mergepoint.node, remove, remove));

        // remove connectors between 'keep' and 'remove' via 'joint.node'
        Connectors.remove(new Connector(mergepoint.node, keep, remove));
        Connectors.remove(new Connector(mergepoint.node, remove, keep));

        // handle creation of cyclic way ('keep'-->'remove'-->'keep')
        if (keep.nodes[0] == remove.nodes[remove.nodes.length - 1]) {
            NodeEntity via = dataset.nodes.get(keep.nodes[0]);

            Connector kr = new Connector(via, keep, remove);
            Connector rk = new Connector(via, remove, keep);

            // create cyclic connectors if they exist
            if (this.from.contains(kr)) this.cyclicStartToEnd = true;
            if (this.to.contains(rk))   this.cyclicEndToStart = true;

            // remove connectors between 'keep' and 'remove'
            Connectors.remove(kr);
            Connectors.remove(rk);
        }

        // add obj0-turns from 'remove'
        for (Connector c : other.uturn) {
            if (c.via == mergepoint.node) continue;    // ignore obj0-turns on merge-point

            Connectors.add(Connectors.create(c.via, keep, keep));
        }

        // add connectors: 'remove' --> x
        for (Connector c : other.from) {
            c.to.get(GraphWayComponent.class).to.remove(c);
            Connectors.add(Connectors.create(c.via, keep, c.to));
        }

        // add connectors 'remove' <-- x
        for (Connector c : other.to) {
            c.from.get(GraphWayComponent.class).from.remove(c);
            Connectors.add(Connectors.create(c.via, c.from, keep));
        }
    }


    /**
     * Clones this component and all contained objects.
     * <p>
     * Note: a GraphWayComponent can only be cloned for a WayEntity
     * </p>
     *
     * @param e the {@code Entity} the new {@code Component} belongs to.
     * @return a clone of this {@code Component}.
     */
    @Override
    public Component clone(Entity e) {
        return new GraphWayComponent(
                (WayEntity) e,
                new HashSet<>(this.uturn),
                new HashSet<>(this.from),
                new HashSet<>(this.to),
                this.cyclicStartToEnd,
                this.cyclicEndToStart
        );
    }

    @Override
    public void split(DataSet dataset, WayEntity[] splits, int[] splitpoints) {
        WayEntity way = (WayEntity) this.getEntity();

        // remove old connectors from adjacent ways
        for (Connector c : this.from)
            c.to.get(GraphWayComponent.class).to.remove(c);

        for (Connector c : this.to)
            c.from.get(GraphWayComponent.class).from.remove(c);

        // create new connectors based on existing ones
        for (WayEntity segment : splits) {
            GraphWayComponent gsc = segment.get(GraphWayComponent.class);
            gsc.uturn.clear();
            gsc.from.clear();
            gsc.to.clear();

            for (Connector c : this.uturn)
                if (c.via.id == segment.nodes[0] || c.via.id == segment.nodes[segment.nodes.length - 1])
                    Connectors.add(Connectors.create(c.via, segment, segment));

            for (Connector c : this.from)
                if (c.via.id == segment.nodes[0] || c.via.id == segment.nodes[segment.nodes.length - 1])
                    Connectors.add(Connectors.create(c.via, segment, c.to));

            for (Connector c : this.to)
                if (c.via.id == segment.nodes[0] || c.via.id == segment.nodes[segment.nodes.length - 1])
                    Connectors.add(Connectors.create(c.via, c.from, segment));
        }

        // propagate cyclic connectors from way to splits
        if (way.nodes[0] == way.nodes[way.nodes.length - 1]) {
            NodeEntity via = dataset.nodes.get(way.nodes[0]);

            if (this.cyclicStartToEnd) Connectors.add(Connectors.create(via, splits[0], splits[splits.length - 1]));
            if (this.cyclicEndToStart) Connectors.add(Connectors.create(via, splits[splits.length - 1], splits[0]));
        }

        // add connectors between segments, including obj0-turns
        WayEntity prev = splits[0];
        for (int i = 1; i < splits.length; i++) {
            NodeEntity via = dataset.nodes.get(splits[i].nodes[0]);

            // add connectors between segments
            Connectors.add(Connectors.create(via, prev, prev));
            Connectors.add(Connectors.create(via, prev, splits[i]));
            Connectors.add(Connectors.create(via, splits[i], prev));
            Connectors.add(Connectors.create(via, splits[i], splits[i]));

            prev = splits[i];
        }

        // create connectors between intersecting segments
        HashSet<Long> xpts = Ways.getSelfIntersectionPoints(dataset, way);
        for (long x : xpts) {

            // get adjacent segments
            HashMultiSet<WayEntity> adjacent = new HashMultiSet<>();
            for (WayEntity segment : splits)
                if (x == segment.nodes[0] || x == segment.nodes[segment.nodes.length - 1]) adjacent.add(segment);

            NodeEntity via    = dataset.nodes.get(x);
            boolean    cyclic = via.id == way.nodes[0] && via.id == way.nodes[way.nodes.length - 1];

            // create connectors between all adjacent segments
            for (WayEntity w1 : adjacent) {
                for (WayEntity w2 : adjacent) {
                    if (w1 == w2) continue;

                    // avoid creating nonexistent cyclic connectors of 'way'
                    if (cyclic && ((w1 == splits[0] && w2 == splits[splits.length - 1])
                                   || (w1 == splits[splits.length - 1] && w2 == splits[0])))
                        continue;

                    Connectors.add(Connectors.create(via, w1, w2));
                    Connectors.add(Connectors.create(via, w2, w1));
                }
            }
        }

        // set cyclic connector on segments
        for (WayEntity segment : splits) {
            GraphWayComponent swc = segment.get(GraphWayComponent.class);
            StreetComponent   sc  = segment.get(StreetComponent.class);

            if (segment.nodes[0] == segment.nodes[segment.nodes.length - 1]) {
                swc.cyclicStartToEnd = true;
                swc.cyclicEndToStart = true;

                if (sc != null) {
                    if (sc.oneway == OnewayInfo.FORWARD)
                        swc.cyclicStartToEnd = false;
                    else if (sc.oneway == OnewayInfo.BACKWARD)
                        swc.cyclicEndToStart = false;
                }
            } else {
                swc.cyclicStartToEnd = false;
                swc.cyclicEndToStart = false;
            }
        }
    }


    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof GraphWayComponent)) return false;

        GraphWayComponent other = (GraphWayComponent) obj;

        // check cyclic connectors
        return this.cyclicStartToEnd == other.cyclicStartToEnd
                && this.cyclicEndToStart == other.cyclicEndToStart
                && checkConnectorEquality(other);
    }

    @Override
    public boolean reverseEquals(Object obj) {
        if (!(obj instanceof GraphWayComponent)) return false;

        GraphWayComponent other = (GraphWayComponent) obj;

        // check cyclic connectors
        return this.cyclicStartToEnd == other.cyclicEndToStart
                && this.cyclicEndToStart == other.cyclicStartToEnd
                && checkConnectorEquality(other);
    }

    /**
     * Checks if the connectors on this component are (semantically) equal to the connectors on {@code other}.
     *
     * @param other the {@code GraphWayComponent} to compare against.
     * @return {@code true} iff the {@link Connector}s are equal on this component and {@code other}.
     */
    private boolean checkConnectorEquality(GraphWayComponent other) {
        // check other connectors by size first
        if (this.uturn.size() != other.uturn.size()) return false;
        if (this.from.size() != other.from.size()) return false;
        if (this.to.size() != other.to.size()) return false;

        WayEntity wo = (WayEntity) other.getEntity();

        // check existence of connectors
        for (Connector c : this.uturn)
            if (!other.uturn.contains(new Connector(c.via, wo, wo)))
                return false;

        for (Connector c : this.from)
            if (c.to != wo)    // skip connectors from this to other
                if (!other.from.contains(new Connector(c.via, wo, c.to)))
                    return false;

        for (Connector c : this.to)
            if (c.from != wo)    // skip connectors from other to this
                if (!other.to.contains(new Connector(c.via, c.from, wo)))
                    return false;

        return true;
    }

    @Override
    public int hashCode() {
        return new FNVHashBuilder()
                .add(uturn)
                .add(from)
                .add(to)
                .add(cyclicStartToEnd)
                .add(cyclicEndToStart)
                .getHash();
    }

    @Override
    public void reverse() {
        boolean swap     = cyclicEndToStart;
        cyclicEndToStart = cyclicStartToEnd;
        cyclicStartToEnd = swap;
    }

    @Override
    public void remove(DataSet dataset) {
        for (Connector c : this.from)
            c.to.get(GraphWayComponent.class).to.remove(c);

        for (Connector c : this.to)
            c.from.get(GraphWayComponent.class).from.remove(c);
    }
}
