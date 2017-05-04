package microtrafficsim.core.exfmt.extractor.simulation;

import microtrafficsim.core.exfmt.Container;
import microtrafficsim.core.exfmt.ExchangeFormat;
import microtrafficsim.core.exfmt.base.ScenarioRouteSet;
import microtrafficsim.core.exfmt.exceptions.ExchangeFormatException;
import microtrafficsim.core.exfmt.exceptions.NotAvailableException;
import microtrafficsim.core.logic.nodes.Node;
import microtrafficsim.core.logic.streetgraph.Graph;
import microtrafficsim.core.logic.streets.DirectedEdge;
import microtrafficsim.core.simulation.builder.ScenarioBuilder;
import microtrafficsim.core.simulation.configs.SimulationConfig;
import microtrafficsim.core.simulation.utils.RouteMatrix;
import microtrafficsim.utils.progressable.ProgressListener;

import java.util.HashMap;

/**
 * @author Dominic Parga Cacheiro
 */
public class RouteMatrixExtractor implements ExchangeFormat.Extractor<RouteMatrix> {

    @Override
    public RouteMatrix extract(ExchangeFormat fmt, ExchangeFormat.Context ctx, Container src) throws Exception {
        Config cfg = fmt.getConfig().get(Config.class);
        if (cfg == null) throw new ExchangeFormatException(
                "Config for " + RouteMatrixExtractor.class.getSimpleName() + " missing");


        /* extract data */
        ScenarioRouteSet routeIDs = src.get(ScenarioRouteSet.class);
        if (routeIDs == null) throw new NotAvailableException(ScenarioRouteSet.class.getSimpleName() + " missing");


        /* create node lexicon */
        HashMap<Long, Node> nodes = new HashMap<>();
        for (Node node : cfg.graph.getNodes())
            nodes.put(node.getId(), node);
        /* create edge lexicon */
        HashMap<Long, DirectedEdge> edges = new HashMap<>();
        for (DirectedEdge edge : cfg.graph.getEdges())
            edges.put(edge.getId(), edge);


        /* create original route matrix referencing to the current graph */
        return RouteMatrix.fromSparse(routeIDs.getAll(), nodes, edges);
    }

    public static class Config extends microtrafficsim.core.exfmt.Config.Entry {
        public Graph graph;
    }
}
