package microtrafficsim.core.exfmt.injector.streetgraph;

import microtrafficsim.core.exfmt.Container;
import microtrafficsim.core.exfmt.ExchangeFormat;
import microtrafficsim.core.exfmt.base.EntitySet;
import microtrafficsim.core.exfmt.ecs.components.GraphNodeComponent;
import microtrafficsim.core.exfmt.ecs.entities.PointEntity;
import microtrafficsim.core.logic.nodes.Node;
import microtrafficsim.core.logic.streets.DirectedEdge;
import microtrafficsim.core.logic.streets.Lane;

import java.util.ArrayList;
import java.util.Map;
import java.util.Set;


public class NodeInjector implements ExchangeFormat.Injector<Node> {

    @Override
    public void inject(ExchangeFormat fmt, ExchangeFormat.Context ctx, Container dst, Node src) throws Exception {
        EntitySet ecs = dst.get(EntitySet.class, EntitySet::new);
        PointEntity entity = ecs.getPoints().computeIfAbsent(src.getId(),
                k -> new PointEntity(src.getId(), src.getCoordinate()));

        GraphNodeComponent gnc = entity.get(GraphNodeComponent.class, () -> new GraphNodeComponent(entity));
        gnc.setCrossingLogicConfig(src.getCrossingLogicConfig());
        Set<Long> edges = gnc.getEdges();
        Set<GraphNodeComponent.Connector> connectors = gnc.getConnectors();

        // add edges
        for (DirectedEdge edge : src.getIncomingEdges())
            edges.add(edge.getId());

        for (DirectedEdge edge : src.getLeavingEdges())
            edges.add(edge.getId());

        // add connectors
        for (Map.Entry<Lane, ArrayList<Lane>> connector : src.getConnectors().entrySet()) {
            Lane fromLane = connector.getKey();
            ArrayList<Lane> connected = connector.getValue();
            DirectedEdge fromEdge = fromLane.getAssociatedEdge();

            for (Lane toLane : connected) {
                DirectedEdge toEdge = toLane.getAssociatedEdge();

                connectors.add(new GraphNodeComponent.Connector(
                        fromEdge.getId(), fromEdge.getEntity().getForwardEdge() == fromEdge, fromLane.getIndex(),
                        toEdge.getId(), toEdge.getEntity().getForwardEdge() == toEdge, toLane.getIndex()));
            }
        }
    }
}
