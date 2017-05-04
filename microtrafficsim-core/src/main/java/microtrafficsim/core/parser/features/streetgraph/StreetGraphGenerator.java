package microtrafficsim.core.parser.features.streetgraph;

import microtrafficsim.core.entities.street.StreetEntity;
import microtrafficsim.core.logic.nodes.Node;
import microtrafficsim.core.logic.streetgraph.Graph;
import microtrafficsim.core.logic.streetgraph.StreetGraph;
import microtrafficsim.core.logic.streets.DirectedEdge;
import microtrafficsim.core.map.Coordinate;
import microtrafficsim.core.parser.processing.Connector;
import microtrafficsim.core.parser.processing.GraphWayComponent;
import microtrafficsim.core.parser.processing.sanitizer.SanitizerWayComponent;
import microtrafficsim.core.simulation.configs.CrossingLogicConfig;
import microtrafficsim.core.simulation.configs.SimulationConfig;
import microtrafficsim.math.DistanceCalculator;
import microtrafficsim.math.HaversineDistanceCalculator;
import microtrafficsim.math.Vec2d;
import microtrafficsim.osm.parser.base.DataSet;
import microtrafficsim.osm.parser.ecs.Component;
import microtrafficsim.osm.parser.ecs.entities.NodeEntity;
import microtrafficsim.osm.parser.ecs.entities.WayEntity;
import microtrafficsim.osm.parser.features.FeatureDefinition;
import microtrafficsim.osm.parser.features.FeatureGenerator;
import microtrafficsim.osm.parser.features.streets.StreetComponent;
import microtrafficsim.osm.parser.features.streets.info.OnewayInfo;
import microtrafficsim.osm.parser.features.streets.info.StreetType;
import microtrafficsim.utils.logging.EasyMarkableLogger;
import org.slf4j.Logger;

import java.util.HashSet;
import java.util.Set;


/**
 * The {@code FeatureGenerator} for the StreetGraph used in the simulation.
 *
 * @author Dominic Parga Cacheiro, Maximilian Luz
 */
public class StreetGraphGenerator implements FeatureGenerator {
    private static Logger logger = new EasyMarkableLogger(StreetGraphGenerator.class);

    private SimulationConfig config;
    private Graph              graph;
    private DistanceCalculator distcalc;

    /**
     * Creates a new {@code StreetGraphGenerator} using the
     * {@link HaversineDistanceCalculator#getDistance(Coordinate, Coordinate)
     * HaversineDistanceCalculator } as {@code DistanceCalculator}.
     * <p>
     * This is equivalent to
     * {@link StreetGraphGenerator#StreetGraphGenerator(SimulationConfig, DistanceCalculator)
     * StreetGraphGenerator(config, HaversineDistanceCalculator::getDistance) }
     */
    public StreetGraphGenerator(SimulationConfig config) {
        this(config, HaversineDistanceCalculator::getDistance);
    }

    /**
     * Creates a new {@code StreetGraphGenerator} with the given
     * {@code DistanceCalculator}.
     *
     * @param distcalc the {@code DistanceCalculator} used to calculate the length of
     *                 streets.
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
        /*
         * STATUS: - all edges are added with only one lane and priority=false -
         * Lane-Connectors are generated directly from Street-Connectors (one to
         * one)
         */

        /*
         * TODO: basics now - tests for StreetGraph/StreetGraphGenerator
         */

        /*
         * TODO: basics future - geographical layout of streets (left/right of
         * ...) - multi-lane support
         */

        /*
         * TODO: nice to have - entfernen kleinerer zusammenhangskomponenten
         */

        logger.info("generating StreetGraph");
        this.graph = null;
        Graph graph = new StreetGraph(dataset.bounds);

        // create required nodes and edges
        for (WayEntity way : dataset.ways.values()) {
            if (!way.features.contains(feature)) continue;
            createAndAddEdges(dataset, graph, way, config);
        }

        // add turn-lanes
        for (WayEntity way : dataset.ways.values()) {
            if (!way.features.contains(feature)) continue;
            addConnectors(dataset, way);
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
        microtrafficsim.core.map.StreetType type= streetinfo.roundabout ? StreetType.ROUNDABOUT.toCoreStreetType() :
                streetinfo.streettype.toCoreStreetType();

        if (streetinfo.oneway == OnewayInfo.NO || streetinfo.oneway == OnewayInfo.FORWARD
            || streetinfo.oneway == OnewayInfo.REVERSIBLE) {
            Vec2d originDirection = new Vec2d(node1.lon - node0.lon, node1.lat - node0.lat);

            Vec2d destinationDirection = new Vec2d(lastNode.lon - secondLastNode.lon,
                                                   lastNode.lat - secondLastNode.lat);

            forward = new DirectedEdge(way.id, length, type, 1, streetinfo.maxspeed.forward, start, end,
                    originDirection, destinationDirection, config.metersPerCell, config.streetPriorityLevel);
        }

        if (streetinfo.oneway == OnewayInfo.NO || streetinfo.oneway == OnewayInfo.BACKWARD) {
            Vec2d originDirection = new Vec2d(secondLastNode.lon - lastNode.lon,
                                              secondLastNode.lat - lastNode.lat);

            Vec2d destinationDirection = new Vec2d(node0.lon - node1.lon, node0.lat - node1.lat);

            backward = new DirectedEdge(way.id, length, type, 1, streetinfo.maxspeed.backward, end, start,
                    originDirection, destinationDirection, config.metersPerCell, config.streetPriorityLevel);
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
     * @param wayFrom the {@code WayEntity} for which all outgoing lane-connectors
     *                should be created.
     */
    private void addConnectors(DataSet dataset, WayEntity wayFrom) {
        StreetGraphWayComponent sgwcFrom = wayFrom.get(StreetGraphWayComponent.class);
        if (sgwcFrom == null) return;
        if (sgwcFrom.forward == null && sgwcFrom.backward == null) return;

        Node sgNodeStart = dataset.nodes.get(wayFrom.nodes[0]).get(StreetGraphNodeComponent.class).node;
        Node sgNodeEnd
                = dataset.nodes.get(wayFrom.nodes[wayFrom.nodes.length - 1]).get(StreetGraphNodeComponent.class).node;

        GraphWayComponent gwcFrom = wayFrom.get(GraphWayComponent.class);

        // obj0-turns
        for (Connector c : gwcFrom.uturn) {
            if (sgwcFrom.forward == null || sgwcFrom.backward == null) continue;

            // XXX obj0-turns should probably have a special direction?
            if (c.via.id == wayFrom.nodes[0]) {
                sgNodeStart.addConnector(sgwcFrom.backward.getLane(0), sgwcFrom.forward.getLane(0));
            } else if (c.via.id == wayFrom.nodes[wayFrom.nodes.length - 1]) {
                sgNodeStart.addConnector(sgwcFrom.forward.getLane(0), sgwcFrom.backward.getLane(0));
            }
        }

        // leaving connectors (no 'else if' because of cyclic connectors)
        for (Connector c : gwcFrom.from) {
            StreetGraphWayComponent sgwcTo = c.to.get(StreetGraphWayComponent.class);
            if (sgwcTo == null) continue;

            // <---o--->
            if (c.via.id == wayFrom.nodes[0] && c.via.id == c.to.nodes[0]) {
                addEdgeConnectors(sgNodeStart, sgwcFrom.backward, sgwcTo.forward);
            }

            // <---o<---
            if (c.via.id == wayFrom.nodes[0] && c.via.id == c.to.nodes[c.to.nodes.length - 1]) {
                addEdgeConnectors(sgNodeStart, sgwcFrom.backward, sgwcTo.backward);
            }

            // --->o--->
            if (c.via.id == wayFrom.nodes[wayFrom.nodes.length - 1] && c.via.id == c.to.nodes[0]) {
                addEdgeConnectors(sgNodeEnd, sgwcFrom.forward, sgwcTo.forward);
            }

            // --->o<---
            if (c.via.id == wayFrom.nodes[wayFrom.nodes.length - 1] && c.via.id == c.to.nodes[c.to.nodes.length - 1]) {
                addEdgeConnectors(sgNodeEnd, sgwcFrom.forward, sgwcTo.backward);
            }
        }

        // cyclic connectors
        if (sgNodeStart == sgNodeEnd) {
            if (gwcFrom.cyclicEndToStart && sgwcFrom.forward != null)
                sgNodeStart.addConnector(sgwcFrom.forward.getLane(0), sgwcFrom.forward.getLane(0));

            if (gwcFrom.cyclicStartToEnd && sgwcFrom.backward != null)
                sgNodeStart.addConnector(sgwcFrom.backward.getLane(0), sgwcFrom.backward.getLane(0));
        }
    }

    /**
     * Adds all lane-connectors from {@code from} via {@code via} to {@code to}.
     *
     * @param via  the {@code Node} via which the generated connectors should go.
     * @param from the {@code DirectedEdge} from which the generated connectors
     *             should originate.
     * @param to   the {@code DirectedEdge} to which the generated connectors
     *             should lead.
     */
    private void addEdgeConnectors(Node via, DirectedEdge from, DirectedEdge to) {
        if (from == null || to == null) return;

        via.addConnector(from.getLane(0), to.getLane(0));
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
        Coordinate a    = new Coordinate(node.lat, node.lon);

        double length = 0;
        for (int i = 1; i < way.nodes.length; i++) {
            node         = dataset.nodes.get(way.nodes[i]);
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
            graphinfo = new StreetGraphNodeComponent(
                    entity,
                    new Node(entity.id, new Coordinate(entity.lat, entity.lon), config)
            );
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
}
