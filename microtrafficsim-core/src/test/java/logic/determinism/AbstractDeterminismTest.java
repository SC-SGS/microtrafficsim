package logic.determinism;

import microtrafficsim.core.logic.streetgraph.Graph;
import microtrafficsim.core.logic.vehicles.VehicleState;
import microtrafficsim.core.logic.vehicles.machines.Vehicle;
import microtrafficsim.core.simulation.configs.ScenarioConfig;
import microtrafficsim.core.simulation.core.Simulation;
import microtrafficsim.core.simulation.core.impl.VehicleSimulation;
import microtrafficsim.core.simulation.scenarios.Scenario;
import microtrafficsim.math.MathUtils;
import microtrafficsim.utils.logging.EasyMarkableLogger;
import microtrafficsim.utils.logging.LoggingLevel;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;

import java.util.HashMap;
import java.util.Iterator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author Dominic Parga Cacheiro
 */
public abstract class AbstractDeterminismTest {

    private static Logger logger = new EasyMarkableLogger(AbstractDeterminismTest.class);

    /* (part of) tested parameters */
    private ScenarioConfig config;
    private Graph graph;
    private Simulation simulation;
    private int expectedAge;

    /* testing parameters */
    private final int checks;
    private final int maxStep;
    private final int simulationRuns;

    /* memory */
    // simulation age -> (vehicle id -> vehicle stamp)
    private HashMap<Integer, HashMap<Long, VehicleStamp>> stamps;


    public AbstractDeterminismTest() {

        /* (part of) tested parameters */
        config      = createConfig();
        graph       = createGraph(config);
        simulation  = new VehicleSimulation();
        expectedAge = 0;

        /* testing parameters */
        checks         = 10;
        maxStep        = 3000;
        simulationRuns = 3;

        /* memory */
        stamps = new HashMap<>();
    }

    protected abstract ScenarioConfig createConfig();

    protected abstract Graph createGraph(ScenarioConfig config);

    protected abstract Scenario createScenario(ScenarioConfig config, Graph graph);


    /*
    |===============|
    | testing utils |
    |===============|
    */
    private void storeStateFor(int simulationAge) {

        /* add stamps for given simulation age */
        HashMap<Long, VehicleStamp> vehicles = stamps.get(simulationAge);
        if (vehicles == null) {
            vehicles = new HashMap<>();
            stamps.put(simulationAge, vehicles);
        }

        getCurrentState(vehicles);
    }

    private void getCurrentState(HashMap<Long, VehicleStamp> stamps) {

        /* add stamp per vehicle */
        for (Vehicle vehicle : simulation.getScenario().getVehicleContainer()) {
            VehicleStamp stamp = new VehicleStamp();
            stamp.edge = vehicle.getDirectedEdge();
            stamp.cellPosition = vehicle.getCellPosition();
            stamps.put(vehicle.getId(), stamp);
        }
    }

    private void simulate(int steps) {

        for (int i = 0; i < steps; i++)
            simulation.runOneStep();
        expectedAge += steps;
        assertEquals(expectedAge, simulation.getAge());
    }

    private void compareStates(int simulationAge) {
        
        HashMap<Long, VehicleStamp> expected = stamps.get(simulationAge);
        HashMap<Long, VehicleStamp> actual = new HashMap<>();
        getCurrentState(actual);
        for (long id : expected.keySet()) {
            assertEquals(expected.get(id).edge, actual.get(id).edge);
            assertEquals(expected.get(id).cellPosition, actual.get(id).cellPosition);
        }
    }


    /*
    |================|
    | test case impl |
    |================|
    */
    private void executeAndRememberFirstRun() {

        Iterator<Integer> simulationAges = MathUtils.createSigmoidSequence(1, maxStep, checks - 2);

        int currentCheck = 1;
        int lastSimulationAge = 0;
        while (simulationAges.hasNext()) {

            int simulationAge = simulationAges.next();
            logger.info("Remember for check #" + currentCheck++ + " after " + simulationAge + " steps.");
            simulate(simulationAge - lastSimulationAge);
            lastSimulationAge = simulationAge;

            storeStateFor(simulationAge);
        }
    }

    private void setupNewSimulationRun() {
        graph.postprocess(config.seed);
        Scenario scenario = createScenario(config, graph);
        simulation.setAndInitPreparedScenario(scenario);
        expectedAge = 0;

        simulate(0);
    }

    private void executeSimulationRun() {

        Iterator<Integer> simulationAges = MathUtils.createSigmoidSequence(1, maxStep, checks - 2);

        int currentCheck = 1;
        int lastSimulationAge = 0;
        while (simulationAges.hasNext()) {

            int simulationAge = simulationAges.next();
            logger.info("Check #" + currentCheck++ + " after " + simulationAge + " steps.");
            simulate(simulationAge - lastSimulationAge);
            lastSimulationAge = simulationAge;

            compareStates(simulationAge);
        }
    }


    /*
    |============|
    | test cases |
    |============|
    */
    @Test
    public void testDeterminism() throws Exception {
        setupNewSimulationRun();
        executeAndRememberFirstRun();
        for (int run = 0; run < simulationRuns; run++) {
            setupNewSimulationRun();
            executeSimulationRun();
        }
    }

    /*
    |=======|
    | utils |
    |=======|
    */
    @BeforeClass
    public static void buildSetup() {
        LoggingLevel.setEnabledGlobally(false, false, true, true, true);
    }
}