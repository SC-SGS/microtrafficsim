package serialization.scenario;

import microtrafficsim.core.convenience.exfmt.ExfmtStorage;
import microtrafficsim.core.convenience.filechoosing.MTSFileChooser;
import microtrafficsim.core.convenience.mapviewer.TileBasedMapViewer;
import microtrafficsim.core.logic.nodes.Node;
import microtrafficsim.core.logic.streetgraph.Graph;
import microtrafficsim.core.map.tiles.QuadTreeTilingScheme;
import microtrafficsim.core.simulation.builder.impl.VehicleScenarioBuilder;
import microtrafficsim.core.simulation.configs.SimulationConfig;
import microtrafficsim.core.simulation.core.Simulation;
import microtrafficsim.core.simulation.core.impl.VehicleSimulation;
import microtrafficsim.core.simulation.scenarios.impl.AreaScenario;
import microtrafficsim.core.simulation.scenarios.impl.RandomRouteScenario;
import microtrafficsim.core.simulation.utils.RouteMatrix;
import microtrafficsim.core.vis.map.projections.MercatorProjection;
import microtrafficsim.math.random.distributions.impl.Random;
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
public class RouteExfmtTest {
    // todo
    public static final Logger logger = new EasyMarkableLogger(RouteExfmtTest.class);


    private SimulationConfig config;
    private ExfmtStorage exfmtStorage;
    private Simulation simulation;
    private Graph streetgraph;


    public RouteExfmtTest() {
        config = new SimulationConfig();
        // general
        config.speedup = Integer.MAX_VALUE;
        config.seed    = new Random().getSeed();
        // crossing logic
        config.crossingLogic.drivingOnTheRight            = true;
        config.crossingLogic.edgePriorityEnabled          = true;
        config.crossingLogic.priorityToTheRightEnabled    = true;
        config.crossingLogic.friendlyStandingInJamEnabled = true;
        config.crossingLogic.onlyOneVehicleEnabled        = false;
        // vehicles
        config.maxVehicleCount = 4000;
        // multithreading
        config.multiThreading.nThreads = 42;
        logger.info("config created with seed = " + config.seed);


        exfmtStorage = new ExfmtStorage(
                config,
                new QuadTreeTilingScheme(new MercatorProjection()),
                TileBasedMapViewer.DEFAULT_TILEGRID_LEVEL);


        try {
            File file = new PackagedResource(RouteExfmtTest.class,
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
    public void testRouteIdentity() throws Exception {
        AreaScenario scenario = new RandomRouteScenario(config.seed, config, streetgraph);


        /* prepare scenario */
        VehicleScenarioBuilder scenarioBuilder = new VehicleScenarioBuilder(config.seed);
        try {
            scenarioBuilder.prepare(scenario);
        } catch (Exception e) {
            e.printStackTrace();
        }
        simulation.setAndInitPreparedScenario(scenario);


        /* remember routes */
        RouteMatrix rm = new RouteMatrix(streetgraph.getGUID());
        rm.addAll(scenario);
        assertFalse("Original route matrix is already empty", rm.isEmpty());


        /* save and reload routes */
        File tmp = File.createTempFile("routes", MTSFileChooser.Filters.ROUTE_POSTFIX);
        exfmtStorage.saveRoutes(tmp, rm, scenario.getAreas());
        RouteMatrix loaded = exfmtStorage.loadRoutes(tmp, streetgraph).obj0;


        /* assertion */
        assertEquals("GraphGUID is not identical", rm.getGraphGUID(), loaded.getGraphGUID());
        assertEquals("Different size", rm.size(), loaded.size());
        assertTrue("KeySets does not contain same elements", rm.keySet().containsAll(loaded.keySet()));
        assertTrue("Values does not contain same elements", rm.values().containsAll(loaded.values()));


        Iterator<Node> iterKeys = rm.keySet().iterator();
        Iterator<Node> iterLoadedKeys = loaded.keySet().iterator();
        while (iterKeys.hasNext()) {
            Node key = iterKeys.next();
            Node loadedKey = iterLoadedKeys.next();
            assertEquals( // todo ERROR?!?!?!?!
                    "Keys are not equal\n" + "expected: " + key + "\nactual:   " + loadedKey,
                    key.hashCode(), loadedKey.hashCode());
        }
        // todo
    }


    @BeforeClass
    public static void buildSetup() {
        LoggingLevel.setEnabledGlobally(false, false, false, false, false);
    }
}
