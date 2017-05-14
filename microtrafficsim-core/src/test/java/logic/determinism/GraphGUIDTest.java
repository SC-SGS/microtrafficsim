package logic.determinism;

import microtrafficsim.core.convenience.exfmt.ExfmtStorage;
import microtrafficsim.core.convenience.mapviewer.TileBasedMapViewer;
import microtrafficsim.core.logic.streetgraph.Graph;
import microtrafficsim.core.logic.streetgraph.GraphGUID;
import microtrafficsim.core.map.tiles.QuadTreeTilingScheme;
import microtrafficsim.core.simulation.configs.SimulationConfig;
import microtrafficsim.core.vis.map.projections.MercatorProjection;
import microtrafficsim.math.random.distributions.impl.Random;
import microtrafficsim.utils.logging.EasyMarkableLogger;
import microtrafficsim.utils.resources.PackagedResource;
import org.junit.Test;
import org.slf4j.Logger;
import testhelper.ResourceClassLinks;

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.assertEquals;

/**
 * @author Dominic Parga Cacheiro
 */
public class GraphGUIDTest {

    public static final Logger logger = new EasyMarkableLogger(GraphGUIDTest.class);

    private final int REPETITIONS = 1000;
    private Graph streetgraph;


    public GraphGUIDTest() {
        SimulationConfig config = new SimulationConfig();
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


        ExfmtStorage exfmtStorage = new ExfmtStorage(
                config,
                new QuadTreeTilingScheme(new MercatorProjection()),
                TileBasedMapViewer.DEFAULT_TILEGRID_LEVEL);


        try {
            File file = new PackagedResource(
                    GraphGUIDTest.class,
                    ResourceClassLinks.BACKNANG_MAP_PATH).asTemporaryFile();
            streetgraph = exfmtStorage.loadMap(file).obj0;
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }


        if (streetgraph == null)
            throw new NullPointerException("StreetGraph is null");
    }


    @Test
    public void testDeterminism() throws Exception {
        GraphGUID expected = streetgraph.getGUID();

        for (int i = 0; i < REPETITIONS; i++) {
            GraphGUID actual = GraphGUID.from(streetgraph);
            assertEquals("Unequal GUID; correct until now: " + i, expected, actual);
        }
    }
}
