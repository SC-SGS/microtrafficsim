package logic.determinism;

import microtrafficsim.core.convenience.DefaultParserConfig;
import microtrafficsim.core.logic.streetgraph.Graph;
import microtrafficsim.core.parser.OSMParser;
import microtrafficsim.core.simulation.builder.impl.VehicleScenarioBuilder;
import microtrafficsim.core.simulation.configs.ScenarioConfig;
import microtrafficsim.core.simulation.scenarios.Scenario;
import microtrafficsim.core.simulation.scenarios.impl.RandomRouteScenario;
import microtrafficsim.math.random.distributions.impl.Random;
import microtrafficsim.utils.logging.EasyMarkableLogger;
import microtrafficsim.utils.resources.PackagedResource;
import org.slf4j.Logger;

import java.io.File;

/**
 * <p>
 * Tests {@link RandomRouteScenario}. For detailed information about the test itself, see superclass.
 *
 * <p>
 * &bull {@code Number of runs = } {@value simulationRuns}<br>
 * &bull {@code Number of steps = } {@value maxStep}<br>
 * &bull {@code Number of checks = } {@value checks}
 *
 * @author Dominic Parga Cacheiro
 */
public class RandomScenarioDeterminismTest extends AbstractDeterminismTest {

    private static final Logger logger = new EasyMarkableLogger(RandomScenarioDeterminismTest.class);


    /* testing parameters */
    private static final int checks = 10;
    private static final int maxStep = 3000;
    private static final int simulationRuns = 3;


    @Override
    protected int getChecks() {
        return checks;
    }

    @Override
    protected int getMaxStep() {
        return maxStep;
    }

    @Override
    protected int getSimulationRuns() {
        return simulationRuns;
    }

    @Override
    protected ScenarioConfig createConfig() {

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
        config.maxVehicleCount = 4000;
        // multithreading
        config.multiThreading.nThreads = 42;

        return config;
    }

    @Override
    protected Graph createGraph(ScenarioConfig config) {

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

        return graph;
    }

    @Override
    protected Scenario createScenario(ScenarioConfig config, Graph graph) {
        return new RandomRouteScenario(config.seed, config, graph);
    }

    @Override
    protected Scenario prepareScenario(ScenarioConfig config, Scenario scenario) {

        VehicleScenarioBuilder scenarioBuilder = new VehicleScenarioBuilder(config.seed);

        try {
            scenarioBuilder.prepare(scenario);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return scenario;
    }
}
