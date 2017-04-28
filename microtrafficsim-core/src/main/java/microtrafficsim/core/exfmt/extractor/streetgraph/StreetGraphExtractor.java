package microtrafficsim.core.exfmt.extractor.streetgraph;

import microtrafficsim.core.entities.street.StreetEntity;
import microtrafficsim.core.exfmt.Container;
import microtrafficsim.core.exfmt.ExchangeFormat;
import microtrafficsim.core.exfmt.base.EntitySet;
import microtrafficsim.core.exfmt.context.StreetFeatureMap;
import microtrafficsim.core.exfmt.ecs.FeatureManager;
import microtrafficsim.core.exfmt.ecs.components.GraphEdgeComponent;
import microtrafficsim.core.exfmt.ecs.components.GraphNodeComponent;
import microtrafficsim.core.exfmt.ecs.entities.LineEntity;
import microtrafficsim.core.exfmt.ecs.entities.PointEntity;
import microtrafficsim.core.exfmt.exceptions.ExchangeFormatException;
import microtrafficsim.core.exfmt.exceptions.NotAvailableException;
import microtrafficsim.core.logic.nodes.Node;
import microtrafficsim.core.logic.streetgraph.StreetGraph;
import microtrafficsim.core.logic.streets.DirectedEdge;
import microtrafficsim.core.map.StreetType;
import microtrafficsim.core.map.features.Street;
import microtrafficsim.core.simulation.configs.CrossingLogicConfig;
import microtrafficsim.core.simulation.configs.SimulationConfig;

import java.util.HashMap;
import java.util.function.Function;


// NOTE: The order of StreetGraph and feature extraction is important: First map features, then graph if no headless
// execution is wanted. This ensures that all StreetEntities get set up correctly. Alternatively the street-entities
// have to be re-created manually.
/**
 * @author Maximilian Luz
 */
public class StreetGraphExtractor implements ExchangeFormat.Extractor<StreetGraph> {

    @Override
    public StreetGraph extract(ExchangeFormat fmt, ExchangeFormat.Context ctx, Container src) throws Exception {
        EntitySet ecs = src.get(EntitySet.class);
        if (ecs == null) throw new NotAvailableException();

        Config cfg = fmt.getConfig().get(Config.class);
        if (cfg == null)
            throw new ExchangeFormatException("No Config for StreetGraphExtractor available");

        // set up graph
        StreetGraph graph = new StreetGraph(ecs.getBounds());
        HashMap<Long, Node> nodes = extractNodes(ecs, cfg);
        HashMap<Long, StreetEntity> edges = extractEdges(fmt, ctx, src, ecs, cfg, nodes, graph);
        setUpConnectors(ecs, nodes, edges);

        // finish graph
        graph.setSeed(cfg.seed);
        for (Node node : graph.getNodes()) {
            node.updateEdgeIndices();
        }
        graph.updateGraphGUID();


        return graph;
    }

    private HashMap<Long, Node> extractNodes(EntitySet ecs, Config cfg) {
        HashMap<Long, Node> nodes = new HashMap<>();
        for (PointEntity entity : ecs.getPoints().values()) {
            GraphNodeComponent gnc = entity.get(GraphNodeComponent.class);
            if (gnc == null) continue;

            Node node = new Node(entity.getId(), entity.getCoordinate(), cfg.crossingLogic);

            nodes.put(entity.getId(), node);
        }

        return nodes;
    }

    private HashMap<Long, StreetEntity> extractEdges(ExchangeFormat fmt, ExchangeFormat.Context ctx, Container src,
                                                     EntitySet ecs, Config cfg, HashMap<Long, Node> nodes,
                                                     StreetGraph graph)
    {
        HashMap<Long, StreetEntity> edges = new HashMap<>();
        StreetFeatureMap geoms = ctx.get(StreetFeatureMap.class);

        for (LineEntity entity : ecs.getLines().values()) {
            GraphEdgeComponent gec = entity.get(GraphEdgeComponent.class);
            if (gec == null) continue;

            Node origin = nodes.get(gec.getOrigin());
            Node destination = nodes.get(gec.getDestination());

            // get street geometry
            Street geom = null;
            if (geoms != null)
                geom = geoms.get(entity.getId());

            // we might have to extract the geometry if it is not contained in any street feature
            if (geom == null) {
                FeatureManager fmgr = fmt.getConfig().get(FeatureManager.class);
                geom = fmgr.getExtractor(Street.class).extract(fmt, ctx, src, ecs, entity);
            }

            // forward edge
            DirectedEdge forward = null;
            if (gec.getForwardLanes() > 0) {
                forward = new DirectedEdge(
                        entity.getId(),
                        gec.getLength(),
                        gec.getStreetType(),
                        gec.getForwardLanes(),
                        gec.getForwardMaxVelocity(),
                        origin,
                        destination,
                        gec.getOriginDirection(),
                        gec.getDestinationDirection(),
                        cfg.metersPerCell,
                        cfg.priorityFn
                );

                graph.addEdge(forward);
                origin.addLeavingEdge(forward);
                destination.addIncomingEdge(forward);
            }

            // backward edge
            DirectedEdge backward = null;
            if (gec.getBackwardLanes() > 0) {
                backward = new DirectedEdge(
                        entity.getId(),
                        gec.getLength(),
                        gec.getStreetType(),
                        gec.getBackwardLanes(),
                        gec.getBackwardMaxVelocity(),
                        destination,
                        origin,
                        gec.getDestinationDirection(),
                        gec.getOriginDirection(),
                        cfg.metersPerCell,
                        cfg.priorityFn
                );
            }

            // entity
            StreetEntity se = new StreetEntity(forward, backward, geom);
            if (forward != null)
                forward.setEntity(se);
            if (backward != null)
                backward.setEntity(se);
            if (geom != null)
                geom.setEntity(se);

            // add to graph
            if (forward != null || backward != null) {
                graph.addNode(origin);
                graph.addNode(destination);
            }

            if (forward != null) {
                graph.addEdge(forward);
                origin.addLeavingEdge(forward);
                destination.addIncomingEdge(forward);
            }

            if (backward != null) {
                graph.addEdge(backward);
                origin.addIncomingEdge(backward);
                destination.addLeavingEdge(backward);
            }

            edges.put(entity.getId(), se);
        }

        return edges;
    }

    private void setUpConnectors(EntitySet ecs, HashMap<Long, Node> nodes, HashMap<Long, StreetEntity> edges) {
        for (Node node : nodes.values()) {
            PointEntity entity = ecs.getPoints().get(node.getId());
            if (entity == null) continue;

            GraphNodeComponent gnc = entity.get(GraphNodeComponent.class);
            if (gnc == null) continue;

            for (GraphNodeComponent.Connector connector : gnc.getConnectors()) {
                DirectedEdge from = getFromEdge(edges.get(connector.fromEdge), entity.getId());
                DirectedEdge to   = getToEdge(edges.get(connector.toEdge), entity.getId());
                if (from == null || to == null) continue;

                node.addConnector(from.getLane(connector.fromLane), to.getLane(connector.toLane));
            }
        }
    }

    private DirectedEdge getFromEdge(StreetEntity entity, long node) {
        if (entity == null) return null;

        if (entity.getForwardEdge() != null) {
            DirectedEdge edge = (DirectedEdge) entity.getForwardEdge();

            if (edge.getDestination().getId() == node)
                return edge;
        }

        return (DirectedEdge) entity.getBackwardEdge();
    }

    private DirectedEdge getToEdge(StreetEntity entity, long node) {
        if (entity == null) return null;

        if (entity.getForwardEdge() != null) {
            DirectedEdge edge = (DirectedEdge) entity.getForwardEdge();

            if (edge.getOrigin().getId() == node)
                return edge;
        }

        return (DirectedEdge) entity.getBackwardEdge();
    }


    public static class Config extends microtrafficsim.core.exfmt.Config.Entry {
        public float metersPerCell;
        public Function<StreetType, Byte> priorityFn;
        public CrossingLogicConfig crossingLogic;
        public long seed;

        public Config(float metersPerCell, Function<StreetType, Byte> priorityFn, CrossingLogicConfig crossingLogic, long seed) {
            this.metersPerCell = metersPerCell;
            this.priorityFn = priorityFn;
            this.crossingLogic = crossingLogic;
            this.seed = seed;
        }

        public Config(SimulationConfig cfg) {
            this.metersPerCell = cfg.metersPerCell;
            this.priorityFn = cfg.streetPriorityLevel;
            this.crossingLogic = cfg.crossingLogic;
            this.seed = cfg.seed;
        }
    }
}
