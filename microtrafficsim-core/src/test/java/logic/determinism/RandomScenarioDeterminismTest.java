package logic.determinism;

import microtrafficsim.core.logic.streetgraph.Graph;
import microtrafficsim.core.simulation.configs.SimulationConfig;
import microtrafficsim.core.simulation.scenarios.Scenario;
import microtrafficsim.core.simulation.scenarios.impl.AreaScenario;
import microtrafficsim.core.simulation.scenarios.impl.RandomRouteScenario;
import microtrafficsim.utils.logging.EasyMarkableLogger;
import org.slf4j.Logger;

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
    protected Scenario createScenario(SimulationConfig config, Graph graph) {
        AreaScenario scenario = new RandomRouteScenario(config.seed, config, graph);
        scenario.redefineMetaRoutes();
        return scenario;
    }
}
