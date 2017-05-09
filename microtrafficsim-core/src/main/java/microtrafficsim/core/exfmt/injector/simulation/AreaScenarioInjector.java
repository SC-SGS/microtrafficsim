package microtrafficsim.core.exfmt.injector.simulation;

import microtrafficsim.core.exfmt.Container;
import microtrafficsim.core.exfmt.ExchangeFormat;
import microtrafficsim.core.exfmt.base.TypedPolygonAreaSet;
import microtrafficsim.core.exfmt.base.SimulationConfigInfo;
import microtrafficsim.core.exfmt.base.ScenarioMetaInfo;
import microtrafficsim.core.exfmt.base.ScenarioRouteInfo;
import microtrafficsim.core.logic.streetgraph.GraphGUID;
import microtrafficsim.core.logic.vehicles.machines.Vehicle;
import microtrafficsim.core.simulation.scenarios.impl.AreaScenario;


/**
 * @deprecated
 */
public class AreaScenarioInjector implements ExchangeFormat.Injector<AreaScenario> {

    /**
     *
     * @param fmt
     * @param ctx
     * @param dst
     * @param src all vehicles in this scenario needs full route-stacks, otherwise they are not fully stored
     * @throws Exception
     */
    @Override
    public void inject(ExchangeFormat fmt, ExchangeFormat.Context ctx, Container dst, AreaScenario src) throws Exception {
//        Config cfg = fmt.getConfig().getOr(Config.class, Config::getDefault);
//
//        // store meta information
//        ScenarioMetaInfo meta = new ScenarioMetaInfo();
//        meta.setGraphGUID(GraphGUID.from(src.getGraph()));
//        meta.setScenarioType(AreaScenario.class);
//        dst.set(meta);
//
//        // store config information
//        SimulationConfigInfo sconfig = new SimulationConfigInfo();
//        sconfig.set(src.getConfig());
//        dst.set(sconfig);
//
//        // store areas
//        TypedPolygonAreaSet areas = new TypedPolygonAreaSet();
//        areas.addAll(src.getAreas());
//        dst.set(areas);
//
//        // store routes
//        if (cfg.storeRoutes) {
//            ScenarioRouteInfo routes = new ScenarioRouteInfo();
//            for (Vehicle vehicle : src.getVehicleContainer()) {
//                routes.put(vehicle.getDriver().getRoute());
//            }
//            dst.set(routes);
//        }
    }

    public static class Config extends microtrafficsim.core.exfmt.Config.Entry {
        public boolean storeRoutes;

        public static Config getDefault() {
            Config cfg = new Config();
            cfg.storeRoutes = false;

            return cfg;
        }
    }
}
