package microtrafficsim.core.exfmt.extractor.scenario;

import microtrafficsim.core.exfmt.Container;
import microtrafficsim.core.exfmt.ExchangeFormat;
import microtrafficsim.core.exfmt.base.ScenarioAreaSet;
import microtrafficsim.core.exfmt.base.ScenarioRouteSet;
import microtrafficsim.core.exfmt.base.SimulationConfigInfo;
import microtrafficsim.core.exfmt.exceptions.ExchangeFormatException;
import microtrafficsim.core.exfmt.exceptions.NotAvailableException;
import microtrafficsim.core.logic.Route;
import microtrafficsim.core.logic.nodes.Node;
import microtrafficsim.core.logic.streetgraph.Graph;
import microtrafficsim.core.logic.streets.DirectedEdge;
import microtrafficsim.core.map.area.polygons.TypedPolygonArea;
import microtrafficsim.core.shortestpath.ShortestPathEdge;
import microtrafficsim.core.simulation.builder.RouteIsNotDefinedException;
import microtrafficsim.core.simulation.builder.ScenarioBuilder;
import microtrafficsim.core.simulation.configs.SimulationConfig;
import microtrafficsim.core.simulation.scenarios.impl.AreaScenario;
import microtrafficsim.core.simulation.utils.RouteMatrix;
import microtrafficsim.interesting.progressable.ProgressListener;

import java.util.HashMap;


public class AreaScenarioExtractor implements ExchangeFormat.Extractor<AreaScenario> {

    @Override
    public AreaScenario extract(ExchangeFormat fmt, ExchangeFormat.Context ctx, Container src)
            throws ExchangeFormatException, RouteIsNotDefinedException, InterruptedException {
        Config cfg = fmt.getConfig().get(Config.class);
        if (cfg == null) throw new ExchangeFormatException("Config for AreaScenarioExtractor missing");

        // load scenario/simulation config
        SimulationConfigInfo sconfig = src.get(SimulationConfigInfo.class);
        if (sconfig == null) throw new NotAvailableException("ScenarioConfigInfo missing");
        // TODO: load scenario/simulation config
        cfg.config.update(sconfig.getConfig());

        AreaScenario scenario = new AreaScenario(cfg.config.seed, cfg.config, cfg.graph);

        // load areas
        ScenarioAreaSet areas = src.get(ScenarioAreaSet.class);
        if (areas == null) throw new NotAvailableException("ScenarioAreaSet missing");

        for (TypedPolygonArea area : areas.getAll())
            scenario.addArea(area);
        scenario.refillNodeLists();

        // load routes
        if (cfg.loadRoutes) {
            ScenarioRouteSet fakeRoutes = src.get(ScenarioRouteSet.class);
            if (fakeRoutes == null) throw new NotAvailableException("ScenarioRouteSet missing");

            // TODO: load routes
            /* create node lexicon */
            HashMap<Long, Node> nodes = new HashMap<>();
            HashMap<Long, DirectedEdge> edges = new HashMap<>();
            for (Node node : cfg.graph.getNodes())
                nodes.put(node.getId(), node);
            /* create edge lexicon */
            for (DirectedEdge edge : cfg.graph.getEdges())
                edges.put(edge.getId(), edge);

            /* if scenario does not fit to graph */
            boolean correctRoutes = true;
            /* create original route matrix referencing to the current graph */
            RouteMatrix matrix = new RouteMatrix();
            for (Node fakeOrigin : fakeRoutes.getAll().keySet()) {
                HashMap<Node, Route<Node>> fakeValue = fakeRoutes.getAll().get(fakeOrigin);

                Node origin = nodes.get(fakeOrigin.getId());
                if (origin == null) {
                    correctRoutes = false;
                    continue;
                }
                HashMap<Node, Route<Node>> value = new HashMap<>();

                for (Node fakeDestination : fakeValue.keySet()) {
                    Node destination = nodes.get(fakeDestination.getId());
                    if (destination == null) {
                        correctRoutes = false;
                        break;
                    }
                    Route<Node> route = new Route<>(origin, destination);
                    for (ShortestPathEdge<Node> fakeSPEdge : fakeValue.get(fakeOrigin)) {
                        DirectedEdge fakeEdge = (DirectedEdge) fakeSPEdge;
                        DirectedEdge edge = edges.get(fakeEdge.getId());
                        if (edge == null) {
                            correctRoutes = false;
                            break;
                        }
                        route.add(edge);
                    }

                    value.put(destination, route);
                }

                if (!value.isEmpty())
                    matrix.put(origin, value);
            }

            if (!correctRoutes)
                throw new RouteIsNotDefinedException("Some routes could not be assigned due to wrong graph.");

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
