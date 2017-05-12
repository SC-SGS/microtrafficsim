package microtrafficsim.core.exfmt.extractor.simulation;

import microtrafficsim.core.exfmt.Container;
import microtrafficsim.core.exfmt.ExchangeFormat;
import microtrafficsim.core.exfmt.exceptions.ExchangeFormatException;
import microtrafficsim.core.logic.streetgraph.Graph;
import microtrafficsim.core.simulation.builder.RouteIsNotDefinedException;
import microtrafficsim.core.simulation.utils.RouteContainer;

/**
 * @author Dominic Parga Cacheiro
 */
public class RouteContainerExtractor implements ExchangeFormat.Extractor<RouteContainer> {

    @Override
    public RouteContainer extract(ExchangeFormat fmt, ExchangeFormat.Context ctx, Container src)
            throws RouteIsNotDefinedException, ExchangeFormatException {
//        Config cfg = fmt.getConfig().get(Config.class);
//        if (cfg == null) throw new ExchangeFormatException(
//                "Config for " + RouteContainerExtractor.class.getSimpleName() + " missing");
//
//
//        /* extract data */
//        ScenarioRouteInfo info = src.get(ScenarioRouteInfo.class);
//        if (info == null) throw new NotAvailableException(ScenarioRouteInfo.class.getSimpleName() + " missing");
//        RouteMatrix.Sparse sparseMatrix = new RouteMatrix.Sparse(info.getRoutes());
//        sparseMatrix.setGraphGUID(info.getGraphGUID());
//
//        /* create original route matrix referencing to the current graph */
//        return RouteMatrix.fromSparse(sparseMatrix, cfg.graph.getNodeMap(), cfg.graph.getEdgeMap());
        return null;
    }

    public static class Config extends microtrafficsim.core.exfmt.Config.Entry {
        public Graph graph;
    }
}
