package logic.determinism;

import microtrafficsim.core.convenience.DefaultParserConfig;
import microtrafficsim.core.logic.streetgraph.Graph;
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
            File file = new PackagedResource(RandomScenarioDeterminismTest.class, "Stuttgart.osm").asTemporaryFile();
            graph = DefaultParserConfig.get(config).build().parse(file).streetgraph;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

        logger.debug("\n" + graph);


        return new RandomRouteScenario(config.seed, config, graph);
    }
}
