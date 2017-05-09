package microtrafficsim.core.exfmt.extractor.simulation;

import microtrafficsim.core.exfmt.Container;
import microtrafficsim.core.exfmt.ExchangeFormat;
import microtrafficsim.core.exfmt.base.ScenarioRouteInfo;
import microtrafficsim.core.exfmt.base.SimulationConfigInfo;
import microtrafficsim.core.exfmt.base.TypedPolygonAreaSet;
import microtrafficsim.core.exfmt.exceptions.ExchangeFormatException;
import microtrafficsim.core.exfmt.exceptions.NotAvailableException;
import microtrafficsim.core.logic.nodes.Node;
import microtrafficsim.core.logic.streetgraph.Graph;
import microtrafficsim.core.logic.streets.DirectedEdge;
import microtrafficsim.core.map.area.polygons.TypedPolygonArea;
import microtrafficsim.core.simulation.builder.RouteIsNotDefinedException;
import microtrafficsim.core.simulation.builder.ScenarioBuilder;
import microtrafficsim.core.simulation.configs.SimulationConfig;
import microtrafficsim.core.simulation.scenarios.impl.AreaScenario;
import microtrafficsim.core.simulation.utils.RouteMatrix;
import microtrafficsim.utils.progressable.ProgressListener;

import java.util.HashMap;


/**
 * @deprecated
 */
public class AreaScenarioExtractor implements ExchangeFormat.Extractor<AreaScenario> {

    @Override
    public AreaScenario extract(ExchangeFormat fmt, ExchangeFormat.Context ctx, Container src)
            throws ExchangeFormatException, RouteIsNotDefinedException, InterruptedException {
        Config cfg = fmt.getConfig().get(Config.class);
        if (cfg == null) throw new ExchangeFormatException("Config for AreaScenarioExtractor missing");

        // load scenario/simulation config
        SimulationConfigInfo sconfig = src.get(SimulationConfigInfo.class);
        if (sconfig == null) throw new NotAvailableException("SimulationConfigInfo missing");
        sconfig.update(cfg.config);

        AreaScenario scenario = new AreaScenario(cfg.config.seed, cfg.config, cfg.graph);

        // load areas
        TypedPolygonAreaSet areas = src.get(TypedPolygonAreaSet.class);
        if (areas == null) throw new NotAvailableException("ScenarioAreaSet missing");

        for (TypedPolygonArea area : areas.getAll())
            scenario.addArea(area);
        scenario.refillNodeLists();

        // load routes
        if (cfg.loadRoutes) {
            ScenarioRouteInfo routeIDs = src.get(ScenarioRouteInfo.class);
            if (routeIDs == null) throw new NotAvailableException("ScenarioRouteSet missing");

            /* create node lexicon */
            HashMap<Long, Node> nodes = new HashMap<>();
            for (Node node : cfg.graph.getNodes())
                nodes.put(node.getId(), node);
            /* create edge lexicon */
            HashMap<Long, DirectedEdge> edges = new HashMap<>();
            for (DirectedEdge edge : cfg.graph.getEdges())
                edges.put(edge.getId(), edge);

            /* create original route matrix referencing to the current graph */
            RouteMatrix matrix = RouteMatrix.fromSparse(routeIDs.getRoutes(), nodes, edges);

            cfg.scenarioBuilder.prepare(scenario, matrix, cfg.progressListener);
        }

        return scenario;
    }


    public static class Config extends microtrafficsim.core.exfmt.Config.Entry {
        public boolean loadRoutes;

        public Graph graph;
        public SimulationConfig config;

        public ScenarioBuilder scenarioBuilder;
        public ProgressListener progressListener;
    }
}
