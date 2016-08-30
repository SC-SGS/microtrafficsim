package logic.validation.scenarios;

import logic.validation.Main;
import logic.validation.ValidationScenario;
import microtrafficsim.core.entities.vehicle.VisualizationVehicleEntity;
import microtrafficsim.core.logic.Node;
import microtrafficsim.core.logic.StreetGraph;
import microtrafficsim.core.logic.vehicles.VehicleState;
import microtrafficsim.core.simulation.configs.SimulationConfig;
import microtrafficsim.core.vis.opengl.utils.Color;
import microtrafficsim.interesting.progressable.ProgressListener;
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
public class RoundaboutScenario extends ValidationScenario {

    private static final String OSM_FILENAME = "roundabout.processing";
    private Node                topRight, topLeft, left, bottom, right;
    // left is no start point!
    private NextScenarioState nextScenarioState;

    /**
     * Standard constructor.
     *
     * @param config         The used config file for this scenarios.
     * @param graph          The streetgraph used for this scenarios.
     * @param vehicleFactory This creates vehicles.
     */
    public RoundaboutScenario(SimulationConfig config, StreetGraph graph,
                              Supplier<VisualizationVehicleEntity> vehicleFactory) {
        super(config, graph, vehicleFactory);
        nextScenarioState = NextScenarioState.TOP_RIGHT;
    }

    private static void setupConfig(SimulationConfig config) {

        // super attributes
        config.speedup                                 = 5;
        config.maxVehicleCount                         = 2;
        config.ageForPause                             = -1;
        config.crossingLogic.drivingOnTheRight         = true;
        config.crossingLogic.edgePriorityEnabled       = true;
        config.crossingLogic.priorityToTheRightEnabled = true;
        config.crossingLogic.setOnlyOneVehicle(false);
        config.crossingLogic.friendlyStandingInJamEnabled = true;
    }

    public static void main(String[] args) throws Exception {
        File file = new PackagedResource(Main.class, OSM_FILENAME).asTemporaryFile();

        SimulationConfig config = new SimulationConfig();
        setupConfig(config);
        Main.show(config.visualization.projection, file, config, RoundaboutScenario.class);
    }

    @Override
    protected final void updateScenarioState(VehicleState vehicleState) {
        int updateGraphDelay = justInitialized ? 0 : 1;

        if (vehicleState == VehicleState.DESPAWNED && getVehiclesCount() == 0) {
            switch (nextScenarioState) {
            case TOP_RIGHT:
                createAndAddCar(topRight, right, 0,                     Color.fromRGB(0xCC4C1A));
                createAndAddCar(topLeft,  left,  13 + updateGraphDelay, Color.fromRGB(0x3EAAAB));
                nextScenarioState = NextScenarioState.TOP_LEFT;
                break;

            case TOP_LEFT:
                createAndAddCar(topLeft, topRight, 5 + updateGraphDelay, Color.fromRGB(0xCC4C1A));
                createAndAddCar(bottom,  left,     0,                    Color.fromRGB(0x3EAAAB));
                nextScenarioState = NextScenarioState.BOTTOM;
                break;

            case BOTTOM:
                createAndAddCar(bottom, left,     0,                    Color.fromRGB(0xCC4C1A));
                createAndAddCar(right,  topRight, 5 + updateGraphDelay, Color.fromRGB(0x3EAAAB));
                nextScenarioState = NextScenarioState.RIGHT;
                break;

            case RIGHT:    // maybe useless, because checked crossroad is same as in TOP_RIGHT
                createAndAddCar(right,   bottom, 0,                    Color.fromRGB(0xCC4C1A));
                createAndAddCar(topLeft, left,   7 + updateGraphDelay, Color.fromRGB(0x3EAAAB));
                nextScenarioState = NextScenarioState.TOP_RIGHT;
            }
        }
    }

    @Override
    protected void createAndAddVehicles(ProgressListener listener) {
        /* sort by lon */
        Iterator<Node>  iter        = graph.getNodeIterator();
        ArrayList<Node> sortedNodes = new ArrayList<>(graph.getNumberOfNodes());
        while (iter.hasNext())
            sortedNodes.add(iter.next());

        sortedNodes.sort((n1, n2) -> {
            boolean sortByLon = false;
            //noinspection ConstantConditions
            if (sortByLon) {
                if (n1.getCoordinate().lon > n2.getCoordinate().lon)
                    return 1;
                else
                    return -1;
            } else {
                if (n1.getCoordinate().lat > n2.getCoordinate().lat)
                    return 1;
                else
                    return -1;
            }
        });

        //    for (Node n : sortedNodes) {
        //      System.out.println("Node(" + n.ID + ").coord = " + n.getCoordinate());
        //    }

        // node IDs in processing-file, sorted by lat ascending:
        // 243.679.734 bottom
        // 242.539.693 right
        //  27.281.851
        // 498.201.336
        // 260.334.121 left
        //  43.055.934
        //  27.281.850
        //  27.281.852
        // 271.387.772 top left
        // 254.636.138 top right

        bottom   = sortedNodes.get(0);
        right    = sortedNodes.get(1);
        left     = sortedNodes.get(4);
        topLeft  = sortedNodes.get(8);
        topRight = sortedNodes.get(9);

        updateScenarioState(VehicleState.DESPAWNED);
        justInitialized = false;
    }

    private enum NextScenarioState { TOP_RIGHT, TOP_LEFT, BOTTOM, RIGHT }
}