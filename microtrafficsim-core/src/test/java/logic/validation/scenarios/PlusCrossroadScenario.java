package logic.validation.scenarios;

import logic.validation.Main;
import logic.validation.ValidationScenario;
import microtrafficsim.core.frameworks.vehicle.ILogicVehicle;
import microtrafficsim.core.frameworks.vehicle.IVisualizationVehicle;
import microtrafficsim.core.logic.Node;
import microtrafficsim.core.logic.StreetGraph;
import microtrafficsim.core.logic.vehicles.VehicleState;
import microtrafficsim.core.logic.vehicles.impl.BlockingCar;
import microtrafficsim.core.map.Coordinate;
import microtrafficsim.core.simulation.controller.configs.SimulationConfig;
import microtrafficsim.core.vis.map.projections.MercatorProjection;
import microtrafficsim.core.vis.opengl.utils.Color;
import microtrafficsim.utils.id.ConcurrentLongIDGenerator;
import microtrafficsim.utils.resources.PackagedResource;

import java.io.File;
import java.util.Iterator;
import java.util.function.Supplier;

/**
 * This class defines a concrete scenarios of a simulation. This means it
 * contains an extension of {@link SimulationConfig} to define simulation
 * parameters. Furthermore, this class serves with a list of uniformly randomly
 * chosen start and end nodes for the vehicles and an age that counts the
 * finished simulation steps.
 *
 * @author Dominic Parga Cacheiro, Jan-Oliver Schmidt
 */
public class PlusCrossroadScenario extends ValidationScenario {

    public static class Config extends ValidationScenario.Config {

        {
            // super attributes
            longIDGenerator = new ConcurrentLongIDGenerator();
            msPerTimeStep = 200;
            maxVehicleCount = 3;
            crossingLogic.drivingOnTheRight = true;
            crossingLogic.edgePriorityEnabled = true;
            crossingLogic.priorityToTheRightEnabled = true;
            crossingLogic.setOnlyOneVehicle(false);
            crossingLogic.goWithoutPriorityEnabled = true;
            // own attributes
            ageForPause = -1;
        }
    }

    private static final String OSM_FILENAME = "plus_crossroad.osm";

    public static void main(String[] args) throws Exception {
        File file;

        if (args.length == 1) {
            switch(args[0]) {
                case "-h":
                case "--help":
                    Main.printUsage();
                    return;
                default:
                    file = new File(args[0]);
            }
        } else {
            file = new PackagedResource(Main.class, OSM_FILENAME).asTemporaryFile();
        }

        Main.show(
                new MercatorProjection(),
                file,
                new Config(),
                PlusCrossroadScenario.class);
    }

    private enum NextScenarioState {
        PRIORITY_TO_THE_RIGHT(Color.fromRGB(0x00ffff)),
        NO_INTERSECTION(Color.fromRGB(0xff0000)),
        LEFT_TURNER_MUST_WAIT(Color.fromRGB(0x00ff00)),
        ALL_LEFT(Color.fromRGB(0x0000ff)),
        GO_WITHOUT_PRIORITY(Color.fromRGB(0xff00ff));

        private final Color color;

        private NextScenarioState(Color color) {
            this.color = color;
        }
    }

    private Node mid = null, bottomLeft = null, bottomRight = null, topLeft = null, topRight = null;
    private NextScenarioState nextScenarioState;

    /**
     * Standard constructor.
     *
     * @param config         The used config file for this scenarios.
     * @param graph          The streetgraph used for this scenarios.
     * @param vehicleFactory This creates vehicles.
     */
    public PlusCrossroadScenario(Config config, StreetGraph graph,
                                 Supplier<IVisualizationVehicle> vehicleFactory) {
        super(config,
                graph,
                vehicleFactory);
        nextScenarioState = NextScenarioState.PRIORITY_TO_THE_RIGHT;
    }

    @Override
    protected final void updateScenarioState(VehicleState vehicleState) {

        int updateGraphDelay = justInitialized ? 0 : 1;

        if (vehicleState == VehicleState.DESPAWNED) {
            if (getVehiclesCount() == 0) {
                switch (nextScenarioState) {
                    case PRIORITY_TO_THE_RIGHT:
                        createAndAddCar(topRight, bottomRight, 0, NextScenarioState.PRIORITY_TO_THE_RIGHT.color);
                        createAndAddCar(
                                topLeft,
                                bottomRight,
                                2 + updateGraphDelay,
                                NextScenarioState.PRIORITY_TO_THE_RIGHT.color);
                        nextScenarioState = NextScenarioState.NO_INTERSECTION;
                        break;
                    case NO_INTERSECTION:
                        createAndAddCar(topRight, bottomLeft, 0, NextScenarioState.NO_INTERSECTION.color);
                        createAndAddCar(
                                bottomLeft,
                                topRight,
                                1 + updateGraphDelay,
                                NextScenarioState.NO_INTERSECTION.color);
                        nextScenarioState = NextScenarioState.LEFT_TURNER_MUST_WAIT;
                        break;
                    case LEFT_TURNER_MUST_WAIT:
                        createAndAddCar(
                                topLeft,
                                bottomRight,
                                5 + updateGraphDelay,
                                NextScenarioState.LEFT_TURNER_MUST_WAIT.color);
                        createAndAddCar(bottomRight, bottomLeft, 0, NextScenarioState.LEFT_TURNER_MUST_WAIT.color);
                        nextScenarioState = NextScenarioState.ALL_LEFT;
                        break;
                    case ALL_LEFT:
                        createAndAddCar(
                                bottomLeft,
                                topLeft,
                                4 + updateGraphDelay,
                                NextScenarioState.ALL_LEFT.color);
                        createAndAddCar(bottomRight, bottomLeft, 0, NextScenarioState.ALL_LEFT.color);
                        createAndAddCar(topRight, bottomRight, 3 + updateGraphDelay, NextScenarioState.ALL_LEFT.color);
                        createAndAddCar(topLeft, topRight, 5 + updateGraphDelay, NextScenarioState.ALL_LEFT.color);
                        nextScenarioState = NextScenarioState.GO_WITHOUT_PRIORITY;
                        break;
                    case GO_WITHOUT_PRIORITY:
                        createAndAddBlockingCar(mid, bottomLeft, NextScenarioState.GO_WITHOUT_PRIORITY.color);
                        createAndAddCar(topRight, bottomLeft, 0, NextScenarioState.GO_WITHOUT_PRIORITY.color);
                        createAndAddCar(bottomRight, topLeft, 0, NextScenarioState.GO_WITHOUT_PRIORITY.color);
                        nextScenarioState = NextScenarioState.PRIORITY_TO_THE_RIGHT;
                        break;
                }
            } else if (getVehiclesCount() == 2) {
                if (nextScenarioState == NextScenarioState.PRIORITY_TO_THE_RIGHT) {
                    getSpawnedVehicles().forEach((ILogicVehicle v) -> {
                        if (v instanceof BlockingCar)
                            ((BlockingCar) v).toggleBlockMode();
                    });
                }
            }
        }
    }

    @Override
    protected void createAndAddVehicles() {
        /*
        top left microtrafficsim.core.map.Coordinate (48.77039, 9.178931)
        top right microtrafficsim.core.map.Coordinate (48.770348, 9.180028)
        bottom left microtrafficsim.core.map.Coordinate (48.769726, 9.178792)
        mid microtrafficsim.core.map.Coordinate (48.770023, 9.179357)
        bottom right microtrafficsim.core.map.Coordinate (48.76942, 9.180072)
        */
        Iterator<Node> iter = graph.getNodeIterator();
        while (iter.hasNext()) {

            Node node = iter.next();
            Coordinate c = node.getCoordinate();
            // top left
            if (c.equals(new Coordinate(48.77039f, 9.178931f))) topLeft = node;
                // top right
            else if (c.equals(new Coordinate(48.770348f, 9.180028f))) topRight = node;
                // bottom left
            else if (c.equals(new Coordinate(48.769726f, 9.178792f))) bottomLeft = node;
                // mid
            else if (c.equals(new Coordinate(48.770023f, 9.179357f))) mid = node;
                // bottom right
            else if (c.equals(new Coordinate(48.76942f, 9.180072f))) bottomRight = node;
        }

        updateScenarioState(VehicleState.DESPAWNED);
        justInitialized = false;
    }
}