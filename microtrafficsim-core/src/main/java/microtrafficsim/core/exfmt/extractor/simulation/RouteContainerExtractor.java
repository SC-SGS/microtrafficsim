package microtrafficsim.core.exfmt.extractor.simulation;

import microtrafficsim.core.exfmt.Container;
import microtrafficsim.core.exfmt.ExchangeFormat;
import microtrafficsim.core.exfmt.base.ScenarioRouteInfo;
import microtrafficsim.core.exfmt.exceptions.ExchangeFormatException;
import microtrafficsim.core.exfmt.exceptions.NotAvailableException;
import microtrafficsim.core.logic.streetgraph.Graph;
import microtrafficsim.core.logic.streetgraph.GraphGUID;
import microtrafficsim.core.simulation.builder.RouteIsNotDefinedException;
import microtrafficsim.core.simulation.utils.RouteContainer;

/**
 * @author Dominic Parga Cacheiro
 */
public class RouteContainerExtractor implements ExchangeFormat.Extractor<RouteContainer> {
    @Override
    public RouteContainer extract(ExchangeFormat fmt, ExchangeFormat.Context ctx, Container src)
            throws RouteIsNotDefinedException, ExchangeFormatException {
        Config cfg = fmt.getConfig().get(Config.class);
        if (cfg == null) throw new ExchangeFormatException(
                "Config for " + getClass().getSimpleName() + " missing");

        /* extract data */
        ScenarioRouteInfo info = src.get(ScenarioRouteInfo.class);
        if (info == null) throw new NotAvailableException(ScenarioRouteInfo.class.getSimpleName() + " missing");

        cfg.loadedGraphGUID = info.getGraphGUID();
        return info.toRouteContainer(cfg.graph);
    }


    public static class Config extends microtrafficsim.core.exfmt.Config.Entry {
        private Graph graph;
        private GraphGUID loadedGraphGUID;

        public void setGraph(Graph graph) {
            this.graph = graph;
        }

        public GraphGUID getLoadedGraphGUID() {
            return loadedGraphGUID;
        }
    }
}