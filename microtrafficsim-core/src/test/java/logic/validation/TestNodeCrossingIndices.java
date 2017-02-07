package logic.validation;

import logic.validation.scenarios.pluscrossroad.AbstractPlusCrossroadScenario;
import logic.validation.scenarios.pluscrossroad.FullPlusCrossroadScenario;
import microtrafficsim.build.BuildSetup;
import microtrafficsim.core.logic.Direction;
import microtrafficsim.core.logic.StreetGraph;
import microtrafficsim.core.logic.vehicles.machines.Vehicle;
import microtrafficsim.core.mapviewer.MapViewer;
import microtrafficsim.core.mapviewer.impl.TileBasedMapViewer;
import microtrafficsim.core.simulation.configs.ScenarioConfig;
import microtrafficsim.core.simulation.core.Simulation;
import microtrafficsim.core.simulation.core.impl.VehicleSimulation;
import microtrafficsim.math.Geometry;
import microtrafficsim.utils.logging.EasyMarkableLogger;
import microtrafficsim.utils.resources.PackagedResource;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;

import java.io.File;

import static org.junit.Assert.assertEquals;

/**
 * @author Dominic Parga Cacheiro
 */
public class TestNodeCrossingIndices {

    private static Logger logger = new EasyMarkableLogger(TestNodeCrossingIndices.class);

    @Test
    public void testPlusCrossroad() throws Exception {

        for (int priorityRun = 0; priorityRun < 2; priorityRun++) {

            /* setup config */
            ScenarioConfig config = new ScenarioConfig();
            AbstractPlusCrossroadScenario.setupConfig(config);
            config.speedup = 100;
            config.crossingLogic.drivingOnTheRight = priorityRun == 0; // first right-before-left

            /* setup graph */
            File file = new PackagedResource(UIValidation.class, "plus_crossroad.osm").asTemporaryFile();
            MapViewer mapviewer = new TileBasedMapViewer(config.visualization.style);
            mapviewer.createParser(config);
            StreetGraph graph = mapviewer.parse(file).streetgraph;

            logger.debug("\n" + graph);

            /* setup simulation and scenario */
            AbstractPlusCrossroadScenario scenario = new FullPlusCrossroadScenario(config, graph, null);

            Simulation sim = new VehicleSimulation();
            scenario.prepare();
            sim.setAndInitPreparedScenario(scenario);
            while (scenario.getVehicleContainer().getVehicleCount() > 0) {
                sim.runOneStep();

                // always 2 vehicles until first one disappears
                if (scenario.getVehicleContainer().getVehicleCount() < 2)
                    continue;

                /* get vehicles */
                int i = 0;
                Vehicle[] vehicles = new Vehicle[2];
                for (Vehicle vehicle : scenario.getVehicleContainer())
                    vehicles[i++] = vehicle;

                /* check only if both are registered in mid */
                if (!scenario.mid.isRegistered(vehicles[0]) || !scenario.mid.isRegistered(vehicles[1]))
                    continue;

                /* get vehicles' position relative to each other */
                Direction direction = Geometry.calcCurveDirection(
                        vehicles[0].getDriver().getRoute().getEnd().getCoordinate(),
                        scenario.mid.getCoordinate(),
                        vehicles[1].getDriver().getRoute().getEnd().getCoordinate());

                // swap vehicles if priority is left-before-right instead of right-before-left
                if (!config.crossingLogic.drivingOnTheRight) {
                    Vehicle tmp = vehicles[0];
                    vehicles[0] = vehicles[1];
                    vehicles[1] = tmp;
                }

                // assert correct priority-to-the-right
                boolean permissionToV0 = scenario.mid.permissionToCross(vehicles[0]);
                boolean permissionToV1 = scenario.mid.permissionToCross(vehicles[1]);
                boolean anyPermission = permissionToV0 || permissionToV1;
                assertEquals(direction == Direction.LEFT && anyPermission, permissionToV0);
                assertEquals(direction == Direction.RIGHT && anyPermission, permissionToV1);
            }
        }
    }

    @BeforeClass
    public static void buildSetup() {
        /* build setup: logging */
        BuildSetup.TRACE_ENABLED = false;
        BuildSetup.DEBUG_ENABLED = false;
        BuildSetup.INFO_ENABLED  = false;
        BuildSetup.WARN_ENABLED  = false;
        BuildSetup.ERROR_ENABLED = false;
    }
}