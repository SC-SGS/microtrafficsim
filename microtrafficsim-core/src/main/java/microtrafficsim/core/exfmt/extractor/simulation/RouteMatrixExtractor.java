package microtrafficsim.core.exfmt.extractor.simulation;

import microtrafficsim.core.exfmt.Container;
import microtrafficsim.core.exfmt.ExchangeFormat;
import microtrafficsim.core.exfmt.base.ScenarioRouteInfo;
import microtrafficsim.core.exfmt.exceptions.ExchangeFormatException;
import microtrafficsim.core.exfmt.exceptions.NotAvailableException;
import microtrafficsim.core.logic.streetgraph.Graph;
import microtrafficsim.core.simulation.utils.RouteMatrix;

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
        ScenarioRouteInfo info = src.get(ScenarioRouteInfo.class);
        if (info == null) throw new NotAvailableException(ScenarioRouteInfo.class.getSimpleName() + " missing");
        RouteMatrix.Sparse sparseMatrix = info.getRoutes();
        sparseMatrix.setGraphGUID(info.getGraphGUID());

        /* create original route matrix referencing to the current graph */
        return RouteMatrix.fromSparse(sparseMatrix, cfg.graph.getNodeMap(), cfg.graph.getEdgeMap());
    }

    public static class Config extends microtrafficsim.core.exfmt.Config.Entry {
        public Graph graph;
    }
}
