package microtrafficsim.core.parser.processing;

import microtrafficsim.osm.parser.features.streets.info.OnewayInfo;
import microtrafficsim.osm.parser.base.DataSet;
import microtrafficsim.osm.parser.ecs.Component;
import microtrafficsim.osm.parser.ecs.components.traits.Mergeable;
import microtrafficsim.osm.parser.ecs.components.traits.Reversible;
import microtrafficsim.osm.parser.ecs.components.traits.Splittable;
import microtrafficsim.osm.parser.ecs.entities.NodeEntity;
import microtrafficsim.osm.parser.ecs.entities.WayEntity;
import microtrafficsim.utils.collections.ArrayUtils;
import microtrafficsim.utils.id.LongIDGenerator;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;


/**
 * Utility-class for various operations on {@code WayEntities}.
 *
 * @author Maximilian Luz
 */
public class Ways {
    private Ways() {}


    /**
     * Returns the {@code WayLayout} of two intersecting {@code WayEntities} or
     * {@code null} if they do not intersect.
     *
     * @param nodeID the ID of the node on which the ways should intersect.
     * @param a      the first {@code WayEntity}.
     * @param b      the second {@code WayEntity}.
     * @return the layout of {@code a} and {@code b} or {@code null} if they do not
     * intersect.
     */
    public static WayLayout getLayout(long nodeID, WayEntity a, WayEntity b) {
        if (nodeID == a.nodes[a.nodes.length - 1] && nodeID == b.nodes[0]
                && nodeID != a.nodes[0] && nodeID != b.nodes[b.nodes.length - 1]) {
            return WayLayout.END_START;

        } else if (nodeID == b.nodes[b.nodes.length - 1] && nodeID == a.nodes[0]
                && nodeID != b.nodes[0] && nodeID != a.nodes[a.nodes.length - 1]) {
            return WayLayout.START_END;

        } else if (nodeID == a.nodes[a.nodes.length - 1] && nodeID == b.nodes[b.nodes.length - 1]
                && nodeID != a.nodes[0] && nodeID != b.nodes[0]) {
            return WayLayout.END_END;

        } else if (nodeID == a.nodes[0] && nodeID == b.nodes[0]
                && nodeID != a.nodes[a.nodes.length - 1] && nodeID != b.nodes[b.nodes.length - 1]) {
            return WayLayout.START_START;

        } else if (nodeID == b.nodes[0] && nodeID != b.nodes[b.nodes.length - 1]
                 && ArrayUtils.contains(a.nodes, b.nodes[0])) {
            return WayLayout.T_FROM_A;

        } else if (nodeID == b.nodes[b.nodes.length - 1] && nodeID != b.nodes[0]
                 && ArrayUtils.contains(a.nodes, b.nodes[b.nodes.length - 1])) {
            return WayLayout.T_TO_A;

        } else if (nodeID == a.nodes[0] && nodeID != a.nodes[a.nodes.length - 1]
                 && ArrayUtils.contains(b.nodes, a.nodes[0])) {
            return WayLayout.T_FROM_B;

        } else if (nodeID == a.nodes[a.nodes.length - 1] && nodeID != a.nodes[0]
                 && ArrayUtils.contains(b.nodes, a.nodes[a.nodes.length - 1])) {
            return WayLayout.T_TO_B;

        } else if (ArrayUtils.contains(a.nodes, nodeID) && ArrayUtils.contains(b.nodes, nodeID)) {
            return WayLayout.X;

        } else {
            return null;
        }
    }

    /**
     * Checks if a connection from {@code a} to {@code b} is possible according to
     * the given {@code OnewayInfo}.
     *
     * @param layout the {@code WayLayout} of the two {@code WayEntities}.
     * @param a      the {@code OnewayInfo} of the first {@code WayEntity}.
     * @param b      the {@code OnewayInfo} of the second {@code WayEntity}.
     * @return {@code true} if a connection from {@code a} to {@code b} is possible.
     */
    public static boolean isConnectionPossible(WayLayout layout, OnewayInfo a, OnewayInfo b) {
        switch (layout) {
        case START_START: return !(a == OnewayInfo.FORWARD || b == OnewayInfo.BACKWARD);   // <---o--->
        case START_END:   return !(a == OnewayInfo.FORWARD || b == OnewayInfo.FORWARD);    // <---o<---
        case END_START:   return !(a == OnewayInfo.BACKWARD || b == OnewayInfo.BACKWARD);  // --->o--->
        case END_END:     return !(a == OnewayInfo.BACKWARD || b == OnewayInfo.FORWARD);   // --->o<---
        case T_FROM_A:    return b != OnewayInfo.BACKWARD;
        case T_TO_A:      return b != OnewayInfo.FORWARD;
        case T_FROM_B:    return a != OnewayInfo.FORWARD;
        case T_TO_B:      return a != OnewayInfo.BACKWARD;
        case X:           return true;
        default:          return false;
        }
    }

    /**
     * Creates a {@code MergePoint} to describe a possible merge-operation on the
     * given {@code WayEntities} and {@code NodeEntity}. If no valid {@code
     * MergePoint} can be created (no valid merge-operation can be performed), this
     * method returns {@code null}.
     * <dl>
     * <dt><strong>Warning:</strong></dt>
     * <dd>
     * {@code a} and {@code b} must be different.
     * </dd>
     * </dl>
     *
     * @param dataset the {@code DataSet} to which the {@code Entities} belong.
     * @param node    the {@code NodeEntity} on which the {@code WayEntities} should
     *                be merged.
     * @param a       the first {@code WayEntity}.
     * @param b       the second {@code WayEntity}.
     * @return a valid {@code MergePoint} if both ways can be merged on the given
     * node, else {@code null}.
     */
    public static MergePoint mergepoint(DataSet dataset, NodeEntity node, WayEntity a, WayEntity b) {
        if (!a.features.equals(b.features)) return null;

        WayLayout layout = getLayout(node.id, a, b);
        if (layout == null || !layout.isAdjacent()) return null;

        // check if components are mergeable
        Set<Class<? extends Component>> types = a.getAll().keySet();
        if (!types.equals(b.getAll().keySet())) return null;

        if (layout.isAligned()) {
            for (Class<? extends Component> type : types)
                if (!forwardMergeable(dataset, layout, node, a.get(type), b.get(type))) return null;
        } else {
            for (Class<? extends Component> type : types)
                if (!reverseMergeable(dataset, layout, node, a.get(type), b.get(type))) return null;
        }


        return new MergePoint(node, a, b, layout);
    }

    /**
     * Checks if two {@code Components} are forward-mergeable. If the {@code
     * Component} does not implement the {@code Mergeable} interface, this method
     * checks for equality.
     *
     * @param dataset the {@code DataSet} to which the {@code Entities} belong.
     * @param layout  the {@code WayLayout} of the underlying {@code WayEntities}.
     * @param node    the {@code NodeEntity} on the merge-point.
     * @param a       the first {@code Component}.
     * @param b       the second {@code Component}.
     * @return {@code true} if {@code a} and {@code b} are forward-mergeable on the
     * specified merge-point.
     */
    @SuppressWarnings("unchecked")
    private static boolean
    forwardMergeable(DataSet dataset, WayLayout layout, NodeEntity node, Component a, Component b) {
        if (a instanceof Mergeable && b instanceof Mergeable)
            return ((Mergeable<Component>) a).forwardMergeable(dataset, layout, node, b);
        else
            return a.equals(b);
    }

    /**
     * Checks if two {@code Components} are reverse-mergeable. If the {@code
     * Component} does not implement the {@code Mergeable} interface, this method
     * checks for equality.
     *
     * @param dataset the {@code DataSet} to which the {@code Entities} belong.
     * @param layout  the {@code WayLayout} of the underlying {@code WayEntities}.
     * @param node    the {@code NodeEntity} on the merge-point.
     * @param a       the first {@code Component}.
     * @param b       the second {@code Component}.
     * @return {@code true} if {@code a} and {@code b} are reverse-mergeable on the
     * specified merge-point.
     */
    @SuppressWarnings("unchecked")
    private static boolean
    reverseMergeable(DataSet dataset, WayLayout layout, NodeEntity node, Component a, Component b) {
        if (a instanceof Mergeable && b instanceof Mergeable)
            return ((Mergeable<Component>) a).reverseMergeable(dataset, layout, node, b);
        else
            return a.equals(b);
    }

    /**
     * Merges the specified {@code WayEntities} on the given {@code MergePoint} in
     * place, which means that either {@code joint.a} or {@code joint.b} will be
     * modified to contain the merged {@code WayEntity} and thus be both input and
     * result of this operation. The other one is removed from the given {@code
     * DataSet} and should be considered invalid.
     * <dl>
     * <dt><strong>Components</strong></dt>
     * <dd>
     * The ID and Components of the merged {@code WayEntity} will be either the
     * ID/Components of {@code joint.a} or {@code joint.b}.
     * </dd>
     * </dl>
     * <dl>
     * <dt><strong>Warning:</strong></dt>
     * <dd>
     * This operation is performed in place, both {@code WayEntities} {@code a}
     * and {@code b} are modified in the process. One of these will be returned as
     * the result, the other one may be in an invalid state after this operation.
     * The invalidated {@code WayEntity} is also removed from the given {@code
     * DataSet}.
     * </dd>
     * </dl>
     *
     * @param dataset the {@code DataSet} on which this action should be performed.
     * @param joint   the {@code MergePoint} on which the given {@code WayEntities}
     *                should be merged.
     * @return the {@code WayEntity} into which {@code joint.a} and {@code joint.b}
     * are merged.
     */
    public static WayEntity merge(DataSet dataset, MergePoint joint) {
        WayEntity keep;      // the first way in order of the node-array
        WayEntity remove;    // the second way in order of the node-array

        // make sure all ways are laid out as 'keep'-->'remove'
        if (joint.layout == WayLayout.END_START) {
            keep   = joint.a;
            remove = joint.b;
        } else if (joint.layout == WayLayout.START_END) {
            keep   = joint.b;
            remove = joint.a;
        } else if (joint.layout == WayLayout.START_START) {
            reverse(joint.a);
            keep   = joint.a;
            remove = joint.b;
        } else if (joint.layout == WayLayout.END_END) {
            reverse(joint.b);
            keep   = joint.a;
            remove = joint.b;
        } else {
            /* no merge possible (no MergePoint with this layout should exist) */
            return null;
        }

        // merge ways
        long[] nodes = new long[keep.nodes.length + remove.nodes.length - 1];
        System.arraycopy(keep.nodes, 0, nodes, 0, keep.nodes.length - 1);
        System.arraycopy(remove.nodes, 0, nodes, keep.nodes.length - 1, remove.nodes.length);
        keep.nodes = nodes;

        joint.node.get(GraphNodeComponent.class).ways.removeCompletely(remove);
        dataset.ways.remove(remove.id);

        // update other GraphNodeComponents
        for (Long ref : remove.nodes) {
            GraphNodeComponent iuc = dataset.nodes.get(ref).get(GraphNodeComponent.class);
            iuc.ways.removeCompletely(remove);
            iuc.ways.add(keep);
        }

        mergeComponents(dataset, joint, keep, remove);

        return keep;
    }

    /**
     * Merge the specified {@code Components} on the specified {@code MergePoint}.
     *
     * @param dataset the {@code DataSet} to which the {@code Entities} belong.
     * @param joint   the {@code MergePoint} on which the {@code Components} should
     *                be merged.
     * @param keep    the underlying {@code WayEntity} into which the {@code
     *                WayEntities} are merged.
     * @param remove  the {@code WayEntity} which is removed in the merge-process.
     */
    @SuppressWarnings({"unchecked"})
    private static void mergeComponents(DataSet dataset, MergePoint joint, WayEntity keep, WayEntity remove) {
        HashSet<Class<? extends Component>> types = new HashSet<>(joint.a.getAll().keySet());
        types.addAll(joint.b.getAll().keySet());

        for (Class<? extends Component> type : types) {
            Component k = keep.get(type);
            Component r = remove.get(type);

            // if component is not present on keep
            if (k == null && r != null) {
                r.setEntity(keep);
                keep.getAll().put(type, r);

                // if component is present on both, and is mergeable
            } else if (k != null && r != null && k instanceof Mergeable && r instanceof Mergeable) {
                ((Mergeable<Component>) k).merge(dataset, joint, r);

                // else: keep component
            }
        }
    }

    /**
     * Split the given {@code WayEntity} at the nodes specified by the indices.
     * Does neither add the splits nor remove the original from the {@code dataset}.
     * <dl>
     * <dt><strong>Components:</strong></dt>
     * <dd>
     * This method clones all {@code Components} on the given {@code WayEntity}
     * for the created splits, except the {@code GraphWayComponent} which is
     * updated separately.
     * </dd>
     * </dl>
     * <dl>
     * <dt><strong>Warning:</strong></dt>
     * <dd>
     * The size of {@code splitpoints} must be greater than one.
     * </dd>
     * </dl>
     *
     * @param dataset     the DataSet on which to perform this action on.
     * @param way         the WayEntity to split
     * @param splitpoints the indices at which the ways should be split.
     * @param idgen       the ID-Generator used to create the IDs for the new {@code
     *                    WayEnities}.
     * @return an Array containing all splits.
     */
    public static WayEntity[] split(DataSet dataset, WayEntity way, int[] splitpoints, LongIDGenerator idgen) {

        // append last node-index to split-indices
        int[] indices = new int[splitpoints.length + 1];
        System.arraycopy(splitpoints, 0, indices, 0, splitpoints.length);
        indices[splitpoints.length] = way.nodes.length - 1;

        // split into segments [last, index] with cloned Components
        WayEntity[] splits = new WayEntity[splitpoints.length + 1];

        int last = 0, n = 0;
        for (int index : indices) {
            WayEntity segment = way.clone();
            segment.id        = idgen.next();
            segment.nodes     = Arrays.copyOfRange(way.nodes, last, index + 1);

            splits[n++] = segment;
            last        = index;
        }

        // split components (not actually a split, rather a post-processing on the cloned components)
        for (Component c : way.getAll().values())
            if (c instanceof Splittable) ((Splittable) c).split(dataset, splits, splitpoints);


        return splits;
    }

    /**
     * Returns a set of references to all Nodes on which the given way intersects
     * with itself.
     *
     * @param dataset the DataSet on which to perform this action on.
     * @param way     the way to get the the self-intersection-points for.
     * @return a list of (references to) Nodes, on which the way intersects with
     * itself.
     */
    public static HashSet<Long> getSelfIntersectionPoints(DataSet dataset, WayEntity way) {
        HashSet<Long> xpts = new HashSet<>();

        for (long ref : way.nodes)
            if (dataset.nodes.get(ref).get(GraphNodeComponent.class).ways.count(way) > 1) xpts.add(ref);

        return xpts;
    }

    /**
     * Reverses the given {@code WayEntity} by inverting the order of Nodes.
     * <dl>
     * <dt><strong>Components:</strong></dt>
     * <dd>
     * This method updates the {@code StreetComponent} and the {@code
     * GraphWayComponent}.
     * </dd>
     * </dl>
     *
     * @param way the {@code WayEntity} to reverse.
     */
    public static void reverse(WayEntity way) {
        // reverse nodes
        long[] nodes = new long[way.nodes.length];

        for (int i   = 0; i < nodes.length; i++)
            nodes[i] = way.nodes[nodes.length - 1 - i];

        way.nodes = nodes;

        // reverse components
        for (Component c : way.getAll().values())
            if (c instanceof Reversible) ((Reversible) c).reverse();
    }

    /**
     * Describes the layout of two intersecting ways.
     */
    public enum WayLayout {
        START_START,
        START_END,
        END_START,
        END_END,
        T_FROM_A,
        T_FROM_B,
        T_TO_A,
        T_TO_B,
        X;

        /**
         * Returns {@code true} if this layout describes two ways which are
         * adjacent at start or end.
         *
         * @return {@code true} if this is either {@code START_END}, {@code
         * END_START}, {@code START_START} or {@code END_END}.
         */
        public boolean isAdjacent() {
            return this == START_END || this == END_START || this == START_START || this == END_END;
        }

        /**
         * Returns {@code true} if this layout describes either a T- or X-crossing.
         *
         * @return {@code true} if this is a {@code T_...}- or {@code X}-layout.
         */
        public boolean isCrossing() {
            return this == T_FROM_A || this == T_FROM_B || this == T_TO_A || this == T_TO_B || this == X;
        }

        /**
         * Returns {@code true} if this layout describes a T-crossing.
         *
         * @return {@code true} if this is a {@code T_...}-layout.
         */
        public boolean isTCrossing() {
            return this == T_FROM_A || this == T_FROM_B || this == T_TO_A || this == T_TO_B;
        }

        /**
         * Returns {@code true} if this layout describes two ways which are
         * directional aligned.
         *
         * @return {@code true} if this is {@code START_END} or {@code END_START}.
         */
        public boolean isAligned() {
            return this == START_END || this == END_START;
        }

        /**
         * Returns the reversed form of this layout, which means the layout when both
         * ways would be reversed.
         *
         * @return the reversed layout of this.
         */
        public WayLayout getReversed() {
            switch (this) {
            case START_START: return END_END;
            case START_END:   return END_START;
            case END_START:   return START_END;
            case END_END:     return START_START;
            case T_FROM_A:    return T_TO_A;
            case T_FROM_B:    return T_TO_B;
            case T_TO_A:      return T_FROM_A;
            case T_TO_B:      return T_FROM_B;
            case X:           return X;
            default:          return null;    // can not happen: no other values possible
            }
        }
    }

    /**
     * Describes how and at which {@code NodeEntity} two {@code WayEntities} can be
     * merged.
     */
    public static class MergePoint {
        public final NodeEntity node;
        public final WayEntity a;
        public final WayEntity b;
        public final WayLayout layout;

        private MergePoint(NodeEntity node, WayEntity a, WayEntity b, WayLayout layout) {
            this.node   = node;
            this.a      = a;
            this.b      = b;
            this.layout = layout;
        }
    }
}
