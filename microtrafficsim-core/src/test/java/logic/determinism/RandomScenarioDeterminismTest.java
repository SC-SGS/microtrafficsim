package logic.determinism;

import microtrafficsim.core.convenience.DefaultParserConfig;
import microtrafficsim.core.logic.streetgraph.Graph;
import microtrafficsim.core.parser.OSMParser;
import microtrafficsim.core.shortestpath.ShortestPathAlgorithm;
import microtrafficsim.core.shortestpath.astar.impl.LinearDistanceBidirectionalAStar;
import microtrafficsim.core.simulation.builder.impl.VehicleScenarioBuilder;
import microtrafficsim.core.simulation.configs.ScenarioConfig;
import microtrafficsim.core.simulation.scenarios.Scenario;
import microtrafficsim.core.simulation.scenarios.containers.VehicleContainer;
import microtrafficsim.core.simulation.scenarios.impl.RandomRouteScenario;
import microtrafficsim.math.random.distributions.impl.Random;
import microtrafficsim.utils.logging.EasyMarkableLogger;
import microtrafficsim.utils.resources.PackagedResource;
import org.slf4j.Logger;

import java.io.File;
import java.util.function.Supplier;

/**
 * @author Dominic Parga Cacheiro
 */
public class RandomScenarioDeterminismTest extends AbstractDeterminismTest {

    private static final Logger logger = new EasyMarkableLogger(RandomScenarioDeterminismTest.class);

    @Override
    protected Scenario createScenario() {

        /* setup config */
        ScenarioConfig config = new ScenarioConfig();
        // general
        config.speedup           = Integer.MAX_VALUE;
        config.seed              = new Random().getSeed();
        // crossing logic
        config.crossingLogic.drivingOnTheRight            = true;
        config.crossingLogic.edgePriorityEnabled          = true;
        config.crossingLogic.priorityToTheRightEnabled    = true;
        config.crossingLogic.friendlyStandingInJamEnabled = true;
        config.crossingLogic.setOnlyOneVehicle(true);
        // vehicles
        config.maxVehicleCount = 5000;
        // multithreading
        config.multiThreading.nThreads = 42;


        /* setup graph */
        Graph graph;
        try {
            File file = new PackagedResource(RandomScenarioDeterminismTest.class, "Backnang.osm").asTemporaryFile();
            OSMParser parser = DefaultParserConfig.get(config).build();
            graph = parser.parse(file).streetgraph;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

        logger.debug("\n" + graph);


        /* setup scenario */
        Scenario scenario = new EditedRandomRouteScenario(config.seed, config, graph);
        VehicleScenarioBuilder scenarioBuilder = new VehicleScenarioBuilder(config.seed);
        try {
            scenarioBuilder.prepare(scenario);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return scenario;
    }

    private class EditedRandomRouteScenario extends RandomRouteScenario {

        private ShortestPathAlgorithm scout = new LinearDistanceBidirectionalAStar(getConfig().metersPerCell);


        public EditedRandomRouteScenario(long seed, ScenarioConfig config, Graph graph) {
            super(seed, config, graph);
        }

        public EditedRandomRouteScenario(Random random, ScenarioConfig config, Graph graph) {
            super(random, config, graph);
        }

        public EditedRandomRouteScenario(long seed, ScenarioConfig config, Graph graph, VehicleContainer vehicleContainer) {
            super(seed, config, graph, vehicleContainer);
        }

        public EditedRandomRouteScenario(Random random, ScenarioConfig config, Graph graph, VehicleContainer vehicleContainer) {
            super(random, config, graph, vehicleContainer);
        }


        @Override
        public Supplier<ShortestPathAlgorithm> getScoutFactory() {
            return () -> scout;
        }
    }
}
