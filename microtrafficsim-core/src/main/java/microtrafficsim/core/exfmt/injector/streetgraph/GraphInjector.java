package microtrafficsim.core.exfmt.injector.streetgraph;

import microtrafficsim.core.exfmt.Container;
import microtrafficsim.core.exfmt.ExchangeFormat;
import microtrafficsim.core.exfmt.base.EntitySet;
import microtrafficsim.core.logic.nodes.Node;
import microtrafficsim.core.logic.streetgraph.Graph;
import microtrafficsim.core.logic.streets.DirectedEdge;


/*
 * NOTE: To properly inject a map segment and a street-graph, it is expected that the IDs of corresponding
 * MultiLine/Street features and DirectedEdges are equivalent.
 */
public class GraphInjector implements ExchangeFormat.Injector<Graph> {

    @Override
    public void inject(ExchangeFormat fmt, ExchangeFormat.Context ctx, Container dst, Graph src) throws Exception {
        EntitySet entities = dst.get(EntitySet.class, EntitySet::new);
        entities.updateBounds(src.getBounds());

        for (Node node : src.getNodes()) {
            fmt.inject(ctx, dst, node);
        }

        for (DirectedEdge edge : src.getEdges()) {
            fmt.inject(ctx, dst, edge);
        }
    }
}
