package logic.determinism;

import microtrafficsim.core.convenience.parser.DefaultParserConfig;
import microtrafficsim.core.logic.streetgraph.Graph;
import microtrafficsim.core.parser.OSMParser;
import microtrafficsim.core.simulation.builder.impl.VehicleScenarioBuilder;
import microtrafficsim.core.simulation.configs.SimulationConfig;
import microtrafficsim.core.simulation.scenarios.Scenario;
import microtrafficsim.core.simulation.scenarios.impl.AreaScenario;
import microtrafficsim.math.random.distributions.impl.Random;
import microtrafficsim.utils.logging.EasyMarkableLogger;
import microtrafficsim.utils.resources.PackagedResource;
import org.slf4j.Logger;

import java.io.File;

/**
 * <p>
 * Tests {@link AreaScenario}. For detailed information about the test itself, see superclass.
 *
 * <p>
 * &bull {@code Number of runs = } {@value simulationRuns}<br>
 * &bull {@code Number of steps = } {@value maxStep}<br>
 * &bull {@code Number of checks = } {@value checks}
 *
 * @author Dominic Parga Cacheiro
 */
public class AreaScenarioDeterminismTest extends AbstractDeterminismTest {

    private static final Logger logger = new EasyMarkableLogger(AreaScenarioDeterminismTest.class);


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
        AreaScenario scenario = new AreaScenario(config.seed, config, graph);
        scenario.refillNodeLists();
        return scenario;
    }
}
