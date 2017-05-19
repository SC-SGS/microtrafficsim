package serialization.scenario;

import microtrafficsim.core.convenience.exfmt.ExfmtStorage;
import microtrafficsim.core.convenience.filechoosing.MTSFileChooser;
import microtrafficsim.core.convenience.mapviewer.TileBasedMapViewer;
import microtrafficsim.core.logic.routes.Route;
import microtrafficsim.core.logic.streetgraph.Graph;
import microtrafficsim.core.logic.streetgraph.GraphGUID;
import microtrafficsim.core.map.UnprojectedAreas;
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
import microtrafficsim.utils.collections.Triple;
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
        AreaScenario scenario = new RandomRouteScenario(config.seed, config, streetgraph);
        scenario.redefineMetaRoutes();
        testAreaScenario(scenario);
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
        RouteContainer routeContainer = new SortedRouteContainer();
        routeContainer.addAll(simulation.getScenario());
        assertFalse("Original route container is already empty", routeContainer.isEmpty());


        /* save and reload routes */
        File tmp = File.createTempFile("routes", MTSFileChooser.Filters.ROUTE_POSTFIX);
        exfmtStorage.saveRoutes(tmp, streetgraph.getGUID(), routeContainer, scenario.getAreaNodeContainer().getAreas());
        Triple<GraphGUID, RouteContainer, UnprojectedAreas> result = exfmtStorage.loadRoutes(tmp, streetgraph);
        RouteContainer loaded = result.obj1;


        /* assert basic attributes */
        assertEquals("GraphGUID is not identical", streetgraph.getGUID(), result.obj0);
        assertEquals("Different size", routeContainer.size(), loaded.size());

        // todo
        Iterator<Route> iterRoutes = routeContainer.iterator();
        Iterator<Route> iterLoaded = loaded.iterator();
        assertTrue("Any route should be stored", iterRoutes.hasNext() && iterLoaded.hasNext());
        while (iterRoutes.hasNext()) {
            assertTrue(
                    "Loaded-routes-iterator is empty before original one does.",
                    iterRoutes.hasNext() && iterLoaded.hasNext());

            Route route = iterRoutes.next();
            Route loadedRoute = iterLoaded.next();
            assertEquals("Routes are not equal.", route.hashCode(), loadedRoute.hashCode());
        }

        assertTrue(
                "Loaded-routes-iterator has remaining elements, but original one doesn't.",
                !iterRoutes.hasNext() && !iterLoaded.hasNext());
    }


    @BeforeClass
    public static void buildSetup() {
        LoggingLevel.setEnabledGlobally(false, false, false, false, false);
    }
}
