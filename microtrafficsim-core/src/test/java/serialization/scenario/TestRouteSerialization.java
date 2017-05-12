package serialization.scenario;

import microtrafficsim.core.convenience.exfmt.ExfmtStorage;
import microtrafficsim.core.convenience.filechoosing.MTSFileChooser;
import microtrafficsim.core.convenience.mapviewer.TileBasedMapViewer;
import microtrafficsim.core.logic.nodes.Node;
import microtrafficsim.core.logic.routes.Route;
import microtrafficsim.core.logic.streetgraph.Graph;
import microtrafficsim.core.map.tiles.QuadTreeTilingScheme;
import microtrafficsim.core.simulation.builder.impl.VehicleScenarioBuilder;
import microtrafficsim.core.simulation.configs.SimulationConfig;
import microtrafficsim.core.simulation.core.Simulation;
import microtrafficsim.core.simulation.core.impl.VehicleSimulation;
import microtrafficsim.core.simulation.scenarios.impl.AreaScenario;
import microtrafficsim.core.simulation.scenarios.impl.RandomRouteScenario;
import microtrafficsim.core.simulation.utils.RouteContainer;
import microtrafficsim.core.simulation.utils.SortedRouteContainer;
import microtrafficsim.core.vis.map.projections.MercatorProjection;
import microtrafficsim.utils.logging.EasyMarkableLogger;
import microtrafficsim.utils.logging.LoggingLevel;
import microtrafficsim.utils.resources.PackagedResource;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import testhelper.ResourceClassLinks;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * @author Dominic Parga Cacheiro
 */
public class TestRouteSerialization {
    public static final Logger logger = new EasyMarkableLogger(TestRouteSerialization.class);


    private SimulationConfig config;
    private ExfmtStorage exfmtStorage;
    private Simulation simulation;
    private Graph streetgraph;


    public TestRouteSerialization() {
        config = new SimulationConfig();
        // general
        config.speedup = Integer.MAX_VALUE;
        config.seed    = 42; // todo new Random().getSeed();
        // crossing logic
        config.crossingLogic.drivingOnTheRight            = true;
        config.crossingLogic.edgePriorityEnabled          = true;
        config.crossingLogic.priorityToTheRightEnabled    = true;
        config.crossingLogic.friendlyStandingInJamEnabled = true;
        config.crossingLogic.onlyOneVehicleEnabled        = false;
        // vehicles
        config.maxVehicleCount = 10;
        // multithreading
        config.multiThreading.nThreads = 42;
        logger.info("config created with seed = " + config.seed);


        exfmtStorage = new ExfmtStorage(
                config,
                new QuadTreeTilingScheme(new MercatorProjection()),
                TileBasedMapViewer.DEFAULT_TILEGRID_LEVEL);


        try {
            File file = new PackagedResource(TestRouteSerialization.class,
                    ResourceClassLinks.BACKNANG_MAP_PATH).asTemporaryFile();
            streetgraph = exfmtStorage.loadMap(file).obj0;
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }


        simulation = new VehicleSimulation();


        if (streetgraph == null)
            throw new NullPointerException("StreetGraph is null");
    }


    @Test
    public void testAreaScenarioRoutes() throws IOException {
        AreaScenario scenario = new AreaScenario(config.seed, config, streetgraph);
        scenario.redefineMetaRoutes();
        testAreaScenario(scenario);
    }

    @Test
    public void testRandomRouteScenarioRoutes() throws IOException {
        testAreaScenario(new RandomRouteScenario(config.seed, config, streetgraph));
    }


    private void testAreaScenario(AreaScenario scenario) throws IOException {
        /* prepare scenario */
        VehicleScenarioBuilder scenarioBuilder = new VehicleScenarioBuilder(config.seed);
        try {
            scenarioBuilder.prepare(scenario);
        } catch (Exception e) {
            e.printStackTrace();
        }
        simulation.setAndInitPreparedScenario(scenario);


        /* remember routes */
        RouteContainer rm = new SortedRouteContainer();
        rm.addAll(simulation.getScenario());
        assertFalse("Original route matrix is already empty", rm.isEmpty());


        /* save and reload routes */
        File tmp = File.createTempFile("routes", MTSFileChooser.Filters.ROUTE_POSTFIX);
        exfmtStorage.saveRoutes(tmp, rm, scenario.getAreaNodeContainer().getAreas());
        RouteContainer loaded = exfmtStorage.loadRoutes(tmp, streetgraph).obj0;


        /* assert basic attributes */ // todo
//        assertEquals("GraphGUID is not identical", rm.getGraphGUID(), loaded.getGraphGUID());
        assertEquals("Different size", rm.size(), loaded.size());

        // todo
        assertTrue(false);
//        Iterator<Node> iterKeys = rm.keySet().iterator();
//        Iterator<Node> iterLoadedKeys = loaded.keySet().iterator();
//        assertTrue("Any route should be stored", iterKeys.hasNext() && iterLoadedKeys.hasNext());
//        while (iterKeys.hasNext()) {
//            /* assert keysets <=> origins */
//            Node origin = iterKeys.next();
//            Node loadedOrigin = iterLoadedKeys.next();
//            assertEquals(
//                    "Key-value-pairs are not equally ordered\n" +
//                            "expected origin node: " + origin + "\n" +
//                            "actual origin node:   " + loadedOrigin,
//                    origin.hashCode(), loadedOrigin.hashCode());
//
//
//            /* assert values <=> (destination -> route) */
//            Map<Node, Route> values = rm.get(origin);
//            Map<Node, Route> loadedValues = loaded.get(loadedOrigin);
//
//
//            Iterator<Node> iterValues = values.keySet().iterator();
//            Iterator<Node> iterLoadedValues = loadedValues.keySet().iterator();
//            assertTrue("Any origin should map to a destination-route-pair",
//                    iterValues.hasNext() && iterLoadedValues.hasNext());
//            while (iterValues.hasNext()) {
//                /* assert keysets of values <=> destinations */
//                Node destination = iterValues.next();
//                Node loadedDestination = iterLoadedValues.next();
//                assertEquals(
//                        "Key-value-pairs are not equally ordered\n" +
//                                "expected destination node: " + destination + "\n" +
//                                "actual destination node:   " + loadedDestination,
//                        destination.hashCode(), loadedDestination.hashCode());
//
//
//                /* assert routes */
//                Route route = values.get(destination);
//                Route loadedRoute = loadedValues.get(loadedDestination);
//                assertEquals("stored route is not as expected", route.hashCode(), loadedRoute.hashCode());
//            }
//
//            assertTrue(!iterValues.hasNext() && !iterLoadedValues.hasNext());
//        }
//
//        assertTrue(!iterKeys.hasNext() && !iterLoadedKeys.hasNext());
    }


    @BeforeClass
    public static void buildSetup() {
        LoggingLevel.setEnabledGlobally(false, false, false, false, false);
    }
}
