package logic.determinism;

import microtrafficsim.core.convenience.DefaultParserConfig;
import microtrafficsim.core.logic.streetgraph.Graph;
import microtrafficsim.core.parser.OSMParser;
import microtrafficsim.core.simulation.builder.impl.VehicleScenarioBuilder;
import microtrafficsim.core.simulation.configs.ScenarioConfig;
import microtrafficsim.core.simulation.scenarios.Scenario;
import microtrafficsim.core.simulation.scenarios.impl.RandomRouteScenario;
import microtrafficsim.utils.logging.EasyMarkableLogger;
import microtrafficsim.utils.resources.PackagedResource;
import org.slf4j.Logger;

import java.io.File;

/**
 * @author Dominic Parga Cacheiro
 */
public class RandomScenarioDeterminismTest extends AbstractDeterminismTest {

    private static final Logger logger = new EasyMarkableLogger(RandomScenarioDeterminismTest.class);

    @Override
    protected Scenario createScenario() {

        /* setup config */
        ScenarioConfig config = new ScenarioConfig();


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
        Scenario scenario = new RandomRouteScenario(config.seed, config, graph);
        VehicleScenarioBuilder scenarioBuilder = new VehicleScenarioBuilder(config.seed);
        try {
            scenarioBuilder.prepare(scenario);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return scenario;
    }
}
