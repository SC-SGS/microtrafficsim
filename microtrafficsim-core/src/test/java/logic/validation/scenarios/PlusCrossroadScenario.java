package logic.validation.scenarios;

import logic.validation.Main;
import logic.validation.ValidationScenario;
import logic.validation.cars.ValidationBlockingCar;
import microtrafficsim.core.frameworks.vehicle.IVisualizationVehicle;
import microtrafficsim.core.frameworks.vehicle.LogicVehicleEntity;
import microtrafficsim.core.logic.Node;
import microtrafficsim.core.logic.StreetGraph;
import microtrafficsim.core.logic.vehicles.VehicleState;
import microtrafficsim.core.simulation.configs.SimulationConfig;
import microtrafficsim.core.vis.opengl.utils.Color;
import microtrafficsim.interesting.progressable.ProgressListener;
import microtrafficsim.utils.id.ConcurrentLongIDGenerator;
import microtrafficsim.utils.resources.PackagedResource;

import java.io.File;
import java.util.ArrayList;
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

    private static final String OSM_FILENAME = "plus_crossroad.osm";
    private Node                mid = null, bottomLeft = null, bottomRight = null, topLeft = null, topRight = null;
    private NextScenarioState   nextScenarioState;

    /**
     * Standard constructor.
     *
     * @param config         The used config file for this scenarios.
     * @param graph          The streetgraph used for this scenarios.
     * @param vehicleFactory This creates vehicles.
     */
    public PlusCrossroadScenario(SimulationConfig config, StreetGraph graph,
                                 Supplier<IVisualizationVehicle> vehicleFactory) {
        super(config, graph, vehicleFactory);
        nextScenarioState = NextScenarioState.PRIORITY_TO_THE_RIGHT;
    }

    private static void setupConfig(SimulationConfig config) {

        // super attributes
        config.longIDGenerator                            = new ConcurrentLongIDGenerator();
        config.speedup                                    = 5;
        config.maxVehicleCount                            = 3;
        config.crossingLogic.drivingOnTheRight            = true;
        config.crossingLogic.edgePriorityEnabled          = true;
        config.crossingLogic.priorityToTheRightEnabled    = true;
        config.crossingLogic.friendlyStandingInJamEnabled = true;
        config.crossingLogic.setOnlyOneVehicle(false);
        // own attributes
        config.ageForPause = -1;
    }

    public static void main(String[] args) throws Exception {
        File file;

        if (args.length == 1) {
            switch (args[0]) {
            case "-h":
            case "--help":
                Main.printUsage(); return;

            default:
                file = new File(args[0]);
            }
        } else {
            file = new PackagedResource(Main.class, OSM_FILENAME).asTemporaryFile();
        }

        SimulationConfig config = new SimulationConfig();
        setupConfig(config);
        Main.show(config.visualization.projection, file, config, PlusCrossroadScenario.class);
    }

    @Override
    protected final void updateScenarioState(VehicleState vehicleState) {
        int updateGraphDelay = justInitialized ? 0 : 1;

        if (vehicleState == VehicleState.DESPAWNED) {
            if (getVehiclesCount() == 0) {
                switch (nextScenarioState) {
                case PRIORITY_TO_THE_RIGHT:
                    createAndAddCar(topRight, bottomRight, 0, NextScenarioState.PRIORITY_TO_THE_RIGHT.color);
                    createAndAddCar(topLeft, bottomRight, 2 + updateGraphDelay, NextScenarioState.PRIORITY_TO_THE_RIGHT.color);
                    nextScenarioState = NextScenarioState.NO_INTERSECTION;
                    break;

                case NO_INTERSECTION:
                    createAndAddCar(topRight, bottomLeft, 0, NextScenarioState.NO_INTERSECTION.color);
                    createAndAddCar(bottomLeft, topRight, 1 + updateGraphDelay, NextScenarioState.NO_INTERSECTION.color);
                    nextScenarioState = NextScenarioState.LEFT_TURNER_MUST_WAIT;
                    break;

                case LEFT_TURNER_MUST_WAIT:
                    createAndAddCar(topLeft, bottomRight, 5 + updateGraphDelay, NextScenarioState.LEFT_TURNER_MUST_WAIT.color);
                    createAndAddCar(bottomRight, bottomLeft, 0, NextScenarioState.LEFT_TURNER_MUST_WAIT.color);
                    nextScenarioState = NextScenarioState.ALL_LEFT;
                    break;

                case ALL_LEFT:
                    createAndAddCar(bottomLeft, topLeft, 4 + updateGraphDelay, NextScenarioState.ALL_LEFT.color);
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
                    getSpawnedVehicles().forEach((LogicVehicleEntity v) -> {
                        if (v instanceof ValidationBlockingCar)
                            ((ValidationBlockingCar) v).toggleBlockMode();
                    });
                }
            }
        }
    }

    @Override
    protected void createAndAddVehicles(ProgressListener listener) {

        /* sort by lon */
        Iterator<Node>  iter        = graph.getNodeIterator();
        ArrayList<Node> sortedNodes = new ArrayList<>(5);
        while (iter.hasNext())
            sortedNodes.add(iter.next());

        sortedNodes.sort((n1, n2) -> n1.getCoordinate().lon > n2.getCoordinate().lon ? 1 : -1);

        bottomLeft  = sortedNodes.get(0);
        topLeft     = sortedNodes.get(1);
        mid         = sortedNodes.get(2);
        topRight    = sortedNodes.get(3);
        bottomRight = sortedNodes.get(4);

        updateScenarioState(VehicleState.DESPAWNED);
        justInitialized = false;
    }

    private enum NextScenarioState {
        PRIORITY_TO_THE_RIGHT(Color.fromRGB(0x00ffff)),
        NO_INTERSECTION(Color.fromRGB(0xff0000)),
        LEFT_TURNER_MUST_WAIT(Color.fromRGB(0x00ff00)),
        ALL_LEFT(Color.fromRGB(0x0000ff)),
        GO_WITHOUT_PRIORITY(Color.fromRGB(0xff00ff));

        private final Color color;

        NextScenarioState(Color color) {
            this.color = color;
        }
    }
}