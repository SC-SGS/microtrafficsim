package microtrafficsim.core.parser.features.streetgraph;

import microtrafficsim.core.entities.street.StreetEntity;
import microtrafficsim.core.logic.nodes.Node;
import microtrafficsim.core.logic.streetgraph.Graph;
import microtrafficsim.core.logic.streetgraph.StreetGraph;
import microtrafficsim.core.logic.streets.DirectedEdge;
import microtrafficsim.core.logic.streets.information.Orientation;
import microtrafficsim.core.map.Bounds;
import microtrafficsim.core.map.Coordinate;
import microtrafficsim.core.map.StreetType;
import microtrafficsim.core.parser.processing.Connector;
import microtrafficsim.core.parser.processing.GraphNodeComponent;
import microtrafficsim.core.parser.processing.GraphWayComponent;
import microtrafficsim.core.parser.processing.sanitizer.SanitizerWayComponent;
import microtrafficsim.core.simulation.configs.CrossingLogicConfig;
import microtrafficsim.core.simulation.configs.SimulationConfig;
import microtrafficsim.math.DistanceCalculator;
import microtrafficsim.math.HaversineDistanceCalculator;
import microtrafficsim.math.MathUtils;
import microtrafficsim.math.Vec2d;
import microtrafficsim.osm.parser.base.DataSet;
import microtrafficsim.osm.parser.ecs.Component;
import microtrafficsim.osm.parser.ecs.entities.NodeEntity;
import microtrafficsim.osm.parser.ecs.entities.WayEntity;
import microtrafficsim.osm.parser.features.FeatureDefinition;
import microtrafficsim.osm.parser.features.FeatureGenerator;
import microtrafficsim.osm.parser.features.streets.StreetComponent;
import microtrafficsim.osm.parser.features.streets.info.OnewayInfo;
import microtrafficsim.utils.logging.EasyMarkableLogger;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;


// TODO: connectors could be optimized


/**
 * The {@code FeatureGenerator} for the StreetGraph used in the simulation.
 *
 * @author Dominic Parga Cacheiro, Maximilian Luz
 */
public class StreetGraphGenerator implements FeatureGenerator {
    private static Logger logger = new EasyMarkableLogger(StreetGraphGenerator.class);

    private SimulationConfig   config;
    private Graph              graph;
    private DistanceCalculator distcalc;


    /**
     * Creates a new {@code StreetGraphGenerator} using the
     * {@link HaversineDistanceCalculator#getDistance(Coordinate, Coordinate) HaversineDistanceCalculator } as
     * {@code DistanceCalculator}.
     * <p>
     * This is equivalent to
     * {@link StreetGraphGenerator#StreetGraphGenerator(SimulationConfig, DistanceCalculator)
     * StreetGraphGenerator(config, HaversineDistanceCalculator::getDistance) }
     */
    public StreetGraphGenerator(SimulationConfig config) {
        this(config, HaversineDistanceCalculator::getDistance);
    }

    /**
     * Creates a new {@code StreetGraphGenerator} with the given * {@code DistanceCalculator}.
     *
     * @param config   the simulation-config to be used with the graph.
     * @param distcalc the {@code DistanceCalculator} used to calculate the length of streets.
     */
    public StreetGraphGenerator(SimulationConfig config, DistanceCalculator distcalc) {
        this.config      = config;
        this.distcalc    = distcalc;
        this.graph       = null;
    }

    /**
     * Returns the generated StreetGraph or {@code null}, if this generator has
     * not been executed yet.
     *
     * @return the generated StreetGraph.
     */
    public Graph getStreetGraph() {
        return graph;
    }

    @Override
    public void execute(DataSet dataset, FeatureDefinition feature, Properties properties) {
        logger.info("generating StreetGraph");
        this.graph = null;

        Bounds bounds;
        if (properties.clip == Properties.BoundaryManagement.CLIP && properties.bounds != null) {
            bounds = properties.bounds;
        } else {
            bounds = dataset.bounds;
        }

        Graph graph = new StreetGraph(bounds);

        // create required nodes and edges
        for (WayEntity way : dataset.ways.values()) {
            if (!way.features.contains(feature)) continue;
            createAndAddEdges(dataset, graph, way, config);
        }

        // add turn-lanes
        for (WayEntity way : dataset.ways.values()) {
            if (!way.features.contains(feature)) continue;
            addLeavingConnectors(dataset, way);
        }

        for (NodeEntity node : dataset.nodes.values())
            node.remove(StreetGraphNodeComponent.class);

        // finish
        graph.setSeed(config.seed);
        for (Node node : graph.getNodes()) {
            node.updateEdgeIndices();
        }
        graph.updateGraphGUID();

        this.graph = graph;
        logger.info("finished generating StreetGraph");
    }

    /**
     * Creates all necessary {@code DirectedEdges} from the given
     * {@code WayEntity} and adds them to the StreetGraph.
     *
     * @param dataset the {@code DataSet} of which {@code way} is part of.
     * @param graph   the StreetGraph to which the generated edges should be added.
     * @param way     the {@code WayEntity} for which the edges should be generated.
     */
    private void createAndAddEdges(DataSet dataset, Graph graph, WayEntity way, SimulationConfig config) {
        NodeEntity      node0          = dataset.nodes.get(way.nodes[0]);
        NodeEntity      node1          = dataset.nodes.get(way.nodes[1]);
        NodeEntity      secondLastNode = dataset.nodes.get(way.nodes[way.nodes.length - 2]);
        NodeEntity      lastNode       = dataset.nodes.get(way.nodes[way.nodes.length - 1]);
        Node            start          = getNode(node0, config.crossingLogic);
        Node            end            = getNode(lastNode, config.crossingLogic);
        StreetComponent streetinfo     = way.get(StreetComponent.class);

        // generate edges
        DirectedEdge forward  = null;
        DirectedEdge backward = null;

        double length = getLength(dataset, way);
        StreetType type;
        if (streetinfo.roundabout) {
            type = microtrafficsim.osm.parser.features.streets.info.StreetType.ROUNDABOUT.toCoreStreetType();
        } else {
            type = streetinfo.streettype.toCoreStreetType();
        }

        if (streetinfo.oneway == OnewayInfo.NO || streetinfo.oneway == OnewayInfo.FORWARD
            || streetinfo.oneway == OnewayInfo.REVERSIBLE) {
            Vec2d originDirection = new Vec2d(node1.lon - node0.lon,
                                              node1.lat - node0.lat);

            Vec2d destinationDirection = new Vec2d(lastNode.lon - secondLastNode.lon,
                                                   lastNode.lat - secondLastNode.lat);

            forward = new DirectedEdge(way.id,
                    length,
                    originDirection, destinationDirection,
                    Orientation.FORWARD,
                    start, end,
                    type,
                    streetinfo.lanes.forward,
                    streetinfo.maxspeed.forward,
                    config.metersPerCell, config.streetPriorityLevel);
        }

        if (streetinfo.oneway == OnewayInfo.NO || streetinfo.oneway == OnewayInfo.BACKWARD) {
            Vec2d originDirection = new Vec2d(secondLastNode.lon - lastNode.lon,
                                              secondLastNode.lat - lastNode.lat);

            Vec2d destinationDirection = new Vec2d(node0.lon - node1.lon,
                                                   node0.lat - node1.lat);

            backward = new DirectedEdge(
                    way.id,
                    length,
                    originDirection, destinationDirection,
                    Orientation.BACKWARD,
                    end, start,
                    type,
                    streetinfo.lanes.backward,
                    streetinfo.maxspeed.backward,
                    config.metersPerCell, config.streetPriorityLevel);
        }

        // create component for ECS
        StreetGraphWayComponent graphinfo = new StreetGraphWayComponent(way, forward, backward);
        way.set(StreetGraphWayComponent.class, graphinfo);

        StreetEntity entity = new StreetEntity(forward, backward, null);

        // register
        if (forward != null || backward != null) {
            graph.addNode(start);
            graph.addNode(end);
        }

        if (forward != null) {
            forward.setEntity(entity);
            graph.addEdge(forward);
            start.addLeavingEdge(forward);
            end.addIncomingEdge(forward);
        }

        if (backward != null) {
            backward.setEntity(entity);
            graph.addEdge(backward);
            start.addIncomingEdge(backward);
            end.addLeavingEdge(backward);
        }
    }

    /**
     * Creates and adds all lane-connectors leading from the given {@code
     * WayEntity} to any other {@code WayEntity}/{@code DirectedEdge} (including
     * itself).
     *
     * @param dataset the {@code DataSet} of which {@code wayFrom} is a part of.
     * @param from the {@code WayEntity} for which all outgoing lane-connectors
     *                should be created.
     */
    private void addLeavingConnectors(DataSet dataset, WayEntity from) {
        StreetGraphWayComponent sgwc = from.get(StreetGraphWayComponent.class);
        if (sgwc == null) return;
        if (sgwc.forward == null && sgwc.backward == null) return;

        GraphWayComponent gwc = from.get(GraphWayComponent.class);

        NodeEntity start = dataset.nodes.get(from.nodes[0]);
        NodeEntity end = dataset.nodes.get(from.nodes[from.nodes.length - 1]);

        addLeavingConnectors(dataset, from, start, gwc, sgwc, true);
        addLeavingConnectors(dataset, from, end, gwc, sgwc, false);
    }

    private void addLeavingConnectors(DataSet dataset, WayEntity from, NodeEntity node, GraphWayComponent gwc,
                                      StreetGraphWayComponent sgwc, boolean isStart)
    {
        DirectedEdge edge = isStart ? sgwc.backward : sgwc.forward;
        if (edge == null) return;

        boolean ccw = config.crossingLogic.drivingOnTheRight;

        LaneConnectorSet connectors = new LaneConnectorSet();

        // add leaving connectors according to priority, incl. cyclic
        for (TargetEdge to : getConnectedEdgesSortedByPriority(dataset, from, node, gwc, sgwc, isStart, ccw))
            if (gwc.from.contains(new Connector(node, from, to.way)))
                addLeavingConnectors(connectors, edge, to);

        // add u-turn connectors
        if (gwc.uturn.contains(new Connector(node, from, from)))
            addUTurnConnectors(connectors, edge, isStart ? sgwc.forward : sgwc.backward);

        // add connectors to node
        Node gn = node.get(StreetGraphNodeComponent.class).node;
        for (LaneConnector c : connectors)
            gn.addConnector(c.from, c.to);
    }

    private void addLeavingConnectors(LaneConnectorSet connectors, DirectedEdge from, TargetEdge to) {
        if (to.angle <= Math.PI) {
            for (int i = 0, j = 0; i < from.getNumberOfLanes() && j < to.edge.getNumberOfLanes(); i++) {
                LaneConnector c = new LaneConnector(from.getLane(i), to.edge.getLane(j), to.angle);

                if (!connectors.collides(c)) {
                    connectors.add(c);
                    j++;
                }
            }
        } else {
            int nLanesFrom = from.getNumberOfLanes();
            int nLanesTo = to.edge.getNumberOfLanes();
            for (int i = 1, j = 1; i <= nLanesFrom && j <= nLanesTo; i++) {
                LaneConnector c = new LaneConnector(from.getLane(nLanesFrom - i), to.edge.getLane(nLanesTo - j), to.angle);

                if (!connectors.collides(c)) {
                    connectors.add(c);
                    j++;
                }
            }
        }
    }

    private void addUTurnConnectors(LaneConnectorSet connectors, DirectedEdge from, DirectedEdge to) {
        if (from == null || to == null) return;

        final double angle = Double.MAX_VALUE;

        int nLanesFrom = from.getNumberOfLanes();
        int nLanesDest = to.getNumberOfLanes();
        int nLanesMin = Math.min(nLanesFrom, nLanesDest);

        // inner-most connector is always possible
        connectors.add(new LaneConnector(from.getLane(nLanesFrom - 1), to.getLane(nLanesDest - 1), angle));

        for (int i = 2; i <= nLanesMin; i++) {
            LaneConnector c = new LaneConnector(from.getLane(nLanesFrom - i), to.getLane(nLanesDest - i), angle);

            if (connectors.collides(c))
                break;

            connectors.add(new LaneConnector(from.getLane(nLanesFrom - i), to.getLane(nLanesDest - i), angle));
        }
    }

    private ArrayList<TargetEdge> getConnectedEdgesSortedByPriority(DataSet dataset, WayEntity from, NodeEntity node,
                                                                    GraphWayComponent gwc, StreetGraphWayComponent sgwc,
                                                                    boolean isStart, boolean ccw)
    {
        GraphNodeComponent gnc = node.get(GraphNodeComponent.class);
        Vec2d reference = getDirectionVector(dataset, node, from);

        // get all connected edges (note: no if-else due to cyclic target ways, skip self edge)
        ArrayList<TargetEdge> leaving = new ArrayList<>();
        for (WayEntity to : gnc.ways) {
            if (to == from) continue;

            StreetGraphWayComponent sgwcto = to.get(StreetGraphWayComponent.class);

            if (to.nodes[0] == node.id && sgwcto.forward != null) {
                double angle = getVectorAngle(reference, getDirectionVector(dataset, to, true), ccw);
                leaving.add(new TargetEdge(to, sgwcto.forward, angle));
            }

            if (to.nodes[to.nodes.length - 1] == node.id && sgwcto.backward != null) {
                double angle = getVectorAngle(reference, getDirectionVector(dataset, to, false), ccw);
                leaving.add(new TargetEdge(to, sgwcto.backward, angle));
            }
        }

        // add self-cyclic edge
        if (isStart && gwc.cyclicStartToEnd && sgwc.backward != null) {
            double angle = getVectorAngle(reference, getDirectionVector(dataset, from, false), ccw);
            leaving.add(new TargetEdge(from, sgwc.backward, angle));
        } else if (!isStart && gwc.cyclicEndToStart && sgwc.forward != null) {
            double angle = getVectorAngle(reference, getDirectionVector(dataset, from, true), ccw);
            leaving.add(new TargetEdge(from, sgwc.forward, angle));
        }

        // sort leaving edges by priority and number of lanes, use id and direction as tie-breaker
        leaving.sort((a, b) -> {
            int cmp = Byte.compare(a.edge.getPriorityLevel(), b.edge.getPriorityLevel());
            if (cmp != 0) return cmp;

            cmp = Integer.compare(a.edge.getNumberOfLanes(), b.edge.getNumberOfLanes());
            if (cmp != 0) return cmp;

            cmp = Long.compare(a.edge.getId(), b.edge.getId());
            if (cmp != 0) return cmp;

            int da = a.edge.getEntity().getForwardEdge() == a.edge ? 1 : -1;
            int db = b.edge.getEntity().getForwardEdge() == b.edge ? 1 : -1;
            return Integer.compare(da, db);
        });

        return leaving;
    }

    private Vec2d getDirectionVector(DataSet dataset, NodeEntity from, WayEntity way) {
        NodeEntity to;

        if (from.id == way.nodes[0])
            to = dataset.nodes.get(way.nodes[1]);
        else
            to = dataset.nodes.get(way.nodes[way.nodes.length - 2]);

        return new Vec2d(to.lon - from.lon, to.lat - from.lat);
    }

    private Vec2d getDirectionVector(DataSet dataset, WayEntity way, boolean start) {
        NodeEntity from = start ? dataset.nodes.get(way.nodes[0]) : dataset.nodes.get(way.nodes[way.nodes.length - 1]);
        NodeEntity to = start ? dataset.nodes.get(way.nodes[1]) : dataset.nodes.get(way.nodes[way.nodes.length - 2]);

        return new Vec2d(to.lon - from.lon, to.lat - from.lat);
    }

    private double getVectorAngle(Vec2d a, Vec2d b, boolean ccw) {
        Vec2d na = Vec2d.normalize(a);
        Vec2d nb = Vec2d.normalize(b);

        double cross = na.cross(nb);
        if (cross == 0.0) {
            return Math.PI;
        } else {
            double inner = Math.acos(MathUtils.clamp(na.dot(nb), -1.0, 1.0));

            if (cross > 0.0)        // b left of a
                return ccw ? inner : (2 * Math.PI - inner);
            else                    // b right of a
                return ccw ? (2 * Math.PI - inner) : inner;
        }
    }


    /**
     * Calculates the length of the given {@code WayEntity} using this {@code
     * StreetGenerator}'s {@code DistanceCalculator}. The unit of the returned
     * value depends on the used {@code DistanceCalculator}.
     *
     * @param dataset the {@code DataSet} of which {@code way} is part of.
     * @param way     the {@code WayEntity} for which the length should be
     *                calculated.
     * @return the length of the given {@code WayEntity}.
     */
    private double getLength(DataSet dataset, WayEntity way) {
        NodeEntity node = dataset.nodes.get(way.nodes[0]);
        Coordinate a = new Coordinate(node.lat, node.lon);

        double length = 0;
        for (int i = 1; i < way.nodes.length; i++) {
            node = dataset.nodes.get(way.nodes[i]);
            Coordinate b = new Coordinate(node.lat, node.lon);

            length += distcalc.getDistance(a, b);
            a = b;
        }

        return length;
    }

    /**
     * Returns the StreetGraph-{@code Node} associated with the given {@code
     * NodeEntity} or creates a new one if it does not exist.
     *
     * @param entity the {@code NodeEntity} for which the {@code Node} should be
     *               returned.
     * @return the {@code Node} associated with the given {@code NodeEntity}.
     */
    private Node getNode(NodeEntity entity, CrossingLogicConfig config) {
        StreetGraphNodeComponent graphinfo = entity.get(StreetGraphNodeComponent.class);

        if (graphinfo == null) {
            Coordinate coord = new Coordinate(entity.lat, entity.lon);
            graphinfo = new StreetGraphNodeComponent(entity, new Node(entity.id, coord, config));
            entity.set(StreetGraphNodeComponent.class, graphinfo);
        }

        return graphinfo.node;
    }


    @Override
    public Set<Class<? extends Component>> getRequiredWayComponents() {
        HashSet<Class<? extends Component>> required = new HashSet<>();
        required.add(StreetComponent.class);
        required.add(SanitizerWayComponent.class);
        return required;
    }


    private static class TargetEdge {
        WayEntity way;
        DirectedEdge edge;
        double angle;

        TargetEdge(WayEntity way, DirectedEdge edge, double angle) {
            this.way = way;
            this.edge = edge;
            this.angle = angle;
        }
    }


    private static class LaneConnector {
        DirectedEdge.Lane from;
        DirectedEdge.Lane to;

        double angle;

        LaneConnector(DirectedEdge.Lane from, DirectedEdge.Lane to, double angle) {
            this.from = from;
            this.to = to;
            this.angle = angle;
        }

        boolean intersects(LaneConnector other) {
            if (this.to.getEdge() == other.to.getEdge() && this.to.getIndex() == other.to.getIndex()) {
                return true;
            }

            if (this.from.getIndex() < other.from.getIndex()) {
                if (this.to.getEdge() == other.to.getEdge()) {
                    if (this.to.getIndex() > other.to.getIndex()) {
                        return true;
                    }
                } else {
                    if (this.angle > other.angle) {
                        return true;
                    }
                }

            } else if (this.from.getIndex() > other.from.getIndex()) {
                if (this.to.getEdge() == other.to.getEdge()) {
                    if (this.to.getIndex() < other.to.getIndex()) {
                        return true;
                    }
                } else {
                    if (this.angle < other.angle) {
                        return true;
                    }
                }
            }

            return false;
        }
    }

    private static class LaneConnectorSet implements Iterable<LaneConnector> {
        private ArrayList<LaneConnector> connectors = new ArrayList<>();

        void add(LaneConnector c) {
            connectors.add(c);
        }

        boolean collides(LaneConnector c) {
            for (LaneConnector x : this)
                if (x.intersects(c))
                    return true;

            return false;
        }


        @Override
        public Iterator<LaneConnector> iterator() {
            return connectors.iterator();
        }
    }
}
