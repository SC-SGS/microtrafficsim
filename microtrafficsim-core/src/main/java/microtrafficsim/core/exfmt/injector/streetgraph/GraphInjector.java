package microtrafficsim.core.exfmt.injector.streetgraph;

import microtrafficsim.core.exfmt.Container;
import microtrafficsim.core.exfmt.ExchangeFormat;
import microtrafficsim.core.exfmt.base.GeometryEntitySet;
import microtrafficsim.core.logic.nodes.Node;
import microtrafficsim.core.logic.streetgraph.Graph;
import microtrafficsim.core.logic.streets.DirectedEdge;


/*
 * NOTE: To properly inject a map segment and a street-graph, it is expected that the IDs of corresponding
 * MultiLine/Street features and DirectedEdges are equivalent.
 */
public class GraphInjector implements ExchangeFormat.Injector<Graph> {

    /**
     * Takes the {@link GeometryEntitySet} from the given container {@code dst} and adds the given {@code graph's nodes} and
     * {@code edges} using the given {@code format}
     *
     * @param fmt
     * @param ctx
     * @param dst
     * @param src
     * @throws Exception
     */
    @Override
    public void inject(ExchangeFormat fmt, ExchangeFormat.Context ctx, Container dst, Graph src) throws Exception {
        GeometryEntitySet entities = dst.get(GeometryEntitySet.class, GeometryEntitySet::new);
        entities.updateBounds(src.getBounds());

        for (Node node : src.getNodes()) {
            fmt.inject(ctx, dst, node);
        }

        for (DirectedEdge edge : src.getEdges()) {
            fmt.inject(ctx, dst, edge);
        }
    }
}
