package microtrafficsim.core.exfmt.extractor.scenario;

import microtrafficsim.core.exfmt.Container;
import microtrafficsim.core.exfmt.ExchangeFormat;
import microtrafficsim.core.exfmt.base.ScenarioAreaSet;
import microtrafficsim.core.exfmt.base.ScenarioConfigInfo;
import microtrafficsim.core.exfmt.base.ScenarioRouteSet;
import microtrafficsim.core.exfmt.exceptions.ExchangeFormatException;
import microtrafficsim.core.exfmt.exceptions.NotAvailableException;
import microtrafficsim.core.logic.streetgraph.Graph;
import microtrafficsim.core.map.area.polygons.TypedPolygonArea;
import microtrafficsim.core.simulation.configs.SimulationConfig;
import microtrafficsim.core.simulation.scenarios.impl.AreaScenario;


public class AreaScenarioExtractor implements ExchangeFormat.Extractor<AreaScenario> {

    @Override
    public AreaScenario extract(ExchangeFormat fmt, ExchangeFormat.Context ctx, Container src) throws Exception {
        Config cfg = fmt.getConfig().get(Config.class);
        if (cfg == null) throw new ExchangeFormatException("Config for AreaScenarioExtractor missing");

        // load scenario/simulation config
        ScenarioConfigInfo sconfig = src.get(ScenarioConfigInfo.class);
        if (sconfig == null) throw new NotAvailableException("ScenarioConfigInfo missing");
        // TODO: load scenario/simulation config

        AreaScenario scenario = new AreaScenario(cfg.seed, cfg.config, cfg.graph);

        // load areas
        ScenarioAreaSet areas = src.get(ScenarioAreaSet.class);
        if (areas == null) throw new NotAvailableException("ScenarioAreaSet missing");

        for (TypedPolygonArea area : areas.getAll())
            scenario.addArea(area);
        scenario.refillNodeLists();

        // load routes
        if (cfg.loadRoutes) {
            ScenarioRouteSet routes = src.get(ScenarioRouteSet.class);
            if (routes == null) throw new NotAvailableException("ScenarioRouteSet missing");

            // TODO: load routes
        }

        return scenario;
    }


    public static class Config extends microtrafficsim.core.exfmt.Config.Entry {
        public Graph graph;
        public long seed;
        public SimulationConfig config;
        public boolean loadRoutes;
    }
}
