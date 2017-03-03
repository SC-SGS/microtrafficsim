package logic.determinism;

import microtrafficsim.build.BuildSetup;
import microtrafficsim.core.simulation.core.Simulation;
import microtrafficsim.core.simulation.core.impl.VehicleSimulation;
import microtrafficsim.core.simulation.scenarios.Scenario;
import microtrafficsim.math.MathUtils;
import microtrafficsim.utils.logging.EasyMarkableLogger;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;

import java.util.Iterator;
import java.util.function.Supplier;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author Dominic Parga Cacheiro
 */
public abstract class AbstractDeterminismTest {

    private static Logger logger = new EasyMarkableLogger(AbstractDeterminismTest.class);

    private Simulation simulation;
    private int expectedAge;

    public AbstractDeterminismTest() {
        simulation  = new VehicleSimulation(createScenario());
        expectedAge = 0;
        simulate(0);
        assertTrue("The scenario is not prepared.", simulation.getScenario().isPrepared());
    }

    protected abstract Scenario createScenario();

    private void rememberStart() {

    }

    private void simulate(int steps) {

        for (int i = 0; i < steps; i++)
            simulation.runOneStep();
        expectedAge += steps;
        assertEquals(expectedAge, simulation.getAge());
    }

    private void compareWithStart() {

//        for (int priorityRun = 0; priorityRun < 2; priorityRun++) {
//
//            Simulation sim = new VehicleSimulation();
//            scenario.prepare();
//            sim.setAndInitPreparedScenario(scenario);
//            while (scenario.getVehicleContainer().getVehicleCount() > 0) {
//                sim.runOneStep();
//
//                // always 2 vehicles until first one disappears
//                if (scenario.getVehicleContainer().getVehicleCount() < 2)
//                    continue;
//
//                /* get vehicles */
//                int i = 0;
//                Vehicle[] vehicles = new Vehicle[2];
//                for (Vehicle vehicle : scenario.getVehicleContainer())
//                    vehicles[i++] = vehicle;
//
//                /* check only if both are registered in mid */
//                if (!scenario.mid.isRegistered(vehicles[0]) || !scenario.mid.isRegistered(vehicles[1]))
//                    continue;
//
//                /* get vehicles' position relative to each other */
//                Direction direction = Geometry.calcCurveDirection(
//                        vehicles[0].getDriver().getRoute().getEnd().getCoordinate(),
//                        scenario.mid.getCoordinate(),
//                        vehicles[1].getDriver().getRoute().getEnd().getCoordinate());
//
//                // swap vehicles if priority is left-before-right instead of right-before-left
//                if (!config.crossingLogic.drivingOnTheRight) {
//                    Vehicle tmp = vehicles[0];
//                    vehicles[0] = vehicles[1];
//                    vehicles[1] = tmp;
//                }
//
//                // assert correct priority-to-the-right
//                boolean permissionToV0 = scenario.mid.permissionToCross(vehicles[0]);
//                boolean permissionToV1 = scenario.mid.permissionToCross(vehicles[1]);
//                boolean anyPermission = permissionToV0 || permissionToV1;
//                assertEquals(direction == Direction.LEFT && anyPermission, permissionToV0);
//                assertEquals(direction == Direction.RIGHT && anyPermission, permissionToV1);
//            }
//        }
    }

    @Test
    public void testDeterminism() throws Exception {

        /* setup */
        int checks = 10;
        int maxStep = 1000;
        Iterator<Integer> steps = MathUtils.createSigmoidSequence(1, maxStep, checks - 2);

        // todo
        rememberStart();
        int currentCheck = 1;
        int lastStep     = 0;
        while (steps.hasNext()) {

            int currentStep = steps.next();
            logger.info("Check #" + currentCheck++ + " after " + currentStep + " steps.");
            simulate(currentStep - lastStep);
            lastStep = currentStep;

            compareWithStart();
        }
    }

    /*
    |=======|
    | utils |
    |=======|
    */
    @BeforeClass
    public static void buildSetup() {

        /* build setup: logging */
        BuildSetup.TRACE_ENABLED = false;
        BuildSetup.DEBUG_ENABLED = false;
        BuildSetup.INFO_ENABLED  = true;
        BuildSetup.WARN_ENABLED  = true;
        BuildSetup.ERROR_ENABLED = true;
    }
}