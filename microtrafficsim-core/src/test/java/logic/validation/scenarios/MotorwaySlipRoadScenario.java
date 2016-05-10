package logic.validation.scenarios;

import logic.validation.Main;
import logic.validation.ValidationScenario;
import microtrafficsim.core.frameworks.vehicle.IVisualizationVehicle;
import microtrafficsim.core.logic.Node;
import microtrafficsim.core.logic.StreetGraph;
import microtrafficsim.core.logic.vehicles.VehicleState;
import microtrafficsim.core.logic.vehicles.impl.Car;
import microtrafficsim.core.map.Coordinate;
import microtrafficsim.core.simulation.controller.Simulation;
import microtrafficsim.core.simulation.controller.configs.SimulationConfig;
import microtrafficsim.core.vis.map.projections.MercatorProjection;
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
public class MotorwaySlipRoadScenario extends ValidationScenario {

    public static void setupConfig(SimulationConfig config){

        // super attributes
        config.longIDGenerator = new ConcurrentLongIDGenerator();
        config.msPerTimeStep = 200;
        config.maxVehicleCount = 3;
        config.crossingLogic.drivingOnTheRight = true;
        config.crossingLogic.edgePriorityEnabled = true;
        config.crossingLogic.priorityToTheRightEnabled = true;
        config.crossingLogic.setOnlyOneVehicle(false);
        config.crossingLogic.goWithoutPriorityEnabled = false;
        // own attributes
        config.ageForPause = -1;
        Car.maxVelocity = 1;
    }

    private static final String OSM_FILENAME = "motorway_slip-road.osm";

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

        SimulationConfig config = new SimulationConfig();
        setupConfig(config);
        Main.show(
                config.visualization.projection,
                file,
                config,
                MotorwaySlipRoadScenario.class);
    }

    private enum NextScenarioState {
        PRIORITY_TO_THE_MOTORWAY;
    }

    private Node bottomMotorway = null, interception = null, topMotorway = null, bottomRight = null;
    private NextScenarioState nextScenarioState;

    /**
     * Default constructor.
     *
     * @param config         The used config file for this scenarios.
     * @param graph          The streetgraph used for this scenarios.
     * @param vehicleFactory This creates vehicles.
     */
    public MotorwaySlipRoadScenario(SimulationConfig config, StreetGraph graph,
                                    Supplier<IVisualizationVehicle> vehicleFactory) {
        super(config,
                graph,
                vehicleFactory);
        nextScenarioState = NextScenarioState.PRIORITY_TO_THE_MOTORWAY;
    }

    @Override
    protected void updateScenarioState(VehicleState vehicleState) {

        int updateGraphDelay = justInitialized ? 0 : 1;

        if (vehicleState == VehicleState.DESPAWNED && getVehiclesCount() == 0) {
            switch (nextScenarioState) {
                case PRIORITY_TO_THE_MOTORWAY:
                    createAndAddCar(bottomMotorway, topMotorway, 0);
                    createAndAddCar(bottomRight, topMotorway, 9 + updateGraphDelay);
            }
        }
    }

    @Override
    protected void createAndAddVehicles() {

        /* sort by lon */
        Iterator<Node> iter = graph.getNodeIterator();
        ArrayList<Node> sortedNodes = new ArrayList<>(4);
        while (iter.hasNext())
            sortedNodes.add(iter.next());

        sortedNodes.sort((n1, n2) -> n1.getCoordinate().lon > n2.getCoordinate().lon ? 1 : -1);

        bottomMotorway = sortedNodes.get(0);
        interception = sortedNodes.get(1);
        topMotorway = sortedNodes.get(2);
        bottomRight = sortedNodes.get(3);

        updateScenarioState(VehicleState.DESPAWNED);
        justInitialized = false;
    }
}